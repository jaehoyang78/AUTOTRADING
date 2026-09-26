package autotrading.core

import kotlin.test.Test
import kotlin.test.assertTrue

class MarketRegimeComposerTest {
    @Test
    fun `macro overlay and credit stress affect composed regime`() {
        val index = PriceFeatureCalculator.IndexTrendFeatures(
            above20d = true,
            above60d = true,
            above200d = true,
            drawdownFrom52wHighPct = -4.0
        )
        val base = MarketRegimeComposer.assess(index)
        val favorable = MarketRegimeComposer.assess(
            index = index,
            macro = macro(score = 90, spreadStress = 10.0)
        )
        val adverse = MarketRegimeComposer.assess(
            index = index,
            macro = macro(score = 10, spreadStress = 90.0)
        )

        assertTrue(favorable.score >= base.score)
        assertTrue(adverse.score < base.score)
        assertTrue(favorable.score > adverse.score)
    }

    private fun macro(score: Int, spreadStress: Double) = MacroFeatureCalculator.MacroFeatures(
        policyRateChange6mPp = null,
        yieldCurve10y2yPp = null,
        inflationYoYPct = null,
        inflationTrend3mPp = null,
        liquidityChange13wPct = null,
        usdKrwChange3mPct = null,
        highYieldOasPct = null,
        macroTailwindScore = score,
        creditSpreadStress = spreadStress,
        dataCoveragePct = 100
    )
}
