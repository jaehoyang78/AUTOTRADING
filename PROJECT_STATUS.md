# Project Status — AUTOTRADING

## Canonical Repository
`jaehoyang78/AUTOTRADING`

## main 기준 완료
- PR #1 Core Engine v2
- PR #2 Data Foundation v1
- PR #3 Provider Adapters v1
- PR #4 FRED Live Data Gateway v1
- PR #5 KIS Live Market v1

핵심 완료 기능:
- Market Regime / 100점 Stock Score / Data Coverage
- Investor Fit / Thesis & Decision Engine
- 종목·섹터·포트폴리오 위험 가드와 Cash Target
- 공통 Quote/DailyBar/Index/Volatility/Macro 모델
- PriceFeatureCalculator / MarketInputFactory
- FRED live HTTP gateway
- KIS OAuth + KR/US 현재가/일봉 market-data gateway
- KRX/NASDAQ/NYSE/AMEX venue 모델
- GitHub Actions CI 및 단위테스트

## 2026-09-26 Macro Environment v1
작업 브랜치: `feature/macro-environment-v1`

### 완료
- FRED catalog에 `INDPRO` 산업생산 추가
- `MacroFeatureCalculator` 구현
  - 산업생산 YoY
  - 산업생산 YoY의 최근 3개월 방향
  - CPI YoY
  - CPI YoY의 최근 3개월 방향
  - Effective Fed Funds 최신값 / 약 3개월 변화
  - 10Y-2Y Treasury curve
  - Fed balance sheet 약 13주 변화율
  - USD/KRW 약 3개월 변화율
  - High Yield OAS 최신값
  - HY OAS → 0~100 credit-spread stress v1
- Growth/Inflation 4분면 v1
  - GROWTH_UP_INFLATION_DOWN
  - GROWTH_UP_INFLATION_UP
  - GROWTH_DOWN_INFLATION_DOWN
  - GROWTH_DOWN_INFLATION_UP
  - UNKNOWN
- 데이터가 부족하면 quadrant/trend를 억지로 추정하지 않고 UNKNOWN/null 유지
- `MacroSectorFitEngine` v1 휴리스틱 구현
  - Technology / Communications / Discretionary / Staples / Healthcare / Financials / Industrials / Energy / Materials / Utilities / Real Estate
  - quadrant별 기본 적합도
  - credit stress가 높으면 민감 업종에 추가 패널티
- `MarketInputFactory.withMacro()`로 HY OAS stress를 Market Regime의 credit stress 입력에 연결
- `StockInputEnricher.withMacroSectorFit()`로 Macro Fit 10점 입력 연결
- macro 계산 / missing data / OAS stress / sector fit / market-stock input integration 테스트 추가
- 브랜치 CI 성공 확인

### 중요한 해석 제한
- Macro 4분면은 현재 산업생산과 CPI의 YoY 추세를 이용한 v1 proxy이며, 시장의 ‘기대 대비 성장/물가 surprise’를 직접 측정하는 모델은 아니다.
- Sector Macro Fit 점수는 아직 연구용 휴리스틱이다. 백테스트/워크포워드 검증 전에는 이 점수만으로 자동주문을 허용하지 않는다.
- Credit stress의 OAS 2.5%→0점, 8%→100점 선형 mapping도 v1 휴리스틱이며 검증 대상이다.

## 다음 작업
1. KOSPI/KOSDAQ/S&P500/Nasdaq index mapping 및 provider
2. VIX/VKOSPI provider
3. Market breadth 계산기
4. 실제 KIS/FRED credential smoke-test 실행 경로
5. Historical snapshot schema / Backtest foundation
6. Macro sector-fit heuristic backtest

## 보안/운영 원칙
- AppKey/AppSecret/FRED key/계좌정보를 GitHub, Issue, fixture에 저장하지 않는다.
- Market-data와 Broker order adapter를 분리한다.
- CI는 외부 API나 비밀키 없이 통과 가능해야 한다.
- 중요한 변경 시 `DECISIONS.md`, `PROJECT_STATUS.md`, `TASKS.md`를 함께 업데이트한다.

## 재개 순서
`README.md` → `PROJECT_STATUS.md` → `DECISIONS.md` → `ARCHITECTURE.md` → `ROADMAP.md` → `TASKS.md` → 최근 PR/CI.
