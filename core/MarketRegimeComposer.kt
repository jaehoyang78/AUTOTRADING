package autotrading.core

/**
 * End-to-end assembly for a market regime assessment.
 * Price/breadth/volatility inputs create the base regime, then macro applies a bounded overlay.
 */
object MarketRegimeComposer {
    fun assess(
        index: PriceFeatureCalculator.IndexTrendFeatures,
        supplementary: MarketInputFactory.SupplementaryMarketData = MarketInputFactory.SupplementaryMarketData(),
        macro: MacroFeatureCalculator.MacroFeatures? = null
    ): HybridStrategyEngine.MarketAssessment {
        val mergedSupplementary = if (macro?.creditSpreadStress != null && supplementary.creditSpreadStress == null) {
            supplementary.copy(creditSpreadStress = macro.creditSpreadStress)
        } else supplementary

        val baseInputs = MarketInputFactory.fromIndexFeatures(index, mergedSupplementary)
        val baseAssessment = HybridStrategyEngine.assessMarket(baseInputs)
        return if (macro == null) baseAssessment else MacroRegimeOverlay.apply(baseAssessment, macro)
    }
}
