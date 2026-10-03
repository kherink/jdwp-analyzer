package com.karelherink.jdwpanalyzer.decode

import com.karelherink.jdwpanalyzer.protocol.Tag

/** What kind of mirror an object ID refers to, as far as the protocol tells us. */
enum class ObjectKind(val label: String) {
    OBJECT("object"),
    ARRAY("array"),
    STRING("string"),
    THREAD("thread"),
    THREAD_GROUP("thread group"),
    CLASS_LOADER("class loader"),
    CLASS_OBJECT("class object"),
    MODULE("module"),
    ;

    companion object {
        fun of(tag: Tag): ObjectKind = when (tag) {
            Tag.ARRAY -> ARRAY
            Tag.STRING -> STRING
            Tag.THREAD -> THREAD
            Tag.THREAD_GROUP -> THREAD_GROUP
            Tag.CLASS_LOADER -> CLASS_LOADER
            Tag.CLASS_OBJECT -> CLASS_OBJECT
            else -> OBJECT
        }
    }
}

/** A reference from a decoded field to something the [com.karelherink.jdwpanalyzer.model.Registry] can describe. */
sealed interface EntityRef {
    data class Obj(val id: Long, val kind: ObjectKind) : EntityRef
    data class Type(val id: Long) : EntityRef
    data class Method(val classId: Long, val methodId: Long) : EntityRef
    data class Field(val classId: Long, val fieldId: Long) : EntityRef
    data class Frame(val id: Long) : EntityRef
    data class EventRequest(val id: Int) : EntityRef
}

/**
 * One field of a decoded packet.
 *
 * @property text the decoded value as text (null for pure groups).
 * @property value the decoded value itself, for programmatic lookups.
 * @property ref the entity this field refers to, resolved to a name at display time.
 * @property offset start of this field within the packet data (not counting the 11-byte header).
 * @property size number of data bytes the field covers.
 */
class DecodedNode(
    val name: String,
    val text: String?,
    val value: Any?,
    val ref: EntityRef?,
    val children: List<DecodedNode>,
    val offset: Int,
    val size: Int,
    val problem: Boolean = false,
) {
    fun child(name: String): DecodedNode? = children.firstOrNull { it.name == name }

    fun long(name: String): Long? = child(name)?.value as? Long

    fun int(name: String): Int? = child(name)?.value as? Int

    fun string(name: String): String? = child(name)?.value as? String

    /** Depth-first walk over this node and everything below it. */
    fun walk(): Sequence<DecodedNode> = sequence {
        yield(this@DecodedNode)
        for (c in children) yieldAll(c.walk())
    }

    override fun toString(): String = buildString {
        if (name.isEmpty() && text == null) children.forEach { it.render(this, 0) } else render(this, 0)
    }

    private fun render(sb: StringBuilder, depth: Int) {
        sb.append("  ".repeat(depth)).append(name)
        if (text != null) sb.append(": ").append(text)
        sb.append('\n')
        for (c in children) c.render(sb, depth + 1)
    }
}

/** A decoded packet body. [problem] is set when the data could not be fully decoded. */
class DecodedPacket(
    val root: DecodedNode,
    val problem: String? = null,
) {
    val fields: List<DecodedNode> get() = root.children
}
