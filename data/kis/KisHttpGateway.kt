package autotrading.data.kis

import autotrading.data.MarketIndex
import autotrading.data.VolatilityIndex
import autotrading.data.http.HttpTransport
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Live KIS market-data gateway only. This class intentionally contains no order methods.
 *
 * Verified scope in v1:
 * - KR current quote
 * - US current quote (explicit venue required)
 * - KR daily close history
 * - US daily close history
 *
 * Index/volatility endpoints remain unimplemented until their exact KIS mappings are verified.
 */
class KisHttpGateway(
    private val credentials: KisCredentials,
    private val http: HttpTransport,
    private val clock: Clock = Clock.systemUTC(),
    private val baseUrl: String = "https://openapi.koreainvestment.com:9443"
) : KisGateway {

    private var accessToken: String? = null
    private var tokenExpiresAt: Instant = Instant.EPOCH

    override fun quote(instrument: KisInstrument): KisQuoteDto {
        validateSymbol(instrument)
        val isKr = instrument.market == KisMarket.KR
        val query = if (isKr) {
            "FID_COND_MRKT_DIV_CODE=J&FID_INPUT_ISCD=${enc(instrument.symbol)}"
        } else {
            "AUTH=&EXCD=${exchangeCode(instrument.venue)}&SYMB=${enc(instrument.symbol.uppercase())}"
        }
        val path = if (isKr) {
            "/uapi/domestic-stock/v1/quotations/inquire-price"
        } else {
            "/uapi/overseas-price/v1/quotations/price"
        }
        val trId = if (isKr) "FHKST01010100" else "HHDFS00000300"
        val output = request(path, trId, query).getJSONObject("output")
        val price = requiredDouble(output, if (isKr) "stck_prpr" else "last")
        val volume = nullableLong(output, if (isKr) "acml_vol" else "tvol")
        val changePercent = nullableDouble(output, if (isKr) "prdy_ctrt" else "rate")
        return KisQuoteDto(
            symbol = instrument.symbol.uppercase(),
            price = price,
            volume = volume,
            changePercent = changePercent,
            observedAt = clock.instant()
        )
    }

    override fun dailyBars(instrument: KisInstrument, limit: Int): List<KisDailyBarDto> {
        require(limit > 0) { "limit must be positive" }
        validateSymbol(instrument)
        val isKr = instrument.market == KisMarket.KR
        val zone = if (isKr) ZoneId.of("Asia/Seoul") else ZoneId.of("America/New_York")
        val today = LocalDate.now(clock.withZone(zone))
        val to = today.format(DateTimeFormatter.BASIC_ISO_DATE)
        val from = today.minusDays((limit * 2L).coerceAtLeast(35L))
            .format(DateTimeFormatter.BASIC_ISO_DATE)

        val query = if (isKr) {
            "FID_COND_MRKT_DIV_CODE=J&FID_INPUT_ISCD=${enc(instrument.symbol)}" +
                "&FID_INPUT_DATE_1=$from&FID_INPUT_DATE_2=$to" +
                "&FID_PERIOD_DIV_CODE=D&FID_ORG_ADJ_PRC=0"
        } else {
            "AUTH=&EXCD=${exchangeCode(instrument.venue)}&SYMB=${enc(instrument.symbol.uppercase())}" +
                "&GUBN=0&BYMD=$to&MODP=0"
        }
        val path = if (isKr) {
            "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice"
        } else {
            "/uapi/overseas-price/v1/quotations/dailyprice"
        }
        val trId = if (isKr) "FHKST03010100" else "HHDFS76240000"
        val root = request(path, trId, query)
        val raw = root.optJSONArray("output2") ?: return emptyList()
        val observedAt = clock.instant()
        val result = mutableListOf<KisDailyBarDto>()
        for (i in 0 until raw.length()) {
            val item = raw.optJSONObject(i) ?: continue
            val rawDate = item.optString(if (isKr) "stck_bsop_date" else "xymd")
            val rawClose = item.optString(if (isKr) "stck_clpr" else "clos")
            val date = parseBasicDate(rawDate) ?: continue
            val close = rawClose.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 } ?: continue
            result += KisDailyBarDto(
                date = date,
                open = if (isKr) nullableDouble(item, "stck_oprc") else null,
                high = if (isKr) nullableDouble(item, "stck_hgpr") else null,
                low = if (isKr) nullableDouble(item, "stck_lwpr") else null,
                close = close,
                volume = if (isKr) nullableLong(item, "acml_vol") else null,
                observedAt = observedAt
            )
        }
        return result.sortedBy { it.date }.takeLast(limit)
    }

    override fun indexBars(index: MarketIndex, limit: Int): List<KisIndexBarDto> =
        throw UnsupportedOperationException("KIS index mapping not verified yet: $index")

    override fun volatility(index: VolatilityIndex, limit: Int): List<KisVolatilityDto> =
        throw UnsupportedOperationException("KIS volatility mapping not verified yet: $index")

    private fun request(path: String, trId: String, query: String): JSONObject {
        val response = http.get(
            "$baseUrl$path?$query",
            mapOf(
                "authorization" to "Bearer ${token()}",
                "appkey" to credentials.appKey,
                "appsecret" to credentials.appSecret,
                "tr_id" to trId,
                "Accept" to "application/json"
            )
        )
        if (response.statusCode !in 200..299) {
            throw IllegalStateException("KIS HTTP ${response.statusCode}")
        }
        val root = JSONObject(response.body)
        if (root.optString("rt_cd") != "0") {
            val message = root.optString("msg1", "market-data request failed")
            throw IllegalStateException("KIS market-data error: $message")
        }
        return root
    }

    @Synchronized
    private fun token(): String {
        val now = clock.instant()
        val existing = accessToken
        if (!existing.isNullOrBlank() && now.isBefore(tokenExpiresAt.minusSeconds(60))) return existing

        val payload = JSONObject()
            .put("grant_type", "client_credentials")
            .put("appkey", credentials.appKey)
            .put("appsecret", credentials.appSecret)
            .toString()
        val response = http.post(
            "$baseUrl/oauth2/tokenP",
            headers = mapOf("content-type" to "application/json", "Accept" to "application/json"),
            body = payload
        )
        if (response.statusCode !in 200..299) {
            throw IllegalStateException("KIS token HTTP ${response.statusCode}")
        }
        val root = JSONObject(response.body)
        val token = root.optString("access_token")
        require(token.isNotBlank()) { "KIS token response missing access_token" }
        val expiresIn = root.optLong("expires_in", 86_400L).coerceIn(60L, 86_400L)
        accessToken = token
        tokenExpiresAt = now.plusSeconds(expiresIn)
        return token
    }

    private fun validateSymbol(instrument: KisInstrument) {
        when (instrument.market) {
            KisMarket.KR -> require(instrument.symbol.matches(Regex("[0-9]{6}"))) {
                "KR symbol must be a 6-digit code"
            }
            KisMarket.US -> require(instrument.symbol.uppercase().matches(Regex("[A-Z.]{1,12}"))) {
                "US symbol format is invalid"
            }
        }
    }

    private fun exchangeCode(venue: KisVenue): String = when (venue) {
        KisVenue.NASDAQ -> "NAS"
        KisVenue.NYSE -> "NYS"
        KisVenue.AMEX -> "AMS"
        KisVenue.KRX -> throw IllegalArgumentException("KRX is not a U.S. exchange code")
    }

    private fun requiredDouble(json: JSONObject, key: String): Double =
        nullableDouble(json, key) ?: throw IllegalStateException("KIS response missing valid $key")

    private fun nullableDouble(json: JSONObject, key: String): Double? =
        json.optString(key).toDoubleOrNull()?.takeIf { it.isFinite() }

    private fun nullableLong(json: JSONObject, key: String): Long? =
        json.optString(key).toLongOrNull()

    private fun parseBasicDate(value: String): LocalDate? = runCatching {
        LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE)
    }.getOrNull()

    private fun enc(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
