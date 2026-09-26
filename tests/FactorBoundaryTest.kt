package autotrading.core

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FactorBoundaryTest {

    private val market = HybridStrategyEngine.MarketAssessment(
        score = 70,
        regime = HybridStrategyEngine.MarketRegime.RISK_ON_NORMAL,
        riskMultiplier = 0.90,
        summary = "normal",
        dataCoveragePct = 100
    )

    @Test
    fun `quality clamps extreme inputs to valid score range`() {
        val a = assess(
            base().copy(
                roicPct = 1000.0,
                roePct = 1000.0,
                operatingMarginPct = 1000.0,
                freeCashFlowPositiveYears = 100,
                debtToEquityPct = -100.0
            )
        )
        val q = assertNotNull(a.components.quality)
        assertTrue(q in 0.0..100.0)
    }

    @Test
    fun `growth rewards strong growth over contraction`() {
        val strong = assess(base().copy(
            revenueGrowthPct = 30.0, epsGrowthPct = 35.0,
            operatingIncomeGrowthPct = 35.0, forwardEpsGrowthPct = 30.0
        ))
        val weak = assess(base().copy(
            revenueGrowthPct = -10.0, epsGrowthPct = -15.0,
            operatingIncomeGrowthPct = -15.0, forwardEpsGrowthPct = -10.0
        ))
        assertTrue(assertNotNull(strong.components.growth) > assertNotNull(weak.components.growth))
    }

    @Test
    fun `value rewards cheaper valuation with positive metrics`() {
        val cheap = assess(base().copy(
            pe = 10.0, sectorPe = 20.0, pb = 0.8, evEbitda = 5.0,
            fcfYieldPct = 10.0, peg = 0.5
        ))
        val expensive = assess(base().copy(
            pe = 32.0, sectorPe = 20.0, pb = 5.0, evEbitda = 25.0,
            fcfYieldPct = 0.0, peg = 2.5
        ))
        assertTrue(assertNotNull(cheap.components.value) > assertNotNull(expensive.components.value))
    }

    @Test
    fun `momentum rewards relative strength and trend`() {
        val strong = assess(base().copy(
            return20dPct = 20.0, return60dPct = 35.0, return120dPct = 50.0,
            relativeStrength60dPct = 20.0, volumeRatio20d = 2.0,
            distanceFrom52wHighPct = 0.0
        ))
        val weak = assess(base().copy(
            return20dPct = -15.0, return60dPct = -20.0, return120dPct = -25.0,
            relativeStrength60dPct = -15.0, volumeRatio20d = 0.5,
            distanceFrom52wHighPct = -40.0
        ))
        assertTrue(assertNotNull(strong.components.momentum) > assertNotNull(weak.components.momentum))
    }

    @Test
    fun `revision rewards upgrades and penalizes deep cuts`() {
        val up = assess(base().copy(epsRevision3mPct = 20.0, estimateUpDownRatio = 2.0))
        val down = assess(base().copy(epsRevision3mPct = -20.0, estimateUpDownRatio = 0.5))
        assertTrue(assertNotNull(up.components.revision) > assertNotNull(down.components.revision))
        assertTrue(up.totalScore > down.totalScore)
    }

    @Test
    fun `all component scores remain bounded under extreme values`() {
        val a = assess(
            base().copy(
                roicPct = -9999.0, roePct = 9999.0, operatingMarginPct = -9999.0,
                debtToEquityPct = 99999.0, revenueGrowthPct = 9999.0,
                epsGrowthPct = -9999.0, pe = 99999.0, sectorPe = 0.01,
                pb = 99999.0, evEbitda = 99999.0, fcfYieldPct = -9999.0,
                peg = 99999.0, return20dPct = 9999.0, return60dPct = -9999.0,
                return120dPct = 9999.0, relativeStrength60dPct = -9999.0,
                volumeRatio20d = 9999.0, distanceFrom52wHighPct = -9999.0,
                epsRevision3mPct = 9999.0, estimateUpDownRatio = -9999.0,
                sectorMacroFitScore = 9999.0
            )
        )
        val values = listOfNotNull(
            a.components.quality, a.components.growth, a.components.value,
            a.components.momentum, a.components.revision, a.components.macroFit
        )
        assertTrue(values.isNotEmpty())
        assertTrue(values.all { it in 0.0..100.0 })
        assertTrue(a.totalScore in 0..100)
    }

    private fun assess(i: HybridStrategyEngine.StockInputs) = HybridStrategyEngine.assessStock(i, market)

    private fun base() = HybridStrategyEngine.StockInputs(
        roicPct = 12.0, roePct = 15.0, operatingMarginPct = 12.0,
        freeCashFlowPositiveYears = 4, debtToEquityPct = 70.0,
        revenueGrowthPct = 10.0, epsGrowthPct = 12.0,
        operatingIncomeGrowthPct = 10.0, forwardEpsGrowthPct = 10.0,
        pe = 15.0, sectorPe = 18.0, pb = 2.0, evEbitda = 10.0,
        fcfYieldPct = 5.0, peg = 1.2,
        return20dPct = 3.0, return60dPct = 8.0, return120dPct = 12.0,
        relativeStrength60dPct = 4.0, volumeRatio20d = 1.1,
        distanceFrom52wHighPct = -10.0,
        epsRevision3mPct = 3.0, estimateUpDownRatio = 1.2,
        sectorMacroFitScore = 60.0
    )
}
