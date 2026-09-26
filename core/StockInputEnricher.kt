package autotrading.core

/** Adds externally derived context to raw stock factor inputs without mutating the scoring engine. */
object StockInputEnricher {

    fun withMacroSectorFit(
        stock: HybridStrategyEngine.StockInputs,
        sector: MacroSectorFitEngine.Sector?,
        macro: MacroFeatureCalculator.MacroFeatures?
    ): HybridStrategyEngine.StockInputs {
        if (sector == null || macro == null) return stock
        return stock.copy(
            sectorMacroFitScore = MacroSectorFitEngine.score(sector, macro)
        )
    }
}
