package autotrading.core

import autotrading.data.*
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PriceFeatureCalculatorTest {
    private val meta = ProviderMeta("test", Instant.parse("2026-09-26T00:00:00Z"))

    @Test
    fun `rising stock has positive returns and near high`() {
        val bars = (0..259).map { n ->
            DailyBar(
                symbol = "TEST", market = Market.US, venue = Venue.NASDAQ,
                date = LocalDate.of(2025, 1, 1).plusDays(n.toLong()),
                open = null, high = null, low = null, close = 100.0 + n,
                volume = if (n == 259) 2_000L else 1_000L, meta = meta
            )
        }
        val f = PriceFeatureCalculator.stockFeatures(bars)
        assertTrue(assertNotNull(f.return20dPct) > 0.0)
        assertTrue(assertNotNull(f.return60dPct) > 0.0)
        assertTrue(assertNotNull(f.return120dPct) > 0.0)
        assertEquals(2.0, assertNotNull(f.volumeRatio20d), 0.0001)
        assertEquals(0.0, assertNotNull(f.distanceFrom52wHighPct), 0.0001)
    }

    @Test
    fun `index trend detects current close above moving averages`() {
        val bars = (0..219).map { n ->
            IndexBar(
                index = MarketIndex.SP500,
                date = LocalDate.of(2025, 1, 1).plusDays(n.toLong()),
                close = 100.0 + n,
                meta = meta
            )
        }
        val f = PriceFeatureCalculator.indexFeatures(bars)
        assertEquals(true, f.above20d)
        assertEquals(true, f.above60d)
        assertEquals(true, f.above200d)
        assertEquals(0.0, assertNotNull(f.drawdownFrom52wHighPct), 0.0001)
    }

    @Test
    fun `insufficient history returns null rather than invented signal`() {
        val closes = List(20) { 100.0 }
        assertEquals(null, PriceFeatureCalculator.periodReturn(closes, 20))
        assertEquals(null, PriceFeatureCalculator.sma(closes, 60))
    }

    @Test
    fun `unordered daily bars are rejected`() {
        val date = LocalDate.of(2026, 1, 1)
        val bars = listOf(
            DailyBar("T", Market.KR, Venue.KRX, date.plusDays(1), null, null, null, 101.0, 100L, meta),
            DailyBar("T", Market.KR, Venue.KRX, date, null, null, null, 100.0, 100L, meta)
        )
        assertFailsWith<IllegalArgumentException> { PriceFeatureCalculator.stockFeatures(bars) }
    }
}
