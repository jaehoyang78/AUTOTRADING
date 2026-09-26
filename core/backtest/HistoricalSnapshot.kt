package autotrading.core.backtest

import autotrading.core.HybridStrategyEngine
import autotrading.data.Market
import autotrading.data.MarketIndex
import java.time.Instant
import java.time.LocalDate

/**
 * Point-in-time backtest schema.
 * `availableAt` means the earliest instant the strategy was allowed to know the value.
 * A backtest snapshot is invalid if any input has availableAt > asOf.
 */
data class HistoricalSnapshot(
    val snapshotId: String,
    val asOf: Instant,
    val market: Market,
    val benchmark: MarketIndex,
    val marketInputs: HistoricalMarketInputs,
    val stocks: List<HistoricalStockSnapshot>,
    val createdAt: Instant,
    val schemaVersion: Int = 1
)

data class HistoricalMarketInputs(
    val inputs: HybridStrategyEngine.MarketInputs,
    val availability: Map<String, PointInTimeMeta>
)

data class HistoricalStockSnapshot(
    val symbol: String,
    val inputs: HybridStrategyEngine.StockInputs,
    val availability: Map<String, PointInTimeMeta>,
    val universeMembership: UniverseMembership,
    val sector: String? = null
)

data class PointInTimeMeta(
    val effectiveDate: LocalDate?,
    val availableAt: Instant,
    val source: String,
    val revisionId: String? = null
)

data class UniverseMembership(
    val member: Boolean,
    val availableAt: Instant,
    val source: String
)

data class SnapshotValidation(
    val valid: Boolean,
    val errors: List<String>
)

object HistoricalSnapshotValidator {
    fun validate(snapshot: HistoricalSnapshot): SnapshotValidation {
        val errors = mutableListOf<String>()
        if (snapshot.snapshotId.isBlank()) errors += "snapshotId is blank"
        if (snapshot.createdAt.isBefore(snapshot.asOf)) {
            // Allowed: a snapshot can be materialized later from point-in-time archives.
        }

        snapshot.marketInputs.availability.forEach { (field, meta) ->
            validateMeta("market.$field", meta, snapshot.asOf, errors)
        }

        val duplicateSymbols = snapshot.stocks.groupingBy { it.symbol }.eachCount().filterValues { it > 1 }.keys
        if (duplicateSymbols.isNotEmpty()) errors += "duplicate stock symbols: ${duplicateSymbols.sorted().joinToString()}"

        snapshot.stocks.forEach { stock ->
            if (stock.symbol.isBlank()) errors += "stock symbol is blank"
            if (stock.universeMembership.availableAt.isAfter(snapshot.asOf)) {
                errors += "${stock.symbol}.universeMembership available after asOf"
            }
            stock.availability.forEach { (field, meta) ->
                validateMeta("${stock.symbol}.$field", meta, snapshot.asOf, errors)
            }
        }

        return SnapshotValidation(errors.isEmpty(), errors)
    }

    private fun validateMeta(
        field: String,
        meta: PointInTimeMeta,
        asOf: Instant,
        errors: MutableList<String>
    ) {
        if (meta.source.isBlank()) errors += "$field source is blank"
        if (meta.availableAt.isAfter(asOf)) errors += "$field available after asOf"
    }
}
