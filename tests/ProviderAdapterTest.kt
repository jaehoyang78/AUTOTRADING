package autotrading.data

import autotrading.data.fred.*
import autotrading.data.kis.*
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProviderAdapterTest {
    private val now = Instant.parse("2026-09-26T00:00:00Z")

    @Test
    fun `KIS adapter normalizes quote and sorts daily bars`() {
        val gateway = object : KisGateway {
            override fun quote(symbol: String, market: KisMarket) = KisQuoteDto(symbol, 123.0, 999L, 1.2, now)
            override fun dailyBars(symbol: String, market: KisMarket, limit: Int) = listOf(
                KisDailyBarDto(LocalDate.of(2026, 9, 26), null, null, null, 123.0, 300L, now),
                KisDailyBarDto(LocalDate.of(2026, 9, 25), null, null, null, 120.0, 200L, now)
            )
            override fun indexBars(index: MarketIndex, limit: Int) = emptyList<KisIndexBarDto>()
            override fun volatility(index: VolatilityIndex, limit: Int) = emptyList<KisVolatilityDto>()
        }
        val provider = KisMarketDataProvider(gateway)
        val quote = provider.quote("005930", Market.KR)!!
        assertEquals("KIS", quote.meta.provider)
        assertEquals(Market.KR, quote.market)
        assertEquals(123.0, quote.price)

        val bars = provider.dailyBars("005930", Market.KR, 10)
        assertEquals(LocalDate.of(2026, 9, 25), bars.first().date)
        assertEquals(LocalDate.of(2026, 9, 26), bars.last().date)
    }

    @Test
    fun `FRED adapter normalizes and limits observations`() {
        val gateway = object : FredGateway {
            override fun observations(seriesId: String, limit: Int) = listOf(
                FredObservationDto(LocalDate.of(2026, 1, 3), 3.0, "%", now),
                FredObservationDto(LocalDate.of(2026, 1, 1), 1.0, "%", now),
                FredObservationDto(LocalDate.of(2026, 1, 2), 2.0, "%", now)
            )
        }
        val provider = FredMacroDataProvider(gateway)
        val points = provider.observations("TEST", 2)
        assertEquals(2, points.size)
        assertEquals(LocalDate.of(2026, 1, 2), points.first().date)
        assertEquals(LocalDate.of(2026, 1, 3), points.last().date)
        assertEquals("FRED", points.last().meta.provider)
    }

    @Test
    fun `adapters reject invalid limits`() {
        val kis = KisMarketDataProvider(object : KisGateway {
            override fun quote(symbol: String, market: KisMarket): KisQuoteDto? = null
            override fun dailyBars(symbol: String, market: KisMarket, limit: Int) = emptyList<KisDailyBarDto>()
            override fun indexBars(index: MarketIndex, limit: Int) = emptyList<KisIndexBarDto>()
            override fun volatility(index: VolatilityIndex, limit: Int) = emptyList<KisVolatilityDto>()
        })
        val fred = FredMacroDataProvider(object : FredGateway {
            override fun observations(seriesId: String, limit: Int) = emptyList<FredObservationDto>()
        })
        assertFailsWith<IllegalArgumentException> { kis.dailyBars("AAPL", Market.US, 0) }
        assertFailsWith<IllegalArgumentException> { fred.observations("DFF", 0) }
    }
}
