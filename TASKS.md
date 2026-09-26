# Tasks — AUTOTRADING

프로젝트 재개 시 이 파일의 가장 높은 우선순위 미완료 항목부터 진행한다.

## P0 — 저장소/운영 기반
- [x] AUTOTRADING 신규 저장소 생성
- [x] canonical repository 확정
- [x] README / STATUS / DECISIONS / ARCHITECTURE / ROADMAP / TASKS 구성
- [x] 전략 상세문서 이전
- [x] 엔진 v1 이전
- [ ] 기본 CI 생성
- [ ] PR/브랜치 운영 규칙 문서화

## P1 — Core Engine
- [x] Market assessment prototype
- [x] Component scoring prototype
- [x] Total score / grade
- [x] Market risk multiplier
- [x] Basic risk penalty
- [ ] Data coverage score
- [ ] Investor fit score
- [ ] Thesis state model
- [ ] Buy/Add/Hold/Reduce/Sell decision rules
- [ ] Portfolio concentration rules
- [ ] Sector concentration rules
- [ ] Cash target rules

## P2 — 테스트
- [ ] Market regime unit tests
- [ ] Quality score tests
- [ ] Growth score tests
- [ ] Value score tests
- [ ] Momentum score tests
- [ ] Revision score tests
- [ ] Position sizing tests
- [ ] Missing-data tests
- [ ] Extreme-value tests
- [ ] Panic regime tests

## P3 — 데이터 연결
### 한국
- [ ] KOSPI/KOSDAQ 지수
- [ ] 국내 종목 일봉
- [ ] 거래량
- [ ] VKOSPI
- [ ] Market breadth
- [ ] 재무
- [ ] 밸류에이션
- [ ] 컨센서스/EPS Revision

### 미국
- [ ] S&P500/Nasdaq 지수
- [ ] 미국 종목 일봉
- [ ] 거래량
- [ ] VIX
- [ ] Market breadth
- [ ] 재무
- [ ] 밸류에이션
- [ ] EPS Revision

### Macro
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

## 재개용 한 줄 지시

> `jaehoyang78/AUTOTRADING`의 README.md, PROJECT_STATUS.md, DECISIONS.md, ARCHITECTURE.md, ROADMAP.md, TASKS.md를 먼저 읽고 TASKS.md의 가장 높은 우선순위 미완료 항목부터 이어서 진행해. 중요한 결정 변경 시 DECISIONS.md와 PROJECT_STATUS.md도 함께 갱신해.
