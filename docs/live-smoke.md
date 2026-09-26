# Live Credential Smoke Test

Purpose: verify that AUTOTRADING can read live KIS and FRED data with real credentials **without placing orders** and without storing secrets in the repository.

## GitHub repository secrets

Configure these in GitHub repository settings before running the workflow:

- `KIS_APP_KEY`
- `KIS_APP_SECRET`
- `FRED_API_KEY`

Never paste these values into README, Issues, PR comments, source code, fixtures, or chat.

## Manual workflow

Workflow: `Live Credential Smoke`

It is `workflow_dispatch` only. Select one target:

- `KIS`
- `FRED`
- `BOTH`

The workflow validates that the required secrets exist and then runs:

```text
gradle liveSmoke --no-daemon
```

## What KIS smoke does

Read-only calls only:

1. Requests OAuth access token with `KIS_APP_KEY` / `KIS_APP_SECRET`.
2. Reads Samsung Electronics (`005930`) current quote.
3. Reads 5 KOSPI daily index bars using the verified `0001` index mapping.
4. Checks positive price, non-empty index history, and oldest-to-newest ordering.

It does **not** contain an account number and cannot place an order.

## What FRED smoke does

1. Reads `FRED_API_KEY` from the environment.
2. Fetches the latest 3 `DFF` observations.
3. Verifies the series is non-empty.

## Output policy

Allowed log output:
- provider success marker
- public symbol/series identifier
- public market price/date/count

Forbidden log output:
- KIS AppKey/AppSecret
- FRED API key
- account information
- access tokens

## Local execution

Use environment variables rather than command-line arguments or committed files.

KIS only:

```text
SMOKE_TARGET=KIS KIS_APP_KEY=... KIS_APP_SECRET=... gradle liveSmoke --no-daemon
```

FRED only:

```text
SMOKE_TARGET=FRED FRED_API_KEY=... gradle liveSmoke --no-daemon
```

Prefer OS/environment secret management. Do not save credentials in shell history where avoidable.

## Completion criterion

The task `actual credential smoke test` is complete only after the manual workflow has successfully run against the real services. Adding this workflow alone does not count as a successful live credential test.
