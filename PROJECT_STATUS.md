# Project Status — AUTOTRADING

## Canonical Repository

`jaehoyang78/AUTOTRADING`

이 저장소가 이제 하이브리드 투자전략 프로젝트의 정식 기준 저장소다. 기존 `tradingbot`의 `feature/hybrid-strategy-engine-v1` 브랜치는 과거 임시 보관 위치이며 앞으로 신규 개발 기준으로 사용하지 않는다.

## 현재 목표

유명 투자자들의 장점을 조합해 시장상황별로 전략 가중치를 바꾸는 개인용 하이브리드 투자 의사결정 시스템을 구축한다.

핵심 목표:

1. 시장 국면 자동 판단
2. 한국/미국 종목 스크리닝
3. Quality / Growth / Value / Momentum / Earnings Revision / Macro Fit 점수화
4. 투자자별 적합도 계산
5. 종목별 추천 비중 계산
6. 가상 포트폴리오 병렬 운용
7. 사용자 승인형 실제 주문
8. 장기적으로 알림/자동감시/조건부 주문 자동화

## 현재까지 완료

### 저장소 분리
- [x] 신규 독립 저장소 `AUTOTRADING` 생성
- [x] 정식 기준 저장소 확정
- [x] README 이전 및 AUTOTRADING 기준으로 수정
- [x] 기존 tradingbot과 분리 원칙 확정

### 투자 철학 정의

- Benjamin Graham → 안전마진 / Value
- Warren Buffett / Charlie Munger → Quality / 장기 경쟁우위
- Peter Lynch → GARP / Growth
- Howard Marks → 사이클 / 시장온도 / Risk
- Ray Dalio → Macro Regime
- George Soros → Trend / Reflexivity
- Stanley Druckenmiller → Regime 대응 / Conviction
- Edward Thorp → Position Sizing / Expected Value

### Market Regime 초안

Market Score 0~100:

- 80~100: RISK_ON_STRONG
- 65~79: RISK_ON_NORMAL
- 40~64: NEUTRAL
- 20~39: RISK_OFF
- 0~19: PANIC

초기 입력:
- 20/60/200일 추세
- 52주 고점 대비 낙폭
- Market Breadth
- VIX/VKOSPI
- EPS Revision Breadth
- Credit Spread Stress

### 종목 100점 체계

- Quality 25
- Growth 20
- Value 20
- Momentum 15
- Earnings Revision 10
- Macro/Sector Fit 10

### 포지션 사이징 초안

- 90+: 7%
- 85~89: 5%
- 80~84: 3%
- 75~79: 2%
- 70~74: 1%
- 70 미만: 신규진입 없음

Market Regime에 따라 risk multiplier를 곱한다.

### 안전장치 초안

- EPS 성장률 < -20%: 감점
- 3개월 EPS Revision < -15%: 감점
- Debt/Equity > 250%: 감점
- PANIC에서도 Quality가 낮으면 신규매수 금지
- 자동 주문보다 사용자 승인형 주문 우선

### 코드 상태

`core/HybridStrategyEngine.kt`

현재 구현 기능:
- `assessMarket()`
- Market Score
- Market Regime
- Risk multiplier
- Quality/Growth/Value/Momentum/Revision/Macro Fit component score
- Hard risk penalty
- Total Score / Grade
- Base Position
- Recommended Position
- 주요 이유 요약

이 코드는 v1 프로토타입이며 이후 순수 core module 구조로 리팩터링한다.

## 현재 미완료

- Data Coverage score
- Investor Fit Score
- Thesis state model
- Buy/Add/Hold/Reduce/Sell decision engine
- Portfolio concentration rules
- Sector concentration rules
- Cash target rules
- 실제 데이터 소스 확정
- Market Score 실시간 데이터 연결
- 재무 데이터 연결
- EPS Revision 데이터 연결
- 가상포트폴리오 저장소/DB
- 백테스트 엔진
- UI
- 알림
- KIS 주문 연동
- CI 테스트

## 다음 작업 우선순위

### P1 — Core Engine 안정화
1. Data Coverage
2. Investor Fit
3. Decision rules
4. Position/sector concentration
5. Cash target
6. 단위테스트

### P2 — 데이터 레이어
1. 시세
2. 일봉
3. 시장지수
4. 변동성
5. 재무
6. 컨센서스
7. 섹터/매크로

### P3 — 검증
- Unit test
- Historical backtest
- Walk-forward test
- Transaction cost
- Slippage
- Survivorship bias 확인

### P4 — 앱
- Dashboard
- Scanner
- Stock Detail
- Portfolio
- Strategy Lab

### P5 — 자동화
- 장 시작 30분 후 스캔
- 장 마감 30분 전 스캔
- 추천 변경 알림
- 보유종목 thesis 변화 감지
- 사용자 승인형 주문

## 프로젝트 운영 원칙

- Chat: 요구사항, 설계, 작업분해, 검수
- Work: 저장소 탐색, 다단계 실행, 테스트/빌드
- Codex: 실제 코드 수정, 오류 해결
- GitHub: Issue/PR/버전/진행상태
- GitHub Actions: 자동 테스트/빌드

중요 변경 시 반드시 함께 업데이트:
- `DECISIONS.md`
- `PROJECT_STATUS.md`
- `TASKS.md`

## 재개할 때 가장 먼저 확인할 것

1. `README.md`
2. `PROJECT_STATUS.md`
3. `DECISIONS.md`
4. `ARCHITECTURE.md`
5. `ROADMAP.md`
6. `TASKS.md`
7. 최근 PR/커밋

그 후 가장 높은 우선순위의 미완료 작업부터 이어간다.
