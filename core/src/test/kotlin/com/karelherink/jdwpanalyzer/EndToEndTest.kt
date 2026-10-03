package com.karelherink.jdwpanalyzer

import com.karelherink.jdwpanalyzer.decode.JdwpSpec
import com.karelherink.jdwpanalyzer.log.PacketLog
import com.karelherink.jdwpanalyzer.log.PacketLogWriter
import com.karelherink.jdwpanalyzer.model.Analyzer
import com.karelherink.jdwpanalyzer.model.Exchange
import com.karelherink.jdwpanalyzer.proxy.JdwpProxy
import com.karelherink.jdwpanalyzer.proxy.ProxyConfig
import com.karelherink.jdwpanalyzer.proxy.ProxyState
import com.sun.jdi.ArrayReference
import com.sun.jdi.ArrayType
import com.sun.jdi.Bootstrap
import com.sun.jdi.ClassType
import com.sun.jdi.IntegerValue
import com.sun.jdi.InterfaceType
import com.sun.jdi.ObjectReference
import com.sun.jdi.PathSearchingVirtualMachine
import com.sun.jdi.StringReference
import com.sun.jdi.ThreadReference
import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.BreakpointEvent
import com.sun.jdi.event.ClassPrepareEvent
import com.sun.jdi.event.Event
import com.sun.jdi.event.ExceptionEvent
import com.sun.jdi.event.StepEvent
import com.sun.jdi.event.VMDeathEvent
import com.sun.jdi.event.VMDisconnectEvent
import com.sun.jdi.request.EventRequest
import com.sun.jdi.request.StepRequest
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import javax.tools.ToolProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Runs a real debug session through the proxy: a Java debuggee under JDWP, driven by JDI, exercising as much of
 * the protocol as JDI exposes. Every packet must decode completely, and every command must get its reply.
 */
class EndToEndTest {

    private val debuggeeSource = """
        public class Debuggee implements Runnable {
            interface Calc {
                static int twice(int x) { return x * 2; }
            }

            static int counter = 1;
            static final Object lock = new Object();

            int value = 7;
            String label = "start";
            int[] numbers = {1, 2, 3};
            String[] words = {"a", "b"};
            double ratio = 0.5;

            public void run() {}

            int add(int x) { return value + x; }

            static void pause(Debuggee d, int arg) {
                int local = arg + 1;
                String text = "local";
                counter = local + text.length();
            }

            public static void main(String[] args) throws Exception {
                Debuggee d = new Debuggee();
                int twiceOne = Calc.twice(1);
                Thread worker = new Thread(() -> {
                    try { Thread.sleep(600_000); } catch (InterruptedException e) { }
                }, "worker");
                worker.start();
                synchronized (lock) {
                    lock.wait(10);
                    Thread contender = new Thread(() -> { synchronized (lock) { counter++; } }, "contender");
                    contender.start();
                    Thread.sleep(200);
                    pause(d, 41);
                }
                d.value = d.add(1);
                int read = d.value;
                try { throw new IllegalStateException("boom " + read); } catch (IllegalStateException e) { }
                Thread.ofVirtual().start(() -> { }).join();
                Thread.sleep(600_000);
            }
        }
    """.trimIndent()

    @Test
    fun `a full JDI session decodes cleanly`() {
        val classes = compileDebuggee()
        val debuggee = ProcessBuilder(
            File(System.getProperty("java.home"), "bin/java").path,
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:0",
            "-cp", classes.path,
            "Debuggee",
        ).redirectErrorStream(true).start()

        val analyzer = Analyzer()
        var proxy: JdwpProxy? = null
        try {
            val banner = debuggee.inputReader().readLine()
            val vmPort = Regex("address: .*?(\\d+)$").find(banner)?.groupValues?.get(1)?.toInt()
                ?: fail("Unexpected debuggee output: $banner")

            // Keep the session as a log; it doubles as sample data for the UI (replay it with :app:run --args=...).
            val log = PacketLogWriter(File("build/e2e-log"))
            proxy = JdwpProxy(ProxyConfig(0, "127.0.0.1", vmPort), analyzer, log).also { it.start() }
            val listenPort = waitFor { (proxy.state.value as? ProxyState.Listening)?.port }

            val connector = Bootstrap.virtualMachineManager().attachingConnectors().first { it.name() == "com.sun.jdi.SocketAttach" }
            val args = connector.defaultArguments()
            args["hostname"]!!.setValue("127.0.0.1")
            args["port"]!!.setValue(listenPort.toString())
            val vm = connector.attach(args)

            drive(vm)
            debuggee.waitFor(30, TimeUnit.SECONDS)
            waitFor { proxy.state.value as? ProxyState.Closed }
        } finally {
            proxy?.stop()
            debuggee.destroyForcibly()
        }

        report(analyzer.exchanges())

        // Replaying the log must reproduce the same exchanges, with directions inferred.
        val replay = Analyzer()
        PacketLog.read(File("build/e2e-log")).forEach { replay.onPacket(it, direction = null) }
        assertEquals(analyzer.exchanges().map { it.name to it.errorCode }, replay.exchanges().map { it.name to it.errorCode })
        assertTrue(replay.exchanges().none { it.hasProblem })
    }

    private fun drive(vm: VirtualMachine) {
        val erm = vm.eventRequestManager()

        // Basic VM queries.
        vm.version(); vm.description(); vm.name()
        vm.canWatchFieldModification(); vm.canGetMethodReturnValues()
        vm.allThreads()
        vm.topLevelThreadGroups().forEach { g -> g.name(); g.parent(); g.threads(); g.threadGroups() }
        (vm as PathSearchingVirtualMachine).classPath()
        vm.allModules().take(3).forEach { m -> m.name(); m.classLoader() }
        vm.setDefaultStratum("Java")
        vm.classesByName("java.lang.String").forEach { it.name() }

        val prepare = erm.createClassPrepareRequest().apply {
            addClassFilter("Debuggee")
            addSourceNameFilter("Debuggee.java")
            enable()
        }
        vm.resume()
        val debuggeeClass = (nextEvent<ClassPrepareEvent>(vm)).referenceType() as ClassType
        prepare.disable()

        val pauseMethod = debuggeeClass.methodsByName("pause").single()
        erm.createBreakpointRequest(pauseMethod.location()).enable()
        erm.createMethodEntryRequest().apply {
            addClassFilter("Debuggee")
            addClassExclusionFilter("java.*")
            setSuspendPolicy(EventRequest.SUSPEND_NONE)
            enable()
        }
        erm.createMethodExitRequest().apply {
            addClassFilter(debuggeeClass)
            setSuspendPolicy(EventRequest.SUSPEND_NONE)
            enable()
        }
        erm.createThreadStartRequest().apply {
            addPlatformThreadsOnlyFilter()
            setSuspendPolicy(EventRequest.SUSPEND_NONE)
            enable()
        }
        erm.createThreadDeathRequest().apply { setSuspendPolicy(EventRequest.SUSPEND_NONE); enable() }
        erm.createMonitorWaitRequest().apply { addClassFilter("java.lang.Object"); setSuspendPolicy(EventRequest.SUSPEND_NONE); enable() }
        erm.createMonitorWaitedRequest().apply { addClassFilter("java.lang.Object"); setSuspendPolicy(EventRequest.SUSPEND_NONE); enable() }
        erm.createMonitorContendedEnterRequest().apply { setSuspendPolicy(EventRequest.SUSPEND_NONE); enable() }
        erm.createMonitorContendedEnteredRequest().apply { setSuspendPolicy(EventRequest.SUSPEND_NONE); enable() }
        erm.createExceptionRequest(null, true, true).apply { addClassFilter("Debuggee"); enable() }
        vm.resume()

        val bp = nextEvent<BreakpointEvent>(vm)
        val thread = bp.thread()
        inspect(vm, thread, debuggeeClass)

        // Watch the instance's `value` field, then step and run to the exception.
        val d = thread.frame(0).getArgumentValues()[0] as ObjectReference
        val valueField = debuggeeClass.fieldByName("value")
        erm.createModificationWatchpointRequest(valueField).apply { addInstanceFilter(d); enable() }
        erm.createAccessWatchpointRequest(valueField).apply { addThreadFilter(thread); enable() }
        erm.createStepRequest(thread, StepRequest.STEP_LINE, StepRequest.STEP_OVER).apply { addCountFilter(1); enable() }
        erm.deleteAllBreakpoints()
        vm.resume()
        nextEvent<StepEvent>(vm)
        erm.stepRequests().toList().forEach(erm::deleteEventRequest)
        vm.resume()
        nextEvent<ExceptionEvent>(vm)

        erm.deleteEventRequests(erm.methodEntryRequests() + erm.methodExitRequests())
        vm.exit(0)
        try {
            while (true) {
                val set = vm.eventQueue().remove(10_000) ?: break
                if (set.any { it is VMDeathEvent || it is VMDisconnectEvent }) break
                set.resume()
            }
        } catch (_: com.sun.jdi.VMDisconnectedException) {
        }
    }

    private fun inspect(vm: VirtualMachine, thread: ThreadReference, debuggeeClass: ClassType) {
        // Threads and frames.
        thread.name(); thread.status(); thread.suspendCount(); thread.threadGroup(); thread.isVirtual
        thread.frameCount()
        val frame = thread.frame(0)
        val method = frame.location().method()
        method.allLineLocations(); method.variables(); method.arguments(); method.bytecodes(); method.isObsolete
        frame.getValues(frame.visibleVariables())
        frame.thisObject()
        thread.ownedMonitors()
        thread.ownedMonitorsAndFrames()
        thread.currentContendedMonitor()

        // Locals: read, then write back.
        val arg = frame.visibleVariableByName("arg")
        frame.setValue(arg, vm.mirrorOf(42))

        // Types.
        debuggeeClass.sourceName(); debuggeeClass.modifiers(); debuggeeClass.superclass(); debuggeeClass.interfaces()
        debuggeeClass.nestedTypes(); debuggeeClass.classLoader(); debuggeeClass.classObject().reflectedType()
        debuggeeClass.majorVersion(); debuggeeClass.constantPoolCount(); debuggeeClass.constantPool()
        debuggeeClass.module().name(); debuggeeClass.genericSignature(); debuggeeClass.isInitialized
        runCatching { debuggeeClass.sourceDebugExtension() }
        debuggeeClass.allFields(); debuggeeClass.allMethods()
        debuggeeClass.getValue(debuggeeClass.fieldByName("counter"))
        debuggeeClass.setValue(debuggeeClass.fieldByName("counter"), vm.mirrorOf(5))
        vm.instanceCounts(listOf(debuggeeClass))
        debuggeeClass.instances(10)

        // Objects.
        val d = frame.getArgumentValues()[0] as ObjectReference
        d.referenceType()
        d.getValues(debuggeeClass.fields())
        d.setValue(debuggeeClass.fieldByName("value"), vm.mirrorOf(8))
        d.setValue(debuggeeClass.fieldByName("ratio"), vm.mirrorOf(1.25))
        d.setValue(debuggeeClass.fieldByName("label"), vm.mirrorOf("changed"))
        d.disableCollection(); d.enableCollection(); d.isCollected
        d.referringObjects(5)
        val lock = debuggeeClass.getValue(debuggeeClass.fieldByName("lock")) as ObjectReference
        lock.owningThread(); lock.entryCount(); lock.waitingThreads()
        (d.getValue(debuggeeClass.fieldByName("label")) as StringReference).value()

        // Arrays.
        val numbers = d.getValue(debuggeeClass.fieldByName("numbers")) as ArrayReference
        numbers.length(); numbers.values
        numbers.setValue(0, vm.mirrorOf(10))
        val words = d.getValue(debuggeeClass.fieldByName("words")) as ArrayReference
        words.values
        words.setValue(1, vm.mirrorOf("z"))
        (numbers.referenceType() as ArrayType).newInstance(4)

        // Method invocation.
        val add = debuggeeClass.methodsByName("add").single()
        val sum = d.invokeMethod(thread, add, listOf(vm.mirrorOf(2)), 0) as IntegerValue
        assertEquals(10, sum.value())
        debuggeeClass.newInstance(thread, debuggeeClass.methodsByName("<init>").single(), emptyList(), ObjectReference.INVOKE_SINGLE_THREADED)
        val calc = vm.classesByName("Debuggee\$Calc").single() as InterfaceType
        calc.invokeMethod(thread, calc.methodsByName("twice").single(), listOf(vm.mirrorOf(3)), 0)
        val string = vm.classesByName("java.lang.String").single() as ClassType
        string.invokeMethod(thread, string.methodsByName("valueOf", "(I)Ljava/lang/String;").single(), listOf(vm.mirrorOf(7)), 0)

        // Another thread: suspend, interrupt, resume.
        val worker = vm.allThreads().single { it.name() == "worker" }
        worker.suspend(); worker.status(); worker.resume()
        worker.interrupt()

        // Whole-VM suspend/resume, string creation, class redefinition and a class loader's view.
        vm.suspend(); vm.resume()
        vm.mirrorOf("created by the debugger")
        val bytes = Files.readAllBytes(compiledClasses.resolve("Debuggee.class").toPath())
        vm.redefineClasses(mapOf(debuggeeClass to bytes))
        debuggeeClass.classLoader()?.visibleClasses()
    }

    private lateinit var compiledClasses: File

    private fun compileDebuggee(): File {
        val dir = Files.createTempDirectory("debuggee").toFile()
        val source = File(dir, "Debuggee.java").apply { writeText(debuggeeSource) }
        val result = ToolProvider.getSystemJavaCompiler().run(null, null, null, "-g", "-d", dir.path, source.path)
        assertEquals(0, result, "Debuggee failed to compile")
        compiledClasses = dir
        return dir
    }

    private inline fun <reified T : Event> nextEvent(vm: VirtualMachine): T {
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            val set = vm.eventQueue().remove(1_000) ?: continue
            set.filterIsInstance<T>().firstOrNull()?.let { return it }
            set.resume()
        }
        fail("Timed out waiting for ${T::class.simpleName}")
    }

    private fun <T : Any> waitFor(get: () -> T?): T {
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            get()?.let { return it }
            Thread.sleep(20)
        }
        fail("Timed out")
    }

    private fun report(exchanges: List<Exchange>) {
        val seen = exchanges.mapNotNull { it.spec }.toSet()
        val events = exchanges.filter { it.isEvent }.flatMap { e ->
            e.decodedCommand!!.root.child("events")!!.children.drop(1).mapNotNull { it.text }
        }.toSortedSet()
        println("Packets: ${exchanges.size} exchanges, ${seen.size} of ${JdwpSpec.all.size} commands seen")
        println("Event kinds seen: $events")
        println("Commands not exercised: " + JdwpSpec.all.filter { it !in seen }.joinToString { it.fullName })
        exchanges.filter { it.isError }.groupBy { it.name }.forEach { (name, list) ->
            println("Error replies: $name × ${list.size} (${list.first().decodedReply?.fields?.firstOrNull()?.text})")
        }

        val problems = exchanges.filter { it.hasProblem }
        problems.take(10).forEach { e ->
            println("PROBLEM in #${e.index} ${e.name}:\n${e.decodedCommand?.root}\n--- reply ---\n${e.decodedReply?.root}")
        }
        assertTrue(problems.isEmpty(), "${problems.size} packets did not decode cleanly")
        val unanswered = exchanges.filter { it.isPending && it.spec?.fullName != "VirtualMachine.Exit" }
        assertTrue(unanswered.isEmpty(), "Commands without a reply: ${unanswered.map { it.name }}")
        assertTrue(seen.size >= 75, "Only ${seen.size} commands were exercised")
        assertTrue(exchanges.none { it.spec == null }, "Unknown commands: ${exchanges.filter { it.spec == null }.map { it.name }}")
    }
}
