package autotrading.data

import java.time.Instant
import java.time.LocalDate

enum class Market { KR, US }
enum class Venue { KRX, NASDAQ, NYSE, AMEX }
enum class DataFreshness { FRESH, STALE, MISSING }
enum class MarketIndex { KOSPI, KOSDAQ, SP500, NASDAQ_COMPOSITE }
enum class VolatilityIndex { VKOSPI, VIX }

data class ProviderMeta(
    val provider: String,
    val observedAt: Instant,
    val freshness: DataFreshness = DataFreshness.FRESH
)

data class Quote(
    val symbol: String,
    val market: Market,
    val venue: Venue?,
    val price: Double,
    val volume: Long?,
    val changePercent: Double?,
    val meta: ProviderMeta
)

data class DailyBar(
    val symbol: String,
    val market: Market,
    val venue: Venue?,
    val date: LocalDate,
    val open: Double?,
    val high: Double?,
    val low: Double?,
    val close: Double,
    val volume: Long?,
    val meta: ProviderMeta
)

data class IndexBar(
    val index: MarketIndex,
    val date: LocalDate,
    val close: Double,
    val meta: ProviderMeta
)

data class VolatilityPoint(
    val index: VolatilityIndex,
    val date: LocalDate,
    val close: Double,
    val meta: ProviderMeta
)

data class MacroPoint(
    val seriesId: String,
    val date: LocalDate,
    val value: Double,
    val unit: String?,
    val meta: ProviderMeta
)
