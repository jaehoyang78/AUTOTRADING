package autotrading.core.data

import java.time.Instant
import java.time.LocalDate

/** Provider-neutral market data contracts. External APIs must map into these models. */
data class Quote(
    val symbol: String,
    val price: Double,
    val volume: Long?,
    val observedAt: Instant,
    val source: String
)

data class DailyBar(
    val symbol: String,
    val date: LocalDate,
    val open: Double?,
    val high: Double?,
    val low: Double?,
    val close: Double,
    val volume: Long?,
    val source: String
)

data class IndexSnapshot(
    val symbol: String,
    val value: Double,
    val observedAt: Instant,
    val source: String
)

data class VolatilitySnapshot(
    val symbol: String,
    val value: Double,
    val observedAt: Instant,
    val source: String
)

interface MarketDataProvider {
    val providerId: String

    fun quote(symbol: String): Quote

    /** Oldest-to-newest daily bars. */
    fun dailyBars(symbol: String, from: LocalDate, to: LocalDate): List<DailyBar>

    fun indexSnapshot(symbol: String): IndexSnapshot

    /** Examples: VIX, VKOSPI. */
    fun volatilitySnapshot(symbol: String): VolatilitySnapshot
}
