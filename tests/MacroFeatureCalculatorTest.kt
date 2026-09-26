package autotrading.core

import autotrading.data.MacroPoint
import autotrading.data.Market
import autotrading.data.ProviderMeta
import autotrading.data.fred.FredSeriesCatalog
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MacroFeatureCalculatorTest {
    @Test
    fun `favorable macro scores above adverse macro`() {
        val favorable = MacroFeatureCalculator.calculate(Market.KR, favorableSeries())
        val adverse = MacroFeatureCalculator.calculate(Market.KR, adverseSeries())

        assertNotNull(favorable.macroTailwindScore)
        assertNotNull(adverse.macroTailwindScore)
        assertTrue(favorable.macroTailwindScore > adverse.macroTailwindScore)
        assertTrue((favorable.creditSpreadStress ?: 100.0) < (adverse.creditSpreadStress ?: 0.0))
        assertTrue(favorable.dataCoveragePct >= 80)
    }

    @Test
    fun `macro overlay raises or lowers base regime score`() {
        val base = HybridStrategyEngine.MarketAssessment(
            score = 60,
            regime = HybridStrategyEngine.MarketRegime.NEUTRAL,
            riskMultiplier = 0.70,
            summary = "base",
            dataCoveragePct = 100
        )
        val favorable = MacroRegimeOverlay.apply(
            base,
            MacroFeatureCalculator.calculate(Market.US, favorableSeries())
        )
        val adverse = MacroRegimeOverlay.apply(
            base,
            MacroFeatureCalculator.calculate(Market.US, adverseSeries())
        )
        assertTrue(favorable.score > base.score)
        assertTrue(adverse.score < base.score)
        assertTrue(favorable.summary.contains("Macro"))
    }

    @Test
    fun `missing macro data leaves base regime unchanged`() {
        val base = HybridStrategyEngine.assessMarket(
            HybridStrategyEngine.MarketInputs(indexAbove200d = true, vix = 18.0)
        )
        val macro = MacroFeatureCalculator.calculate(Market.US, emptyMap())
        val overlaid = MacroRegimeOverlay.apply(base, macro)
        assertTrue(macro.macroTailwindScore == null)
        assertTrue(overlaid == base)
    }

    private fun favorableSeries(): Map<String, List<MacroPoint>> = mapOf(
        FredSeriesCatalog.FED_FUNDS_EFFECTIVE to linearSeries(FredSeriesCatalog.FED_FUNDS_EFFECTIVE, 130, 5.0, 3.0),
        FredSeriesCatalog.TREASURY_2Y to linearSeries(FredSeriesCatalog.TREASURY_2Y, 130, 3.2, 3.0),
        FredSeriesCatalog.TREASURY_10Y to linearSeries(FredSeriesCatalog.TREASURY_10Y, 130, 4.0, 4.0),
        FredSeriesCatalog.CPI_ALL_URBAN_SA to linearSeries(FredSeriesCatalog.CPI_ALL_URBAN_SA, 18, 100.0, 103.0, 30),
        FredSeriesCatalog.FED_TOTAL_ASSETS to linearSeries(FredSeriesCatalog.FED_TOTAL_ASSETS, 15, 7000.0, 7350.0, 7),
        FredSeriesCatalog.USD_KRW to linearSeries(FredSeriesCatalog.USD_KRW, 70, 1400.0, 1330.0),
        FredSeriesCatalog.US_HIGH_YIELD_OAS to linearSeries(FredSeriesCatalog.US_HIGH_YIELD_OAS, 5, 3.2, 3.0)
    )

    private fun adverseSeries(): Map<String, List<MacroPoint>> = mapOf(
        FredSeriesCatalog.FED_FUNDS_EFFECTIVE to linearSeries(FredSeriesCatalog.FED_FUNDS_EFFECTIVE, 130, 3.0, 5.0),
        FredSeriesCatalog.TREASURY_2Y to linearSeries(FredSeriesCatalog.TREASURY_2Y, 130, 5.0, 5.0),
        FredSeriesCatalog.TREASURY_10Y to linearSeries(FredSeriesCatalog.TREASURY_10Y, 130, 3.8, 3.8),
        FredSeriesCatalog.CPI_ALL_URBAN_SA to linearSeries(FredSeriesCatalog.CPI_ALL_URBAN_SA, 18, 100.0, 110.0, 30),
        FredSeriesCatalog.FED_TOTAL_ASSETS to linearSeries(FredSeriesCatalog.FED_TOTAL_ASSETS, 15, 7000.0, 6650.0, 7),
        FredSeriesCatalog.USD_KRW to linearSeries(FredSeriesCatalog.USD_KRW, 70, 1300.0, 1450.0),
        FredSeriesCatalog.US_HIGH_YIELD_OAS to linearSeries(FredSeriesCatalog.US_HIGH_YIELD_OAS, 5, 7.5, 8.0)
    )

    private fun linearSeries(
        id: String,
        count: Int,
        start: Double,
        end: Double,
        stepDays: Long = 1
    ): List<MacroPoint> {
        val firstDate = LocalDate.of(2025, 1, 1)
        return (0 until count).map { i ->
            val t = if (count <= 1) 0.0 else i.toDouble() / (count - 1)
            MacroPoint(
                seriesId = id,
                date = firstDate.plusDays(i * stepDays),
                value = start + (end - start) * t,
                unit = null,
                meta = ProviderMeta("test", Instant.parse("2026-09-26T00:00:00Z"))
            )
        }
    }
}
