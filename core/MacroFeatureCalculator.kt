package autotrading.core

import autotrading.data.MacroPoint
import autotrading.data.Market
import autotrading.data.fred.FredSeriesCatalog

/**
 * Converts normalized macro series into bounded strategy features.
 * All input lists must be oldest-to-newest. Missing series remain null and are
 * excluded from the composite instead of being treated as zero.
 */
object MacroFeatureCalculator {
    data class MacroFeatures(
        val policyRateChange6mPp: Double?,
        val yieldCurve10y2yPp: Double?,
        val inflationYoYPct: Double?,
        val inflationTrend3mPp: Double?,
        val liquidityChange13wPct: Double?,
        val usdKrwChange3mPct: Double?,
        val highYieldOasPct: Double?,
        val macroTailwindScore: Int?,
        val creditSpreadStress: Double?,
        val dataCoveragePct: Int
    )

    fun calculate(market: Market, series: Map<String, List<MacroPoint>>): MacroFeatures {
        val fedFunds = clean(series[FredSeriesCatalog.FED_FUNDS_EFFECTIVE])
        val treasury2y = clean(series[FredSeriesCatalog.TREASURY_2Y])
        val treasury10y = clean(series[FredSeriesCatalog.TREASURY_10Y])
        val cpi = clean(series[FredSeriesCatalog.CPI_ALL_URBAN_SA])
        val fedAssets = clean(series[FredSeriesCatalog.FED_TOTAL_ASSETS])
        val usdKrw = clean(series[FredSeriesCatalog.USD_KRW])
        val highYield = clean(series[FredSeriesCatalog.US_HIGH_YIELD_OAS])

        val policyChange = change(fedFunds, 126)
        val curve = if (treasury10y.isNotEmpty() && treasury2y.isNotEmpty()) {
            treasury10y.last().value - treasury2y.last().value
        } else null

        val inflationYoY = pctChange(cpi, 12)
        val inflationYoY3mAgo = if (cpi.size >= 16) {
            val currentIndex = cpi.lastIndex - 3
            val pastIndex = currentIndex - 12
            if (pastIndex >= 0) pct(cpi[pastIndex].value, cpi[currentIndex].value) else null
        } else null
        val inflationTrend = if (inflationYoY != null && inflationYoY3mAgo != null) {
            inflationYoY - inflationYoY3mAgo
        } else null

        val liquidityChange = pctChange(fedAssets, 13)
        val usdKrwChange = pctChange(usdKrw, 63)
        val highYieldOas = highYield.lastOrNull()?.value

        val weighted = mutableListOf<Pair<Double, Double>>()
        policyChange?.let { weighted += inverseScale(it, -2.0, 2.0) to 20.0 }
        curve?.let { weighted += scale(it, -1.5, 1.5) to 15.0 }
        inflationScore(inflationYoY, inflationTrend)?.let { weighted += it to 30.0 }
        liquidityChange?.let { weighted += scale(it, -5.0, 5.0) to 20.0 }
        if (market == Market.KR) {
            usdKrwChange?.let { weighted += inverseScale(it, -5.0, 10.0) to 15.0 }
        }

        val macroScore = if (weighted.isEmpty()) null else weightedAverage(weighted).toInt().coerceIn(0, 100)
        val spreadStress = highYieldOas?.let { scale(it, 2.0, 10.0) }

        val required = if (market == Market.KR) 7 else 6
        val available = listOfNotNull(
            policyChange,
            curve,
            inflationYoY,
            liquidityChange,
            highYieldOas,
            if (market == Market.KR) usdKrwChange else null
        ).size
        val denominator = if (market == Market.KR) 6 else 5
        val coverage = if (required == 0) 0 else (available * 100 / denominator).coerceIn(0, 100)

        return MacroFeatures(
            policyRateChange6mPp = policyChange,
            yieldCurve10y2yPp = curve,
            inflationYoYPct = inflationYoY,
            inflationTrend3mPp = inflationTrend,
            liquidityChange13wPct = liquidityChange,
            usdKrwChange3mPct = usdKrwChange,
            highYieldOasPct = highYieldOas,
            macroTailwindScore = macroScore,
            creditSpreadStress = spreadStress,
            dataCoveragePct = coverage
        )
    }

    private fun clean(points: List<MacroPoint>?): List<MacroPoint> =
        points.orEmpty().filter { it.value.isFinite() }.sortedBy { it.date }

    private fun change(points: List<MacroPoint>, periodsBack: Int): Double? {
        if (points.size <= periodsBack) return null
        return points.last().value - points[points.lastIndex - periodsBack].value
    }

    private fun pctChange(points: List<MacroPoint>, periodsBack: Int): Double? {
        if (points.size <= periodsBack) return null
        return pct(points[points.lastIndex - periodsBack].value, points.last().value)
    }

    private fun pct(old: Double, new: Double): Double? =
        if (!old.isFinite() || !new.isFinite() || old == 0.0) null else (new / old - 1.0) * 100.0

    private fun inflationScore(yoy: Double?, trend: Double?): Double? {
        if (yoy == null) return null
        var score = when {
            yoy < 0.0 -> 25.0
            yoy < 1.0 -> 45.0
            yoy <= 3.0 -> 80.0
            yoy <= 4.0 -> 60.0
            yoy <= 6.0 -> 35.0
            else -> 15.0
        }
        if (trend != null) {
            score += when {
                trend <= -1.0 -> 15.0
                trend <= -0.3 -> 8.0
                trend >= 1.0 -> -15.0
                trend >= 0.3 -> -8.0
                else -> 0.0
            }
        }
        return score.coerceIn(0.0, 100.0)
    }

    private fun weightedAverage(values: List<Pair<Double, Double>>): Double {
        val totalWeight = values.sumOf { it.second }
        return values.sumOf { it.first * it.second } / totalWeight
    }

    private fun scale(value: Double, low: Double, high: Double): Double {
        if (high <= low) return 50.0
        return ((value - low) / (high - low) * 100.0).coerceIn(0.0, 100.0)
    }

    private fun inverseScale(value: Double, best: Double, worst: Double): Double =
        100.0 - scale(value, best, worst)
}
