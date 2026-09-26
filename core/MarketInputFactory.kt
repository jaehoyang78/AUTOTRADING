package autotrading.core

/**
 * Assembly layer between normalized market features and the pure regime engine.
 * Optional fields remain null when a trustworthy source is unavailable.
 */
object MarketInputFactory {

    data class SupplementaryMarketData(
        val breadthPct: Double? = null,
        val volatilityIndexClose: Double? = null,
        val earningsRevisionBreadthPct: Double? = null,
        val creditSpreadStress: Double? = null
    )

    fun fromIndexFeatures(
        index: PriceFeatureCalculator.IndexTrendFeatures,
        supplementary: SupplementaryMarketData = SupplementaryMarketData()
    ): HybridStrategyEngine.MarketInputs = HybridStrategyEngine.MarketInputs(
        indexAbove20d = index.above20d,
        indexAbove60d = index.above60d,
        indexAbove200d = index.above200d,
        drawdownFrom52wHighPct = index.drawdownFrom52wHighPct,
        breadthPct = supplementary.breadthPct?.coerceIn(0.0, 100.0),
        vix = supplementary.volatilityIndexClose?.takeIf { it.isFinite() && it >= 0.0 },
        earningsRevisionBreadthPct = supplementary.earningsRevisionBreadthPct?.coerceIn(0.0, 100.0),
        creditSpreadStress = supplementary.creditSpreadStress?.coerceIn(0.0, 100.0)
    )
}
