# Data Sources — AUTOTRADING

Last reviewed: 2026-09-26

## Principle

Core strategy code must not depend directly on a vendor response format. Every external source is mapped into provider-neutral models under `autotrading.core.data`.

Required metadata for every stored observation:
- provider/source
- observed time or effective date
- retrieval time
- stale flag/age policy
- raw symbol/series identifier

## Phase 1 recommendation

### 1. Korea Investment & Securities (KIS Open API)

Primary role:
- Korean stock quotes
- Korean daily bars
- U.S. stock quotes/daily bars where supported
- later broker order adapter

Why:
- the project already plans to use Korea Investment Securities for execution
- the official KIS Developers GitHub repository provides authentication and market/trading examples
- official examples distinguish live and paper credentials/endpoints

Important engineering rule:
- market-data adapter and broker/order adapter remain separate modules even if both use KIS
- rate limiting, token renewal, retries and caching belong in the adapter layer, not in strategy logic
- ambiguous order responses must never be automatically retried

Status: **preferred first market-data adapter**

### 2. FRED (Federal Reserve Bank of St. Louis)

Primary role:
- U.S. policy rates
- Treasury yields
- inflation series
- credit/liquidity/macro series where appropriate

Why:
- official REST API supports series observations and historical data
- suitable for slower-moving macro inputs rather than intraday trading signals

Implementation note:
- map FRED series into a future `MacroDataProvider`
- preserve effective observation date and provider update time when available

Status: **preferred macro source**

### 3. Cboe VIX data

Primary role:
- VIX historical benchmark/reference

Why:
- Cboe publishes VIX historical daily data and documents it as its U.S. equity volatility gauge

Implementation note:
- historical daily VIX can support backtests and regime validation
- production/live use must use an explicitly permitted/API-capable source; do not assume a public historical download is a live redistribution feed

Status: **historical/reference source; live provider still to be selected**

## Items still requiring provider selection

### VKOSPI
Need an official/licensed source suitable for automated retrieval. Candidates should be checked against KRX/KIS availability and usage terms.

### Market breadth
Need consistent constituent-level advance/decline and 52-week high/low data for:
- KOSPI/KOSDAQ
- S&P 500/Nasdaq universe

Initial fallback can be computed internally from the project universe if constituent history is available.

### Fundamentals / valuation
Required fields:
- ROIC, ROE, operating margin
- debt/equity
- free cash flow history
- PER, PBR, EV/EBITDA, FCF yield, PEG

Provider selection criteria:
- point-in-time historical availability for backtests
- corporate-action normalization
- licensing/redistribution constraints
- KR + US coverage
- cost and request limits

### Consensus / EPS revision
Required fields:
- forward EPS growth
- 3-month EPS estimate change
- up/down revision breadth

This is likely the hardest data source. Do not substitute current consensus into historical backtests because that creates look-ahead bias.

## Provider interface already added

`core/data/MarketDataProvider.kt`

Current neutral models:
- `Quote`
- `DailyBar`
- `IndexSnapshot`
- `VolatilitySnapshot`
- `MarketDataProvider`

The first concrete adapters should be:
1. `KisMarketDataProvider`
2. a test/fake provider for deterministic unit tests

## Data quality gates

Before data is passed to the strategy engine:
1. validate numeric range and nullability
2. reject impossible timestamps/future observations
3. normalize symbol/exchange identifiers
4. record provider and observation timestamp
5. mark stale data rather than silently treating it as current
6. never replace missing values with zero unless zero is economically meaningful

## Backtest-specific rule

Historical testing must use information that was actually available at that point in time. Current fundamentals or revised consensus copied backward into history is prohibited.
