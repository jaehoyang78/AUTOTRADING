package autotrading.data.fred

import autotrading.data.MacroDataProvider
import autotrading.data.MacroPoint
import autotrading.data.ProviderMeta

class FredMacroDataProvider(
    private val gateway: FredGateway
) : MacroDataProvider {

    override val providerName: String = gateway.providerName

    override fun observations(seriesId: String, limit: Int): List<MacroPoint> {
        require(seriesId.isNotBlank()) { "seriesId must not be blank" }
        require(limit > 0) { "limit must be positive" }
        return gateway.observations(seriesId, limit)
            .sortedBy { it.date }
            .takeLast(limit)
            .map { dto ->
                MacroPoint(
                    seriesId = seriesId,
                    date = dto.date,
                    value = dto.value,
                    unit = dto.unit,
                    meta = ProviderMeta(providerName, dto.observedAt)
                )
            }
    }
}
