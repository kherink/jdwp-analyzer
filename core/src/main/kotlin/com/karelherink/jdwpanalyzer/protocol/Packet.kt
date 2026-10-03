package com.karelherink.jdwpanalyzer.protocol

import java.io.DataInputStream
import java.io.EOFException
import java.io.OutputStream

/** Which way a packet travelled through the proxy. */
enum class Direction(val arrow: String) {
    DEBUGGER_TO_VM("→"),
    VM_TO_DEBUGGER("←"),
    ;

    val opposite: Direction get() = if (this == DEBUGGER_TO_VM) VM_TO_DEBUGGER else DEBUGGER_TO_VM
}

/**
 * A single JDWP packet, kept as the exact bytes seen on the wire so it can be forwarded and logged unchanged.
 *
 * Layout: length (4) · id (4) · flags (1) · then either command set (1) + command (1) for a command packet,
 * or error code (2) for a reply packet · data.
 */
class JdwpPacket(val bytes: ByteArray) {
    init {
        require(bytes.size >= HEADER_SIZE) { "JDWP packet shorter than its header: ${bytes.size} bytes" }
    }

    val length: Int get() = int(0)
    val id: Int get() = int(4)
    val flags: Int get() = bytes[8].toInt() and 0xff
    val isReply: Boolean get() = flags and FLAG_REPLY != 0

    /** Command set; only meaningful for command packets. */
    val commandSet: Int get() = bytes[9].toInt() and 0xff

    /** Command within [commandSet]; only meaningful for command packets. */
    val command: Int get() = bytes[10].toInt() and 0xff

    /** Error code; only meaningful for reply packets. */
    val errorCode: Int get() = ((bytes[9].toInt() and 0xff) shl 8) or (bytes[10].toInt() and 0xff)

    val dataSize: Int get() = bytes.size - HEADER_SIZE

    fun data(): ByteArray = bytes.copyOfRange(HEADER_SIZE, bytes.size)

    private fun int(at: Int): Int =
        ((bytes[at].toInt() and 0xff) shl 24) or ((bytes[at + 1].toInt() and 0xff) shl 16) or
            ((bytes[at + 2].toInt() and 0xff) shl 8) or (bytes[at + 3].toInt() and 0xff)

    fun writeTo(out: OutputStream) = out.write(bytes)

    companion object {
        const val HEADER_SIZE = 11
        const val FLAG_REPLY = 0x80

        /** The 14 ASCII bytes each side sends once before any packet. */
        val HANDSHAKE: ByteArray = "JDWP-Handshake".toByteArray(Charsets.US_ASCII)

        /** Reads one packet, or returns null on a clean end of stream before the first header byte. */
        fun read(input: DataInputStream): JdwpPacket? {
            val header = ByteArray(HEADER_SIZE)
            val first = input.read()
            if (first < 0) return null
            header[0] = first.toByte()
            try {
                input.readFully(header, 1, HEADER_SIZE - 1)
            } catch (e: EOFException) {
                throw EOFException("Stream ended inside a JDWP packet header")
            }
            val length = ((header[0].toInt() and 0xff) shl 24) or ((header[1].toInt() and 0xff) shl 16) or
                ((header[2].toInt() and 0xff) shl 8) or (header[3].toInt() and 0xff)
            if (length < HEADER_SIZE) throw java.io.IOException("Invalid JDWP packet length $length")
            val bytes = header.copyOf(length)
            input.readFully(bytes, HEADER_SIZE, length - HEADER_SIZE)
            return JdwpPacket(bytes)
        }

        fun command(id: Int, commandSet: Int, command: Int, data: ByteArray = ByteArray(0)): JdwpPacket =
            build(id, 0, (commandSet shl 8) or command, data)

        fun reply(id: Int, errorCode: Int = 0, data: ByteArray = ByteArray(0)): JdwpPacket =
            build(id, FLAG_REPLY, errorCode, data)

        private fun build(id: Int, flags: Int, lastTwo: Int, data: ByteArray): JdwpPacket {
            val length = HEADER_SIZE + data.size
            val bytes = ByteArray(length)
            for (i in 0 until 4) bytes[i] = (length ushr (24 - 8 * i)).toByte()
            for (i in 0 until 4) bytes[4 + i] = (id ushr (24 - 8 * i)).toByte()
            bytes[8] = flags.toByte()
            bytes[9] = (lastTwo ushr 8).toByte()
            bytes[10] = lastTwo.toByte()
            data.copyInto(bytes, HEADER_SIZE)
            return JdwpPacket(bytes)
        }
    }
}
