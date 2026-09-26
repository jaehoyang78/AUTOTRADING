package autotrading.data.kis

import autotrading.data.MarketIndex
import autotrading.data.VolatilityIndex
import java.time.Instant
import java.time.LocalDate

/**
 * Low-level KIS contract. Live HTTP/OAuth details stay behind this interface.
 * Tests can replace it with an in-memory fake without any credentials.
 */
interface KisGateway {
    val providerName: String get() = "KIS"

    fun quote(symbol: String, market: KisMarket): KisQuoteDto?
    fun dailyBars(symbol: String, market: KisMarket, limit: Int): List<KisDailyBarDto>
    fun indexBars(index: MarketIndex, limit: Int): List<KisIndexBarDto>
    fun volatility(index: VolatilityIndex, limit: Int): List<KisVolatilityDto>
}

enum class KisMarket { KR, US }

data class KisQuoteDto(
    val symbol: String,
    val price: Double,
    val volume: Long?,
    val changePercent: Double?,
    val observedAt: Instant
)

data class KisDailyBarDto(
    val date: LocalDate,
    val open: Double?,
    val high: Double?,
    val low: Double?,
    val close: Double,
    val volume: Long?,
    val observedAt: Instant
)

data class KisIndexBarDto(
    val date: LocalDate,
    val close: Double,
    val observedAt: Instant
)

data class KisVolatilityDto(
    val date: LocalDate,
    val close: Double,
    val observedAt: Instant
)
