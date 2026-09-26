package autotrading.core.data

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

object MarketDataValidation {
    data class ValidationResult(
        val valid: Boolean,
        val stale: Boolean,
        val errors: List<String>
    )

    fun validateQuote(
        quote: Quote,
        now: Instant,
        staleAfter: Duration = Duration.ofMinutes(5),
        futureTolerance: Duration = Duration.ofSeconds(30)
    ): ValidationResult {
        val errors = mutableListOf<String>()
        if (quote.symbol.isBlank()) errors += "symbol is blank"
        if (!quote.price.isFinite() || quote.price <= 0.0) errors += "price must be positive and finite"
        if (quote.volume != null && quote.volume < 0L) errors += "volume cannot be negative"
        if (quote.source.isBlank()) errors += "source is blank"
        if (quote.observedAt.isAfter(now.plus(futureTolerance))) errors += "observation is in the future"
        val stale = Duration.between(quote.observedAt, now) > staleAfter
        return ValidationResult(errors.isEmpty(), stale, errors)
    }

    fun validateBars(
        bars: List<DailyBar>,
        today: LocalDate,
        expectedSymbol: String? = null
    ): ValidationResult {
        val errors = mutableListOf<String>()
        if (bars.any { it.symbol.isBlank() }) errors += "bar symbol is blank"
        if (expectedSymbol != null && bars.any { it.symbol != expectedSymbol }) errors += "mixed/unexpected symbols"
        if (bars.any { !it.close.isFinite() || it.close <= 0.0 }) errors += "close must be positive and finite"
        if (bars.any { it.volume != null && it.volume < 0L }) errors += "volume cannot be negative"
        if (bars.any { it.date.isAfter(today) }) errors += "future daily bar"
        if (bars.zipWithNext().any { (a, b) -> a.date >= b.date }) errors += "daily bars must be strictly oldest-to-newest"
        if (bars.any { it.source.isBlank() }) errors += "bar source is blank"
        return ValidationResult(errors.isEmpty(), stale = false, errors = errors)
    }
}
