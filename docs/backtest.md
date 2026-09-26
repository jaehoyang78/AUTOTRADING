# Backtest Principles — AUTOTRADING

## Goal

Backtesting exists to test whether the investment process had a repeatable edge using only information that was actually available at each historical decision time.

A high historical return is not accepted if it depends on future information, revised data, today’s index membership, or unrealistic execution assumptions.

## 1. Point-in-time rule

Every historical decision has an `asOf` timestamp.

Every input used by the strategy must record:
- effective date, when relevant
- `availableAt`: earliest timestamp the strategy could actually know the value
- source
- revision ID/version when available

Hard rule:

```text
input.availableAt <= snapshot.asOf
```

If this is false, the snapshot is invalid and must not enter a backtest.

## 2. HistoricalSnapshot v1

Implemented in:

`core/backtest/HistoricalSnapshot.kt`

Snapshot contains:
- snapshot ID
- decision `asOf`
- market
- benchmark
- MarketInputs + per-field availability metadata
- stock universe
- StockInputs + per-field availability metadata
- point-in-time universe membership
- sector
- creation timestamp
- schema version

The snapshot may be physically created later than `asOf` if it is reconstructed from a trustworthy point-in-time archive. What matters is each field’s `availableAt`.

## 3. Bias controls

### Look-ahead bias
Future-released values are rejected by `HistoricalSnapshotValidator`.

Examples:
- quarterly earnings announced after the decision date
- consensus revision published the next day
- revised macro data that was not available in the original vintage

### Survivorship bias
Universe membership is stored with its own `availableAt`.

Do not backtest 2020 by taking today’s S&P 500/KOSPI constituents and projecting them backward.

### Revision bias
Where a provider exposes revisions/vintages, preserve `revisionId` or equivalent version information.

For macro series, future work should prefer vintage-aware data when strategy conclusions are sensitive to later revisions.

## 4. Execution assumptions

Later backtest engine must explicitly model:
- decision timestamp
- executable timestamp
- next-open / next-close / limit-price convention
- transaction fees
- taxes
- FX cost where applicable
- slippage
- position-size caps
- cash balance
- corporate actions

No same-bar execution is allowed when the signal requires that bar’s closing data unless the strategy can realistically submit after the data becomes known.

## 5. Strategy portfolios

Planned parallel portfolios:
- Buffett
- Graham
- Lynch
- Momentum
- Hybrid

Each strategy must consume the exact same point-in-time snapshot universe where comparable.

## 6. Required reports

Minimum metrics:
- CAGR
- cumulative return
- Max Drawdown
- volatility
- Sharpe
- Sortino
- win rate
- average win / average loss
- payoff ratio
- turnover
- transaction cost
- slippage cost
- exposure and cash history

Compare against explicit benchmarks such as KOSPI/KOSDAQ/S&P 500 rather than return alone.

## 7. Train / validation / walk-forward

Do not tune weights on the same period used for final evaluation.

Target process:
1. development/training window
2. validation window
3. walk-forward test
4. untouched holdout period where feasible

A factor-weight change is promoted only when improvement is reasonably stable across regimes and not just one short period.

## 8. Persistence direction

v1 defines in-memory schema and validation only.

Next persistence layer should support:
- append-only immutable snapshots
- schema version
- deterministic replay
- content hash / integrity check
- provider/source lineage
- query by `asOf`, market and symbol

Candidate local formats for research:
- Parquet for larger historical numeric datasets
- SQLite/DuckDB for metadata, queries and reproducible local research

The final storage choice will be benchmarked before adoption.
