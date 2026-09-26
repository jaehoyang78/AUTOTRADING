package autotrading.data.http

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

interface HttpTransport {
    fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResult
}

data class HttpResult(val statusCode: Int, val body: String)

class JdkHttpTransport(
    private val connectTimeout: Duration = Duration.ofSeconds(8),
    private val requestTimeout: Duration = Duration.ofSeconds(10)
) : HttpTransport {
    private val client = HttpClient.newBuilder().connectTimeout(connectTimeout).build()

    override fun get(url: String, headers: Map<String, String>): HttpResult {
        val builder = HttpRequest.newBuilder(URI.create(url)).GET().timeout(requestTimeout)
        headers.forEach { (key, value) -> builder.header(key, value) }
        val response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        return HttpResult(response.statusCode(), response.body())
    }
}
