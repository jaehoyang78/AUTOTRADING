package autotrading.data.kis

import autotrading.data.*

class KisMarketDataProvider(
    private val gateway: KisGateway
) : MarketDataProvider {

    override val providerName: String = gateway.providerName

    override fun quote(symbol: String, market: Market): Quote? {
        val dto = gateway.quote(symbol, market.toKis()) ?: return null
        return Quote(
            symbol = dto.symbol,
            market = market,
            price = dto.price,
            volume = dto.volume,
            changePercent = dto.changePercent,
            meta = ProviderMeta(providerName, dto.observedAt)
        )
    }

    override fun dailyBars(symbol: String, market: Market, limit: Int): List<DailyBar> {
        require(limit > 0) { "limit must be positive" }
        return gateway.dailyBars(symbol, market.toKis(), limit)
            .sortedBy { it.date }
            .takeLast(limit)
            .map { dto ->
                DailyBar(
                    symbol = symbol,
                    market = market,
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

    private fun Market.toKis() = when (this) {
        Market.KR -> KisMarket.KR
        Market.US -> KisMarket.US
    }
}
