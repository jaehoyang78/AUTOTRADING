package autotrading.tools

import autotrading.data.MarketIndex
import autotrading.data.fred.FredHttpGateway
import autotrading.data.fred.FredSeriesCatalog
import autotrading.data.http.JdkHttpTransport
import autotrading.data.kis.KisCredentials
import autotrading.data.kis.KisHttpGateway
import autotrading.data.kis.KisInstrument
import autotrading.data.kis.KisMarket
import autotrading.data.kis.KisVenue

/**
 * Read-only live connectivity smoke test.
 *
 * Required environment variables depend on SMOKE_TARGET:
 * - KIS:  KIS_APP_KEY, KIS_APP_SECRET
 * - FRED: FRED_API_KEY
 * - BOTH: all three
 *
 * This tool contains no broker/order code and never prints credentials.
 */
fun main() {
    val target = System.getenv("SMOKE_TARGET")?.trim()?.uppercase().orEmpty().ifBlank { "BOTH" }
    require(target in setOf("KIS", "FRED", "BOTH")) { "SMOKE_TARGET must be KIS, FRED, or BOTH" }

    val http = JdkHttpTransport()
    if (target == "KIS" || target == "BOTH") runKis(http)
    if (target == "FRED" || target == "BOTH") runFred(http)
    println("LIVE_SMOKE_OK target=$target")
}

private fun runKis(http: JdkHttpTransport) {
    val gateway = KisHttpGateway(KisCredentials.fromEnvironment(), http)

    val krQuote = gateway.quote(KisInstrument("005930", KisMarket.KR, KisVenue.KRX))
    require(krQuote.price > 0.0) { "KIS KR quote returned invalid price" }

    val kospi = gateway.indexBars(MarketIndex.KOSPI, 5)
    require(kospi.isNotEmpty()) { "KIS KOSPI index returned no bars" }
    require(kospi.zipWithNext().all { (a, b) -> a.date < b.date }) { "KIS KOSPI bars not ordered" }

    println(
        "KIS_SMOKE_OK symbol=005930 price=${krQuote.price} " +
            "kospiBars=${kospi.size} latestKospiDate=${kospi.last().date}"
    )
}

private fun runFred(http: JdkHttpTransport) {
    val gateway = FredHttpGateway.fromEnvironment(http)
    val observations = gateway.observations(FredSeriesCatalog.FED_FUNDS_EFFECTIVE, 3)
    require(observations.isNotEmpty()) { "FRED DFF returned no observations" }
    println(
        "FRED_SMOKE_OK series=${FredSeriesCatalog.FED_FUNDS_EFFECTIVE} " +
            "count=${observations.size} latestDate=${observations.last().date}"
    )
}
