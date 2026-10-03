package com.karelherink.jdwpanalyzer.model

/** Turns JNI type and method signatures into Java-style names. */
object Signatures {

    /** `Ljava/util/List;` → `java.util.List`, `[I` → `int[]`. */
    fun typeName(signature: String): String = parse(signature, 0, simple = false).first

    /** `Ljava/util/Map$Entry;` → `Map$Entry`, `[Ljava/lang/String;` → `String[]`. */
    fun simpleTypeName(signature: String): String = parse(signature, 0, simple = true).first

    /** `(ILjava/lang/String;)V` → `(int, String)`. */
    fun parameterList(methodSignature: String): String {
        if (!methodSignature.startsWith("(")) return "(…)"
        val params = mutableListOf<String>()
        var i = 1
        while (i < methodSignature.length && methodSignature[i] != ')') {
            val (name, next) = parse(methodSignature, i, simple = true)
            params += name
            if (next <= i) break
            i = next
        }
        return params.joinToString(", ", "(", ")")
    }

    private fun parse(sig: String, start: Int, simple: Boolean): Pair<String, Int> {
        if (start >= sig.length) return "?" to start + 1
        return when (val c = sig[start]) {
            'B' -> "byte" to start + 1
            'C' -> "char" to start + 1
            'D' -> "double" to start + 1
            'F' -> "float" to start + 1
            'I' -> "int" to start + 1
            'J' -> "long" to start + 1
            'S' -> "short" to start + 1
            'Z' -> "boolean" to start + 1
            'V' -> "void" to start + 1
            '[' -> parse(sig, start + 1, simple).let { (n, next) -> "$n[]" to next }
            'L', 'Q' -> {
                val end = sig.indexOf(';', start).let { if (it < 0) sig.length else it }
                var name = sig.substring(start + 1, end)
                val generic = name.indexOf('<')
                if (generic >= 0) name = name.substring(0, generic)
                name = name.replace('/', '.')
                if (simple) name = name.substringAfterLast('.')
                name to end + 1
            }
            else -> c.toString() to start + 1
        }
    }
}
