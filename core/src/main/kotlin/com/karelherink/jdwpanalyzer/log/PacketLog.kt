package com.karelherink.jdwpanalyzer.log

import com.karelherink.jdwpanalyzer.protocol.JdwpPacket
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.File
import java.io.IOException

/**
 * Packet logs use the same format as the original Java tool: a `seq.log` file holding every packet's raw
 * bytes back to back, in the order the proxy saw them, without the handshake.
 */
object PacketLog {
    const val FILE_NAME = "seq.log"

    /** Accepts either a log directory (containing `seq.log`) or the log file itself. */
    fun resolve(path: File): File = if (path.isDirectory) File(path, FILE_NAME) else path

    fun read(path: File): List<JdwpPacket> {
        val file = resolve(path)
        if (!file.isFile) throw IOException("No packet log at ${file.path}")
        return DataInputStream(BufferedInputStream(file.inputStream())).use { input ->
            buildList {
                while (true) add(JdwpPacket.read(input) ?: break)
            }
        }
    }

    fun write(path: File, packets: List<JdwpPacket>) {
        PacketLogWriter(path).use { writer -> packets.forEach(writer::write) }
    }
}

/** Appends packets to a log as they arrive. [path] is a directory (created if needed) or a file. */
class PacketLogWriter(path: File) : AutoCloseable {
    private val file: File = if (path.extension == "log") path else File(path, PacketLog.FILE_NAME)
    private val out: BufferedOutputStream
    private var closed = false

    init {
        file.parentFile?.let { if (!it.isDirectory && !it.mkdirs()) throw IOException("Couldn't create directory $it") }
        out = BufferedOutputStream(file.outputStream())
    }

    val location: File get() = file

    @Synchronized
    fun write(packet: JdwpPacket) {
        if (closed) return
        packet.writeTo(out)
        out.flush()
    }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        out.close()
    }
}
