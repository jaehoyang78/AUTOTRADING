package autotrading.core

/**
 * v1 heuristic mapping between macro quadrants and broad equity sectors.
 * This is explanatory/research logic only until backtests validate the weights.
 */
object MacroSectorFitEngine {

    enum class Sector {
        TECHNOLOGY,
        COMMUNICATION_SERVICES,
        CONSUMER_DISCRETIONARY,
        CONSUMER_STAPLES,
        HEALTHCARE,
        FINANCIALS,
        INDUSTRIALS,
        ENERGY,
        MATERIALS,
        UTILITIES,
        REAL_ESTATE
    }

    fun score(
        sector: Sector,
        macro: MacroFeatureCalculator.MacroFeatures
    ): Double? {
        if (macro.quadrant == MacroFeatureCalculator.MacroQuadrant.UNKNOWN) return null
        val base = when (macro.quadrant) {
            MacroFeatureCalculator.MacroQuadrant.GROWTH_UP_INFLATION_DOWN -> when (sector) {
                Sector.TECHNOLOGY -> 85.0
                Sector.COMMUNICATION_SERVICES -> 75.0
                Sector.CONSUMER_DISCRETIONARY -> 80.0
                Sector.CONSUMER_STAPLES -> 50.0
                Sector.HEALTHCARE -> 60.0
                Sector.FINANCIALS -> 65.0
                Sector.INDUSTRIALS -> 70.0
                Sector.ENERGY -> 45.0
                Sector.MATERIALS -> 55.0
                Sector.UTILITIES -> 50.0
                Sector.REAL_ESTATE -> 70.0
            }
            MacroFeatureCalculator.MacroQuadrant.GROWTH_UP_INFLATION_UP -> when (sector) {
                Sector.TECHNOLOGY -> 55.0
                Sector.COMMUNICATION_SERVICES -> 60.0
                Sector.CONSUMER_DISCRETIONARY -> 65.0
                Sector.CONSUMER_STAPLES -> 55.0
                Sector.HEALTHCARE -> 55.0
                Sector.FINANCIALS -> 70.0
                Sector.INDUSTRIALS -> 75.0
                Sector.ENERGY -> 85.0
                Sector.MATERIALS -> 80.0
                Sector.UTILITIES -> 45.0
                Sector.REAL_ESTATE -> 40.0
            }
            MacroFeatureCalculator.MacroQuadrant.GROWTH_DOWN_INFLATION_DOWN -> when (sector) {
                Sector.TECHNOLOGY -> 65.0
                Sector.COMMUNICATION_SERVICES -> 60.0
                Sector.CONSUMER_DISCRETIONARY -> 45.0
                Sector.CONSUMER_STAPLES -> 80.0
                Sector.HEALTHCARE -> 80.0
                Sector.FINANCIALS -> 45.0
                Sector.INDUSTRIALS -> 45.0
                Sector.ENERGY -> 40.0
                Sector.MATERIALS -> 40.0
                Sector.UTILITIES -> 80.0
                Sector.REAL_ESTATE -> 70.0
            }
            MacroFeatureCalculator.MacroQuadrant.GROWTH_DOWN_INFLATION_UP -> when (sector) {
                Sector.TECHNOLOGY -> 35.0
                Sector.COMMUNICATION_SERVICES -> 45.0
                Sector.CONSUMER_DISCRETIONARY -> 30.0
                Sector.CONSUMER_STAPLES -> 70.0
                Sector.HEALTHCARE -> 65.0
                Sector.FINANCIALS -> 45.0
                Sector.INDUSTRIALS -> 40.0
                Sector.ENERGY -> 75.0
                Sector.MATERIALS -> 65.0
                Sector.UTILITIES -> 55.0
                Sector.REAL_ESTATE -> 30.0
            }
            MacroFeatureCalculator.MacroQuadrant.UNKNOWN -> return null
        }

        val stress = macro.creditSpreadStress
        if (stress == null || stress <= 50.0) return base
        val sensitivity = when (sector) {
            Sector.CONSUMER_STAPLES, Sector.HEALTHCARE, Sector.UTILITIES -> 0.10
            else -> 0.25
        }
        val penalty = (stress - 50.0) * sensitivity
        return (base - penalty).coerceIn(0.0, 100.0)
    }
}
