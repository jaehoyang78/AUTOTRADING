package autotrading.core.backtest

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Immutable storage boundary for deterministic backtest replay. */
data class SnapshotEnvelope(
    val snapshot: HistoricalSnapshot,
    val sha256: String
)

interface SnapshotStore {
    fun append(snapshot: HistoricalSnapshot): SnapshotEnvelope
    fun get(snapshotId: String): SnapshotEnvelope?
    fun all(): List<SnapshotEnvelope>
}

class InMemoryAppendOnlySnapshotStore : SnapshotStore {
    private val records = linkedMapOf<String, SnapshotEnvelope>()

    override fun append(snapshot: HistoricalSnapshot): SnapshotEnvelope {
        val validation = HistoricalSnapshotValidator.validate(snapshot)
        require(validation.valid) { "invalid snapshot: ${validation.errors.joinToString("; ")}" }
        require(snapshot.snapshotId !in records) { "snapshotId already exists: ${snapshot.snapshotId}" }
        val envelope = SnapshotEnvelope(snapshot, HistoricalSnapshotHasher.sha256(snapshot))
        records[snapshot.snapshotId] = envelope
        return envelope
    }

    override fun get(snapshotId: String): SnapshotEnvelope? = records[snapshotId]

    override fun all(): List<SnapshotEnvelope> = records.values.toList()
}

object HistoricalSnapshotHasher {
    fun sha256(snapshot: HistoricalSnapshot): String {
        val bytes = canonical(snapshot).toByteArray(StandardCharsets.UTF_8)
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }

    private fun canonical(s: HistoricalSnapshot): String = buildString {
        append("schema=").append(s.schemaVersion)
        append("|id=").append(s.snapshotId)
        append("|asOf=").append(s.asOf)
        append("|market=").append(s.market)
        append("|benchmark=").append(s.benchmark)
        append("|marketInputs=").append(s.marketInputs.inputs)
        s.marketInputs.availability.toSortedMap().forEach { (k, v) ->
            append("|m:").append(k).append('=').append(meta(v))
        }
        s.stocks.sortedBy { it.symbol }.forEach { stock ->
            append("|stock=").append(stock.symbol)
            append("|sector=").append(stock.sector ?: "")
            append("|inputs=").append(stock.inputs)
            append("|member=").append(stock.universeMembership.member)
            append('@').append(stock.universeMembership.availableAt)
            append('@').append(stock.universeMembership.source)
            stock.availability.toSortedMap().forEach { (k, v) ->
                append("|s:").append(stock.symbol).append(':').append(k).append('=').append(meta(v))
            }
        }
    }

    private fun meta(m: PointInTimeMeta): String =
        listOf(m.effectiveDate?.toString().orEmpty(), m.availableAt.toString(), m.source, m.revisionId.orEmpty())
            .joinToString("@")
}
