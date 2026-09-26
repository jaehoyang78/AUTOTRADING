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
- [x] Price feature calculator tests
- [x] Market input assembly tests
- [x] Provider adapter normalization tests
- [x] FRED HTTP parser/error tests
- [x] KIS OAuth/quote/daily/venue/error tests
- [x] Macro feature / regime overlay tests
- [x] KOSPI/KOSDAQ index mapping tests
- [x] S&P500 overseas-index mapping tests
- [x] Historical point-in-time snapshot validation tests

## P3 — 데이터 연결
### 기반
- [x] 데이터 공급자 비교/우선순위 문서 v1
- [x] Provider-agnostic Quote/DailyBar/Index/Volatility/Macro 모델
- [x] MarketDataProvider 인터페이스
- [x] MacroDataProvider 인터페이스
- [x] DailyBar → 20/60/120일 수익률 계산
- [x] IndexBar → 20/60/200일 추세 계산
- [x] 52주 고점 대비 거리/낙폭 계산
- [x] 20일 거래량 비율 계산
- [x] normalized feature → MarketInputs assembly
- [x] KIS gateway/adapter skeleton
- [x] FRED gateway/adapter skeleton
- [x] injectable JDK HTTP transport
- [x] POST support for OAuth
- [x] Venue model: KRX / NASDAQ / NYSE / AMEX
- [x] Macro feature 계산 및 Market Regime 반영
- [x] 수동 live credential smoke-test 실행 경로 (`Live Credential Smoke` workflow)

### 한국
- [x] KIS live HTTP/OAuth market-data gateway
- [x] KOSPI/KOSDAQ 지수 mapping (KOSPI=0001, KOSDAQ=1001, FHKUP03500100)
- [x] 국내 종목 현재가 연결 코드
- [x] 국내 종목 일봉 연결 코드
- [x] 국내 일봉 거래량 파싱
- [ ] 실제 KIS credential smoke test 성공 확인
- [ ] VKOSPI
- [ ] Market breadth
- [ ] 재무
- [ ] 밸류에이션
- [ ] 컨센서스/EPS Revision

### 미국
- [x] KIS US live HTTP market-data gateway
- [x] 미국 거래소/venue 식별 규칙
- [x] S&P500 공식 해외지수 API mapping (`SPX`, `FHKST03030100`)
- [ ] Nasdaq Composite 지수 코드 — 공식 KIS master에서 확정 전 fail-closed
- [x] 미국 종목 현재가 연결 코드
- [x] 미국 종목 일봉 종가 연결 코드
- [ ] 미국 일봉 거래량/OHLC 추가 검증
- [ ] 실제 KIS credential smoke test 성공 확인
- [ ] VIX
- [ ] Market breadth
- [ ] 재무
- [ ] 밸류에이션
- [ ] EPS Revision

### Macro
- [x] FRED live HTTP gateway
- [x] 정책금리 series config: DFF
- [x] 국채금리 series config: DGS2 / DGS10
- [x] 인플레이션 series config: CPIAUCSL
- [x] 달러/환율 series config: DEXKOUS
- [x] 유동성 proxy series config: WALCL
- [x] credit spread series config: BAMLH0A0HYM2
- [x] macro feature 계산 및 Market Regime 반영
- [ ] 실제 FRED credential smoke test 성공 확인

## P4 — Backtest
- [x] Historical snapshot schema v1 (asOf / availableAt / source / revision / universe membership)
- [ ] Snapshot persistence (append-only + integrity hash)
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
- [ ] KIS broker adapter
- [ ] paper/simulation mode
- [ ] live mode 분리
- [ ] 계좌/키 secure storage
- [ ] fresh quote before order
- [ ] user approval
- [ ] duplicate-order guard
- [ ] audit log
- [ ] timeout no-retry rule

## 다음 최우선 작업
1. GitHub Secrets 구성 후 KIS/FRED live smoke 성공 확인
2. Nasdaq Composite 공식 KIS master 코드 확정
3. VIX/VKOSPI + market breadth
4. Snapshot persistence + benchmark/backtest runner
5. 미국 일봉 거래량/OHLC 추가 검증

## 재개용 한 줄 지시
> `jaehoyang78/AUTOTRADING`의 README.md, PROJECT_STATUS.md, DECISIONS.md, ARCHITECTURE.md, ROADMAP.md, TASKS.md를 먼저 읽고 TASKS.md의 가장 높은 우선순위 미완료 항목부터 이어서 진행해. 중요한 결정 변경 시 DECISIONS.md와 PROJECT_STATUS.md도 함께 갱신해.
