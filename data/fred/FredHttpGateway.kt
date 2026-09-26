package autotrading.data.fred

import autotrading.data.http.HttpTransport
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate

class FredHttpGateway(
    private val apiKey: String,
    private val http: HttpTransport,
    private val baseUrl: String = "https://api.stlouisfed.org"
) : FredGateway {

    init {
        require(apiKey.isNotBlank()) { "FRED API key is required" }
    }

    override fun observations(seriesId: String, limit: Int): List<FredObservationDto> {
        require(seriesId.isNotBlank()) { "seriesId must not be blank" }
        require(limit > 0) { "limit must be positive" }
        val encodedSeries = URLEncoder.encode(seriesId, StandardCharsets.UTF_8)
        val encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
        val url = "$baseUrl/fred/series/observations?series_id=$encodedSeries&api_key=$encodedKey&file_type=json&sort_order=desc&limit=$limit"
        val response = http.get(url, mapOf("Accept" to "application/json"))
        if (response.statusCode !in 200..299) {
            throw IllegalStateException("FRED HTTP ${response.statusCode}")
        }
        val root = JSONObject(response.body)
        val array = root.optJSONArray("observations") ?: return emptyList()
        val receivedAt = Instant.now()
        val result = mutableListOf<FredObservationDto>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val rawValue = item.optString("value")
            val value = rawValue.toDoubleOrNull() ?: continue // FRED missing observations may be "."
            val date = runCatching { LocalDate.parse(item.getString("date")) }.getOrNull() ?: continue
            result += FredObservationDto(date = date, value = value, observedAt = receivedAt)
        }
        return result.sortedBy { it.date }
    }

    companion object {
        fun fromEnvironment(http: HttpTransport): FredHttpGateway {
            val key = System.getenv("FRED_API_KEY")?.trim().orEmpty()
            require(key.isNotBlank()) { "FRED_API_KEY environment variable is required" }
            return FredHttpGateway(key, http)
        }
    }
}
