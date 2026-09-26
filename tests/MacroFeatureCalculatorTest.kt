package autotrading.core

import autotrading.data.MacroPoint
import autotrading.data.ProviderMeta
import autotrading.data.fred.FredSeriesCatalog
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MacroFeatureCalculatorTest {
    private val meta = ProviderMeta("test", Instant.parse("2026-09-26T00:00:00Z"))

    @Test
    fun `growth rising and inflation falling produces favorable quadrant`() {
        val series = mutableMapOf<String, List<MacroPoint>>()
        series[FredSeriesCatalog.INDUSTRIAL_PRODUCTION] = monthlySeries(
            start = LocalDate.of(2024, 1, 1), months = 33
        ) { index ->
            val year = index / 12
            val monthInYear = index % 12
            100.0 + year * 3.0 + monthInYear * (if (index >= 21) 0.45 else 0.15)
        }
        series[FredSeriesCatalog.CPI_ALL_URBAN_SA] = monthlySeries(
            start = LocalDate.of(2024, 1, 1), months = 33
        ) { index ->
            // Inflation pace cools in the latest year.
            if (index < 21) 300.0 + index * 0.9 else 318.9 + (index - 21) * 0.35
        }
        series[FredSeriesCatalog.TREASURY_2Y] = points("DGS2", listOf("2026-09-24" to 4.8))
        series[FredSeriesCatalog.TREASURY_10Y] = points("DGS10", listOf("2026-09-24" to 5.0))
        series[FredSeriesCatalog.US_HIGH_YIELD_OAS] = points("HY", listOf("2026-09-24" to 3.0))

        val f = MacroFeatureCalculator.calculate(series)
        assertEquals(MacroFeatureCalculator.Trend.RISING, f.industrialProductionTrend)
        assertEquals(MacroFeatureCalculator.Trend.FALLING, f.inflationTrend)
        assertEquals(MacroFeatureCalculator.MacroQuadrant.GROWTH_UP_INFLATION_DOWN, f.quadrant)
        assertEquals(0.2, assertNotNull(f.yieldCurve10y2yPctPoints), 0.0001)
        assertTrue(assertNotNull(f.creditSpreadStress) < 20.0)
    }

    @Test
    fun `high OAS maps to high stress and reduces cyclical sector fit`() {
        val calm = MacroFeatureCalculator.MacroFeatures(
            industrialProductionYoYPct = 2.0,
            industrialProductionTrend = MacroFeatureCalculator.Trend.RISING,
            cpiYoYPct = 3.0,
            inflationTrend = MacroFeatureCalculator.Trend.RISING,
            fedFundsPct = null,
            fedFunds3mChangePctPoints = null,
            yieldCurve10y2yPctPoints = null,
            fedBalanceSheet13wChangePct = null,
            usdKrw3mChangePct = null,
            highYieldOasPct = 3.0,
            creditSpreadStress = MacroFeatureCalculator.oasToStress(3.0),
            quadrant = MacroFeatureCalculator.MacroQuadrant.GROWTH_UP_INFLATION_UP
        )
        val stressed = calm.copy(
            highYieldOasPct = 8.0,
            creditSpreadStress = MacroFeatureCalculator.oasToStress(8.0)
        )
        val calmTech = assertNotNull(MacroSectorFitEngine.score(MacroSectorFitEngine.Sector.TECHNOLOGY, calm))
        val stressedTech = assertNotNull(MacroSectorFitEngine.score(MacroSectorFitEngine.Sector.TECHNOLOGY, stressed))
        assertTrue(stressedTech < calmTech)
        assertEquals(100.0, MacroFeatureCalculator.oasToStress(20.0))
        assertEquals(0.0, MacroFeatureCalculator.oasToStress(1.0))
    }

    @Test
    fun `missing year history remains unknown instead of inventing a trend`() {
        val series = mapOf(
            FredSeriesCatalog.CPI_ALL_URBAN_SA to monthlySeries(LocalDate.of(2026, 1, 1), 6) { 300.0 + it }
        )
        val f = MacroFeatureCalculator.calculate(series)
        assertEquals(null, f.cpiYoYPct)
        assertEquals(MacroFeatureCalculator.Trend.UNKNOWN, f.inflationTrend)
        assertEquals(MacroFeatureCalculator.MacroQuadrant.UNKNOWN, f.quadrant)
    }

    @Test
    fun `rate liquidity and fx changes use older observations without interpolation`() {
        val series = mapOf(
            FredSeriesCatalog.FED_FUNDS_EFFECTIVE to points("DFF", listOf(
                "2026-06-20" to 5.0,
                "2026-09-24" to 4.5
            )),
            FredSeriesCatalog.FED_TOTAL_ASSETS to points("WALCL", listOf(
                "2026-06-20" to 7000.0,
                "2026-09-24" to 7140.0
            )),
            FredSeriesCatalog.USD_KRW to points("DEXKOUS", listOf(
                "2026-06-20" to 1300.0,
                "2026-09-24" to 1365.0
            ))
        )
        val f = MacroFeatureCalculator.calculate(series)
        assertEquals(-0.5, assertNotNull(f.fedFunds3mChangePctPoints), 0.0001)
        assertEquals(2.0, assertNotNull(f.fedBalanceSheet13wChangePct), 0.0001)
        assertEquals(5.0, assertNotNull(f.usdKrw3mChangePct), 0.0001)
    }

    private fun monthlySeries(
        start: LocalDate,
        months: Int,
        value: (Int) -> Double
    ): List<MacroPoint> = (0 until months).map { n ->
        MacroPoint("M", start.plusMonths(n.toLong()), value(n), null, meta)
    }

    private fun points(id: String, values: List<Pair<String, Double>>): List<MacroPoint> =
        values.map { (date, value) -> MacroPoint(id, LocalDate.parse(date), value, null, meta) }
}
