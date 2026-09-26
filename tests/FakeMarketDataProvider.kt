package autotrading.core.data

import java.time.LocalDate

class FakeMarketDataProvider(
    private val quotes: Map<String, Quote> = emptyMap(),
    private val bars: Map<String, List<DailyBar>> = emptyMap(),
    private val indices: Map<String, IndexSnapshot> = emptyMap(),
    private val volatility: Map<String, VolatilitySnapshot> = emptyMap()
) : MarketDataProvider {
    override val providerId: String = "fake"

    override fun quote(symbol: String): Quote =
        quotes[symbol] ?: error("missing fake quote: $symbol")

    override fun dailyBars(symbol: String, from: LocalDate, to: LocalDate): List<DailyBar> =
        (bars[symbol] ?: emptyList()).filter { !it.date.isBefore(from) && !it.date.isAfter(to) }

    override fun indexSnapshot(symbol: String): IndexSnapshot =
        indices[symbol] ?: error("missing fake index: $symbol")

    override fun volatilitySnapshot(symbol: String): VolatilitySnapshot =
        volatility[symbol] ?: error("missing fake volatility: $symbol")
}
