package com.karelherink.jdwpanalyzer.decode

import com.karelherink.jdwpanalyzer.model.EventRequestInfo
import com.karelherink.jdwpanalyzer.model.FrameInfo
import com.karelherink.jdwpanalyzer.model.Signatures
import com.karelherink.jdwpanalyzer.model.Variable
import com.karelherink.jdwpanalyzer.protocol.JdwpConstants
import com.karelherink.jdwpanalyzer.protocol.Tag

typealias Body = Decoder.() -> Unit

/** Layout of one JDWP command: what the command packet carries ([out]) and what its reply carries ([reply]). */
class CommandSpec(
    val commandSet: Int,
    val command: Int,
    val setName: String,
    val name: String,
    val out: Body,
    val reply: Body,
) {
    val fullName: String get() = "$setName.$name"
}

/**
 * The complete JDWP protocol as of JDK 26: 18 command sets, 95 commands (Field defines none), every event kind
 * and event modifier.
 * See https://docs.oracle.com/en/java/javase/26/docs/specs/jdwp/jdwp-protocol.html.
 */
object JdwpSpec {

    private val sets = linkedMapOf<Int, String>()
    private val commands = linkedMapOf<Int, CommandSpec>()

    val commandSets: Map<Int, String> get() = sets
    val all: Collection<CommandSpec> get() = commands.values

    fun find(commandSet: Int, command: Int): CommandSpec? = commands[(commandSet shl 8) or command]

    fun nameOf(commandSet: Int, command: Int): String =
        find(commandSet, command)?.fullName
            ?: "${sets[commandSet] ?: "CommandSet$commandSet"}.Command$command"

    private class SetBuilder(val id: Int, val name: String) {
        fun cmd(id: Int, name: String, out: Body = {}, reply: Body = {}) {
            commands[(this.id shl 8) or id] = CommandSpec(this.id, id, this.name, name, out, reply)
        }
    }

    private fun set(id: Int, name: String, block: SetBuilder.() -> Unit) {
        sets[id] = name
        SetBuilder(id, name).block()
    }

    // ---- shared fragments ------------------------------------------------------------------------------------

    /** `Repeat` of single fields: an int count, then the items directly (no per-item group). */
    private fun Decoder.list(name: String, item: Decoder.(Int) -> Unit): Int = group(name) {
        val count = int("count")
        for (i in 0 until count) item(i)
        summary("$count")
        count
    }

    private fun Decoder.cmdLong(name: String): Long? = command?.long(name)

    private fun Decoder.typeEntry(signature: Boolean, generic: Boolean) {
        val tag = typeTag()
        val id = referenceType("typeID")
        val sig = if (signature) string("signature") else null
        val gen = if (generic) string("genericSignature") else null
        val status = classStatus()
        sig?.let { summary(it) }
        learn {
            type(id).apply {
                typeTag = tag
                if (sig != null) this.signature = sig
                if (gen != null) genericSignature = gen
                this.status = status
            }
        }
    }

    private fun Decoder.tagAndType(learnType: (Long, Int) -> Unit = { _, _ -> }) {
        val tag = typeTag()
        val id = referenceType("typeID")
        learn { type(id).typeTag = tag }
        learnType(id, tag)
    }

    private fun Decoder.members(isMethod: Boolean, generic: Boolean) {
        val owner = cmdLong("refType") ?: return
        val ids = mutableListOf<Long>()
        repeat(if (isMethod) "methods" else "fields", if (isMethod) "method" else "field") {
            val id = if (isMethod) method("methodID", owner) else field("fieldID", owner)
            val name = string("name")
            val sig = string("signature")
            val gen = if (generic) string("genericSignature") else null
            val mods = modBits()
            summary(name + if (isMethod) Signatures.parameterList(sig) else "")
            ids += id
            learn {
                if (isMethod) {
                    method(owner, id).apply {
                        this.name = name; signature = sig; modifiers = mods
                        if (gen != null) genericSignature = gen
                    }
                } else {
                    field(owner, id).apply {
                        this.name = name; signature = sig; modifiers = mods
                        if (gen != null) genericSignature = gen
                    }
                }
            }
        }
        learn { if (isMethod) type(owner).methods = ids.toList() else type(owner).fields = ids.toList() }
    }

    private fun Decoder.invoke(withObject: Boolean) {
        if (withObject) objectId("object")
        if (!withObject) referenceType("clazz")
        thread()
        if (withObject) referenceType("clazz")
        method()
        list("arguments") { value("[$it]") }
        int("options", JdwpConstants::invokeOptions)
    }

    private fun Decoder.invokeReply() {
        value("returnValue")
        taggedObject("exception")
    }

    private fun Decoder.variableTable(generic: Boolean) {
        val classId = cmdLong("refType")
        val methodId = cmdLong("methodID")
        val argCnt = int("argCnt")
        val vars = mutableListOf<Variable>()
        repeat("slots", "slot") {
            val codeIndex = long("codeIndex")
            val name = string("name")
            val sig = string("signature")
            if (generic) string("genericSignature")
            val length = int("length")
            val slot = int("slot")
            summary("$slot: $name")
            vars += Variable(codeIndex, name, sig, length, slot)
        }
        if (classId != null && methodId != null) learn {
            method(classId, methodId).apply { argCount = argCnt; variables = vars.toList() }
        }
    }

    /** The class of an object, when an earlier ObjectReference.ReferenceType reply told us. */
    private fun Decoder.classOf(objectId: Long?): Long? = objectId?.let { registry.objectType(it) }

    private fun Decoder.untaggedFieldValue(classId: Long?, fieldId: Long, objectId: Long?) {
        val sig = registry.fieldSignature(classId, fieldId, objectId)
        untaggedValue("value", sig?.let { Tag.forSignature(it) }, "the signature of field #$fieldId hasn't been seen")
    }

    // ---- command sets ----------------------------------------------------------------------------------------

    init {
        set(1, "VirtualMachine") {
            cmd(1, "Version", reply = {
                string("description")
                int("jdwpMajor")
                int("jdwpMinor")
                string("vmVersion")
                string("vmName")
            })
            cmd(2, "ClassesBySignature", out = { string("signature") }, reply = {
                val sig = command?.string("signature")
                repeat("classes", "class") {
                    val tag = typeTag()
                    val id = referenceType("typeID")
                    val status = classStatus()
                    learn { type(id).apply { typeTag = tag; if (sig != null) signature = sig; this.status = status } }
                }
            })
            cmd(3, "AllClasses", reply = { repeat("classes", "class") { typeEntry(signature = true, generic = false) } })
            cmd(4, "AllThreads", reply = { list("threads") { thread("[$it]") } })
            cmd(5, "TopLevelThreadGroups", reply = { list("groups") { threadGroup("[$it]") } })
            cmd(6, "Dispose")
            cmd(7, "IDSizes", reply = {
                val sizes = IdSizes(int("fieldIDSize"), int("methodIDSize"), int("objectIDSize"), int("referenceTypeIDSize"), int("frameIDSize"))
                learn { idSizes(sizes) }
            })
            cmd(8, "Suspend")
            cmd(9, "Resume")
            cmd(10, "Exit", out = { int("exitCode") })
            cmd(11, "CreateString", out = { string("utf") }, reply = {
                val id = stringId("stringObject")
                val value = command?.string("utf")
                if (value != null) learn { obj(id).stringValue = value }
            })
            cmd(12, "Capabilities", reply = {
                listOf(
                    "canWatchFieldModification", "canWatchFieldAccess", "canGetBytecodes", "canGetSyntheticAttribute",
                    "canGetOwnedMonitorInfo", "canGetCurrentContendedMonitor", "canGetMonitorInfo",
                ).forEach { boolean(it) }
            })
            cmd(13, "ClassPaths", reply = {
                string("baseDir")
                list("classpaths") { string("[$it]") }
                list("bootclasspaths") { string("[$it]") }
            })
            cmd(14, "DisposeObjects", out = {
                repeat("requests", "request") {
                    objectId("object")
                    int("refCnt")
                }
            })
            cmd(15, "HoldEvents")
            cmd(16, "ReleaseEvents")
            cmd(17, "CapabilitiesNew", reply = {
                listOf(
                    "canWatchFieldModification", "canWatchFieldAccess", "canGetBytecodes", "canGetSyntheticAttribute",
                    "canGetOwnedMonitorInfo", "canGetCurrentContendedMonitor", "canGetMonitorInfo",
                    "canRedefineClasses", "canAddMethod", "canUnrestrictedlyRedefineClasses", "canPopFrames",
                    "canUseInstanceFilters", "canGetSourceDebugExtension", "canRequestVMDeathEvent",
                    "canSetDefaultStratum", "canGetInstanceInfo", "canRequestMonitorEvents",
                    "canGetMonitorFrameInfo", "canUseSourceNameFilters", "canGetConstantPool", "canForceEarlyReturn",
                ).forEach { boolean(it) }
                for (i in 22..32) boolean("reserved$i")
            })
            cmd(18, "RedefineClasses", out = {
                repeat("classes", "class") {
                    referenceType()
                    bytes("classfile")
                }
            })
            cmd(19, "SetDefaultStratum", out = { string("stratumID") })
            cmd(20, "AllClassesWithGeneric", reply = { repeat("classes", "class") { typeEntry(signature = true, generic = true) } })
            cmd(21, "InstanceCounts", out = { list("refTypesCount") { referenceType("[$it]") } }, reply = {
                list("counts") { long("[$it]") }
            })
            cmd(22, "AllModules", reply = { list("modules") { module("[$it]") } })
        }

        set(2, "ReferenceType") {
            val refType: Body = { referenceType() }
            fun Decoder.owner(): Long? = cmdLong("refType")
            cmd(1, "Signature", out = refType, reply = {
                val sig = string("signature")
                owner()?.let { t -> learn { type(t).signature = sig } }
            })
            cmd(2, "ClassLoader", out = refType, reply = {
                val loader = classLoader()
                owner()?.let { t -> learn { type(t).classLoader = loader } }
            })
            cmd(3, "Modifiers", out = refType, reply = {
                val mods = modBits()
                owner()?.let { t -> learn { type(t).modifiers = mods } }
            })
            cmd(4, "Fields", out = refType, reply = { members(isMethod = false, generic = false) })
            cmd(5, "Methods", out = refType, reply = { members(isMethod = true, generic = false) })
            cmd(6, "GetValues", out = {
                referenceType()
                list("fields") { field("[$it]") }
            }, reply = { list("values") { value("[$it]") } })
            cmd(7, "SourceFile", out = refType, reply = {
                val file = string("sourceFile")
                owner()?.let { t -> learn { type(t).sourceFile = file } }
            })
            cmd(8, "NestedTypes", out = refType, reply = {
                val nested = mutableListOf<Long>()
                repeat("classes", "class") { tagAndType { id, _ -> nested += id } }
                owner()?.let { t -> learn { type(t).nestedTypes = nested.toList() } }
            })
            cmd(9, "Status", out = refType, reply = {
                val status = classStatus()
                owner()?.let { t -> learn { type(t).status = status } }
            })
            cmd(10, "Interfaces", out = refType, reply = {
                val ids = mutableListOf<Long>()
                list("interfaces") { ids += referenceType("[$it]") }
                owner()?.let { t -> learn { type(t).interfaces = ids.toList() } }
            })
            cmd(11, "ClassObject", out = refType, reply = {
                val co = classObject()
                owner()?.let { t -> learn { type(t).classObject = co; obj(co).reflectedType = t } }
            })
            cmd(12, "SourceDebugExtension", out = refType, reply = {
                val ext = string("extension")
                owner()?.let { t -> learn { type(t).sourceDebugExtension = ext } }
            })
            cmd(13, "SignatureWithGeneric", out = refType, reply = {
                val sig = string("signature")
                val gen = string("genericSignature")
                owner()?.let { t -> learn { type(t).apply { signature = sig; genericSignature = gen } } }
            })
            cmd(14, "FieldsWithGeneric", out = refType, reply = { members(isMethod = false, generic = true) })
            cmd(15, "MethodsWithGeneric", out = refType, reply = { members(isMethod = true, generic = true) })
            cmd(16, "Instances", out = {
                referenceType()
                int("maxInstances")
            }, reply = {
                val t = owner()
                list("instances") {
                    val o = taggedObject("[$it]")
                    val id = o.objectId
                    if (t != null && id != null && id != 0L) learn { obj(id).type = t }
                }
            })
            cmd(17, "ClassFileVersion", out = refType, reply = {
                val major = int("majorVersion")
                val minor = int("minorVersion")
                owner()?.let { t -> learn { type(t).classFileVersion = "$major.$minor" } }
            })
            cmd(18, "ConstantPool", out = refType, reply = {
                int("count")
                bytes("bytes")
            })
            cmd(19, "Module", out = refType, reply = {
                val m = module()
                owner()?.let { t -> learn { type(t).module = m } }
            })
        }

        set(3, "ClassType") {
            cmd(1, "Superclass", out = { referenceType("clazz") }, reply = {
                val sup = referenceType("superclass")
                cmdLong("clazz")?.let { c -> learn { type(c).superclass = sup } }
            })
            cmd(2, "SetValues", out = {
                val clazz = referenceType("clazz")
                repeat("values", "value") {
                    val f = field("fieldID", clazz)
                    untaggedFieldValue(clazz, f, null)
                }
            })
            cmd(3, "InvokeMethod", out = { invoke(withObject = false) }, reply = { invokeReply() })
            cmd(4, "NewInstance", out = { invoke(withObject = false) }, reply = {
                val o = taggedObject("newObject")
                taggedObject("exception")
                val clazz = cmdLong("clazz")
                val id = o.objectId
                if (clazz != null && id != null && id != 0L) learn { obj(id).type = clazz }
            })
        }

        set(4, "ArrayType") {
            cmd(1, "NewInstance", out = {
                referenceType("arrType")
                int("length")
            }, reply = {
                val o = taggedObject("newArray")
                val t = cmdLong("arrType")
                val id = o.objectId
                if (t != null && id != null && id != 0L) learn { obj(id).type = t }
            })
        }

        set(5, "InterfaceType") {
            cmd(1, "InvokeMethod", out = { invoke(withObject = false) }, reply = { invokeReply() })
        }

        set(6, "Method") {
            val methodOut: Body = {
                referenceType()
                method()
            }
            cmd(1, "LineTable", out = methodOut, reply = {
                val start = long("start")
                val end = long("end")
                val lines = mutableListOf<Pair<Long, Int>>()
                repeat("lines", "line") {
                    val idx = long("lineCodeIndex")
                    val line = int("lineNumber")
                    summary("line $line @ $idx")
                    lines += idx to line
                }
                val c = cmdLong("refType")
                val m = cmdLong("methodID")
                if (c != null && m != null) learn {
                    method(c, m).apply { lineTable = lines.toList(); codeRange = start..end }
                }
            })
            cmd(2, "VariableTable", out = methodOut, reply = { variableTable(generic = false) })
            cmd(3, "Bytecodes", out = methodOut, reply = {
                val code = bytes("bytes")
                val c = cmdLong("refType")
                val m = cmdLong("methodID")
                if (c != null && m != null) learn { method(c, m).bytecodeSize = code.size }
            })
            cmd(4, "IsObsolete", out = methodOut, reply = {
                val obsolete = boolean("isObsolete")
                val c = cmdLong("refType")
                val m = cmdLong("methodID")
                if (c != null && m != null) learn { method(c, m).obsolete = obsolete }
            })
            cmd(5, "VariableTableWithGeneric", out = methodOut, reply = { variableTable(generic = true) })
        }

        // The Field command set exists in the protocol but defines no commands.
        set(8, "Field") {}

        set(9, "ObjectReference") {
            val objectOut: Body = { objectId("object") }
            cmd(1, "ReferenceType", out = objectOut, reply = {
                val o = cmdLong("object")
                tagAndType { id, _ -> if (o != null) learn { obj(o).type = id } }
            })
            cmd(2, "GetValues", out = {
                val o = objectId("object")
                val cls = classOf(o)
                list("fields") { field("[$it]", cls) }
            }, reply = { list("values") { value("[$it]") } })
            cmd(3, "SetValues", out = {
                val o = objectId("object")
                val cls = classOf(o)
                repeat("values", "value") {
                    val f = field("fieldID", cls)
                    untaggedFieldValue(cls, f, o)
                }
            })
            cmd(5, "MonitorInfo", out = objectOut, reply = {
                thread("owner")
                int("entryCount")
                list("waiters") { thread("[$it]") }
            })
            cmd(6, "InvokeMethod", out = { invoke(withObject = true) }, reply = { invokeReply() })
            cmd(7, "DisableCollection", out = objectOut)
            cmd(8, "EnableCollection", out = objectOut)
            cmd(9, "IsCollected", out = objectOut, reply = { boolean("isCollected") })
            cmd(10, "ReferringObjects", out = {
                objectId("object")
                int("maxReferrers")
            }, reply = { list("referringObjects") { taggedObject("[$it]") } })
        }

        set(10, "StringReference") {
            cmd(1, "Value", out = { stringId("stringObject") }, reply = {
                val value = string("stringValue")
                cmdLong("stringObject")?.let { s -> learn { obj(s).stringValue = value } }
            })
        }

        set(11, "ThreadReference") {
            val threadOut: Body = { thread() }
            fun Decoder.self(): Long? = cmdLong("thread")
            cmd(1, "Name", out = threadOut, reply = {
                val name = string("threadName")
                self()?.let { t -> learn { obj(t).name = name } }
            })
            cmd(2, "Suspend", out = threadOut)
            cmd(3, "Resume", out = threadOut)
            cmd(4, "Status", out = threadOut, reply = {
                int("threadStatus") { JdwpConstants.threadStatus[it] ?: "UNKNOWN" }
                int("suspendStatus", JdwpConstants::suspendStatus)
            })
            cmd(5, "ThreadGroup", out = threadOut, reply = {
                val g = threadGroup()
                self()?.let { t -> learn { obj(t).threadGroup = g } }
            })
            cmd(6, "Frames", out = {
                thread()
                int("startFrame")
                int("length")
            }, reply = {
                val t = self()
                val start = command?.int("startFrame") ?: 0
                repeat("frames", "frame") { i ->
                    val id = frame("frameID")
                    val loc = location()
                    if (t != null) learn { frame(FrameInfo(id, t, loc, start + i)) }
                }
            })
            cmd(7, "FrameCount", out = threadOut, reply = { int("frameCount") })
            cmd(8, "OwnedMonitors", out = threadOut, reply = { list("owned") { taggedObject("[$it]") } })
            cmd(9, "CurrentContendedMonitor", out = threadOut, reply = { taggedObject("monitor") })
            cmd(10, "Stop", out = {
                thread()
                objectId("throwable")
            })
            cmd(11, "Interrupt", out = threadOut)
            cmd(12, "SuspendCount", out = threadOut, reply = { int("suspendCount") })
            cmd(13, "OwnedMonitorsStackDepthInfo", out = threadOut, reply = {
                repeat("owned", "monitor") {
                    taggedObject("monitor")
                    int("stack_depth")
                }
            })
            cmd(14, "ForceEarlyReturn", out = {
                thread()
                value("value")
            })
            cmd(15, "IsVirtual", out = threadOut, reply = { boolean("isVirtual") })
        }

        set(12, "ThreadGroupReference") {
            val groupOut: Body = { threadGroup() }
            cmd(1, "Name", out = groupOut, reply = {
                val name = string("groupName")
                cmdLong("group")?.let { g -> learn { obj(g).name = name } }
            })
            cmd(2, "Parent", out = groupOut, reply = {
                val parent = threadGroup("parentGroup")
                cmdLong("group")?.let { g -> learn { obj(g).parent = parent } }
            })
            cmd(3, "Children", out = groupOut, reply = {
                val g = cmdLong("group")
                list("childThreads") {
                    val t = thread("[$it]")
                    if (g != null) learn { obj(t).threadGroup = g }
                }
                list("childGroups") {
                    val c = threadGroup("[$it]")
                    if (g != null) learn { obj(c).parent = g }
                }
            })
        }

        set(13, "ArrayReference") {
            cmd(1, "Length", out = { arrayId() }, reply = { int("arrayLength") })
            cmd(2, "GetValues", out = {
                arrayId()
                int("firstIndex")
                int("length")
            }, reply = {
                val tag = arrayRegion()
                cmdLong("arrayObject")?.let { a -> learn { obj(a).componentTag = tag } }
            })
            cmd(3, "SetValues", out = {
                val a = arrayId()
                int("firstIndex")
                val tag = registry.arrayComponentTag(a)
                list("values") {
                    untaggedValue("[$it]", tag, "the component type of array #$a hasn't been seen")
                }
            })
        }

        set(14, "ClassLoaderReference") {
            cmd(1, "VisibleClasses", out = { classLoader("classLoaderObject") }, reply = {
                repeat("classes", "class") { tagAndType() }
            })
        }

        set(15, "EventRequest") {
            cmd(1, "Set", out = {
                byte("eventKind", JdwpConstants::eventKind)
                byte("suspendPolicy") { JdwpConstants.suspendPolicies[it] ?: "UNKNOWN" }
                repeat("modifiers", "modifier") { modifier() }
            }, reply = {
                val id = eventRequestId()
                val cmd = command
                if (cmd != null) {
                    val kind = cmd.int("eventKind") ?: 0
                    val policy = cmd.int("suspendPolicy") ?: 0
                    val mods = cmd.child("modifiers")?.children?.drop(1).orEmpty()
                    learn { eventRequest(EventRequestInfo(id, kind, policy, mods)) }
                }
            })
            cmd(2, "Clear", out = {
                byte("eventKind", JdwpConstants::eventKind)
                eventRequestId()
            })
            cmd(3, "ClearAllBreakpoints")
        }

        set(16, "StackFrame") {
            val frameOut: Body = {
                thread()
                frame()
            }
            cmd(1, "GetValues", out = {
                thread()
                val f = frame()
                repeat("slots", "slot") {
                    val slot = int("slot")
                    byte("sigbyte") { Tag.of(it)?.description ?: it.toChar().toString() }
                    registry.slotName(f, slot)?.let { summary(it) }
                }
            }, reply = { list("values") { value("[$it]") } })
            cmd(2, "SetValues", out = {
                thread()
                val f = frame()
                repeat("slotValues", "slotValue") {
                    val slot = int("slot")
                    value("slotValue")
                    registry.slotName(f, slot)?.let { summary(it) }
                }
            })
            cmd(3, "ThisObject", out = frameOut, reply = { taggedObject("objectThis") })
            cmd(4, "PopFrames", out = frameOut)
        }

        set(17, "ClassObjectReference") {
            cmd(1, "ReflectedType", out = { classObject() }, reply = {
                val co = cmdLong("classObject")
                tagAndType { id, _ -> if (co != null) learn { obj(co).reflectedType = id; type(id).classObject = co } }
            })
        }

        set(18, "ModuleReference") {
            val moduleOut: Body = { module() }
            cmd(1, "Name", out = moduleOut, reply = {
                val name = string("name")
                cmdLong("module")?.let { m -> learn { obj(m).name = name } }
            })
            cmd(2, "ClassLoader", out = moduleOut, reply = {
                val loader = classLoader()
                cmdLong("module")?.let { m -> learn { obj(m).classLoader = loader } }
            })
        }

        set(64, "Event") {
            cmd(100, "Composite", out = {
                byte("suspendPolicy") { JdwpConstants.suspendPolicies[it] ?: "UNKNOWN" }
                repeat("events", "event") { event() }
            })
        }
    }

    /** One `EventRequest.Set` modifier, selected by its modKind byte. */
    private fun Decoder.modifier() {
        val kind = byte("modKind") { JdwpConstants.modifierKinds[it] ?: "UNKNOWN" }
        summary(JdwpConstants.modifierKinds[kind] ?: "modKind $kind")
        when (kind) {
            1 -> int("count")
            2 -> int("exprID")
            3 -> thread()
            4 -> referenceType("clazz")
            5 -> string("classPattern")
            6 -> string("classPattern")
            7 -> location("loc")
            8 -> {
                referenceType("exceptionOrNull")
                boolean("caught")
                boolean("uncaught")
            }
            9 -> {
                val declaring = referenceType("declaring")
                field("fieldID", declaring)
            }
            10 -> {
                thread()
                int("size") { JdwpConstants.stepSizes[it] ?: "UNKNOWN" }
                int("depth") { JdwpConstants.stepDepths[it] ?: "UNKNOWN" }
            }
            11 -> objectId("instance")
            12 -> string("sourceNamePattern")
            13 -> {} // PlatformThreadsOnly has no data.
            else -> throw UndecodableException("Unknown event modifier kind $kind")
        }
    }

    /** One event of an `Event.Composite` command, selected by its eventKind byte. */
    private fun Decoder.event() {
        val kind = byte("eventKind", JdwpConstants::eventKind)
        summary(JdwpConstants.eventKind(kind))
        eventRequestId()
        when (kind) {
            90 -> thread() // VM_START
            1, 2, 40, 41 -> { // SINGLE_STEP, BREAKPOINT, METHOD_ENTRY, METHOD_EXIT
                thread()
                location()
            }
            42 -> { // METHOD_EXIT_WITH_RETURN_VALUE
                thread()
                location()
                value("value")
            }
            43, 44 -> { // MONITOR_CONTENDED_ENTER, MONITOR_CONTENDED_ENTERED
                thread()
                taggedObject("object")
                location()
            }
            45 -> { // MONITOR_WAIT
                thread()
                taggedObject("object")
                location()
                long("timeout")
            }
            46 -> { // MONITOR_WAITED
                thread()
                taggedObject("object")
                location()
                boolean("timed_out")
            }
            4 -> { // EXCEPTION
                thread()
                location()
                taggedObject("exception")
                location("catchLocation")
            }
            6, 7 -> { // THREAD_START, THREAD_DEATH
                thread()
            }
            8 -> { // CLASS_PREPARE
                thread()
                val tag = typeTag()
                val id = referenceType("typeID")
                val sig = string("signature")
                val status = classStatus()
                learn { type(id).apply { typeTag = tag; signature = sig; this.status = status } }
            }
            9 -> string("signature") // CLASS_UNLOAD
            20, 21 -> { // FIELD_ACCESS, FIELD_MODIFICATION
                thread()
                location()
                typeTag()
                val id = referenceType("typeID")
                field("fieldID", id)
                taggedObject("object")
                if (kind == 21) value("valueToBe")
            }
            99 -> {} // VM_DEATH
            else -> throw UndecodableException("Unknown event kind $kind")
        }
    }
}
