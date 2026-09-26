package autotrading.core

import autotrading.data.MacroPoint
import autotrading.data.fred.FredSeriesCatalog
import java.time.LocalDate
import java.time.YearMonth

/**
 * Converts normalized macro observations into interpretable regime features.
 * This layer deliberately separates data calculation from investment decisions.
 */
object MacroFeatureCalculator {

    enum class Trend { RISING, FALLING, STABLE, UNKNOWN }

    enum class MacroQuadrant {
        GROWTH_UP_INFLATION_DOWN,
        GROWTH_UP_INFLATION_UP,
        GROWTH_DOWN_INFLATION_DOWN,
        GROWTH_DOWN_INFLATION_UP,
        UNKNOWN
    }

    data class MacroFeatures(
        val industrialProductionYoYPct: Double?,
        val industrialProductionTrend: Trend,
        val cpiYoYPct: Double?,
        val inflationTrend: Trend,
        val fedFundsPct: Double?,
        val fedFunds3mChangePctPoints: Double?,
        val yieldCurve10y2yPctPoints: Double?,
        val fedBalanceSheet13wChangePct: Double?,
        val usdKrw3mChangePct: Double?,
        val highYieldOasPct: Double?,
        val creditSpreadStress: Double?,
        val quadrant: MacroQuadrant
    )

    fun calculate(series: Map<String, List<MacroPoint>>): MacroFeatures {
        val industrial = sorted(series[FredSeriesCatalog.INDUSTRIAL_PRODUCTION])
        val cpi = sorted(series[FredSeriesCatalog.CPI_ALL_URBAN_SA])
        val fedFunds = sorted(series[FredSeriesCatalog.FED_FUNDS_EFFECTIVE])
        val treasury2 = sorted(series[FredSeriesCatalog.TREASURY_2Y])
        val treasury10 = sorted(series[FredSeriesCatalog.TREASURY_10Y])
        val balanceSheet = sorted(series[FredSeriesCatalog.FED_TOTAL_ASSETS])
        val usdKrw = sorted(series[FredSeriesCatalog.USD_KRW])
        val highYield = sorted(series[FredSeriesCatalog.US_HIGH_YIELD_OAS])

        val ipYoy = yoyLatest(industrial)
        val ipPriorYoy = yoyForMonthOffset(industrial, 3)
        val growthTrend = trend(ipYoy, ipPriorYoy, deadband = 0.15)

        val cpiYoy = yoyLatest(cpi)
        val cpiPriorYoy = yoyForMonthOffset(cpi, 3)
        val inflationTrend = trend(cpiYoy, cpiPriorYoy, deadband = 0.10)

        val fedFundsNow = latestValue(fedFunds)
        val fedFunds3m = changeFromDaysAgo(fedFunds, 90, percentage = false)
        val yieldCurve = pairLatest(treasury10, treasury2)?.let { (ten, two) -> ten - two }
        val liquidity13w = changeFromDaysAgo(balanceSheet, 91, percentage = true)
        val fx3m = changeFromDaysAgo(usdKrw, 90, percentage = true)
        val oas = latestValue(highYield)
        val stress = oas?.let(::oasToStress)

        val quadrant = when {
            growthTrend == Trend.UNKNOWN || inflationTrend == Trend.UNKNOWN -> MacroQuadrant.UNKNOWN
            growthTrend == Trend.RISING && inflationTrend != Trend.RISING -> MacroQuadrant.GROWTH_UP_INFLATION_DOWN
            growthTrend == Trend.RISING && inflationTrend == Trend.RISING -> MacroQuadrant.GROWTH_UP_INFLATION_UP
            growthTrend != Trend.RISING && inflationTrend != Trend.RISING -> MacroQuadrant.GROWTH_DOWN_INFLATION_DOWN
            else -> MacroQuadrant.GROWTH_DOWN_INFLATION_UP
        }

        return MacroFeatures(
            industrialProductionYoYPct = ipYoy,
            industrialProductionTrend = growthTrend,
            cpiYoYPct = cpiYoy,
            inflationTrend = inflationTrend,
            fedFundsPct = fedFundsNow,
            fedFunds3mChangePctPoints = fedFunds3m,
            yieldCurve10y2yPctPoints = yieldCurve,
            fedBalanceSheet13wChangePct = liquidity13w,
            usdKrw3mChangePct = fx3m,
            highYieldOasPct = oas,
            creditSpreadStress = stress,
            quadrant = quadrant
        )
    }

    /**
     * Converts HY OAS to a bounded 0..100 stress score.
     * 2.5% or lower = low stress; 8% or higher = extreme stress.
     * This is an explicit v1 heuristic and must be backtested before execution use.
     */
    fun oasToStress(oasPct: Double): Double =
        (((oasPct - 2.5) / (8.0 - 2.5)) * 100.0).coerceIn(0.0, 100.0)

    private fun sorted(points: List<MacroPoint>?): List<MacroPoint> =
        points.orEmpty().filter { it.value.isFinite() }.sortedBy { it.date }

    private fun latestValue(points: List<MacroPoint>): Double? = points.lastOrNull()?.value

    private fun pairLatest(a: List<MacroPoint>, b: List<MacroPoint>): Pair<Double, Double>? {
        if (a.isEmpty() || b.isEmpty()) return null
        return a.last().value to b.last().value
    }

    private fun yoyLatest(points: List<MacroPoint>): Double? {
        val latest = points.lastOrNull() ?: return null
        return yoyAt(points, YearMonth.from(latest.date))
    }

    private fun yoyForMonthOffset(points: List<MacroPoint>, monthsAgo: Long): Double? {
        val latest = points.lastOrNull() ?: return null
        return yoyAt(points, YearMonth.from(latest.date).minusMonths(monthsAgo))
    }

    private fun yoyAt(points: List<MacroPoint>, month: YearMonth): Double? {
        val current = points.lastOrNull { YearMonth.from(it.date) == month } ?: return null
        val previousMonth = month.minusYears(1)
        val previous = points.lastOrNull { YearMonth.from(it.date) == previousMonth } ?: return null
        if (previous.value == 0.0) return null
        return ((current.value / previous.value) - 1.0) * 100.0
    }

    private fun trend(current: Double?, previous: Double?, deadband: Double): Trend {
        if (current == null || previous == null) return Trend.UNKNOWN
        val delta = current - previous
        return when {
            delta > deadband -> Trend.RISING
            delta < -deadband -> Trend.FALLING
            else -> Trend.STABLE
        }
    }

    private fun changeFromDaysAgo(
        points: List<MacroPoint>,
        days: Long,
        percentage: Boolean
    ): Double? {
        val latest = points.lastOrNull() ?: return null
        val target = latest.date.minusDays(days)
        val prior = nearestAtOrBefore(points, target) ?: return null
        return if (percentage) {
            if (prior.value == 0.0) null else ((latest.value / prior.value) - 1.0) * 100.0
        } else {
            latest.value - prior.value
        }
    }

    private fun nearestAtOrBefore(points: List<MacroPoint>, date: LocalDate): MacroPoint? =
        points.lastOrNull { !it.date.isAfter(date) }
}
