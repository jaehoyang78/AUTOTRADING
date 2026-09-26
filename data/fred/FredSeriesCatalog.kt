package autotrading.data.fred

/**
 * Initial macro series used by AUTOTRADING.
 * Series IDs are kept outside strategy math so they can be changed/configured later.
 */
object FredSeriesCatalog {
    const val FED_FUNDS_EFFECTIVE = "DFF"
    const val TREASURY_2Y = "DGS2"
    const val TREASURY_10Y = "DGS10"
    const val CPI_ALL_URBAN_SA = "CPIAUCSL"
    const val FED_TOTAL_ASSETS = "WALCL"
    const val USD_KRW = "DEXKOUS"
    const val US_HIGH_YIELD_OAS = "BAMLH0A0HYM2"

    val defaultSeries: List<String> = listOf(
        FED_FUNDS_EFFECTIVE,
        TREASURY_2Y,
        TREASURY_10Y,
        CPI_ALL_URBAN_SA,
        FED_TOTAL_ASSETS,
        USD_KRW,
        US_HIGH_YIELD_OAS
    )
}
