package autotrading.data.fred

import autotrading.data.http.HttpResult
import autotrading.data.http.HttpTransport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FredHttpGatewayTest {

    @Test
    fun `parses observations and skips missing values`() {
        var requestedUrl = ""
        val http = object : HttpTransport {
            override fun get(url: String, headers: Map<String, String>): HttpResult {
                requestedUrl = url
                return HttpResult(200, """
                    {
                      "observations": [
                        {"date":"2026-09-01","value":"4.25"},
                        {"date":"2026-08-01","value":"."},
                        {"date":"2026-07-01","value":"4.50"}
                      ]
                    }
                """.trimIndent())
            }
        }
        val gateway = FredHttpGateway("secret-test-key", http)
        val values = gateway.observations("TEST SERIES", 3)
        assertEquals(2, values.size)
        assertEquals("2026-07-01", values.first().date.toString())
        assertEquals(4.25, values.last().value)
        assertTrue(requestedUrl.contains("series_id=TEST+SERIES"))
        assertTrue(requestedUrl.contains("file_type=json"))
    }

    @Test
    fun `non successful HTTP response fails closed`() {
        val http = object : HttpTransport {
            override fun get(url: String, headers: Map<String, String>) = HttpResult(429, "rate limited")
        }
        val gateway = FredHttpGateway("key", http)
        assertFailsWith<IllegalStateException> { gateway.observations("DFF", 10) }
    }

    @Test
    fun `blank api key is rejected`() {
        val http = object : HttpTransport {
            override fun get(url: String, headers: Map<String, String>) = HttpResult(200, "{}")
        }
        assertFailsWith<IllegalArgumentException> { FredHttpGateway("", http) }
    }
}
