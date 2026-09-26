# Project Status — AUTOTRADING

## Canonical Repository
`jaehoyang78/AUTOTRADING`

## main 기준 완료
- PR #1 Core Engine v2
- PR #2 Data Foundation v1
- PR #3 Provider Adapters v1
- PR #4 FRED Live Data Gateway v1
- PR #7 Macro Regime Integration v1
- PR #8 Verified KOSPI/KOSDAQ Index Mapping v1
- PR #9 Secure read-only live credential smoke workflow
- PR #10 Verified KIS S&P500 daily index mapping

핵심 완료 기능:
- Market Regime / 100점 Stock Score / Data Coverage
- Investor Fit / Thesis & Decision Engine
- 종목·섹터·포트폴리오 위험 가드와 Cash Target
- 공통 Quote/DailyBar/Index/Volatility/Macro 모델
- PriceFeatureCalculator / MarketInputFactory
- KIS/FRED gateway 경계와 정규화 provider adapter
- FRED live HTTP gateway
- MacroFeatureCalculator / MacroRegimeOverlay / MarketRegimeComposer
- KOSPI/KOSDAQ/S&P500 verified KIS index history
- secret-backed manual read-only live smoke workflow
- GitHub Actions CI 및 단위테스트

## 2026-09-26 Live Data / Index 상태
### KIS
- KR/US 현재가
- KR 일봉 OHLC/거래량
- US 일봉 종가
- KOSPI `0001`, KOSDAQ `1001`
- S&P500 해외지수 `SPX`
- Nasdaq Composite는 공식 KIS master symbol 확정 전 fail-closed
- VIX/VKOSPI는 공식/허용 가능한 공급자 검증 전 미지원

### FRED / Macro
- DFF, DGS2, DGS10, CPIAUCSL, DEXKOUS, WALCL, BAMLH0A0HYM2
- 정책금리, yield curve, CPI, 유동성, FX, HY OAS를 Macro Tailwind/Credit Stress로 정규화
- base Market Regime 85% + Macro 15%

### Live credential smoke
- 수동 `Live Credential Smoke` workflow 구현 완료
- KIS/FRED 실제 credential 성공 확인은 아직 미완료
- credential은 GitHub Secrets/환경변수에서만 사용

## 2026-09-26 Backtest Historical Snapshot v1
작업 브랜치: `feature/backtest-snapshot-v1`

### 구현
- `core/backtest/HistoricalSnapshot.kt`
- `HistoricalSnapshot`
- `HistoricalMarketInputs`
- `HistoricalStockSnapshot`
- `PointInTimeMeta`
- `UniverseMembership`
- `HistoricalSnapshotValidator`

### Point-in-time 핵심 규칙
각 전략 입력값은 값뿐 아니라 다음을 보존한다:
- effective date
- `availableAt`: 전략이 실제로 그 값을 알 수 있었던 최초 시점
- source
- revision/version ID (가능한 경우)

백테스트 입력 하드 규칙:

`availableAt <= snapshot.asOf`

위반 시 snapshot을 invalid 처리한다.

### Bias 방지
- 미래 발표 재무/컨센서스 사용 금지
- 오늘의 지수 구성종목을 과거에 소급하는 survivorship bias 차단
- universe membership 자체에도 `availableAt` 보존
- revised macro/fundamental 데이터는 revision/vintage 보존 방향
- 종가로 신호를 만들었다면 동일 종가에 이미 체결된 것으로 가정하지 않음

### 테스트
- point-in-time 정상 snapshot
- 미래 발표 fundamental → invalid
- 미래 universe membership → invalid
- duplicate symbol → invalid

### 문서
- `docs/backtest.md`에 look-ahead, survivorship, revision bias, 실행시점, 비용/슬리피지, walk-forward 원칙 기록

## 다음 작업
1. Repository Secrets 설정 후 KIS/FRED live smoke 성공 확인
2. Nasdaq Composite 공식 KIS master 코드 확정
3. Snapshot persistence: append-only + integrity hash + deterministic replay
4. Backtest benchmark / execution / transaction-cost model
5. VIX/VKOSPI + market breadth
6. 재무/밸류에이션/EPS Revision 데이터 공급자 연결

## 운영 원칙
- Chat: 요구사항·설계·작업분해·검수
- Work: 저장소 탐색·다단계 실행·테스트/빌드
- Codex: 실제 코드 수정·오류 해결
- GitHub: Issue/PR/버전/진행상태
- GitHub Actions: 자동 테스트/빌드

중요 변경 시 `DECISIONS.md`, `PROJECT_STATUS.md`, `TASKS.md`를 함께 업데이트한다.

## 재개 순서
`README.md` → `PROJECT_STATUS.md` → `DECISIONS.md` → `ARCHITECTURE.md` → `ROADMAP.md` → `TASKS.md` → 최근 PR/CI.
