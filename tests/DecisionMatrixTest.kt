package autotrading.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecisionMatrixTest {
    private val market = HybridStrategyEngine.assessMarket(
        HybridStrategyEngine.MarketInputs(
            indexAbove20d = true,
            indexAbove60d = true,
            indexAbove200d = true,
            drawdownFrom52wHighPct = -2.0,
            breadthPct = 80.0,
            vix = 14.0,
            earningsRevisionBreadthPct = 75.0,
            creditSpreadStress = 10.0
        )
    )

    @Test
    fun `zero holding with positive target becomes buy`() {
        val a = HybridStrategyEngine.assessStock(strongStock(), market)
        val d = HybridStrategyEngine.decide(
            HybridStrategyEngine.DecisionInputs(a, HybridStrategyEngine.ThesisState.INTACT, currentPositionPct = 0.0)
        )
        assertEquals(HybridStrategyEngine.DecisionAction.BUY, d.action)
        assertTrue(d.targetPositionPct > 0.0)
    }

    @Test
    fun `holding materially below target becomes add`() {
        val a = HybridStrategyEngine.assessStock(strongStock(), market)
        val current = (a.recommendedPositionPct - 1.0).coerceAtLeast(0.1)
        val d = HybridStrategyEngine.decide(
            HybridStrategyEngine.DecisionInputs(a, HybridStrategyEngine.ThesisState.INTACT, currentPositionPct = current)
        )
        assertEquals(HybridStrategyEngine.DecisionAction.ADD, d.action)
    }

    @Test
    fun `holding near target becomes hold`() {
        val a = HybridStrategyEngine.assessStock(strongStock(), market)
        val d = HybridStrategyEngine.decide(
            HybridStrategyEngine.DecisionInputs(a, HybridStrategyEngine.ThesisState.INTACT, currentPositionPct = a.recommendedPositionPct)
        )
        assertEquals(HybridStrategyEngine.DecisionAction.HOLD, d.action)
    }

    @Test
    fun `holding above target becomes reduce`() {
        val a = HybridStrategyEngine.assessStock(strongStock(), market)
        val d = HybridStrategyEngine.decide(
            HybridStrategyEngine.DecisionInputs(a, HybridStrategyEngine.ThesisState.INTACT, currentPositionPct = a.recommendedPositionPct + 1.0)
        )
        assertEquals(HybridStrategyEngine.DecisionAction.REDUCE, d.action)
    }

    @Test
    fun `low score existing holding becomes sell`() {
        val a = HybridStrategyEngine.assessStock(weakButCoveredStock(), market)
        assertTrue(a.totalScore < 60)
        val d = HybridStrategyEngine.decide(
            HybridStrategyEngine.DecisionInputs(a, HybridStrategyEngine.ThesisState.INTACT, currentPositionPct = 2.0)
        )
        assertEquals(HybridStrategyEngine.DecisionAction.SELL, d.action)
    }

    @Test
    fun `low score with no holding becomes avoid`() {
        val a = HybridStrategyEngine.assessStock(weakButCoveredStock(), market)
        val d = HybridStrategyEngine.decide(
            HybridStrategyEngine.DecisionInputs(a, HybridStrategyEngine.ThesisState.INTACT, currentPositionPct = 0.0)
        )
        assertEquals(HybridStrategyEngine.DecisionAction.AVOID, d.action)
    }

    @Test
    fun `insufficient coverage becomes watch`() {
        val a = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(roicPct = 18.0, return60dPct = 10.0), market
        )
        val d = HybridStrategyEngine.decide(
            HybridStrategyEngine.DecisionInputs(a, HybridStrategyEngine.ThesisState.UNKNOWN, currentPositionPct = 0.0)
        )
        assertEquals(HybridStrategyEngine.DecisionAction.WATCH, d.action)
    }

    private fun strongStock() = HybridStrategyEngine.StockInputs(
        roicPct = 20.0, roePct = 25.0, operatingMarginPct = 25.0,
        freeCashFlowPositiveYears = 5, debtToEquityPct = 30.0,
        revenueGrowthPct = 30.0, epsGrowthPct = 35.0, operatingIncomeGrowthPct = 35.0,
        forwardEpsGrowthPct = 30.0,
        pe = 12.0, sectorPe = 24.0, pb = 1.0, evEbitda = 6.0,
        fcfYieldPct = 9.0, peg = 0.7,
        return20dPct = 18.0, return60dPct = 30.0, return120dPct = 45.0,
        relativeStrength60dPct = 18.0, volumeRatio20d = 1.8, distanceFrom52wHighPct = -2.0,
        epsRevision3mPct = 18.0, estimateUpDownRatio = 1.9,
        sectorMacroFitScore = 90.0
    )

    private fun weakButCoveredStock() = HybridStrategyEngine.StockInputs(
        roicPct = 0.0, roePct = 0.0, operatingMarginPct = 0.0,
        freeCashFlowPositiveYears = 0, debtToEquityPct = 240.0,
        revenueGrowthPct = -10.0, epsGrowthPct = -15.0, operatingIncomeGrowthPct = -15.0,
        forwardEpsGrowthPct = -10.0,
        pe = 16.0, sectorPe = 10.0, pb = 5.0, evEbitda = 25.0,
        fcfYieldPct = 0.0, peg = 2.5,
        return20dPct = -15.0, return60dPct = -20.0, return120dPct = -25.0,
        relativeStrength60dPct = -15.0, volumeRatio20d = 0.5, distanceFrom52wHighPct = -40.0,
        epsRevision3mPct = -20.0, estimateUpDownRatio = 0.5,
        sectorMacroFitScore = 10.0
    )
}
