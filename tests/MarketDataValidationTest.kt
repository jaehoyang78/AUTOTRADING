package autotrading.core.data

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarketDataValidationTest {
    @Test
    fun `fresh positive quote is valid`() {
        val now = Instant.parse("2026-09-26T12:00:00Z")
        val quote = Quote("005930", 75000.0, 1_000_000L, now.minusSeconds(30), "fake")
        val r = MarketDataValidation.validateQuote(quote, now)
        assertTrue(r.valid)
        assertFalse(r.stale)
    }

    @Test
    fun `old quote is valid but explicitly stale`() {
        val now = Instant.parse("2026-09-26T12:00:00Z")
        val quote = Quote("AAPL", 200.0, null, now.minus(Duration.ofMinutes(10)), "fake")
        val r = MarketDataValidation.validateQuote(quote, now, staleAfter = Duration.ofMinutes(5))
        assertTrue(r.valid)
        assertTrue(r.stale)
    }

    @Test
    fun `future or nonpositive quote is rejected`() {
        val now = Instant.parse("2026-09-26T12:00:00Z")
        val quote = Quote("AAPL", 0.0, -1L, now.plusSeconds(120), "fake")
        val r = MarketDataValidation.validateQuote(quote, now)
        assertFalse(r.valid)
        assertTrue(r.errors.size >= 3)
    }

    @Test
    fun `daily bars must be oldest to newest and not from the future`() {
        val bars = listOf(
            DailyBar("AAPL", LocalDate.of(2026, 9, 25), null, null, null, 201.0, 10L, "fake"),
            DailyBar("AAPL", LocalDate.of(2026, 9, 24), null, null, null, 200.0, 10L, "fake")
        )
        val r = MarketDataValidation.validateBars(bars, LocalDate.of(2026, 9, 26), "AAPL")
        assertFalse(r.valid)
    }
}
