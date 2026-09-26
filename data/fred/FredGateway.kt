package autotrading.data.fred

import java.time.Instant
import java.time.LocalDate

/** Low-level FRED contract; HTTP/API-key handling belongs in a later live gateway. */
interface FredGateway {
    val providerName: String get() = "FRED"
    fun observations(seriesId: String, limit: Int): List<FredObservationDto>
}

data class FredObservationDto(
    val date: LocalDate,
    val value: Double,
    val unit: String? = null,
    val observedAt: Instant
)
