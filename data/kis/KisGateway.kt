package autotrading.data.kis

import autotrading.data.MarketIndex
import autotrading.data.VolatilityIndex
import java.time.Instant
import java.time.LocalDate

/**
 * Low-level KIS contract. Live HTTP/OAuth details stay behind this interface.
 * U.S. instruments require an explicit venue because KIS uses exchange-specific codes.
 */
interface KisGateway {
    val providerName: String get() = "KIS"

    fun quote(instrument: KisInstrument): KisQuoteDto?
    fun dailyBars(instrument: KisInstrument, limit: Int): List<KisDailyBarDto>
    fun indexBars(index: MarketIndex, limit: Int): List<KisIndexBarDto>
    fun volatility(index: VolatilityIndex, limit: Int): List<KisVolatilityDto>
}

enum class KisMarket { KR, US }
enum class KisVenue { KRX, NASDAQ, NYSE, AMEX }

data class KisInstrument(
    val symbol: String,
    val market: KisMarket,
    val venue: KisVenue
) {
    init {
        require(symbol.isNotBlank()) { "symbol is required" }
        require(market != KisMarket.KR || venue == KisVenue.KRX) { "KR instruments must use KRX" }
        require(market != KisMarket.US || venue != KisVenue.KRX) { "US instruments require NASDAQ/NYSE/AMEX" }
    }
}

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
