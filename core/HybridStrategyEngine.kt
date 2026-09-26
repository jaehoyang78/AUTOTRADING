package autotrading.core

/**
 * AUTOTRADING hybrid investment strategy engine v2.
 *
 * Design goals:
 * - separate market regime, stock scoring and position sizing
 * - do not treat missing data as bad data
 * - expose data coverage so low-information scores are obvious
 * - explain which investor styles a stock resembles
 * - translate analysis into a bounded action recommendation
 */
object HybridStrategyEngine {

    enum class MarketRegime { RISK_ON_STRONG, RISK_ON_NORMAL, NEUTRAL, RISK_OFF, PANIC }
    enum class DecisionAction { BUY, ADD, HOLD, REDUCE, SELL, AVOID, WATCH }
    enum class ThesisState { STRONG, INTACT, WEAKENING, BROKEN, UNKNOWN }

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
        val summary: String,
        val dataCoveragePct: Int
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

    data class DataCoverage(
        val availableFields: Int,
        val totalFields: Int,
        val percentage: Int,
        val criticalGroupsAvailable: Int,
        val totalCriticalGroups: Int,
        val sufficientForRecommendation: Boolean
    )

    data class InvestorFit(
        val buffett: Int,
        val graham: Int,
        val lynch: Int,
        val sorosMomentum: Int,
        val druckenmiller: Int
    )

    data class StockAssessment(
        val totalScore: Int,
        val grade: String,
        val components: ComponentScores,
        val coverage: DataCoverage,
        val investorFit: InvestorFit,
        val basePositionPct: Double,
        val recommendedPositionPct: Double,
        val action: String,
        val reasons: List<String>
    )

    data class DecisionInputs(
        val assessment: StockAssessment,
        val thesisState: ThesisState = ThesisState.UNKNOWN,
        val currentPositionPct: Double = 0.0,
        val currentSectorPositionPct: Double = 0.0,
        val portfolioDrawdownPct: Double = 0.0
    )

    data class Decision(
        val action: DecisionAction,
        val targetPositionPct: Double,
        val rationale: List<String>
    )

    fun assessMarket(i: MarketInputs): MarketAssessment {
        val weighted = mutableListOf<Pair<Double, Double>>()
        var available = 0
        fun add(value: Double?, weight: Double) {
            if (value != null) {
                available++
                weighted += value.coerceIn(0.0, 100.0) to weight
            }
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
        return MarketAssessment(score, regime, riskMultiplier, summary, available * 100 / 8)
    }

    fun assessStock(i: StockInputs, market: MarketAssessment): StockAssessment {
        val components = ComponentScores(
            quality = qualityScore(i),
            growth = growthScore(i),
            value = valueScore(i),
            momentum = momentumScore(i),
            revision = revisionScore(i),
            macroFit = i.sectorMacroFitScore?.coerceIn(0.0, 100.0)
        )
        val weighted = mutableListOf<Pair<Double, Double>>()
        components.quality?.let { weighted += it to 25.0 }
        components.growth?.let { weighted += it to 20.0 }
        components.value?.let { weighted += it to 20.0 }
        components.momentum?.let { weighted += it to 15.0 }
        components.revision?.let { weighted += it to 10.0 }
        components.macroFit?.let { weighted += it to 10.0 }

        var total = weightedAverage(weighted)
        if (i.epsGrowthPct != null && i.epsGrowthPct < -20.0) total -= 8.0
        if (i.epsRevision3mPct != null && i.epsRevision3mPct < -15.0) total -= 8.0
        if (i.debtToEquityPct != null && i.debtToEquityPct > 250.0) total -= 6.0
        total = total.coerceIn(0.0, 100.0)

        val coverage = dataCoverage(i, components)
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
        if (market.regime == MarketRegime.PANIC && (components.quality ?: 0.0) < 75.0) recommended = 0.0
        if (!coverage.sufficientForRecommendation) recommended = 0.0
        recommended = floor1(recommended)

        val fit = investorFit(components)
        val action = when {
            !coverage.sufficientForRecommendation -> "데이터 보강"
            totalInt < 60 -> "제외"
            totalInt < 70 -> "관찰"
            totalInt < 80 -> "관심종목"
            recommended <= 0.0 -> "대기"
            else -> "매수 검토"
        }
        return StockAssessment(
            totalInt, grade, components, coverage, fit, base, recommended, action,
            buildReasons(i, components, market, coverage)
        )
    }

    fun decide(i: DecisionInputs): Decision {
        val a = i.assessment
        val rationale = mutableListOf<String>()
        if (i.thesisState == ThesisState.BROKEN) {
            return Decision(DecisionAction.SELL, 0.0, listOf("투자논리 훼손"))
        }
        if (!a.coverage.sufficientForRecommendation) {
            return Decision(DecisionAction.WATCH, i.currentPositionPct, listOf("데이터 충족률 부족"))
        }
        var target = a.recommendedPositionPct
        if (i.currentSectorPositionPct >= 25.0) {
            target = minOf(target, i.currentPositionPct)
            rationale += "섹터 집중도 25% 이상"
        }
        if (i.portfolioDrawdownPct <= -15.0) {
            target = minOf(target, i.currentPositionPct)
            rationale += "포트폴리오 낙폭 -15% 이하: 신규 위험 확대 제한"
        }
        if (i.thesisState == ThesisState.WEAKENING) {
            target = minOf(target, i.currentPositionPct * 0.5)
            rationale += "투자논리 약화"
        }
        target = floor1(target.coerceIn(0.0, 7.0))

        val action = when {
            a.totalScore < 60 && i.currentPositionPct > 0.0 -> DecisionAction.SELL
            a.totalScore < 60 -> DecisionAction.AVOID
            target == 0.0 && i.currentPositionPct == 0.0 -> DecisionAction.WATCH
            target == 0.0 && i.currentPositionPct > 0.0 -> DecisionAction.REDUCE
            i.currentPositionPct == 0.0 && target > 0.0 -> DecisionAction.BUY
            target > i.currentPositionPct + 0.3 -> DecisionAction.ADD
            target < i.currentPositionPct - 0.3 -> DecisionAction.REDUCE
            else -> DecisionAction.HOLD
        }
        if (rationale.isEmpty()) rationale += "점수·시장국면·현재비중 기준"
        return Decision(action, target, rationale)
    }

    private fun dataCoverage(i: StockInputs, c: ComponentScores): DataCoverage {
        val fields = listOf<Any?>(
            i.roicPct, i.roePct, i.operatingMarginPct, i.freeCashFlowPositiveYears, i.debtToEquityPct,
            i.revenueGrowthPct, i.epsGrowthPct, i.operatingIncomeGrowthPct, i.forwardEpsGrowthPct,
            i.pe, i.sectorPe, i.pb, i.evEbitda, i.fcfYieldPct, i.peg,
            i.return20dPct, i.return60dPct, i.return120dPct, i.relativeStrength60dPct, i.volumeRatio20d,
            i.distanceFrom52wHighPct, i.epsRevision3mPct, i.estimateUpDownRatio, i.sectorMacroFitScore
        )
        val groups = listOf(c.quality, c.growth, c.value, c.momentum, c.revision, c.macroFit)
        val available = fields.count { it != null }
        val groupCount = groups.count { it != null }
        val pct = available * 100 / fields.size
        return DataCoverage(
            availableFields = available,
            totalFields = fields.size,
            percentage = pct,
            criticalGroupsAvailable = groupCount,
            totalCriticalGroups = groups.size,
            sufficientForRecommendation = pct >= 50 && groupCount >= 4 && c.quality != null && c.momentum != null
        )
    }

    private fun investorFit(c: ComponentScores): InvestorFit {
        fun avg(vararg v: Double?): Int {
            val values = v.filterNotNull()
            return if (values.isEmpty()) 0 else values.average().toInt().coerceIn(0, 100)
        }
        return InvestorFit(
            buffett = avg(c.quality, c.quality, c.growth),
            graham = avg(c.value, c.value, c.quality),
            lynch = avg(c.growth, c.value, c.revision),
            sorosMomentum = avg(c.momentum, c.momentum, c.revision),
            druckenmiller = avg(c.momentum, c.growth, c.revision, c.macroFit)
        )
    }

    private fun qualityScore(i: StockInputs): Double? {
        val p = mutableListOf<Double>()
        i.roicPct?.let { p += scale(it, 0.0, 20.0) }
        i.roePct?.let { p += scale(it, 0.0, 25.0) }
        i.operatingMarginPct?.let { p += scale(it, 0.0, 25.0) }
        i.freeCashFlowPositiveYears?.let { p += scale(it.toDouble(), 0.0, 5.0) }
        i.debtToEquityPct?.let { p += inverseScale(it, 30.0, 250.0) }
        return averageOrNull(p)
    }

    private fun growthScore(i: StockInputs): Double? {
        val p = mutableListOf<Double>()
        i.revenueGrowthPct?.let { p += scale(it, -10.0, 30.0) }
        i.epsGrowthPct?.let { p += scale(it, -15.0, 35.0) }
        i.operatingIncomeGrowthPct?.let { p += scale(it, -15.0, 35.0) }
        i.forwardEpsGrowthPct?.let { p += scale(it, -10.0, 30.0) }
        return averageOrNull(p)
    }

    private fun valueScore(i: StockInputs): Double? {
        val p = mutableListOf<Double>()
        if (i.pe != null && i.sectorPe != null && i.pe > 0 && i.sectorPe > 0) p += inverseScale(i.pe / i.sectorPe, 0.6, 1.6)
        i.pb?.let { if (it > 0) p += inverseScale(it, 0.7, 5.0) }
        i.evEbitda?.let { if (it > 0) p += inverseScale(it, 5.0, 25.0) }
        i.fcfYieldPct?.let { p += scale(it, 0.0, 10.0) }
        i.peg?.let { if (it > 0) p += inverseScale(it, 0.5, 2.5) }
        return averageOrNull(p)
    }

    private fun momentumScore(i: StockInputs): Double? {
        val p = mutableListOf<Double>()
        i.return20dPct?.let { p += scale(it, -15.0, 20.0) }
        i.return60dPct?.let { p += scale(it, -20.0, 35.0) }
        i.return120dPct?.let { p += scale(it, -25.0, 50.0) }
        i.relativeStrength60dPct?.let { p += scale(it, -15.0, 20.0) }
        i.volumeRatio20d?.let { p += scale(it, 0.5, 2.0) }
        i.distanceFrom52wHighPct?.let { p += scale(it, -40.0, 0.0) }
        return averageOrNull(p)
    }

    private fun revisionScore(i: StockInputs): Double? {
        val p = mutableListOf<Double>()
        i.epsRevision3mPct?.let { p += scale(it, -20.0, 20.0) }
        i.estimateUpDownRatio?.let { p += scale(it, 0.5, 2.0) }
        return averageOrNull(p)
    }

    private fun buildReasons(i: StockInputs, c: ComponentScores, market: MarketAssessment, coverage: DataCoverage): List<String> {
        val r = mutableListOf<String>()
        fun hi(name: String, score: Double?) { if (score != null && score >= 75) r += "$name 강점" }
        fun lo(name: String, score: Double?) { if (score != null && score < 40) r += "$name 약점" }
        hi("퀄리티", c.quality); hi("성장", c.growth); hi("가치", c.value); hi("모멘텀", c.momentum); hi("실적추정", c.revision)
        lo("퀄리티", c.quality); lo("성장", c.growth); lo("가치", c.value); lo("모멘텀", c.momentum); lo("실적추정", c.revision)
        if ((i.epsRevision3mPct ?: 0.0) < -15.0) r += "EPS 추정치 급격한 하향"
        if ((i.debtToEquityPct ?: 0.0) > 250.0) r += "과도한 부채"
        if (!coverage.sufficientForRecommendation) r += "데이터 충족률 ${coverage.percentage}% · 자동추천 보류"
        r += "시장 ${market.score}점 · ${market.summary}"
        return r.take(6)
    }

    private fun weightedAverage(v: List<Pair<Double, Double>>): Double {
        if (v.isEmpty()) return 50.0
        val w = v.sumOf { it.second }
        return v.sumOf { it.first * it.second } / w
    }
    private fun averageOrNull(v: List<Double>): Double? = if (v.isEmpty()) null else v.average().coerceIn(0.0, 100.0)
    private fun scale(value: Double, low: Double, high: Double): Double = if (high <= low) 50.0 else ((value - low) / (high - low) * 100.0).coerceIn(0.0, 100.0)
    private fun inverseScale(value: Double, best: Double, worst: Double): Double = if (worst <= best) 50.0 else (100.0 - ((value - best) / (worst - best) * 100.0)).coerceIn(0.0, 100.0)
    private fun floor1(value: Double): Double = kotlin.math.floor(value * 10.0) / 10.0
}
