# Tasks — AUTOTRADING

프로젝트 재개 시 이 파일의 가장 높은 우선순위 미완료 항목부터 진행한다.

## P0 — 저장소/운영 기반
- [x] AUTOTRADING 신규 저장소 생성
- [x] canonical repository 확정
- [x] README / STATUS / DECISIONS / ARCHITECTURE / ROADMAP / TASKS 구성
- [x] 전략 상세문서 이전
- [x] 엔진 v1 이전
- [x] 기본 CI 생성
- [x] PR/브랜치 운영 규칙 문서화

## P1 — Core Engine
- [x] Market assessment prototype
- [x] Component scoring prototype
- [x] Total score / grade
- [x] Market risk multiplier
- [x] Basic risk penalty
- [x] Data coverage score
- [x] Investor fit score
- [x] Thesis state model
- [x] Buy/Add/Hold/Reduce/Sell decision rules
- [x] Portfolio equity exposure rule v1
- [x] Sector concentration rule v1 (25% 이상 증액 제한)
- [x] Cash target rules v1
- [x] Portfolio drawdown risk gate v1 (-15% 이하 증액 제한)

## P2 — 테스트
- [x] Market regime unit tests
- [x] Quality score boundary tests
- [x] Growth score boundary tests
- [x] Value score boundary tests
- [x] Momentum score boundary tests
- [x] Revision score boundary tests
- [x] Position sizing/decision smoke tests
- [x] Missing-data tests
- [x] Extreme-value tests
- [x] Panic regime tests
- [x] Broken-thesis SELL test
- [x] Sector-concentration risk test
- [x] Portfolio cash/exposure tests
- [ ] Decision matrix 확대 (BUY/ADD/HOLD/REDUCE/SELL/Avoid/Watch 전체 조합)

## P3 — 데이터 연결
### 공통
- [x] 데이터 공급자 비교/선정 초안 문서
- [x] provider-neutral Quote / DailyBar / Index / Volatility 모델
- [x] KR/US 시세·일봉 `MarketDataProvider` interface
- [ ] FakeMarketDataProvider 테스트 구현
- [ ] stale-data / timestamp validation layer

### 한국
- [ ] KIS MarketDataProvider adapter
- [ ] KOSPI/KOSDAQ 지수
- [ ] 국내 종목 일봉
- [ ] 거래량
- [ ] VKOSPI
- [ ] Market breadth
- [ ] 재무
- [ ] 밸류에이션
- [ ] 컨센서스/EPS Revision

### 미국
- [ ] KIS 또는 별도 US MarketDataProvider adapter
- [ ] S&P500/Nasdaq 지수
- [ ] 미국 종목 일봉
- [ ] 거래량
- [ ] VIX production source
- [ ] Market breadth
- [ ] 재무
- [ ] 밸류에이션
- [ ] EPS Revision

### Macro
- [ ] `MacroDataProvider` interface
- [ ] FRED adapter
- [ ] 정책금리
- [ ] 국채금리
- [ ] 인플레이션
- [ ] 달러/환율
- [ ] 유동성 proxy
- [ ] credit spread

## P4 — Backtest
- [ ] Historical snapshot schema
- [ ] Benchmark 정의
- [ ] Buffett portfolio
- [ ] Graham portfolio
- [ ] Lynch portfolio
- [ ] Momentum portfolio
- [ ] Hybrid portfolio
- [ ] Transaction cost
- [ ] Slippage
- [ ] Walk-forward
- [ ] CAGR/MDD/Sharpe/Sortino/Turnover report

## P5 — UI
- [ ] Dashboard
- [ ] Market Regime card
- [ ] Scanner
- [ ] Score breakdown
- [ ] Data coverage
- [ ] Stock Detail
- [ ] Investor fit chart
- [ ] Thesis/risk
- [ ] Portfolio
- [ ] Strategy Lab

## P6 — 자동화
- [ ] 장 시작 30분 후 스캔
- [ ] 장 마감 30분 전 스캔
- [ ] 추천 변경 감지
- [ ] 보유종목 score 급락 감지
- [ ] EPS revision 급변 감지
- [ ] 리밸런싱 알림
- [ ] 서버/푸시 구조

## P7 — Broker
- [ ] KIS adapter
- [ ] paper/simulation mode
- [ ] live mode 분리
- [ ] 계좌/키 secure storage
- [ ] fresh quote before order
- [ ] user approval
- [ ] duplicate-order guard
- [ ] audit log
- [ ] timeout no-retry rule

## 다음 최우선 작업
1. P2 Decision matrix 확대
2. P3 FakeMarketDataProvider + 데이터 검증 레이어
3. P3 KIS MarketDataProvider adapter
4. P3 MacroDataProvider + FRED adapter
5. 실제 시장 데이터로 Market Regime 연결

## 재개용 한 줄 지시
> `jaehoyang78/AUTOTRADING`의 README.md, PROJECT_STATUS.md, DECISIONS.md, ARCHITECTURE.md, ROADMAP.md, TASKS.md를 먼저 읽고 TASKS.md의 가장 높은 우선순위 미완료 항목부터 이어서 진행해. 중요한 결정 변경 시 DECISIONS.md와 PROJECT_STATUS.md도 함께 갱신해.
