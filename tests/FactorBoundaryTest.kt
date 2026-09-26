package autotrading.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FactorBoundaryTest {
    private val market = HybridStrategyEngine.assessMarket(HybridStrategyEngine.MarketInputs())

    @Test
    fun `quality ROIC boundary clips to zero and one hundred`() {
        val low = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(roicPct = 0.0), market
        ).components.quality
        val high = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(roicPct = 20.0), market
        ).components.quality
        assertEquals(0.0, low)
        assertEquals(100.0, high)
    }

    @Test
    fun `growth revenue boundary clips to zero and one hundred`() {
        val low = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(revenueGrowthPct = -10.0), market
        ).components.growth
        val high = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(revenueGrowthPct = 30.0), market
        ).components.growth
        assertEquals(0.0, low)
        assertEquals(100.0, high)
    }

    @Test
    fun `value relative PE boundary favors discount and penalizes premium`() {
        val cheap = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(pe = 6.0, sectorPe = 10.0), market
        ).components.value
        val expensive = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(pe = 16.0, sectorPe = 10.0), market
        ).components.value
        assertEquals(100.0, cheap)
        assertEquals(0.0, expensive)
    }

    @Test
    fun `momentum return boundary clips to zero and one hundred`() {
        val weak = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(return20dPct = -15.0), market
        ).components.momentum
        val strong = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(return20dPct = 20.0), market
        ).components.momentum
        assertEquals(0.0, weak)
        assertEquals(100.0, strong)
    }

    @Test
    fun `revision boundary clips to zero and one hundred`() {
        val down = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(epsRevision3mPct = -20.0), market
        ).components.revision
        val up = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(epsRevision3mPct = 20.0), market
        ).components.revision
        assertEquals(0.0, down)
        assertEquals(100.0, up)
    }

    @Test
    fun `extreme inputs never produce component or total score outside zero to one hundred`() {
        val a = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(
                roicPct = 10000.0,
                roePct = -10000.0,
                operatingMarginPct = 5000.0,
                freeCashFlowPositiveYears = 100,
                debtToEquityPct = 10000.0,
                revenueGrowthPct = 10000.0,
                epsGrowthPct = -10000.0,
                operatingIncomeGrowthPct = 10000.0,
                forwardEpsGrowthPct = -10000.0,
                pe = 0.01,
                sectorPe = 10000.0,
                pb = 10000.0,
                evEbitda = 10000.0,
                fcfYieldPct = 10000.0,
                peg = 10000.0,
                return20dPct = 10000.0,
                return60dPct = -10000.0,
                return120dPct = 10000.0,
                relativeStrength60dPct = -10000.0,
                volumeRatio20d = 10000.0,
                distanceFrom52wHighPct = -10000.0,
                epsRevision3mPct = 10000.0,
                estimateUpDownRatio = 10000.0,
                sectorMacroFitScore = 10000.0
            ), market
        )
        val scores = listOfNotNull(
            a.components.quality, a.components.growth, a.components.value,
            a.components.momentum, a.components.revision, a.components.macroFit
        )
        assertTrue(scores.isNotEmpty())
        scores.forEach { assertTrue(it in 0.0..100.0) }
        assertTrue(a.totalScore in 0..100)
    }

    @Test
    fun `missing factor group remains null rather than becoming a bad score`() {
        val a = HybridStrategyEngine.assessStock(
            HybridStrategyEngine.StockInputs(return60dPct = 5.0), market
        )
        assertNotNull(a.components.momentum)
        assertEquals(null, a.components.quality)
        assertEquals(null, a.components.growth)
        assertEquals(null, a.components.value)
        assertEquals(null, a.components.revision)
    }
}
