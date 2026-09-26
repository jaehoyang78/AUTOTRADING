# Data Sources — AUTOTRADING

Updated: 2026-09-26

## Principles

1. Core strategy code must not depend directly on a vendor response schema.
2. Every external record carries provider, observation timestamp and freshness metadata.
3. Market data, macro data, fundamentals and consensus may use different providers.
4. Live/paper broker credentials must never be committed to GitHub.
5. A provider can be replaced without changing scoring logic.
6. API limits, market holidays, delayed quotes and stale values must be explicit.

## Initial provider plan

### Korea Investment Securities Open API (KIS)

Primary intended role:
- Korean stock quote and historical prices
- U.S. stock quote/historical prices where supported
- later: user-approved brokerage execution

Why:
- The project already intends to use KIS for brokerage.
- The official KIS Developers portal documents REST market-data APIs, including Korean current-price and period/daily-price endpoints, and also provides WebSocket services.

Official references:
- https://apiportal.koreainvestment.com/
- https://apiportal.koreainvestment.com/apiservice-category

Design rule:
- `KisMarketDataProvider` will implement `MarketDataProvider`.
- KIS request/response field names stay inside the adapter.
- The strategy engine receives only AUTOTRADING models (`Quote`, `DailyBar`, etc.).
- Brokerage order methods must live in a separate broker adapter, not this data provider.

Operational note:
KIS publishes API change notices and rate-limit notices. Provider implementation must therefore use explicit throttling, error classification and API-version/change monitoring rather than assuming endpoints are permanent.

### FRED API

Primary intended role:
- U.S. policy rate / treasury yields
- inflation and selected growth/liquidity series
- selected credit-spread series

Official reference:
- https://fred.stlouisfed.org/docs/api/fred/
- https://fred.stlouisfed.org/docs/api/fred/series_observations.html

Design rule:
- `FredMacroDataProvider` will implement `MacroDataProvider`.
- FRED series IDs remain configuration, not hard-coded assumptions inside the strategy engine.
- Vintage/look-ahead handling must be considered before historical backtests use revised macro data.

### Cboe VIX

Primary intended role:
- authoritative historical VIX index values for validation/backtesting where appropriate.

Official reference:
- https://www.cboe.com/tradable-products/vix/vix-historical-data

Cboe publishes VIX historical closing values from 1990 onward and updates the dataset daily.

Design rule:
For operational market scans, a broker/provider feed may be used if timely and licensed for the use case; Cboe data can be an authoritative validation source. The exact live VIX integration will be decided after provider/API access checks.

### VKOSPI

Status: provider selection pending.

Requirements:
- reliable daily history
- explicit timestamp
- permissible automated access
- predictable availability

Do not silently substitute a different volatility measure and label it VKOSPI.

## Data categories still requiring provider selection

### Fundamentals / valuation
Needed fields include:
- ROIC / ROE
- operating margin
- FCF history
- debt/equity
- revenue / EPS / operating-income growth
- PER/PBR/EV-EBITDA/FCF yield/PEG
- sector comparison values

The first implementation may combine KIS-accessible company information with a second provider if necessary. Licensing/call limits and historical point-in-time availability matter more than convenience because backtests must avoid look-ahead bias.

### Analyst consensus / earnings revisions
Needed:
- current forward EPS
- historical estimate snapshots or revision history
- estimate up/down counts if available

This is the most likely data category to require a paid or specialized provider. Until reliable revision data is available, Coverage should expose the missing Revision group rather than inventing a proxy.

### Market breadth
Needed:
- advancing vs declining stocks
- 52-week highs/lows where possible
- percentage above key moving averages or an equivalent breadth measure

Initial implementation can calculate breadth from the project’s own daily universe if sufficient universe history is collected. This reduces dependency on a breadth-specific vendor.

## Provider priority order

1. KIS quote/daily-bar adapter for KR/US
2. Index history adapter
3. FRED macro adapter
4. VIX / VKOSPI adapter
5. internally computed market breadth
6. fundamentals/valuation adapter
7. consensus/EPS revision adapter

## Freshness policy draft

Every record uses `ProviderMeta`.

Suggested initial operational rules:
- quote: stale if older than the expected scan window/market context
- daily bar: stale if latest completed trading day is missing
- macro: freshness depends on the series release frequency, not wall-clock minutes
- missing/stale critical data reduces coverage or blocks automated recommendation eligibility

Exact thresholds will be defined per provider rather than globally.

## Backtest warning

Historical tests must preserve what could have been known at the time. Particular risks:
- revised macro observations
- restated fundamentals
- current constituents used for historical universes
- analyst estimates obtained only as today’s value

A dataset that cannot provide point-in-time history may still be useful for current screening but must not be treated as valid historical backtest input.
