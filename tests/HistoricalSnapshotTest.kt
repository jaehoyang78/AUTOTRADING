package autotrading.core.backtest

import autotrading.core.HybridStrategyEngine
import autotrading.data.Market
import autotrading.data.MarketIndex
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HistoricalSnapshotTest {
    private val asOf = Instant.parse("2026-09-25T20:00:00Z")

    @Test
    fun `snapshot using only information available by asOf is valid`() {
        val snapshot = sampleSnapshot(
            marketAvailableAt = asOf.minusSeconds(60),
            stockAvailableAt = asOf.minusSeconds(120),
            membershipAvailableAt = asOf.minusSeconds(3600)
        )
        val result = HistoricalSnapshotValidator.validate(snapshot)
        assertTrue(result.valid, result.errors.joinToString())
    }

    @Test
    fun `future released fundamental is rejected as look ahead`() {
        val snapshot = sampleSnapshot(
            marketAvailableAt = asOf.minusSeconds(60),
            stockAvailableAt = asOf.plusSeconds(1),
            membershipAvailableAt = asOf.minusSeconds(3600)
        )
        val result = HistoricalSnapshotValidator.validate(snapshot)
        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("available after asOf") })
    }

    @Test
    fun `future universe membership creates survivorship look ahead error`() {
        val snapshot = sampleSnapshot(
            marketAvailableAt = asOf.minusSeconds(60),
            stockAvailableAt = asOf.minusSeconds(120),
            membershipAvailableAt = asOf.plusSeconds(1)
        )
        val result = HistoricalSnapshotValidator.validate(snapshot)
        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("universeMembership") })
    }

    @Test
    fun `duplicate symbols are rejected`() {
        val one = sampleSnapshot(
            marketAvailableAt = asOf.minusSeconds(60),
            stockAvailableAt = asOf.minusSeconds(120),
            membershipAvailableAt = asOf.minusSeconds(3600)
        )
        val duplicate = one.copy(stocks = one.stocks + one.stocks.first())
        val result = HistoricalSnapshotValidator.validate(duplicate)
        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("duplicate stock symbols") })
    }

    private fun sampleSnapshot(
        marketAvailableAt: Instant,
        stockAvailableAt: Instant,
        membershipAvailableAt: Instant
    ): HistoricalSnapshot {
        return HistoricalSnapshot(
            snapshotId = "KR-2026-09-25-close",
            asOf = asOf,
            market = Market.KR,
            benchmark = MarketIndex.KOSPI,
            marketInputs = HistoricalMarketInputs(
                inputs = HybridStrategyEngine.MarketInputs(indexAbove200d = true, vix = 20.0),
                availability = mapOf(
                    "indexAbove200d" to PointInTimeMeta(
                        LocalDate.of(2026, 9, 25), marketAvailableAt, "test-market"
                    ),
                    "vix" to PointInTimeMeta(
                        LocalDate.of(2026, 9, 25), marketAvailableAt, "test-vol"
                    )
                )
            ),
            stocks = listOf(
                HistoricalStockSnapshot(
                    symbol = "005930",
                    inputs = HybridStrategyEngine.StockInputs(
                        roicPct = 12.0,
                        return60dPct = 8.0
                    ),
                    availability = mapOf(
                        "roicPct" to PointInTimeMeta(
                            LocalDate.of(2026, 6, 30), stockAvailableAt, "test-fundamental", "rev-1"
                        ),
                        "return60dPct" to PointInTimeMeta(
                            LocalDate.of(2026, 9, 25), asOf.minusSeconds(10), "test-price"
                        )
                    ),
                    universeMembership = UniverseMembership(
                        member = true,
                        availableAt = membershipAvailableAt,
                        source = "test-universe"
                    ),
                    sector = "Semiconductors"
                )
            ),
            createdAt = asOf.plusSeconds(600)
        )
    }
}
