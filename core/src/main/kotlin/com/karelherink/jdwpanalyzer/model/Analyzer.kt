package com.karelherink.jdwpanalyzer.model

import com.karelherink.jdwpanalyzer.decode.CommandSpec
import com.karelherink.jdwpanalyzer.decode.DecodedNode
import com.karelherink.jdwpanalyzer.decode.DecodedPacket
import com.karelherink.jdwpanalyzer.decode.Decoder
import com.karelherink.jdwpanalyzer.decode.JdwpSpec
import com.karelherink.jdwpanalyzer.decode.UndecodableException
import com.karelherink.jdwpanalyzer.protocol.Direction
import com.karelherink.jdwpanalyzer.protocol.JdwpConstants
import com.karelherink.jdwpanalyzer.protocol.JdwpPacket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A packet as captured: its position in the stream, which way it went, and when. */
class CapturedPacket(
    val sequence: Int,
    val direction: Direction,
    val packet: JdwpPacket,
    val timeMillis: Long,
)

/**
 * A command and its reply. Events (`Event.Composite`, sent by the VM) normally have no reply.
 * A reply whose command was never seen (e.g. a log that starts mid-session) has a null [command].
 */
data class Exchange(
    val index: Int,
    val commandSet: Int,
    val commandId: Int,
    val spec: CommandSpec?,
    val command: CapturedPacket?,
    val decodedCommand: DecodedPacket?,
    val reply: CapturedPacket? = null,
    val decodedReply: DecodedPacket? = null,
) {
    val name: String get() = spec?.fullName ?: if (command == null) "Unmatched reply" else JdwpSpec.nameOf(commandSet, commandId)
    val isEvent: Boolean get() = commandSet == 64
    val packetId: Int get() = (command ?: reply)!!.packet.id
    val direction: Direction get() = command?.direction ?: reply!!.direction.opposite
    val errorCode: Int get() = reply?.packet?.errorCode ?: 0
    val isError: Boolean get() = errorCode != 0
    val isPending: Boolean get() = reply == null && !isEvent
    val hasProblem: Boolean get() = decodedCommand?.problem != null || decodedReply?.problem != null
    val latencyMillis: Long? get() = if (command != null && reply != null) reply.timeMillis - command.timeMillis else null
    val firstTime: Long get() = (command ?: reply)!!.timeMillis

    /** For events, the kinds of events in the composite, e.g. "BREAKPOINT, METHOD_ENTRY". */
    val eventKinds: String?
        get() = if (!isEvent) null else decodedCommand?.root?.child("events")?.children?.drop(1)?.mapNotNull { it.text }?.joinToString(", ")
}

/**
 * Pairs commands with replies, decodes both against [JdwpSpec], and feeds what it learns into [registry].
 * Safe to call from the proxy's two pump threads; [version] ticks on every change for the UI to observe.
 */
class Analyzer(val registry: Registry = Registry()) {
    private val lock = Any()
    private val exchanges = ArrayList<Exchange>()
    private val packets = ArrayList<CapturedPacket>()
    private val pending = HashMap<Pair<Direction, Int>, Int>()

    private val _version = MutableStateFlow(0L)
    val version: StateFlow<Long> = _version.asStateFlow()

    fun exchanges(): List<Exchange> = synchronized(lock) { exchanges.toList() }

    /** All packets in arrival order, e.g. for saving a log. */
    fun packets(): List<CapturedPacket> = synchronized(lock) { packets.toList() }

    /**
     * Records one packet. [direction] is null when replaying a log that doesn't record it; it is then inferred:
     * events come from the VM, other commands from the debugger, and replies go back the other way.
     */
    fun onPacket(packet: JdwpPacket, direction: Direction?, timeMillis: Long = System.currentTimeMillis()) {
        synchronized(lock) {
            if (packet.isReply) onReply(packet, direction, timeMillis) else onCommand(packet, direction, timeMillis)
        }
        _version.value++
    }

    private fun onCommand(packet: JdwpPacket, direction: Direction?, time: Long) {
        val dir = direction ?: if (packet.commandSet == 64) Direction.VM_TO_DEBUGGER else Direction.DEBUGGER_TO_VM
        val captured = CapturedPacket(packets.size, dir, packet, time).also { packets += it }
        val spec = JdwpSpec.find(packet.commandSet, packet.command)
        val decoded = decode(spec?.out, packet.data(), null, spec == null)
        val exchange = Exchange(exchanges.size, packet.commandSet, packet.command, spec, captured, decoded)
        exchanges += exchange
        pending[dir to packet.id] = exchange.index
    }

    private fun onReply(packet: JdwpPacket, direction: Direction?, time: Long) {
        val commandDirection = when (direction) {
            null -> listOf(Direction.DEBUGGER_TO_VM, Direction.VM_TO_DEBUGGER).firstOrNull { pending.containsKey(it to packet.id) }
                ?: Direction.DEBUGGER_TO_VM
            else -> direction.opposite
        }
        val captured = CapturedPacket(packets.size, commandDirection.opposite, packet, time).also { packets += it }
        val index = pending.remove(commandDirection to packet.id)
        if (index == null) {
            val decoded = decode(null, packet.data(), null, unknown = true)
            exchanges += Exchange(exchanges.size, 0, 0, null, null, null, captured, decoded)
            return
        }
        val exchange = exchanges[index]
        val decoded = if (packet.errorCode != 0) {
            errorReply(packet)
        } else {
            decode(exchange.spec?.reply, packet.data(), exchange.decodedCommand?.root, exchange.spec == null)
        }
        exchanges[index] = exchange.copy(reply = captured, decodedReply = decoded)
    }

    private fun errorReply(packet: JdwpPacket): DecodedPacket {
        val d = Decoder(packet.data(), registry)
        d.note("error", "${JdwpConstants.error(packet.errorCode)} (${packet.errorCode})")
        return d.finish(null)
    }

    private fun decode(body: (Decoder.() -> Unit)?, data: ByteArray, command: DecodedNode?, unknown: Boolean): DecodedPacket {
        val d = Decoder(data, registry, command)
        val problem = when {
            unknown -> "Unknown command; data shown as raw bytes".takeIf { data.isNotEmpty() }
            else -> try {
                body?.invoke(d)
                null
            } catch (e: UndecodableException) {
                e.message
            } catch (e: Exception) {
                "Decoder failure: $e"
            }
        }
        val result = d.finish(problem)
        registry.apply(d.facts)
        return result
    }
}
