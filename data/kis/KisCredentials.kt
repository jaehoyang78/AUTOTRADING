package autotrading.data.kis

class KisCredentials(
    val appKey: String,
    val appSecret: String
) {
    init {
        require(appKey.isNotBlank()) { "KIS app key is required" }
        require(appSecret.isNotBlank()) { "KIS app secret is required" }
    }

    override fun toString(): String = "KisCredentials(appKey=***, appSecret=***)"

    companion object {
        fun fromEnvironment(): KisCredentials {
            val key = System.getenv("KIS_APP_KEY")?.trim().orEmpty()
            val secret = System.getenv("KIS_APP_SECRET")?.trim().orEmpty()
            return KisCredentials(key, secret)
        }
    }
}
