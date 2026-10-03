package com.karelherink.jdwpanalyzer.protocol

/**
 * JDWP constant sets, as defined by the JDK 26 JDWP specification
 * (https://docs.oracle.com/en/java/javase/26/docs/specs/jdwp/jdwp-protocol.html).
 */
object JdwpConstants {

    val errors: Map<Int, String> = mapOf(
        0 to "NONE",
        10 to "INVALID_THREAD",
        11 to "INVALID_THREAD_GROUP",
        12 to "INVALID_PRIORITY",
        13 to "THREAD_NOT_SUSPENDED",
        14 to "THREAD_SUSPENDED",
        15 to "THREAD_NOT_ALIVE",
        20 to "INVALID_OBJECT",
        21 to "INVALID_CLASS",
        22 to "CLASS_NOT_PREPARED",
        23 to "INVALID_METHODID",
        24 to "INVALID_LOCATION",
        25 to "INVALID_FIELDID",
        30 to "INVALID_FRAMEID",
        31 to "NO_MORE_FRAMES",
        32 to "OPAQUE_FRAME",
        33 to "NOT_CURRENT_FRAME",
        34 to "TYPE_MISMATCH",
        35 to "INVALID_SLOT",
        40 to "DUPLICATE",
        41 to "NOT_FOUND",
        42 to "INVALID_MODULE",
        50 to "INVALID_MONITOR",
        51 to "NOT_MONITOR_OWNER",
        52 to "INTERRUPT",
        60 to "INVALID_CLASS_FORMAT",
        61 to "CIRCULAR_CLASS_DEFINITION",
        62 to "FAILS_VERIFICATION",
        63 to "ADD_METHOD_NOT_IMPLEMENTED",
        64 to "SCHEMA_CHANGE_NOT_IMPLEMENTED",
        65 to "INVALID_TYPESTATE",
        66 to "HIERARCHY_CHANGE_NOT_IMPLEMENTED",
        67 to "DELETE_METHOD_NOT_IMPLEMENTED",
        68 to "UNSUPPORTED_VERSION",
        69 to "NAMES_DONT_MATCH",
        70 to "CLASS_MODIFIERS_CHANGE_NOT_IMPLEMENTED",
        71 to "METHOD_MODIFIERS_CHANGE_NOT_IMPLEMENTED",
        72 to "CLASS_ATTRIBUTE_CHANGE_NOT_IMPLEMENTED",
        99 to "NOT_IMPLEMENTED",
        100 to "NULL_POINTER",
        101 to "ABSENT_INFORMATION",
        102 to "INVALID_EVENT_TYPE",
        103 to "ILLEGAL_ARGUMENT",
        110 to "OUT_OF_MEMORY",
        111 to "ACCESS_DENIED",
        112 to "VM_DEAD",
        113 to "INTERNAL",
        115 to "UNATTACHED_THREAD",
        500 to "INVALID_TAG",
        502 to "ALREADY_INVOKING",
        503 to "INVALID_INDEX",
        504 to "INVALID_LENGTH",
        506 to "INVALID_STRING",
        507 to "INVALID_CLASS_LOADER",
        508 to "INVALID_ARRAY",
        509 to "TRANSPORT_LOAD",
        510 to "TRANSPORT_INIT",
        511 to "NATIVE_METHOD",
        512 to "INVALID_COUNT",
    )

    val eventKinds: Map<Int, String> = mapOf(
        1 to "SINGLE_STEP",
        2 to "BREAKPOINT",
        3 to "FRAME_POP",
        4 to "EXCEPTION",
        5 to "USER_DEFINED",
        6 to "THREAD_START",
        7 to "THREAD_DEATH",
        8 to "CLASS_PREPARE",
        9 to "CLASS_UNLOAD",
        10 to "CLASS_LOAD",
        20 to "FIELD_ACCESS",
        21 to "FIELD_MODIFICATION",
        30 to "EXCEPTION_CATCH",
        40 to "METHOD_ENTRY",
        41 to "METHOD_EXIT",
        42 to "METHOD_EXIT_WITH_RETURN_VALUE",
        43 to "MONITOR_CONTENDED_ENTER",
        44 to "MONITOR_CONTENDED_ENTERED",
        45 to "MONITOR_WAIT",
        46 to "MONITOR_WAITED",
        90 to "VM_START",
        99 to "VM_DEATH",
        100 to "VM_DISCONNECTED",
    )

    val threadStatus: Map<Int, String> = mapOf(
        0 to "ZOMBIE",
        1 to "RUNNING",
        2 to "SLEEPING",
        3 to "MONITOR",
        4 to "WAIT",
    )

    val typeTags: Map<Int, String> = mapOf(
        1 to "CLASS",
        2 to "INTERFACE",
        3 to "ARRAY",
    )

    val suspendPolicies: Map<Int, String> = mapOf(
        0 to "NONE",
        1 to "EVENT_THREAD",
        2 to "ALL",
    )

    val stepDepths: Map<Int, String> = mapOf(
        0 to "INTO",
        1 to "OVER",
        2 to "OUT",
    )

    val stepSizes: Map<Int, String> = mapOf(
        0 to "MIN",
        1 to "LINE",
    )

    val modifierKinds: Map<Int, String> = mapOf(
        1 to "Count",
        2 to "Conditional",
        3 to "ThreadOnly",
        4 to "ClassOnly",
        5 to "ClassMatch",
        6 to "ClassExclude",
        7 to "LocationOnly",
        8 to "ExceptionOnly",
        9 to "FieldOnly",
        10 to "Step",
        11 to "InstanceOnly",
        12 to "SourceNameMatch",
        13 to "PlatformThreadsOnly",
    )

    fun error(code: Int): String = errors[code] ?: "UNKNOWN_ERROR_$code"

    fun eventKind(kind: Int): String = eventKinds[kind] ?: "UNKNOWN_EVENT_$kind"

    fun typeTag(tag: Int): String = typeTags[tag] ?: "UNKNOWN_TYPE_TAG_$tag"

    fun classStatus(status: Int): String = flags(
        status,
        listOf(1 to "VERIFIED", 2 to "PREPARED", 4 to "INITIALIZED", 8 to "ERROR"),
    )

    fun suspendStatus(status: Int): String = if (status and 1 != 0) "SUSPENDED" else "NOT_SUSPENDED"

    fun invokeOptions(options: Int): String = flags(
        options,
        listOf(1 to "INVOKE_SINGLE_THREADED", 2 to "INVOKE_NONVIRTUAL"),
    )

    /** Java access flags, as returned by ReferenceType.Modifiers, Fields and Methods. */
    fun modifiers(bits: Int): String = flags(
        bits,
        listOf(
            0x0001 to "public",
            0x0002 to "private",
            0x0004 to "protected",
            0x0008 to "static",
            0x0010 to "final",
            0x0020 to "synchronized/super",
            0x0040 to "volatile/bridge",
            0x0080 to "transient/varargs",
            0x0100 to "native",
            0x0200 to "interface",
            0x0400 to "abstract",
            0x0800 to "strict",
            0x1000 to "synthetic",
            0x2000 to "annotation",
            0x4000 to "enum",
            0x8000 to "mandated/module",
            0xf0000000.toInt() to "VM-synthetic",
        ),
    )

    private fun flags(value: Int, names: List<Pair<Int, String>>): String {
        if (value == 0) return "none"
        val set = names.filter { (bit, _) -> value and bit != 0 }.map { it.second }
        val known = names.fold(0) { acc, (bit, _) -> acc or bit }
        val unknown = value and known.inv()
        return (set + if (unknown != 0) listOf("0x" + unknown.toString(16)) else emptyList()).joinToString(" | ")
    }
}

/** A value tag: the first byte of a tagged value or tagged object ID. */
enum class Tag(val code: Char, val description: String, val isObject: Boolean) {
    ARRAY('[', "array", true),
    BYTE('B', "byte", false),
    CHAR('C', "char", false),
    OBJECT('L', "object", true),
    FLOAT('F', "float", false),
    DOUBLE('D', "double", false),
    INT('I', "int", false),
    LONG('J', "long", false),
    SHORT('S', "short", false),
    VOID('V', "void", false),
    BOOLEAN('Z', "boolean", false),
    STRING('s', "string", true),
    THREAD('t', "thread", true),
    THREAD_GROUP('g', "thread group", true),
    CLASS_LOADER('l', "class loader", true),
    CLASS_OBJECT('c', "class object", true),
    ;

    companion object {
        private val byCode = entries.associateBy { it.code.code }

        fun of(code: Int): Tag? = byCode[code and 0xff]

        /** The tag for a JNI type signature's first character, e.g. `I` or `Ljava/lang/String;`. */
        fun forSignature(signature: String): Tag? = signature.firstOrNull()?.let { byCode[it.code] }
    }
}
