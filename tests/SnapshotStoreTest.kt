package autotrading.core.backtest

import autotrading.core.HybridStrategyEngine
import autotrading.data.Market
import autotrading.data.MarketIndex
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class SnapshotStoreTest {
    private val asOf = Instant.parse("2026-09-25T20:00:00Z")

    @Test
    fun `append stores immutable identity and stable hash`() {
        val store = InMemoryAppendOnlySnapshotStore()
        val snapshot = snapshot("one", 12.0)
        val first = store.append(snapshot)
        val loaded = store.get("one")!!
        assertEquals(first.sha256, loaded.sha256)
        assertEquals(64, first.sha256.length)
        assertEquals(first.sha256, HistoricalSnapshotHasher.sha256(snapshot))
    }

    @Test
    fun `duplicate snapshot id cannot overwrite prior record`() {
        val store = InMemoryAppendOnlySnapshotStore()
        store.append(snapshot("one", 12.0))
        assertFailsWith<IllegalArgumentException> {
            store.append(snapshot("one", 13.0))
        }
        assertEquals(1, store.all().size)
    }

    @Test
    fun `changed economic content changes integrity hash`() {
        val a = HistoricalSnapshotHasher.sha256(snapshot("a", 12.0))
        val b = HistoricalSnapshotHasher.sha256(snapshot("a", 13.0))
        assertNotEquals(a, b)
    }

    private fun snapshot(id: String, roic: Double): HistoricalSnapshot = HistoricalSnapshot(
        snapshotId = id,
        asOf = asOf,
        market = Market.KR,
        benchmark = MarketIndex.KOSPI,
        marketInputs = HistoricalMarketInputs(
            inputs = HybridStrategyEngine.MarketInputs(indexAbove200d = true),
            availability = mapOf(
                "indexAbove200d" to PointInTimeMeta(
                    LocalDate.of(2026, 9, 25), asOf.minusSeconds(10), "market"
                )
            )
        ),
        stocks = listOf(
            HistoricalStockSnapshot(
                symbol = "005930",
                inputs = HybridStrategyEngine.StockInputs(roicPct = roic, return60dPct = 5.0),
                availability = mapOf(
                    "roicPct" to PointInTimeMeta(
                        LocalDate.of(2026, 6, 30), asOf.minusSeconds(60), "fundamental", "v1"
                    )
                ),
                universeMembership = UniverseMembership(true, asOf.minusSeconds(3600), "universe")
            )
        ),
        createdAt = asOf.plusSeconds(60)
    )
}
