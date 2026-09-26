package autotrading.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MarketInputFactoryTest {

    @Test
    fun `missing supplementary data remains missing`() {
        val inputs = MarketInputFactory.fromIndexFeatures(
            PriceFeatureCalculator.IndexTrendFeatures(
                above20d = true,
                above60d = true,
                above200d = null,
                drawdownFrom52wHighPct = -7.0
            )
        )
        assertEquals(true, inputs.indexAbove20d)
        assertEquals(true, inputs.indexAbove60d)
        assertNull(inputs.indexAbove200d)
        assertNull(inputs.vix)
        assertNull(inputs.breadthPct)
    }

    @Test
    fun `supplementary percentages are bounded`() {
        val inputs = MarketInputFactory.fromIndexFeatures(
            PriceFeatureCalculator.IndexTrendFeatures(true, true, true, -2.0),
            MarketInputFactory.SupplementaryMarketData(
                breadthPct = 130.0,
                volatilityIndexClose = 18.0,
                earningsRevisionBreadthPct = -10.0,
                creditSpreadStress = 140.0
            )
        )
        assertEquals(100.0, inputs.breadthPct)
        assertEquals(18.0, inputs.vix)
        assertEquals(0.0, inputs.earningsRevisionBreadthPct)
        assertEquals(100.0, inputs.creditSpreadStress)
    }
}
