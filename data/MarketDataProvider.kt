package autotrading.data

interface MarketDataProvider {
    val providerName: String

    fun quote(symbol: String, market: Market, venue: Venue? = null): Quote?

    /** Oldest-to-newest daily bars. */
    fun dailyBars(symbol: String, market: Market, venue: Venue? = null, limit: Int = 260): List<DailyBar>

    /** Oldest-to-newest index closes. */
    fun indexBars(index: MarketIndex, limit: Int = 260): List<IndexBar>

    fun volatility(index: VolatilityIndex, limit: Int = 260): List<VolatilityPoint>
}

interface MacroDataProvider {
    val providerName: String

    /** Oldest-to-newest observations for an external macro series identifier. */
    fun observations(seriesId: String, limit: Int = 260): List<MacroPoint>
}
