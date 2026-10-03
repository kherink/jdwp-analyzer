package com.karelherink.jdwpanalyzer.decode

import com.karelherink.jdwpanalyzer.model.Registry
import com.karelherink.jdwpanalyzer.protocol.JdwpConstants
import com.karelherink.jdwpanalyzer.protocol.Tag

/** Thrown when the rest of a packet can't be decoded, e.g. an untagged value whose type is unknown. */
class UndecodableException(message: String) : Exception(message)

/** Deferred update to the [Registry], applied once a packet has been decoded. */
typealias Fact = Registry.() -> Unit

/**
 * Reads one packet's data and builds its [DecodedNode] tree.
 *
 * Command specs in [JdwpSpec] drive a decoder imperatively: each call reads one field, appends a node for it,
 * and returns the value so later fields can depend on it. Things learnt along the way (class signatures, thread
 * names, ...) are queued with [learn] and applied by the caller.
 *
 * @param command the decoded command this packet replies to, so reply specs can refer to its fields.
 */
class Decoder(
    private val data: ByteArray,
    val registry: Registry,
    val command: DecodedNode? = null,
) {
    private val sizes: IdSizes = registry.idSizes

    private class Frame(val name: String, val start: Int) {
        var text: String? = null
        var value: Any? = null
        var ref: EntityRef? = null
        val children = mutableListOf<DecodedNode>()
    }

    private val stack = ArrayDeque<Frame>().apply { addLast(Frame("", 0)) }

    val facts = mutableListOf<Fact>()

    /** The most recent reference type ID read, which method and field IDs belong to. */
    var currentClass: Long? = command?.walk()?.lastOrNull { it.ref is EntityRef.Type }?.value as? Long

    var position: Int = 0
        private set

    val remaining: Int get() = data.size - position

    fun learn(fact: Fact) {
        facts += fact
    }

    // ---- raw reads -------------------------------------------------------------------------------------------

    private fun need(n: Int) {
        if (n < 0 || position + n > data.size) {
            throw UndecodableException("Expected $n more bytes at offset $position, but only $remaining remain")
        }
    }

    private fun readU8(): Int {
        need(1)
        return data[position++].toInt() and 0xff
    }

    private fun readN(n: Int): Long {
        need(n)
        var v = 0L
        repeat(n) { v = (v shl 8) or (data[position++].toLong() and 0xff) }
        return v
    }

    private fun readInt(): Int = readN(4).toInt()

    private fun readLong(): Long = readN(8)

    private fun readString(): String {
        val len = readInt()
        need(len)
        val s = decodeModifiedUtf8(data, position, len)
        position += len
        return s
    }

    // ---- node building ---------------------------------------------------------------------------------------

    private fun <T> leaf(name: String, ref: (T) -> EntityRef? = { null }, text: (T) -> String, read: () -> T): T {
        val start = position
        val v = read()
        stack.last().children += DecodedNode(name, text(v), v, ref(v), emptyList(), start, position - start)
        return v
    }

    private fun idLeaf(name: String, size: Int, ref: (Long) -> EntityRef?): Long {
        val start = position
        val id = readN(size)
        stack.last().children += DecodedNode(name, "#$id", id, if (id == 0L) null else ref(id), emptyList(), start, size)
        return id
    }

    /** Groups the nodes read by [body] under one node named [name]. */
    fun <T> group(name: String, text: String? = null, body: () -> T): T {
        val frame = Frame(name, position).also { it.text = text }
        stack.addLast(frame)
        try {
            return body()
        } finally {
            stack.removeLast()
            stack.last().children += DecodedNode(
                name, frame.text, frame.value, frame.ref, frame.children.toList(), frame.start, position - frame.start,
            )
        }
    }

    /** Sets the summary text (and optionally the value / entity reference) of the group being built. */
    fun summary(text: String?, value: Any? = null, ref: EntityRef? = null) {
        val f = stack.last()
        f.text = text
        if (value != null) f.value = value
        if (ref != null) f.ref = ref
    }

    /** Adds a node that isn't backed by bytes, e.g. an explanation. */
    fun note(name: String, text: String, problem: Boolean = false) {
        stack.last().children += DecodedNode(name, text, null, null, emptyList(), position, 0, problem)
    }

    // ---- primitive fields ------------------------------------------------------------------------------------

    fun byte(name: String, describe: ((Int) -> String)? = null): Int =
        leaf(name, text = { v -> describe?.let { "${it(v)} ($v)" } ?: v.toString() }) { readU8() }

    fun boolean(name: String): Boolean = leaf(name, text = { it.toString() }) { readU8() != 0 }

    fun int(name: String, describe: ((Int) -> String)? = null): Int =
        leaf(name, text = { v -> describe?.let { "${it(v)} ($v)" } ?: v.toString() }) { readInt() }

    /** An event request ID, linked to the EventRequest.Set that created it. */
    fun eventRequestId(name: String = "requestID"): Int =
        leaf(name, ref = { EntityRef.EventRequest(it) }, text = { it.toString() }) { readInt() }

    fun long(name: String): Long = leaf(name, text = { it.toString() }) { readLong() }

    fun string(name: String): String = leaf(name, text = { quote(it) }) { readString() }

    fun typeTag(name: String = "refTypeTag"): Int = byte(name, JdwpConstants::typeTag)

    fun classStatus(name: String = "status"): Int = int(name, JdwpConstants::classStatus)

    fun modBits(name: String = "modBits"): Int = int(name, JdwpConstants::modifiers)

    // ---- IDs -------------------------------------------------------------------------------------------------

    fun objectId(name: String, kind: ObjectKind = ObjectKind.OBJECT): Long {
        val id = idLeaf(name, sizes.objectId) { EntityRef.Obj(it, kind) }
        if (id != 0L && kind != ObjectKind.OBJECT) learn { objectKind(id, kind) }
        return id
    }

    fun thread(name: String = "thread") = objectId(name, ObjectKind.THREAD)

    fun threadGroup(name: String = "group") = objectId(name, ObjectKind.THREAD_GROUP)

    fun stringId(name: String) = objectId(name, ObjectKind.STRING)

    fun classLoader(name: String = "classLoader") = objectId(name, ObjectKind.CLASS_LOADER)

    fun classObject(name: String = "classObject") = objectId(name, ObjectKind.CLASS_OBJECT)

    fun arrayId(name: String = "arrayObject") = objectId(name, ObjectKind.ARRAY)

    fun module(name: String = "module") = objectId(name, ObjectKind.MODULE)

    /** A referenceTypeID (also used for classID, interfaceID and arrayTypeID). */
    fun referenceType(name: String = "refType"): Long {
        val id = idLeaf(name, sizes.referenceTypeId) { EntityRef.Type(it) }
        if (id != 0L) currentClass = id
        return id
    }

    fun method(name: String = "methodID", classId: Long? = currentClass): Long =
        idLeaf(name, sizes.methodId) { if (classId != null) EntityRef.Method(classId, it) else null }

    fun field(name: String = "fieldID", classId: Long? = currentClass): Long =
        idLeaf(name, sizes.fieldId) { if (classId != null) EntityRef.Field(classId, it) else null }

    fun frame(name: String = "frame"): Long = idLeaf(name, sizes.frameId) { EntityRef.Frame(it) }

    fun location(name: String = "location"): Location = group(name) {
        val tag = typeTag("typeTag")
        val classId = idLeaf("classID", sizes.referenceTypeId) { EntityRef.Type(it) }
        val methodId = idLeaf("methodID", sizes.methodId) { EntityRef.Method(classId, it) }
        val index = long("index")
        val loc = Location(tag, classId, methodId, index)
        summary("index $index", loc, if (classId != 0L) EntityRef.Method(classId, methodId) else null)
        loc
    }

    fun taggedObject(name: String): JdwpValue {
        val start = position
        val code = readU8()
        val tag = Tag.of(code)
            ?: throw UndecodableException("Unknown tag ${code.toChar()} ($code) at offset $start")
        val id = readN(sizes.objectId)
        val kind = ObjectKind.of(tag)
        val value = JdwpValue(tag, id)
        stack.last().children += DecodedNode(
            name, value.text(), value, if (id == 0L) null else EntityRef.Obj(id, kind), emptyList(), start, position - start,
        )
        if (id != 0L) learn { objectKind(id, kind) }
        return value
    }

    // ---- values ----------------------------------------------------------------------------------------------

    /** A tagged value: one tag byte, then the value. */
    fun value(name: String = "value"): JdwpValue {
        val start = position
        val code = readU8()
        val tag = Tag.of(code)
            ?: throw UndecodableException("Unknown value tag ${code.toChar()} ($code) at offset $start")
        return emitValue(name, tag, start)
    }

    /** An untagged value, whose [tag] comes from context (a field signature, an array's component type, ...). */
    fun untaggedValue(name: String, tag: Tag?, why: String): JdwpValue {
        if (tag == null) {
            throw UndecodableException("Can't decode untagged $name: $why")
        }
        return emitValue(name, tag, position)
    }

    private fun emitValue(name: String, tag: Tag, start: Int): JdwpValue {
        val raw: Any? = when (tag) {
            Tag.BYTE -> readN(1).toByte()
            Tag.BOOLEAN -> readN(1) != 0L
            Tag.CHAR -> readN(2).toInt().toChar()
            Tag.SHORT -> readN(2).toShort()
            Tag.INT -> readN(4).toInt()
            Tag.LONG -> readN(8)
            Tag.FLOAT -> Float.fromBits(readN(4).toInt())
            Tag.DOUBLE -> Double.fromBits(readN(8))
            Tag.VOID -> null
            else -> readN(sizes.objectId)
        }
        val value = JdwpValue(tag, raw)
        val id = value.objectId
        val ref = if (id != null && id != 0L) EntityRef.Obj(id, ObjectKind.of(tag)) else null
        stack.last().children += DecodedNode(name, value.text(), value, ref, emptyList(), start, position - start)
        if (id != null && id != 0L) learn { objectKind(id, ObjectKind.of(tag)) }
        return value
    }

    /** An arrayregion: a tag, a count, then values (untagged for primitive arrays, tagged for object arrays). */
    fun arrayRegion(name: String = "values"): Tag = group(name) {
        val code = readU8()
        val tag = Tag.of(code) ?: throw UndecodableException("Unknown array region tag $code")
        stack.last().children += DecodedNode("tag", "${tag.description} (${tag.code})", tag, null, emptyList(), position - 1, 1)
        val count = int("count")
        for (i in 0 until count) {
            if (tag.isObject) value("[$i]") else emitValue("[$i]", tag, position)
        }
        summary("$count × ${tag.description}")
        tag
    }

    /** A length-prefixed byte array (`Repeat byte` in the spec). */
    fun bytes(name: String): ByteArray {
        val start = position
        val len = readInt()
        need(len)
        val b = data.copyOfRange(position, position + len)
        position += len
        val preview = b.take(32).joinToString(" ") { "%02x".format(it) } + if (len > 32) " …" else ""
        stack.last().children += DecodedNode(name, "$len bytes: $preview", b, null, emptyList(), start, position - start)
        return b
    }

    /** `Repeat`: an int count followed by that many items, each decoded by [item] inside its own group. */
    fun repeat(name: String, itemName: String = name, item: (Int) -> Unit): Int = group(name) {
        val count = int("count")
        for (i in 0 until count) group("$itemName[$i]") { item(i) }
        summary("$count")
        count
    }

    /** Builds the result, recording any undecoded bytes or a decoding failure as a problem. */
    fun finish(problem: String?): DecodedPacket {
        var message = problem
        if (message != null) {
            note("Decoding stopped", message, problem = true)
        }
        if (remaining > 0) {
            val rest = data.copyOfRange(position, data.size)
            note(
                "Undecoded bytes",
                "${rest.size} bytes at offset $position: " +
                    rest.take(48).joinToString(" ") { "%02x".format(it) } + if (rest.size > 48) " …" else "",
                problem = true,
            )
            if (message == null) message = "${rest.size} trailing bytes"
        }
        val root = stack.first()
        return DecodedPacket(DecodedNode("", null, null, null, root.children.toList(), 0, data.size), message)
    }

    companion object {
        fun quote(s: String): String = buildString {
            append('"')
            for (c in s) when {
                c == '"' -> append("\\\"")
                c == '\n' -> append("\\n")
                c == '\t' -> append("\\t")
                c.isISOControl() -> append("\\u%04x".format(c.code))
                else -> append(c)
            }
            append('"')
        }
    }
}
