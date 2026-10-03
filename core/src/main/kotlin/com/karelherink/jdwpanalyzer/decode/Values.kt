package com.karelherink.jdwpanalyzer.decode

import com.karelherink.jdwpanalyzer.protocol.JdwpConstants
import com.karelherink.jdwpanalyzer.protocol.Tag

/** ID sizes announced by the VM in its VirtualMachine.IDSizes reply. Every HotSpot VM uses 8 throughout. */
data class IdSizes(
    val fieldId: Int = 8,
    val methodId: Int = 8,
    val objectId: Int = 8,
    val referenceTypeId: Int = 8,
    val frameId: Int = 8,
)

/** A JDWP value: a tag plus a primitive, an object ID (Long), or nothing for void. */
data class JdwpValue(val tag: Tag, val raw: Any?) {
    val objectId: Long? get() = if (tag.isObject) raw as Long else null

    fun text(): String = when {
        tag == Tag.VOID -> "void"
        tag.isObject -> if (raw == 0L) "null" else "${tag.description} #$raw"
        tag == Tag.CHAR -> {
            val c = raw as Char
            "char '${if (c.isISOControl()) "\\u%04x".format(c.code) else c.toString()}' (${c.code})"
        }
        tag == Tag.LONG -> "long ${raw}L"
        tag == Tag.FLOAT -> "float ${raw}f"
        else -> "${tag.description} $raw"
    }
}

data class Location(val typeTag: Int, val classId: Long, val methodId: Long, val index: Long) {
    override fun toString(): String = "${JdwpConstants.typeTag(typeTag)} #$classId, method #$methodId, index $index"
}

/** Decodes Java's "modified UTF-8", which JDWP uses for every string. */
fun decodeModifiedUtf8(bytes: ByteArray, from: Int, length: Int): String {
    val sb = StringBuilder(length)
    var i = from
    val end = from + length
    while (i < end) {
        val a = bytes[i].toInt() and 0xff
        when {
            a < 0x80 -> {
                sb.append(a.toChar()); i += 1
            }
            a and 0xe0 == 0xc0 && i + 1 < end -> {
                val b = bytes[i + 1].toInt() and 0x3f
                sb.append((((a and 0x1f) shl 6) or b).toChar()); i += 2
            }
            a and 0xf0 == 0xe0 && i + 2 < end -> {
                val b = bytes[i + 1].toInt() and 0x3f
                val c = bytes[i + 2].toInt() and 0x3f
                sb.append((((a and 0x0f) shl 12) or (b shl 6) or c).toChar()); i += 3
            }
            else -> {
                sb.append('�'); i += 1
            }
        }
    }
    return sb.toString()
}
