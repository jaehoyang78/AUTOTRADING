package autotrading.core

/** Portfolio-level guardrails. These rules constrain risk; they do not create buy signals. */
object PortfolioRiskEngine {

    data class PortfolioRisk(
        val recommendedCashPct: Double,
        val maxEquityExposurePct: Double,
        val canIncreaseEquityExposure: Boolean,
        val reasons: List<String>
    )

    fun assess(
        market: HybridStrategyEngine.MarketAssessment,
        currentEquityExposurePct: Double,
        portfolioDrawdownPct: Double = 0.0
    ): PortfolioRisk {
        val regimeCash = when (market.regime) {
            HybridStrategyEngine.MarketRegime.RISK_ON_STRONG -> 10.0
            HybridStrategyEngine.MarketRegime.RISK_ON_NORMAL -> 15.0
            HybridStrategyEngine.MarketRegime.NEUTRAL -> 25.0
            HybridStrategyEngine.MarketRegime.RISK_OFF -> 40.0
            HybridStrategyEngine.MarketRegime.PANIC -> 35.0
        }

        val drawdownBuffer = when {
            portfolioDrawdownPct <= -20.0 -> 15.0
            portfolioDrawdownPct <= -15.0 -> 10.0
            portfolioDrawdownPct <= -10.0 -> 5.0
            else -> 0.0
        }
        val cash = (regimeCash + drawdownBuffer).coerceIn(10.0, 60.0)
        val maxEquity = 100.0 - cash
        val reasons = mutableListOf<String>()
        reasons += "${market.regime}: 기본 현금 ${regimeCash.toInt()}%"
        if (drawdownBuffer > 0) reasons += "포트폴리오 낙폭 보호 +${drawdownBuffer.toInt()}% 현금"
        if (currentEquityExposurePct >= maxEquity) reasons += "현재 주식비중이 목표 상한에 도달"

        return PortfolioRisk(
            recommendedCashPct = cash,
            maxEquityExposurePct = maxEquity,
            canIncreaseEquityExposure = currentEquityExposurePct < maxEquity,
            reasons = reasons
        )
    }
}
