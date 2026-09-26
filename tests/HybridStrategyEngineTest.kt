package autotrading.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HybridStrategyEngineTest {

    @Test
    fun `strong market becomes risk on`() {
        val m = HybridStrategyEngine.assessMarket(
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
        assertEquals(HybridStrategyEngine.MarketRegime.RISK_ON_STRONG, m.regime)
        assertEquals(100, m.dataCoveragePct)
        assertTrue(m.score >= 80)
    }

    @Test
    fun `insufficient stock data blocks recommendation`() {
        val market = HybridStrategyEngine.assessMarket(
            HybridStrategyEngine.MarketInputs(indexAbove200d = true, vix = 18.0)
        )
        val a = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(roicPct = 20.0, return60dPct = 30.0), market
        )
        assertTrue(!a.coverage.sufficientForRecommendation)
        assertEquals(0.0, a.recommendedPositionPct)
        assertEquals("데이터 보강", a.action)
    }

    @Test
    fun `high quality complete candidate gets investor fit and position`() {
        val market = HybridStrategyEngine.assessMarket(
            HybridStrategyEngine.MarketInputs(
                indexAbove20d = true, indexAbove60d = true, indexAbove200d = true,
                drawdownFrom52wHighPct = -3.0, breadthPct = 75.0, vix = 16.0,
                earningsRevisionBreadthPct = 70.0, creditSpreadStress = 15.0
            )
        )
        val a = HybridStrategyEngine.assessStock(fullStrongStock(), market)
        assertTrue(a.coverage.sufficientForRecommendation)
        assertTrue(a.totalScore >= 70)
        assertTrue(a.investorFit.buffett > 60)
        assertTrue(a.investorFit.lynch > 60)
        assertTrue(a.recommendedPositionPct > 0.0)
    }

    @Test
    fun `panic blocks low quality candidate`() {
        val panic = HybridStrategyEngine.assessMarket(
            HybridStrategyEngine.MarketInputs(
                indexAbove20d = false, indexAbove60d = false, indexAbove200d = false,
                drawdownFrom52wHighPct = -40.0, breadthPct = 10.0, vix = 50.0,
                earningsRevisionBreadthPct = 10.0, creditSpreadStress = 95.0
            )
        )
        val stock = fullStrongStock().copy(
            roicPct = 1.0, roePct = 2.0, operatingMarginPct = 1.0,
            freeCashFlowPositiveYears = 0, debtToEquityPct = 240.0
        )
        val a = HybridStrategyEngine.assessStock(stock, panic)
        assertEquals(HybridStrategyEngine.MarketRegime.PANIC, panic.regime)
        assertEquals(0.0, a.recommendedPositionPct)
    }

    @Test
    fun `broken thesis forces sell`() {
        val market = HybridStrategyEngine.assessMarket(
            HybridStrategyEngine.MarketInputs(indexAbove20d = true, indexAbove60d = true, indexAbove200d = true,
                breadthPct = 70.0, vix = 18.0, earningsRevisionBreadthPct = 65.0)
        )
        val assessment = HybridStrategyEngine.assessStock(fullStrongStock(), market)
        val d = HybridStrategyEngine.decide(
            HybridStrategyEngine.DecisionInputs(
                assessment = assessment,
                thesisState = HybridStrategyEngine.ThesisState.BROKEN,
                currentPositionPct = 5.0
            )
        )
        assertEquals(HybridStrategyEngine.DecisionAction.SELL, d.action)
        assertEquals(0.0, d.targetPositionPct)
    }

    @Test
    fun `sector concentration prevents increasing risk`() {
        val market = HybridStrategyEngine.assessMarket(
            HybridStrategyEngine.MarketInputs(indexAbove20d = true, indexAbove60d = true, indexAbove200d = true,
                breadthPct = 80.0, vix = 15.0, earningsRevisionBreadthPct = 75.0)
        )
        val assessment = HybridStrategyEngine.assessStock(fullStrongStock(), market)
        val d = HybridStrategyEngine.decide(
            HybridStrategyEngine.DecisionInputs(
                assessment = assessment,
                thesisState = HybridStrategyEngine.ThesisState.INTACT,
                currentPositionPct = 2.0,
                currentSectorPositionPct = 28.0
            )
        )
        assertTrue(d.targetPositionPct <= 2.0)
        assertTrue(d.action != HybridStrategyEngine.DecisionAction.ADD)
    }

    private fun fullStrongStock() = HybridStrategyEngine.StockInputs(
        roicPct = 18.0, roePct = 22.0, operatingMarginPct = 20.0,
        freeCashFlowPositiveYears = 5, debtToEquityPct = 40.0,
        revenueGrowthPct = 20.0, epsGrowthPct = 25.0, operatingIncomeGrowthPct = 22.0,
        forwardEpsGrowthPct = 20.0,
        pe = 18.0, sectorPe = 24.0, pb = 2.0, evEbitda = 10.0,
        fcfYieldPct = 6.0, peg = 1.0,
        return20dPct = 8.0, return60dPct = 18.0, return120dPct = 28.0,
        relativeStrength60dPct = 12.0, volumeRatio20d = 1.4, distanceFrom52wHighPct = -5.0,
        epsRevision3mPct = 10.0, estimateUpDownRatio = 1.6,
        sectorMacroFitScore = 80.0
    )
}
