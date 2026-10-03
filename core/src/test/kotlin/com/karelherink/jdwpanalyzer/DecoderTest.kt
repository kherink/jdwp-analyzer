package com.karelherink.jdwpanalyzer

import com.karelherink.jdwpanalyzer.decode.JdwpValue
import com.karelherink.jdwpanalyzer.decode.decodeModifiedUtf8
import com.karelherink.jdwpanalyzer.log.PacketLog
import com.karelherink.jdwpanalyzer.model.Analyzer
import com.karelherink.jdwpanalyzer.model.Signatures
import com.karelherink.jdwpanalyzer.protocol.Direction
import com.karelherink.jdwpanalyzer.protocol.JdwpPacket
import com.karelherink.jdwpanalyzer.proxy.ProxyConfig
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DecoderTest {

    private fun bytes(block: DataOutputStream.() -> Unit): ByteArray =
        ByteArrayOutputStream().also { DataOutputStream(it).block() }.toByteArray()

    @Test
    fun `version reply decodes every field`() {
        val analyzer = Analyzer()
        analyzer.onPacket(JdwpPacket.command(1, 1, 1), Direction.DEBUGGER_TO_VM)
        analyzer.onPacket(
            JdwpPacket.reply(1, data = bytes {
                writeInt(3); write("Ok!".toByteArray()); writeInt(26); writeInt(0)
                writeInt(2); write("26".toByteArray()); writeInt(2); write("VM".toByteArray())
            }),
            Direction.VM_TO_DEBUGGER,
        )
        val exchange = analyzer.exchanges().single()
        assertEquals("VirtualMachine.Version", exchange.name)
        val reply = assertNotNull(exchange.decodedReply)
        assertNull(reply.problem)
        assertEquals(26, reply.root.int("jdwpMajor"))
        assertEquals("VM", reply.root.string("vmName"))
    }

    @Test
    fun `trailing bytes and unknown commands are reported, not dropped`() {
        val analyzer = Analyzer()
        analyzer.onPacket(JdwpPacket.command(1, 1, 8, byteArrayOf(1, 2)), Direction.DEBUGGER_TO_VM) // Suspend has no data
        analyzer.onPacket(JdwpPacket.command(2, 99, 1, byteArrayOf(5)), Direction.DEBUGGER_TO_VM)
        val (suspend, unknown) = analyzer.exchanges()
        assertEquals("2 trailing bytes", suspend.decodedCommand?.problem)
        assertEquals("CommandSet99.Command1", unknown.name)
        assertTrue(unknown.hasProblem)
    }

    @Test
    fun `untagged values need a known field signature`() {
        val analyzer = Analyzer()
        // ObjectReference.SetValues for object 5, field 9, without having seen field 9's signature.
        analyzer.onPacket(
            JdwpPacket.command(1, 9, 3, bytes { writeLong(5); writeInt(1); writeLong(9); writeInt(42) }),
            Direction.DEBUGGER_TO_VM,
        )
        val problem = analyzer.exchanges().single().decodedCommand?.problem
        assertTrue(problem!!.contains("field #9"), problem)
    }

    @Test
    fun `replies pair with commands from the opposite direction`() {
        val analyzer = Analyzer()
        // The debugger and the VM number their commands independently, so both may use ID 1.
        analyzer.onPacket(JdwpPacket.command(1, 1, 1), Direction.DEBUGGER_TO_VM)
        analyzer.onPacket(JdwpPacket.command(1, 64, 100, bytes { writeByte(0); writeInt(1); writeByte(99); writeInt(0) }), Direction.VM_TO_DEBUGGER)
        analyzer.onPacket(JdwpPacket.reply(1, errorCode = 112), Direction.VM_TO_DEBUGGER)
        val (version, event) = analyzer.exchanges()
        assertEquals(112, version.errorCode)
        assertNull(event.reply)
        assertEquals("VM_DEATH", event.eventKinds)
    }

    @Test
    fun `values, strings and signatures render like Java`() {
        assertEquals("float 1.5f", JdwpValue(com.karelherink.jdwpanalyzer.protocol.Tag.FLOAT, 1.5f).text())
        assertEquals("a\u0000€", decodeModifiedUtf8(byteArrayOf(0x61, 0xc0.toByte(), 0x80.toByte(), 0xe2.toByte(), 0x82.toByte(), 0xac.toByte()), 0, 6))
        assertEquals("java.util.Map\$Entry[][]", Signatures.typeName("[[Ljava/util/Map\$Entry;"))
        assertEquals("(int, String[], long)", Signatures.parameterList("(I[Ljava/lang/String;J)V"))
        assertEquals("localhost" to 5005, ProxyConfig.parseAddress("5005"))
        assertEquals("10.0.0.2" to 8000, ProxyConfig.parseAddress("10.0.0.2:8000"))
    }

    @Test
    fun `packet logs round-trip`() {
        val dir = Files.createTempDirectory("log").toFile()
        val packets = listOf(JdwpPacket.command(7, 1, 1), JdwpPacket.reply(7, data = byteArrayOf(0, 0, 0, 0)))
        PacketLog.write(dir, packets)
        val read = PacketLog.read(dir)
        assertEquals(packets.map { it.bytes.toList() }, read.map { it.bytes.toList() })
    }
}
