package com.karelherink.jdwpanalyzer.ui

import com.karelherink.jdwpanalyzer.decode.DecodedNode
import com.karelherink.jdwpanalyzer.decode.Location
import com.karelherink.jdwpanalyzer.model.Exchange
import com.karelherink.jdwpanalyzer.model.Registry

/** What a node refers to, in words: a class name, a method, a thread name, a source line... */
fun describeNode(node: DecodedNode, registry: Registry): String? {
    (node.value as? Location)?.let { return registry.describeLocation(it) }
    return node.ref?.let { registry.describe(it) }
}

private fun leaves(node: DecodedNode): Sequence<DecodedNode> =
    node.walk().drop(1).filter { it.children.isEmpty() && it.text != null && it.name != "count" }

private fun brief(node: DecodedNode, registry: Registry): String =
    describeNode(node, registry) ?: node.text ?: ""

/** One-line summary of an exchange for the packet table: its main argument, and its result if it is small. */
fun summarize(exchange: Exchange, registry: Registry): String {
    val command = exchange.decodedCommand?.root
    if (exchange.isEvent && command != null) {
        return command.child("events")?.children?.drop(1)?.joinToString("; ") { event ->
            val where = event.children.firstOrNull { it.value is Location }?.let { describeNode(it, registry) }
                ?: event.children.firstOrNull { it.name == "signature" }?.text
                ?: event.children.firstOrNull { it.name == "thread" }?.let { brief(it, registry) }
            listOfNotNull(event.text, where).joinToString(" ")
        }.orEmpty()
    }
    val args = command?.let { root ->
        val described = root.walk().drop(1).firstOrNull { it.ref != null || it.value is Location }
        (described ?: leaves(root).firstOrNull())?.let { brief(it, registry) }
    }
    val reply = exchange.decodedReply?.root?.takeIf { exchange.errorCode == 0 }?.let { root ->
        val all = leaves(root).take(4).toList()
        if (all.size in 1..3) all.joinToString(", ") { brief(it, registry) }
        else root.children.firstOrNull { it.name != "count" }?.let { first ->
            if (first.children.isNotEmpty()) "${first.name}: ${first.text ?: first.children.size}" else brief(first, registry)
        }
    }
    return listOfNotNull(args?.takeIf { it.isNotEmpty() }, reply?.takeIf { it.isNotEmpty() }?.let { "→ $it" }).joinToString("  ")
}
