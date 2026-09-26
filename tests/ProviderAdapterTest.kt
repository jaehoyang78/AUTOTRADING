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
    fun `KIS adapter normalizes KR quote and sorts daily bars`() {
        val gateway = object : KisGateway {
            override fun quote(instrument: KisInstrument) = KisQuoteDto(instrument.symbol, 123.0, 999L, 1.2, now)
            override fun dailyBars(instrument: KisInstrument, limit: Int) = listOf(
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
        assertEquals(Venue.KRX, quote.venue)
        assertEquals(123.0, quote.price)

        val bars = provider.dailyBars("005930", Market.KR, limit = 10)
        assertEquals(Venue.KRX, bars.first().venue)
        assertEquals(LocalDate.of(2026, 9, 25), bars.first().date)
        assertEquals(LocalDate.of(2026, 9, 26), bars.last().date)
    }

    @Test
    fun `US venue is required and mapped to KIS`() {
        var captured: KisInstrument? = null
        val gateway = object : KisGateway {
            override fun quote(instrument: KisInstrument): KisQuoteDto? {
                captured = instrument
                return KisQuoteDto(instrument.symbol, 200.0, 1000L, 2.0, now)
            }
            override fun dailyBars(instrument: KisInstrument, limit: Int) = emptyList<KisDailyBarDto>()
            override fun indexBars(index: MarketIndex, limit: Int) = emptyList<KisIndexBarDto>()
            override fun volatility(index: VolatilityIndex, limit: Int) = emptyList<KisVolatilityDto>()
        }
        val provider = KisMarketDataProvider(gateway)
        assertFailsWith<IllegalArgumentException> { provider.quote("AAPL", Market.US) }
        val quote = provider.quote("AAPL", Market.US, Venue.NASDAQ)!!
        assertEquals(KisVenue.NASDAQ, captured!!.venue)
        assertEquals(Venue.NASDAQ, quote.venue)
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
            override fun quote(instrument: KisInstrument): KisQuoteDto? = null
            override fun dailyBars(instrument: KisInstrument, limit: Int) = emptyList<KisDailyBarDto>()
            override fun indexBars(index: MarketIndex, limit: Int) = emptyList<KisIndexBarDto>()
            override fun volatility(index: VolatilityIndex, limit: Int) = emptyList<KisVolatilityDto>()
        })
        val fred = FredMacroDataProvider(object : FredGateway {
            override fun observations(seriesId: String, limit: Int) = emptyList<FredObservationDto>()
        })
        assertFailsWith<IllegalArgumentException> { kis.dailyBars("AAPL", Market.US, Venue.NASDAQ, 0) }
        assertFailsWith<IllegalArgumentException> { fred.observations("DFF", 0) }
    }
}
