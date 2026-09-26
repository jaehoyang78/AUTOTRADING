package autotrading.data.kis

import autotrading.data.MarketIndex
import autotrading.data.http.HttpResult
import autotrading.data.http.HttpTransport
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KisHttpGatewayTest {
    private val fixed = Clock.fixed(Instant.parse("2026-09-26T00:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `KR quote obtains token once and reuses it`() {
        val http = RecordingTransport { url, headers ->
            assertTrue(url.contains("inquire-price"))
            assertEquals("FHKST01010100", headers["tr_id"])
            assertTrue(url.contains("FID_INPUT_ISCD=005930"))
            HttpResult(200, """{"rt_cd":"0","output":{"stck_prpr":"70000","acml_vol":"123456","prdy_ctrt":"1.23"}}""")
        }
        val gateway = KisHttpGateway(KisCredentials("key", "secret"), http, fixed, "https://kis.test")
        val instrument = KisInstrument("005930", KisMarket.KR, KisVenue.KRX)

        val first = gateway.quote(instrument)
        val second = gateway.quote(instrument)

        assertEquals(70000.0, first.price)
        assertEquals(123456L, first.volume)
        assertEquals(1.23, first.changePercent)
        assertEquals(70000.0, second.price)
        assertEquals(1, http.postCount)
        assertEquals(2, http.getCount)
        assertEquals("Bearer token-1", http.lastGetHeaders["authorization"])
    }

    @Test
    fun `US quote sends venue specific EXCD`() {
        val requested = mutableListOf<String>()
        val http = RecordingTransport { url, headers ->
            requested += url
            assertEquals("HHDFS00000300", headers["tr_id"])
            HttpResult(200, """{"rt_cd":"0","output":{"last":"250.50","tvol":"98765","rate":"2.50"}}""")
        }
        val gateway = KisHttpGateway(KisCredentials("key", "secret"), http, fixed, "https://kis.test")
        gateway.quote(KisInstrument("AAPL", KisMarket.US, KisVenue.NASDAQ))
        gateway.quote(KisInstrument("IBM", KisMarket.US, KisVenue.NYSE))
        gateway.quote(KisInstrument("TEST", KisMarket.US, KisVenue.AMEX))

        assertTrue(requested[0].contains("EXCD=NAS"))
        assertTrue(requested[1].contains("EXCD=NYS"))
        assertTrue(requested[2].contains("EXCD=AMS"))
    }

    @Test
    fun `daily bars parse verified close fields and remain ordered`() {
        val http = RecordingTransport { url, _ ->
            if (url.contains("daily-itemchartprice")) {
                HttpResult(200, """
                    {"rt_cd":"0","output2":[
                      {"stck_bsop_date":"20260926","stck_oprc":"71000","stck_hgpr":"72000","stck_lwpr":"70000","stck_clpr":"71500","acml_vol":"2000"},
                      {"stck_bsop_date":"20260925","stck_oprc":"70000","stck_hgpr":"71500","stck_lwpr":"69500","stck_clpr":"70500","acml_vol":"1000"}
                    ]}
                """.trimIndent())
            } else {
                HttpResult(200, """{"rt_cd":"0","output2":[
                    {"xymd":"20260926","clos":"250.5"},
                    {"xymd":"20260925","clos":"245.0"}
                ]}""")
            }
        }
        val gateway = KisHttpGateway(KisCredentials("key", "secret"), http, fixed, "https://kis.test")
        val kr = gateway.dailyBars(KisInstrument("005930", KisMarket.KR, KisVenue.KRX), 10)
        val us = gateway.dailyBars(KisInstrument("AAPL", KisMarket.US, KisVenue.NASDAQ), 10)

        assertEquals("2026-09-25", kr.first().date.toString())
        assertEquals(70500.0, kr.first().close)
        assertEquals(1000L, kr.first().volume)
        assertEquals("2026-09-25", us.first().date.toString())
        assertEquals(245.0, us.first().close)
        assertEquals(null, us.first().volume)
    }

    @Test
    fun `KOSPI and KOSDAQ daily index use verified KIS mapping`() {
        val requested = mutableListOf<String>()
        val http = RecordingTransport { url, headers ->
            requested += url
            assertTrue(url.contains("inquire-daily-indexchartprice"))
            assertEquals("FHKUP03500100", headers["tr_id"])
            assertTrue(url.contains("FID_COND_MRKT_DIV_CODE=U"))
            HttpResult(200, """{"rt_cd":"0","output2":[
                {"stck_bsop_date":"20260926","bstp_nmix_prpr":"3500.12"},
                {"stck_bsop_date":"20260925","bstp_nmix_prpr":"3488.50"}
            ]}""")
        }
        val gateway = KisHttpGateway(KisCredentials("key", "secret"), http, fixed, "https://kis.test")

        val kospi = gateway.indexBars(MarketIndex.KOSPI, 2)
        val kosdaq = gateway.indexBars(MarketIndex.KOSDAQ, 2)

        assertTrue(requested[0].contains("FID_INPUT_ISCD=0001"))
        assertTrue(requested[1].contains("FID_INPUT_ISCD=1001"))
        assertEquals("2026-09-25", kospi.first().date.toString())
        assertEquals(3488.50, kospi.first().close)
        assertEquals(3500.12, kosdaq.last().close)
    }

    @Test
    fun `unverified US index mapping fails closed`() {
        val http = RecordingTransport { _, _ -> error("GET should not be called") }
        val gateway = KisHttpGateway(KisCredentials("key", "secret"), http, fixed, "https://kis.test")
        assertFailsWith<UnsupportedOperationException> { gateway.indexBars(MarketIndex.SP500, 260) }
        assertFailsWith<UnsupportedOperationException> { gateway.indexBars(MarketIndex.NASDAQ_COMPOSITE, 260) }
    }

    @Test
    fun `KIS business error and HTTP error fail closed without secrets`() {
        val businessError = RecordingTransport { _, _ ->
            HttpResult(200, """{"rt_cd":"1","msg1":"invalid request"}""")
        }
        val gateway1 = KisHttpGateway(KisCredentials("super-key", "super-secret"), businessError, fixed, "https://kis.test")
        val e1 = assertFailsWith<IllegalStateException> {
            gateway1.quote(KisInstrument("005930", KisMarket.KR, KisVenue.KRX))
        }
        assertFalse(e1.message.orEmpty().contains("super-secret"))
        assertFalse(e1.message.orEmpty().contains("super-key"))

        val httpError = RecordingTransport { _, _ -> HttpResult(429, "rate limit") }
        val gateway2 = KisHttpGateway(KisCredentials("key", "secret"), httpError, fixed, "https://kis.test")
        assertFailsWith<IllegalStateException> {
            gateway2.quote(KisInstrument("005930", KisMarket.KR, KisVenue.KRX))
        }
    }

    @Test
    fun `credentials toString is redacted and invalid symbols are rejected`() {
        val credentials = KisCredentials("actual-key", "actual-secret")
        assertFalse(credentials.toString().contains("actual-key"))
        assertFalse(credentials.toString().contains("actual-secret"))

        val http = RecordingTransport { _, _ -> error("GET should not be called") }
        val gateway = KisHttpGateway(credentials, http, fixed, "https://kis.test")
        assertFailsWith<IllegalArgumentException> {
            gateway.quote(KisInstrument("BAD", KisMarket.KR, KisVenue.KRX))
        }
    }

    private class RecordingTransport(
        private val getHandler: (String, Map<String, String>) -> HttpResult
    ) : HttpTransport {
        var postCount = 0
        var getCount = 0
        var lastGetHeaders: Map<String, String> = emptyMap()

        override fun get(url: String, headers: Map<String, String>): HttpResult {
            getCount++
            lastGetHeaders = headers
            return getHandler(url, headers)
        }

        override fun post(url: String, headers: Map<String, String>, body: String): HttpResult {
            postCount++
            assertTrue(url.endsWith("/oauth2/tokenP"))
            assertTrue(body.contains("client_credentials"))
            return HttpResult(200, """{"access_token":"token-1","expires_in":3600}""")
        }
    }
}
