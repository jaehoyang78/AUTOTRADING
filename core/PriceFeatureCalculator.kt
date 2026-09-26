package autotrading.core

import autotrading.data.DailyBar
import autotrading.data.IndexBar

/**
 * Converts provider-agnostic daily price history into strategy inputs.
 * Input bars must be ordered oldest -> newest.
 * 52-week distance/drawdown currently uses closing prices (252 trading sessions).
 */
object PriceFeatureCalculator {

    data class StockPriceFeatures(
        val return20dPct: Double?,
        val return60dPct: Double?,
        val return120dPct: Double?,
        val volumeRatio20d: Double?,
        val distanceFrom52wHighPct: Double?
    )

    data class IndexTrendFeatures(
        val above20d: Boolean?,
        val above60d: Boolean?,
        val above200d: Boolean?,
        val drawdownFrom52wHighPct: Double?
    )

    fun stockFeatures(bars: List<DailyBar>): StockPriceFeatures {
        validateDates(bars.map { it.date.toEpochDay() })
        val closes = bars.map { it.close }
        return StockPriceFeatures(
            return20dPct = periodReturn(closes, 20),
            return60dPct = periodReturn(closes, 60),
            return120dPct = periodReturn(closes, 120),
            volumeRatio20d = volumeRatio(bars, 20),
            distanceFrom52wHighPct = distanceFromHigh(closes, 252)
        )
    }

    fun indexFeatures(bars: List<IndexBar>): IndexTrendFeatures {
        validateDates(bars.map { it.date.toEpochDay() })
        val closes = bars.map { it.close }
        val last = closes.lastOrNull()
        return IndexTrendFeatures(
            above20d = last?.let { sma(closes, 20)?.let { avg -> it > avg } },
            above60d = last?.let { sma(closes, 60)?.let { avg -> it > avg } },
            above200d = last?.let { sma(closes, 200)?.let { avg -> it > avg } },
            drawdownFrom52wHighPct = distanceFromHigh(closes, 252)
        )
    }

    fun periodReturn(closes: List<Double>, sessions: Int): Double? {
        require(sessions > 0)
        if (closes.size <= sessions) return null
        val current = closes.last()
        val previous = closes[closes.lastIndex - sessions]
        if (!current.isFinite() || !previous.isFinite() || previous <= 0.0) return null
        return ((current / previous) - 1.0) * 100.0
    }

    fun sma(closes: List<Double>, sessions: Int): Double? {
        require(sessions > 0)
        if (closes.size < sessions) return null
        val window = closes.takeLast(sessions)
        if (window.any { !it.isFinite() || it <= 0.0 }) return null
        return window.average()
    }

    private fun distanceFromHigh(closes: List<Double>, sessions: Int): Double? {
        if (closes.isEmpty()) return null
        val window = closes.takeLast(minOf(sessions, closes.size)).filter { it.isFinite() && it > 0.0 }
        if (window.isEmpty()) return null
        val current = closes.last()
        if (!current.isFinite() || current <= 0.0) return null
        val high = window.maxOrNull() ?: return null
        return ((current / high) - 1.0) * 100.0
    }

    private fun volumeRatio(bars: List<DailyBar>, sessions: Int): Double? {
        if (bars.size < sessions + 1) return null
        val current = bars.last().volume ?: return null
        val prior = bars.dropLast(1).takeLast(sessions).mapNotNull { it.volume }
        if (prior.size < sessions || prior.any { it < 0L }) return null
        val avg = prior.average()
        if (avg <= 0.0) return null
        return current / avg
    }

    private fun validateDates(epochDays: List<Long>) {
        require(epochDays.zipWithNext().all { (a, b) -> a < b }) {
            "daily bars must be strictly ordered oldest to newest without duplicate dates"
        }
    }
}
