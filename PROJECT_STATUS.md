# Project Status — AUTOTRADING

## Canonical Repository
`jaehoyang78/AUTOTRADING`

## main 기준 완료
- PR #1 Core Engine v2
- PR #2 Data Foundation v1
- PR #3 Provider Adapters v1

핵심 완료 기능:
- Market Regime / 100점 Stock Score / Data Coverage
- Investor Fit / Thesis & Decision Engine
- 종목·섹터·포트폴리오 위험 가드와 Cash Target
- 공통 Quote/DailyBar/Index/Volatility/Macro 모델
- PriceFeatureCalculator / MarketInputFactory
- KIS/FRED gateway 경계와 정규화 provider adapter
- GitHub Actions CI 및 단위테스트

## 2026-09-26 Live Data Gateways v1
작업 브랜치: `feature/live-data-gateways-v1`

### 완료
- injectable `HttpTransport` / JDK HTTP 구현
- FRED `series/observations` 실제 REST 호출용 `FredHttpGateway` 구현
- `FRED_API_KEY` 환경변수 주입 경로 제공
- JSON missing value(`.`)를 신호로 변조하지 않고 건너뜀
- HTTP 2xx가 아니면 fail-closed
- 네트워크 없이 fixture로 HTTP/JSON parsing 테스트
- 최신 Maven Central `org.json:json:20260814` 사용
- FRED macro series catalog v1:
  - DFF: Effective Federal Funds Rate
  - DGS2 / DGS10: 2Y / 10Y Treasury
  - CPIAUCSL: CPI
  - WALCL: Fed total assets liquidity proxy
  - DEXKOUS: USD/KRW
  - BAMLH0A0HYM2: US High Yield OAS

### 중요한 데이터 주의
- FRED macro는 발표주기와 revision을 고려해야 하며 단순 실시간 데이터처럼 취급하지 않는다.
- historical backtest에는 당시 이용 가능했던 값(vintage/point-in-time)을 고려해야 한다.
- BAMLH0A0HYM2는 2026년 4월부터 FRED에서 제공되는 과거 관측치가 3년으로 제한된다는 공식 안내가 있으므로 장기 백테스트 소스로 그대로 사용하지 않는다.

## 다음 작업
1. KIS live HTTP/OAuth market-data gateway
2. 미국 종목 venue(NASDAQ/NYSE/AMEX) 모델
3. macro feature calculator (금리곡선, CPI YoY, 유동성/credit 변화)
4. 실제 FRED/KIS smoke test → Market Regime 생성
5. KOSPI/KOSDAQ/S&P500/Nasdaq index mapping

## 보안/운영 원칙
- AppKey/AppSecret/FRED key/계좌정보를 GitHub, Issue, 테스트 fixture에 저장하지 않는다.
- Market-data와 Broker order adapter를 분리한다.
- CI는 외부 API나 비밀키 없이 통과 가능해야 한다.
- 중요한 변경 시 `DECISIONS.md`, `PROJECT_STATUS.md`, `TASKS.md`를 함께 업데이트한다.

## 재개 순서
`README.md` → `PROJECT_STATUS.md` → `DECISIONS.md` → `ARCHITECTURE.md` → `ROADMAP.md` → `TASKS.md` → 최근 PR/CI.
