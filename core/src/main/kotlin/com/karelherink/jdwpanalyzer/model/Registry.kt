package com.karelherink.jdwpanalyzer.model

import com.karelherink.jdwpanalyzer.decode.DecodedNode
import com.karelherink.jdwpanalyzer.decode.Decoder
import com.karelherink.jdwpanalyzer.decode.EntityRef
import com.karelherink.jdwpanalyzer.decode.IdSizes
import com.karelherink.jdwpanalyzer.decode.Location
import com.karelherink.jdwpanalyzer.decode.ObjectKind
import com.karelherink.jdwpanalyzer.protocol.JdwpConstants
import com.karelherink.jdwpanalyzer.protocol.Tag
import java.util.concurrent.atomic.AtomicLong

class TypeInfo(val id: Long) {
    var typeTag: Int? = null
    var signature: String? = null
    var genericSignature: String? = null
    var status: Int? = null
    var sourceFile: String? = null
    var modifiers: Int? = null
    var superclass: Long? = null
    var interfaces: List<Long>? = null
    var nestedTypes: List<Long>? = null
    var classLoader: Long? = null
    var classObject: Long? = null
    var module: Long? = null
    var classFileVersion: String? = null
    var sourceDebugExtension: String? = null
    var methods: List<Long>? = null
    var fields: List<Long>? = null
}

class MethodInfo(val classId: Long, val id: Long) {
    var name: String? = null
    var signature: String? = null
    var genericSignature: String? = null
    var modifiers: Int? = null
    var lineTable: List<Pair<Long, Int>>? = null
    var codeRange: LongRange? = null
    var argCount: Int? = null
    var variables: List<Variable>? = null
    var bytecodeSize: Int? = null
    var obsolete: Boolean? = null
}

data class Variable(val codeIndex: Long, val name: String, val signature: String, val length: Int, val slot: Int)

class FieldInfo(val classId: Long, val id: Long) {
    var name: String? = null
    var signature: String? = null
    var genericSignature: String? = null
    var modifiers: Int? = null
}

class ObjectInfo(val id: Long) {
    var kind: ObjectKind = ObjectKind.OBJECT
    var type: Long? = null
    var name: String? = null
    var stringValue: String? = null
    var componentTag: Tag? = null
    var threadGroup: Long? = null
    var parent: Long? = null
    var reflectedType: Long? = null
    var classLoader: Long? = null
}

class FrameInfo(val id: Long, val thread: Long, val location: Location, val depth: Int)

class EventRequestInfo(val id: Int, val eventKind: Int, val suspendPolicy: Int, val modifiers: List<DecodedNode>)

/** One row of the entity inspector. [ref] makes the row navigable. */
data class InspectorRow(val label: String, val text: String, val ref: EntityRef? = null)

/**
 * Everything learnt about the debuggee from the traffic so far: ID sizes, types, members, objects, frames and
 * event requests. The decoder uses it for context-dependent decoding (untagged values), and the UI uses it to
 * turn IDs into names. All access is synchronized; [revision] changes whenever something new is learnt.
 */
class Registry {
    private val lock = Any()
    private val types = HashMap<Long, TypeInfo>()
    private val methods = HashMap<Pair<Long, Long>, MethodInfo>()
    private val fields = HashMap<Pair<Long, Long>, FieldInfo>()
    private val objects = HashMap<Long, ObjectInfo>()
    private val frames = HashMap<Long, FrameInfo>()
    private val eventRequests = HashMap<Int, EventRequestInfo>()

    private val _revision = AtomicLong()
    val revision: Long get() = _revision.get()

    @Volatile
    var idSizes: IdSizes = IdSizes()
        private set

    fun apply(facts: List<Registry.() -> Unit>) {
        if (facts.isEmpty()) return
        synchronized(lock) { facts.forEach { it(this) } }
        _revision.incrementAndGet()
    }

    // ---- learning (called from facts, under the lock) ---------------------------------------------------------

    fun idSizes(sizes: IdSizes) {
        idSizes = sizes
    }

    fun type(id: Long): TypeInfo = types.getOrPut(id) { TypeInfo(id) }

    fun method(classId: Long, id: Long): MethodInfo = methods.getOrPut(classId to id) { MethodInfo(classId, id) }

    fun field(classId: Long, id: Long): FieldInfo = fields.getOrPut(classId to id) { FieldInfo(classId, id) }

    fun obj(id: Long): ObjectInfo = objects.getOrPut(id) { ObjectInfo(id) }

    fun objectKind(id: Long, kind: ObjectKind) {
        val o = obj(id)
        if (kind != ObjectKind.OBJECT) o.kind = kind
    }

    fun frame(info: FrameInfo) {
        frames[info.id] = info
    }

    fun eventRequest(info: EventRequestInfo) {
        eventRequests[info.id] = info
    }

    // ---- lookups for the decoder ------------------------------------------------------------------------------

    /** Finds the JNI signature of a field, using the object's class when known, else any class that has the field. */
    fun fieldSignature(classId: Long?, fieldId: Long, objectId: Long? = null): String? = synchronized(lock) {
        val cls = classId ?: objectId?.let { objects[it]?.type }
        if (cls != null) {
            fields[cls to fieldId]?.signature?.let { return it }
            // Inherited fields are declared by a superclass.
            var sup = types[cls]?.superclass
            while (sup != null && sup != 0L) {
                fields[sup to fieldId]?.signature?.let { return it }
                sup = types[sup]?.superclass
            }
        }
        // Field IDs are only unique within a class. If every class with this ID agrees on the value's type,
        // the decoding is still unambiguous.
        val candidates = fields.values.filter { it.id == fieldId }.mapNotNull { it.signature }
        val tags = candidates.map { Tag.forSignature(it)?.let { t -> if (t.isObject) Tag.OBJECT else t } }.distinct()
        if (tags.size == 1) candidates.first() else null
    }

    fun arrayComponentTag(arrayId: Long): Tag? = synchronized(lock) {
        val o = objects[arrayId] ?: return null
        o.componentTag ?: o.type?.let { types[it]?.signature }?.takeIf { it.startsWith("[") }
            ?.let { Tag.forSignature(it.substring(1)) }
    }

    fun objectType(objectId: Long): Long? = synchronized(lock) { objects[objectId]?.type }

    /** The name of a local variable slot in a frame, when the frame's method has a known variable table. */
    fun slotName(frameId: Long, slot: Int): String? = synchronized(lock) {
        val f = frames[frameId] ?: return null
        methods[f.location.classId to f.location.methodId]?.variables
            ?.firstOrNull { it.slot == slot && f.location.index >= it.codeIndex && f.location.index < it.codeIndex + it.length }
            ?.let { "${it.name} : ${Signatures.simpleTypeName(it.signature)}" }
    }

    fun frameInfo(frameId: Long): FrameInfo? = synchronized(lock) { frames[frameId] }

    fun methodInfo(classId: Long, methodId: Long): MethodInfo? = synchronized(lock) { methods[classId to methodId] }

    fun typeSignature(id: Long): String? = synchronized(lock) { types[id]?.signature }

    // ---- descriptions for the UI ------------------------------------------------------------------------------

    /** A short human-readable name for [ref], or null when nothing is known about it yet. */
    fun describe(ref: EntityRef): String? = synchronized(lock) {
        when (ref) {
            is EntityRef.Type -> types[ref.id]?.signature?.let { Signatures.typeName(it) }
            is EntityRef.Method -> describeMethod(ref.classId, ref.methodId)
            is EntityRef.Field -> fields[ref.classId to ref.fieldId]?.let { f ->
                val owner = types[ref.classId]?.signature?.let { Signatures.simpleTypeName(it) }
                val name = f.name ?: return@let null
                val type = f.signature?.let { Signatures.simpleTypeName(it) }
                listOfNotNull(owner?.let { "$it.$name" } ?: name, type).joinToString(" : ")
            }
            is EntityRef.Obj -> describeObject(ref.id, ref.kind)
            is EntityRef.Frame -> frames[ref.id]?.let { f ->
                val thread = describeObject(f.thread, ObjectKind.THREAD) ?: "thread #${f.thread}"
                "frame ${f.depth} of $thread at ${describeLocation(f.location) ?: f.location.toString()}"
            }
            is EntityRef.EventRequest -> eventRequests[ref.id]?.let { describeEventRequest(it) }
        }
    }

    /** Like [describe] for a location, adding the source line when the method's line table is known. */
    fun describeLocation(location: Location): String? = synchronized(lock) {
        val method = describeMethod(location.classId, location.methodId) ?: return null
        val line = lineFor(location)
        if (line != null) "$method line $line" else "$method @${location.index}"
    }

    private fun lineFor(location: Location): Int? {
        val table = methods[location.classId to location.methodId]?.lineTable ?: return null
        return table.filter { it.first <= location.index }.maxByOrNull { it.first }?.second
    }

    private fun describeMethod(classId: Long, methodId: Long): String? {
        val m = methods[classId to methodId] ?: return null
        val name = m.name ?: return null
        val owner = types[classId]?.signature?.let { Signatures.simpleTypeName(it) }
        val params = m.signature?.let { Signatures.parameterList(it) } ?: "(…)"
        return (if (owner != null) "$owner.$name" else name) + params
    }

    private fun describeObject(id: Long, kindHint: ObjectKind): String? {
        val o = objects[id]
        val kind = if (o != null && o.kind != ObjectKind.OBJECT) o.kind else kindHint
        val typeName = o?.type?.let { types[it]?.signature }?.let { Signatures.typeName(it) }
        val stringValue = o?.stringValue
        val name = o?.name
        val reflected = o?.reflectedType
        return when {
            stringValue != null -> Decoder.quote(stringValue)
            name != null && kind == ObjectKind.MODULE -> "module ${name.ifEmpty { "<unnamed>" }}"
            name != null -> "${kind.label} \"$name\""
            reflected != null -> types[reflected]?.signature?.let { "class ${Signatures.typeName(it)}" }
            typeName != null -> "instance of $typeName"
            else -> null
        }
    }

    private fun describeEventRequest(r: EventRequestInfo): String {
        val parts = r.modifiers.mapNotNull { m ->
            when (m.text) {
                "ClassMatch", "ClassExclude", "SourceNameMatch" ->
                    m.children.drop(1).firstOrNull()?.let { "${m.text} ${it.text}" }
                "LocationOnly" -> (m.children.getOrNull(1)?.value as? Location)?.let { "at " + (describeLocation(it) ?: it.toString()) }
                "ClassOnly" -> (m.children.getOrNull(1)?.value as? Long)?.let { id ->
                    "in " + (types[id]?.signature?.let { Signatures.typeName(it) } ?: "#$id")
                }
                "Count" -> m.children.getOrNull(1)?.text?.let { "count $it" }
                else -> m.text
            }
        }
        return (listOf(JdwpConstants.eventKind(r.eventKind)) + parts).joinToString(" · ")
    }

    /** Everything known about [ref], for the inspector panel. */
    fun details(ref: EntityRef): List<InspectorRow> = synchronized(lock) {
        when (ref) {
            is EntityRef.Type -> typeDetails(ref.id)
            is EntityRef.Method -> methodDetails(ref.classId, ref.methodId)
            is EntityRef.Field -> fieldDetails(ref.classId, ref.fieldId)
            is EntityRef.Obj -> objectDetails(ref.id, ref.kind)
            is EntityRef.Frame -> frames[ref.id]?.let { f ->
                listOf(
                    InspectorRow("Frame ID", "#${f.id}"),
                    InspectorRow("Depth", f.depth.toString()),
                    InspectorRow("Thread", describeObject(f.thread, ObjectKind.THREAD) ?: "#${f.thread}", EntityRef.Obj(f.thread, ObjectKind.THREAD)),
                    InspectorRow("Location", describeLocation(f.location) ?: f.location.toString(), EntityRef.Method(f.location.classId, f.location.methodId)),
                ) + (methods[f.location.classId to f.location.methodId]?.variables.orEmpty()
                    .filter { f.location.index >= it.codeIndex && f.location.index < it.codeIndex + it.length }
                    .map { InspectorRow("Slot ${it.slot}", "${it.name} : ${Signatures.simpleTypeName(it.signature)}") })
            } ?: listOf(InspectorRow("Frame ID", "#${ref.id}"), InspectorRow("", "Not seen in a ThreadReference.Frames reply yet"))
            is EntityRef.EventRequest -> eventRequests[ref.id]?.let { r ->
                listOf(
                    InspectorRow("Request ID", r.id.toString()),
                    InspectorRow("Event kind", JdwpConstants.eventKind(r.eventKind)),
                    InspectorRow("Suspend policy", JdwpConstants.suspendPolicies[r.suspendPolicy] ?: r.suspendPolicy.toString()),
                ) + r.modifiers.map { m ->
                    InspectorRow(m.text ?: m.name, m.children.drop(1).joinToString(", ") { c -> "${c.name}=${c.text ?: ""}" }, m.children.drop(1).firstNotNullOfOrNull { it.ref })
                }
            } ?: listOf(InspectorRow("Request ID", ref.id.toString()), InspectorRow("", "The EventRequest.Set for this ID wasn't seen"))
        }
    }

    private fun typeDetails(id: Long): List<InspectorRow> {
        val t = types[id] ?: return listOf(InspectorRow("Reference type", "#$id"), InspectorRow("", "Nothing known yet"))
        val rows = mutableListOf(InspectorRow("Reference type", "#$id"))
        t.signature?.let { rows += InspectorRow("Name", Signatures.typeName(it)); rows += InspectorRow("Signature", it) }
        t.genericSignature?.takeIf { it.isNotEmpty() }?.let { rows += InspectorRow("Generic signature", it) }
        t.typeTag?.let { rows += InspectorRow("Kind", JdwpConstants.typeTag(it)) }
        t.status?.let { rows += InspectorRow("Status", JdwpConstants.classStatus(it)) }
        t.modifiers?.let { rows += InspectorRow("Modifiers", JdwpConstants.modifiers(it)) }
        t.sourceFile?.let { rows += InspectorRow("Source file", it) }
        t.classFileVersion?.let { rows += InspectorRow("Class file version", it) }
        t.superclass?.let { rows += InspectorRow("Superclass", typeLabel(it), if (it != 0L) EntityRef.Type(it) else null) }
        t.interfaces?.forEach { rows += InspectorRow("Interface", typeLabel(it), EntityRef.Type(it)) }
        t.nestedTypes?.forEach { rows += InspectorRow("Nested type", typeLabel(it), EntityRef.Type(it)) }
        t.classLoader?.let { rows += InspectorRow("Class loader", if (it == 0L) "bootstrap" else "#$it", if (it != 0L) EntityRef.Obj(it, ObjectKind.CLASS_LOADER) else null) }
        t.classObject?.let { rows += InspectorRow("Class object", "#$it", EntityRef.Obj(it, ObjectKind.CLASS_OBJECT)) }
        t.module?.let { rows += InspectorRow("Module", describeObject(it, ObjectKind.MODULE) ?: "#$it", EntityRef.Obj(it, ObjectKind.MODULE)) }
        t.sourceDebugExtension?.let { rows += InspectorRow("Source debug extension", it) }
        t.fields?.forEach { f -> rows += InspectorRow("Field", describe(EntityRef.Field(id, f)) ?: "#$f", EntityRef.Field(id, f)) }
        t.methods?.forEach { m -> rows += InspectorRow("Method", describeMethod(id, m) ?: "#$m", EntityRef.Method(id, m)) }
        return rows
    }

    private fun typeLabel(id: Long): String =
        if (id == 0L) "none" else types[id]?.signature?.let { Signatures.typeName(it) } ?: "#$id"

    private fun methodDetails(classId: Long, id: Long): List<InspectorRow> {
        val rows = mutableListOf(
            InspectorRow("Method ID", "#$id"),
            InspectorRow("Declared by", typeLabel(classId), EntityRef.Type(classId)),
        )
        val m = methods[classId to id] ?: return rows + InspectorRow("", "Nothing known yet")
        m.name?.let { rows += InspectorRow("Name", it) }
        m.signature?.let { rows += InspectorRow("Signature", it) }
        m.genericSignature?.takeIf { it.isNotEmpty() }?.let { rows += InspectorRow("Generic signature", it) }
        m.modifiers?.let { rows += InspectorRow("Modifiers", JdwpConstants.modifiers(it)) }
        m.obsolete?.let { rows += InspectorRow("Obsolete", it.toString()) }
        m.bytecodeSize?.let { rows += InspectorRow("Bytecode size", "$it bytes") }
        m.codeRange?.let { rows += InspectorRow("Code index range", "${it.first}..${it.last}") }
        m.lineTable?.let { lines -> rows += InspectorRow("Line table", lines.joinToString(", ") { "${it.first}→${it.second}" }) }
        m.argCount?.let { rows += InspectorRow("Argument slots", it.toString()) }
        m.variables?.forEach { v ->
            rows += InspectorRow("Slot ${v.slot}", "${v.name} : ${Signatures.simpleTypeName(v.signature)} (code ${v.codeIndex}+${v.length})")
        }
        return rows
    }

    private fun fieldDetails(classId: Long, id: Long): List<InspectorRow> {
        val rows = mutableListOf(
            InspectorRow("Field ID", "#$id"),
            InspectorRow("Declared by", typeLabel(classId), EntityRef.Type(classId)),
        )
        val f = fields[classId to id] ?: return rows + InspectorRow("", "Nothing known yet")
        f.name?.let { rows += InspectorRow("Name", it) }
        f.signature?.let { rows += InspectorRow("Type", Signatures.typeName(it)); rows += InspectorRow("Signature", it) }
        f.genericSignature?.takeIf { it.isNotEmpty() }?.let { rows += InspectorRow("Generic signature", it) }
        f.modifiers?.let { rows += InspectorRow("Modifiers", JdwpConstants.modifiers(it)) }
        return rows
    }

    private fun objectDetails(id: Long, kindHint: ObjectKind): List<InspectorRow> {
        val o = objects[id]
        val kind = if (o != null && o.kind != ObjectKind.OBJECT) o.kind else kindHint
        val rows = mutableListOf(InspectorRow("Object ID", "#$id"), InspectorRow("Kind", kind.label))
        if (o == null) return rows + InspectorRow("", "Nothing else known yet")
        o.type?.let { rows += InspectorRow("Type", typeLabel(it), EntityRef.Type(it)) }
        o.name?.let { rows += InspectorRow("Name", it) }
        o.stringValue?.let { rows += InspectorRow("Value", "\"$it\"") }
        o.componentTag?.let { rows += InspectorRow("Component type", it.description) }
        o.threadGroup?.let { rows += InspectorRow("Thread group", describeObject(it, ObjectKind.THREAD_GROUP) ?: "#$it", EntityRef.Obj(it, ObjectKind.THREAD_GROUP)) }
        o.parent?.let { rows += InspectorRow("Parent group", if (it == 0L) "none" else describeObject(it, ObjectKind.THREAD_GROUP) ?: "#$it", if (it != 0L) EntityRef.Obj(it, ObjectKind.THREAD_GROUP) else null) }
        o.reflectedType?.let { rows += InspectorRow("Reflected type", typeLabel(it), EntityRef.Type(it)) }
        o.classLoader?.let { rows += InspectorRow("Class loader", if (it == 0L) "bootstrap" else "#$it", if (it != 0L) EntityRef.Obj(it, ObjectKind.CLASS_LOADER) else null) }
        if (kind == ObjectKind.THREAD) {
            frames.values.filter { it.thread == id }.sortedBy { it.depth }.forEach { f ->
                rows += InspectorRow("Frame ${f.depth}", describeLocation(f.location) ?: f.location.toString(), EntityRef.Frame(f.id))
            }
        }
        return rows
    }
}
