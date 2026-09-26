package autotrading.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MacroIntegrationTest {

    @Test
    fun `macro credit stress can populate market supplementary input`() {
        val macro = sampleMacro(creditStress = 80.0)
        val supplementary = MarketInputFactory.withMacro(
            MarketInputFactory.SupplementaryMarketData(breadthPct = 55.0),
            macro
        )
        val input = MarketInputFactory.fromIndexFeatures(
            PriceFeatureCalculator.IndexTrendFeatures(true, true, true, -5.0),
            supplementary
        )
        assertEquals(80.0, input.creditSpreadStress)
        assertEquals(55.0, input.breadthPct)
    }

    @Test
    fun `explicit market stress overrides macro fallback`() {
        val macro = sampleMacro(creditStress = 80.0)
        val supplementary = MarketInputFactory.withMacro(
            MarketInputFactory.SupplementaryMarketData(creditSpreadStress = 35.0),
            macro
        )
        assertEquals(35.0, supplementary.creditSpreadStress)
    }

    @Test
    fun `macro sector fit enriches stock input without changing other factors`() {
        val stock = HybridStrategyEngine.StockInputs(
            roicPct = 15.0,
            return60dPct = 10.0
        )
        val enriched = StockInputEnricher.withMacroSectorFit(
            stock,
            MacroSectorFitEngine.Sector.ENERGY,
            sampleMacro(
                quadrant = MacroFeatureCalculator.MacroQuadrant.GROWTH_UP_INFLATION_UP,
                creditStress = 20.0
            )
        )
        assertEquals(15.0, enriched.roicPct)
        assertNotNull(enriched.sectorMacroFitScore)
        assertEquals(85.0, enriched.sectorMacroFitScore)
    }

    private fun sampleMacro(
        quadrant: MacroFeatureCalculator.MacroQuadrant = MacroFeatureCalculator.MacroQuadrant.GROWTH_UP_INFLATION_DOWN,
        creditStress: Double
    ) = MacroFeatureCalculator.MacroFeatures(
        industrialProductionYoYPct = 2.0,
        industrialProductionTrend = MacroFeatureCalculator.Trend.RISING,
        cpiYoYPct = 2.5,
        inflationTrend = MacroFeatureCalculator.Trend.FALLING,
        fedFundsPct = 4.0,
        fedFunds3mChangePctPoints = -0.25,
        yieldCurve10y2yPctPoints = 0.2,
        fedBalanceSheet13wChangePct = 1.0,
        usdKrw3mChangePct = 0.0,
        highYieldOasPct = 4.0,
        creditSpreadStress = creditStress,
        quadrant = quadrant
    )
}
