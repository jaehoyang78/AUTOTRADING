package autotrading.core

/**
 * Applies a bounded macro overlay to the base market regime assessment.
 * The base engine remains pure and stable; macro can be evolved independently.
 */
object MacroRegimeOverlay {
    private const val MACRO_WEIGHT = 0.15

    fun apply(
        base: HybridStrategyEngine.MarketAssessment,
        macro: MacroFeatureCalculator.MacroFeatures
    ): HybridStrategyEngine.MarketAssessment {
        val macroScore = macro.macroTailwindScore ?: return base
        val adjustedScore = (
            base.score * (1.0 - MACRO_WEIGHT) + macroScore * MACRO_WEIGHT
        ).toInt().coerceIn(0, 100)

        val regime = when {
            adjustedScore >= 80 -> HybridStrategyEngine.MarketRegime.RISK_ON_STRONG
            adjustedScore >= 65 -> HybridStrategyEngine.MarketRegime.RISK_ON_NORMAL
            adjustedScore >= 40 -> HybridStrategyEngine.MarketRegime.NEUTRAL
            adjustedScore >= 20 -> HybridStrategyEngine.MarketRegime.RISK_OFF
            else -> HybridStrategyEngine.MarketRegime.PANIC
        }
        val multiplier = when (regime) {
            HybridStrategyEngine.MarketRegime.RISK_ON_STRONG -> 1.00
            HybridStrategyEngine.MarketRegime.RISK_ON_NORMAL -> 0.90
            HybridStrategyEngine.MarketRegime.NEUTRAL -> 0.70
            HybridStrategyEngine.MarketRegime.RISK_OFF -> 0.55
            HybridStrategyEngine.MarketRegime.PANIC -> 0.65
        }
        val regimeSummary = when (regime) {
            HybridStrategyEngine.MarketRegime.RISK_ON_STRONG -> "강한 Risk-On · 성장/모멘텀 비중 확대"
            HybridStrategyEngine.MarketRegime.RISK_ON_NORMAL -> "정상 상승 · 퀄리티+성장 중심"
            HybridStrategyEngine.MarketRegime.NEUTRAL -> "중립/횡보 · 가치+퀄리티 우선"
            HybridStrategyEngine.MarketRegime.RISK_OFF -> "Risk-Off · 신규매수 축소, 현금 비중 확대"
            HybridStrategyEngine.MarketRegime.PANIC -> "패닉 · 우량주만 분할매수, 저품질주는 회피"
        }
        return HybridStrategyEngine.MarketAssessment(
            score = adjustedScore,
            regime = regime,
            riskMultiplier = multiplier,
            summary = "$regimeSummary · Macro ${macroScore}점",
            dataCoveragePct = base.dataCoveragePct
        )
    }
}
