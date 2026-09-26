package autotrading.data.kis

import autotrading.data.*

class KisMarketDataProvider(
    private val gateway: KisGateway
) : MarketDataProvider {

    override val providerName: String = gateway.providerName

    override fun quote(symbol: String, market: Market, venue: Venue?): Quote? {
        val resolvedVenue = resolveVenue(market, venue)
        val dto = gateway.quote(KisInstrument(symbol, market.toKis(), resolvedVenue.toKis())) ?: return null
        return Quote(
            symbol = dto.symbol,
            market = market,
            venue = resolvedVenue,
            price = dto.price,
            volume = dto.volume,
            changePercent = dto.changePercent,
            meta = ProviderMeta(providerName, dto.observedAt)
        )
    }

    override fun dailyBars(symbol: String, market: Market, venue: Venue?, limit: Int): List<DailyBar> {
        require(limit > 0) { "limit must be positive" }
        val resolvedVenue = resolveVenue(market, venue)
        val instrument = KisInstrument(symbol, market.toKis(), resolvedVenue.toKis())
        return gateway.dailyBars(instrument, limit)
            .sortedBy { it.date }
            .takeLast(limit)
            .map { dto ->
                DailyBar(
                    symbol = symbol,
                    market = market,
                    venue = resolvedVenue,
                    date = dto.date,
                    open = dto.open,
                    high = dto.high,
                    low = dto.low,
                    close = dto.close,
                    volume = dto.volume,
                    meta = ProviderMeta(providerName, dto.observedAt)
                )
            }
    }

    override fun indexBars(index: MarketIndex, limit: Int): List<IndexBar> {
        require(limit > 0) { "limit must be positive" }
        return gateway.indexBars(index, limit)
            .sortedBy { it.date }
            .takeLast(limit)
            .map { dto -> IndexBar(index, dto.date, dto.close, ProviderMeta(providerName, dto.observedAt)) }
    }

    override fun volatility(index: VolatilityIndex, limit: Int): List<VolatilityPoint> {
        require(limit > 0) { "limit must be positive" }
        return gateway.volatility(index, limit)
            .sortedBy { it.date }
            .takeLast(limit)
            .map { dto -> VolatilityPoint(index, dto.date, dto.close, ProviderMeta(providerName, dto.observedAt)) }
    }

    private fun resolveVenue(market: Market, venue: Venue?): Venue = when (market) {
        Market.KR -> {
            require(venue == null || venue == Venue.KRX) { "KR instruments must use KRX" }
            Venue.KRX
        }
        Market.US -> {
            require(venue in setOf(Venue.NASDAQ, Venue.NYSE, Venue.AMEX)) {
                "US instruments require NASDAQ/NYSE/AMEX venue"
            }
            venue!!
        }
    }

    private fun Market.toKis() = when (this) {
        Market.KR -> KisMarket.KR
        Market.US -> KisMarket.US
    }

    private fun Venue.toKis() = when (this) {
        Venue.KRX -> KisVenue.KRX
        Venue.NASDAQ -> KisVenue.NASDAQ
        Venue.NYSE -> KisVenue.NYSE
        Venue.AMEX -> KisVenue.AMEX
    }
}
