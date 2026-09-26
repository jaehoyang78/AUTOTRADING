package autotrading.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PortfolioRiskEngineTest {

    @Test
    fun `risk on strong keeps lower cash target`() {
        val market = HybridStrategyEngine.assessMarket(
            HybridStrategyEngine.MarketInputs(
                indexAbove20d = true, indexAbove60d = true, indexAbove200d = true,
                drawdownFrom52wHighPct = -2.0, breadthPct = 80.0, vix = 14.0,
                earningsRevisionBreadthPct = 80.0, creditSpreadStress = 10.0
            )
        )
        val risk = PortfolioRiskEngine.assess(market, currentEquityExposurePct = 70.0)
        assertEquals(10.0, risk.recommendedCashPct)
        assertEquals(90.0, risk.maxEquityExposurePct)
        assertTrue(risk.canIncreaseEquityExposure)
    }

    @Test
    fun `risk off raises cash target`() {
        val market = HybridStrategyEngine.MarketAssessment(
            score = 30,
            regime = HybridStrategyEngine.MarketRegime.RISK_OFF,
            riskMultiplier = 0.55,
            summary = "risk off",
            dataCoveragePct = 100
        )
        val risk = PortfolioRiskEngine.assess(market, currentEquityExposurePct = 65.0)
        assertEquals(40.0, risk.recommendedCashPct)
        assertFalse(risk.canIncreaseEquityExposure)
    }

    @Test
    fun `large portfolio drawdown adds cash buffer`() {
        val market = HybridStrategyEngine.MarketAssessment(
            score = 55,
            regime = HybridStrategyEngine.MarketRegime.NEUTRAL,
            riskMultiplier = 0.70,
            summary = "neutral",
            dataCoveragePct = 100
        )
        val risk = PortfolioRiskEngine.assess(
            market,
            currentEquityExposurePct = 50.0,
            portfolioDrawdownPct = -16.0
        )
        assertEquals(35.0, risk.recommendedCashPct)
        assertEquals(65.0, risk.maxEquityExposurePct)
        assertTrue(risk.canIncreaseEquityExposure)
    }
}
