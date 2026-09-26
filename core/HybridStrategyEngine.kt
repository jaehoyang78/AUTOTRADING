package autotrading.core

/**
 * Hybrid investment strategy engine v1.
 *
 * Separates market regime, stock scoring and position sizing.
 * Missing inputs are re-normalized across available metrics rather than
 * automatically treated as zero. Data coverage will be added explicitly.
 */
object HybridStrategyEngine {

    enum class MarketRegime {
        RISK_ON_STRONG,
        RISK_ON_NORMAL,
        NEUTRAL,
        RISK_OFF,
        PANIC
    }

    data class MarketInputs(
        val indexAbove20d: Boolean? = null,
        val indexAbove60d: Boolean? = null,
        val indexAbove200d: Boolean? = null,
        val drawdownFrom52wHighPct: Double? = null,
        val breadthPct: Double? = null,
        val vix: Double? = null,
        val earningsRevisionBreadthPct: Double? = null,
        val creditSpreadStress: Double? = null
    )

    data class MarketAssessment(
        val score: Int,
        val regime: MarketRegime,
        val riskMultiplier: Double,
        val summary: String
    )

    data class StockInputs(
        val roicPct: Double? = null,
        val roePct: Double? = null,
        val operatingMarginPct: Double? = null,
        val freeCashFlowPositiveYears: Int? = null,
        val debtToEquityPct: Double? = null,
        val revenueGrowthPct: Double? = null,
        val epsGrowthPct: Double? = null,
        val operatingIncomeGrowthPct: Double? = null,
        val forwardEpsGrowthPct: Double? = null,
        val pe: Double? = null,
        val sectorPe: Double? = null,
        val pb: Double? = null,
        val evEbitda: Double? = null,
        val fcfYieldPct: Double? = null,
        val peg: Double? = null,
        val return20dPct: Double? = null,
        val return60dPct: Double? = null,
        val return120dPct: Double? = null,
        val relativeStrength60dPct: Double? = null,
        val volumeRatio20d: Double? = null,
        val distanceFrom52wHighPct: Double? = null,
        val epsRevision3mPct: Double? = null,
        val estimateUpDownRatio: Double? = null,
        val sectorMacroFitScore: Double? = null
    )

    data class ComponentScores(
        val quality: Double?,
        val growth: Double?,
        val value: Double?,
        val momentum: Double?,
        val revision: Double?,
        val macroFit: Double?
    )

    data class StockAssessment(
        val totalScore: Int,
        val grade: String,
        val components: ComponentScores,
        val basePositionPct: Double,
        val recommendedPositionPct: Double,
        val action: String,
        val reasons: List<String>
    )

    fun assessMarket(i: MarketInputs): MarketAssessment {
        val weighted = mutableListOf<Pair<Double, Double>>()
        fun add(value: Double?, weight: Double) {
            if (value != null) weighted += value.coerceIn(0.0, 100.0) to weight
        }

        add(i.indexAbove20d?.let { if (it) 75.0 else 25.0 }, 10.0)
        add(i.indexAbove60d?.let { if (it) 78.0 else 22.0 }, 15.0)
        add(i.indexAbove200d?.let { if (it) 82.0 else 18.0 }, 20.0)
        add(i.drawdownFrom52wHighPct?.let {
            when {
                it >= -5.0 -> 85.0
                it >= -10.0 -> 70.0
                it >= -20.0 -> 45.0
                it >= -30.0 -> 22.0
                else -> 8.0
            }
        }, 15.0)
        add(i.breadthPct?.coerceIn(0.0, 100.0), 15.0)
        add(i.vix?.let {
            when {
                it < 15.0 -> 82.0
                it < 20.0 -> 72.0
                it < 30.0 -> 50.0
                it < 40.0 -> 25.0
                else -> 8.0
            }
        }, 10.0)
        add(i.earningsRevisionBreadthPct?.coerceIn(0.0, 100.0), 10.0)
        add(i.creditSpreadStress?.let { (100.0 - it).coerceIn(0.0, 100.0) }, 5.0)

        val score = weightedAverage(weighted).toInt().coerceIn(0, 100)
        val regime = when {
            score >= 80 -> MarketRegime.RISK_ON_STRONG
            score >= 65 -> MarketRegime.RISK_ON_NORMAL
            score >= 40 -> MarketRegime.NEUTRAL
            score >= 20 -> MarketRegime.RISK_OFF
            else -> MarketRegime.PANIC
        }
        val riskMultiplier = when (regime) {
            MarketRegime.RISK_ON_STRONG -> 1.00
            MarketRegime.RISK_ON_NORMAL -> 0.90
            MarketRegime.NEUTRAL -> 0.70
            MarketRegime.RISK_OFF -> 0.55
            MarketRegime.PANIC -> 0.65
        }
        val summary = when (regime) {
            MarketRegime.RISK_ON_STRONG -> "강한 Risk-On · 성장/모멘텀 비중 확대"
            MarketRegime.RISK_ON_NORMAL -> "정상 상승 · 퀄리티+성장 중심"
            MarketRegime.NEUTRAL -> "중립/횡보 · 가치+퀄리티 우선"
            MarketRegime.RISK_OFF -> "Risk-Off · 신규매수 축소, 현금 비중 확대"
            MarketRegime.PANIC -> "패닉 · 우량주만 분할매수, 저품질주는 회피"
        }
        return MarketAssessment(score, regime, riskMultiplier, summary)
    }

    fun assessStock(i: StockInputs, market: MarketAssessment): StockAssessment {
        val quality = qualityScore(i)
        val growth = growthScore(i)
        val value = valueScore(i)
        val momentum = momentumScore(i)
        val revision = revisionScore(i)
        val macro = i.sectorMacroFitScore?.coerceIn(0.0, 100.0)

        val components = ComponentScores(quality, growth, value, momentum, revision, macro)
        val weighted = mutableListOf<Pair<Double, Double>>()
        quality?.let { weighted += it to 25.0 }
        growth?.let { weighted += it to 20.0 }
        value?.let { weighted += it to 20.0 }
        momentum?.let { weighted += it to 15.0 }
        revision?.let { weighted += it to 10.0 }
        macro?.let { weighted += it to 10.0 }

        var total = weightedAverage(weighted)
        if (i.epsGrowthPct != null && i.epsGrowthPct < -20.0) total -= 8.0
        if (i.epsRevision3mPct != null && i.epsRevision3mPct < -15.0) total -= 8.0
        if (i.debtToEquityPct != null && i.debtToEquityPct > 250.0) total -= 6.0
        total = total.coerceIn(0.0, 100.0)

        val totalInt = total.toInt()
        val grade = when {
            totalInt >= 90 -> "S"
            totalInt >= 85 -> "A+"
            totalInt >= 80 -> "A"
            totalInt >= 75 -> "B+"
            totalInt >= 70 -> "B"
            totalInt >= 60 -> "C"
            else -> "D"
        }
        val base = when {
            totalInt >= 90 -> 7.0
            totalInt >= 85 -> 5.0
            totalInt >= 80 -> 3.0
            totalInt >= 75 -> 2.0
            totalInt >= 70 -> 1.0
            else -> 0.0
        }
        var recommended = base * market.riskMultiplier
        if (market.regime == MarketRegime.PANIC && (quality ?: 0.0) < 75.0) recommended = 0.0
        recommended = (recommended * 10.0).toInt() / 10.0

        val action = when {
            totalInt < 60 -> "제외"
            totalInt < 70 -> "관찰"
            totalInt < 80 -> "관심종목"
            recommended <= 0.0 -> "대기"
            else -> "매수 검토"
        }

        return StockAssessment(
            totalScore = totalInt,
            grade = grade,
            components = components,
            basePositionPct = base,
            recommendedPositionPct = recommended,
            action = action,
            reasons = buildReasons(i, components, market)
        )
    }

    private fun qualityScore(i: StockInputs): Double? {
        val parts = mutableListOf<Double>()
        i.roicPct?.let { parts += scale(it, 0.0, 20.0) }
        i.roePct?.let { parts += scale(it, 0.0, 25.0) }
        i.operatingMarginPct?.let { parts += scale(it, 0.0, 25.0) }
        i.freeCashFlowPositiveYears?.let { parts += scale(it.toDouble(), 0.0, 5.0) }
        i.debtToEquityPct?.let { parts += inverseScale(it, 30.0, 250.0) }
        return averageOrNull(parts)
    }

    private fun growthScore(i: StockInputs): Double? {
        val parts = mutableListOf<Double>()
        i.revenueGrowthPct?.let { parts += scale(it, -10.0, 30.0) }
        i.epsGrowthPct?.let { parts += scale(it, -15.0, 35.0) }
        i.operatingIncomeGrowthPct?.let { parts += scale(it, -15.0, 35.0) }
        i.forwardEpsGrowthPct?.let { parts += scale(it, -10.0, 30.0) }
        return averageOrNull(parts)
    }

    private fun valueScore(i: StockInputs): Double? {
        val parts = mutableListOf<Double>()
        if (i.pe != null && i.sectorPe != null && i.pe > 0.0 && i.sectorPe > 0.0) {
            parts += inverseScale(i.pe / i.sectorPe, 0.6, 1.6)
        }
        i.pb?.let { if (it > 0) parts += inverseScale(it, 0.7, 5.0) }
        i.evEbitda?.let { if (it > 0) parts += inverseScale(it, 5.0, 25.0) }
        i.fcfYieldPct?.let { parts += scale(it, 0.0, 10.0) }
        i.peg?.let { if (it > 0) parts += inverseScale(it, 0.5, 2.5) }
        return averageOrNull(parts)
    }

    private fun momentumScore(i: StockInputs): Double? {
        val parts = mutableListOf<Double>()
        i.return20dPct?.let { parts += scale(it, -15.0, 20.0) }
        i.return60dPct?.let { parts += scale(it, -20.0, 35.0) }
        i.return120dPct?.let { parts += scale(it, -25.0, 50.0) }
        i.relativeStrength60dPct?.let { parts += scale(it, -15.0, 20.0) }
        i.volumeRatio20d?.let { parts += scale(it, 0.5, 2.0) }
        i.distanceFrom52wHighPct?.let { parts += scale(it, -40.0, 0.0) }
        return averageOrNull(parts)
    }

    private fun revisionScore(i: StockInputs): Double? {
        val parts = mutableListOf<Double>()
        i.epsRevision3mPct?.let { parts += scale(it, -20.0, 20.0) }
        i.estimateUpDownRatio?.let { parts += scale(it, 0.5, 2.0) }
        return averageOrNull(parts)
    }

    private fun buildReasons(i: StockInputs, c: ComponentScores, market: MarketAssessment): List<String> {
        val reasons = mutableListOf<String>()
        fun hi(name: String, score: Double?) { if (score != null && score >= 75) reasons += "$name 강점" }
        fun lo(name: String, score: Double?) { if (score != null && score < 40) reasons += "$name 약점" }
        hi("퀄리티", c.quality); hi("성장", c.growth); hi("가치", c.value)
        hi("모멘텀", c.momentum); hi("실적추정", c.revision); hi("매크로 적합도", c.macroFit)
        lo("퀄리티", c.quality); lo("성장", c.growth); lo("가치", c.value)
        lo("모멘텀", c.momentum); lo("실적추정", c.revision)
        if ((i.epsRevision3mPct ?: 0.0) < -15.0) reasons += "EPS 추정치 급격한 하향"
        if ((i.debtToEquityPct ?: 0.0) > 250.0) reasons += "과도한 부채"
        reasons += "시장 ${market.score}점 · ${market.summary}"
        return reasons.take(5)
    }

    private fun weightedAverage(values: List<Pair<Double, Double>>): Double {
        if (values.isEmpty()) return 50.0
        val weight = values.sumOf { it.second }
        return values.sumOf { it.first * it.second } / weight
    }

    private fun averageOrNull(values: List<Double>): Double? =
        if (values.isEmpty()) null else values.average().coerceIn(0.0, 100.0)

    private fun scale(value: Double, low: Double, high: Double): Double {
        if (high <= low) return 50.0
        return ((value - low) / (high - low) * 100.0).coerceIn(0.0, 100.0)
    }

    private fun inverseScale(value: Double, best: Double, worst: Double): Double {
        if (worst <= best) return 50.0
        return (100.0 - ((value - best) / (worst - best) * 100.0)).coerceIn(0.0, 100.0)
    }
}
