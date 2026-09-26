# Project Status — AUTOTRADING

## Canonical Repository
`jaehoyang78/AUTOTRADING`

이 저장소가 하이브리드 투자전략 프로젝트의 정식 기준 저장소다.

## 현재 목표
유명 투자자들의 장점을 조합해 시장상황별로 전략 가중치를 바꾸는 개인용 하이브리드 투자 의사결정 시스템을 구축한다.

핵심 목표:
1. 시장 국면 자동 판단
2. 한국/미국 종목 스크리닝
3. Quality / Growth / Value / Momentum / Earnings Revision / Macro Fit 점수화
4. 투자자별 적합도 계산
5. 종목별 추천 비중 및 행동 계산
6. 포트폴리오 현금/익스포저 관리
7. 가상 포트폴리오 병렬 운용
8. 사용자 승인형 실제 주문

## 2026-09-26 Core Engine v2
초기 작업 브랜치: `feature/core-engine-v2`, PR #1 병합 완료.

### 완료
- Kotlin/JVM Gradle 프로젝트 초기화
- GitHub Actions `Core CI` 생성 및 테스트 성공 확인
- `CONTRIBUTING.md`에 feature/fix 브랜치 + PR 운영규칙 기록
- Stock Data Coverage 구현
- Coverage 부족 시 신규 추천비중 0 처리
- Investor Fit: Buffett / Graham / Lynch / Soros-Momentum / Druckenmiller
- ThesisState: STRONG / INTACT / WEAKENING / BROKEN / UNKNOWN
- DecisionAction: BUY / ADD / HOLD / REDUCE / SELL / AVOID / WATCH
- Thesis BROKEN → 즉시 SELL / 목표 0%
- 섹터비중 25% 이상 → 해당 종목 증액 제한
- 포트폴리오 drawdown -15% 이하 → 신규 위험 확대 제한
- 개별 종목 추천비중 상한 7%
- PortfolioRiskEngine 추가
- 시장국면별 현금 목표 및 최대 주식 익스포저 계산
- 낙폭에 따른 추가 현금 버퍼
- 단위테스트 추가

### Data Coverage v2
24개 종목 입력 기준:
- 전체 필드 50% 이상
- 6개 핵심 factor group 중 4개 이상
- Quality와 Momentum 필수

미충족 시 점수는 참고용으로 유지하되 추천비중은 0으로 둔다.

### Portfolio Risk v1
기본 현금 목표:
- RISK_ON_STRONG 10%
- RISK_ON_NORMAL 15%
- NEUTRAL 25%
- RISK_OFF 40%
- PANIC 35%

낙폭 버퍼:
- -10% 이하 +5%
- -15% 이하 +10%
- -20% 이하 +15%

현금 목표 상한/하한은 10~60%. 최대 주식비중은 `100 - 현금목표`.

### Decision Engine 우선순위
1. Thesis BROKEN → SELL
2. Coverage 부족 → WATCH
3. 종합점수/추천비중 계산
4. 섹터 집중도 제한
5. 포트폴리오 drawdown gate
6. Thesis WEAKENING 시 목표비중 축소
7. 현재비중과 목표비중 차이로 BUY/ADD/HOLD/REDUCE 판단

## 2026-09-26 Validation & Data Layer Foundation
현재 `feature/core-engine-v2`에서 PR #1 이후 추가 작업 진행 중.

### 테스트 강화
- Quality / Growth / Value / Momentum / Revision 경계값 테스트 추가
- extreme input clipping 테스트 추가
- missing factor가 0점이 아니라 `null`로 유지되는지 확인
- BUY / ADD / HOLD / REDUCE / SELL / AVOID / WATCH 전체 Decision matrix 테스트 추가

### Data Layer 기반
- `core/data/MarketDataProvider.kt`
  - Quote
  - DailyBar
  - IndexSnapshot
  - VolatilitySnapshot
  - MarketDataProvider
- `core/data/MarketDataValidation.kt`
  - 미래 시각 관측 거부
  - 가격/거래량 기본 범위 검증
  - stale quote 명시
  - 일봉 oldest-to-newest 순서 검증
- `tests/FakeMarketDataProvider.kt`
  - 외부 API 없이 deterministic data-layer 테스트 가능
- `docs/data-sources.md`
  - KIS를 KR/US 1차 시장데이터 및 향후 주문 연동 후보로 선정
  - FRED를 Macro 1차 후보로 선정
  - Cboe VIX는 historical/reference 데이터로 사용하고 production live source는 별도 확정

### 현재 데이터 원칙
- 전략 엔진은 공급자 응답 형식을 직접 알지 않는다.
- 모든 외부 데이터는 provider-neutral 모델로 변환한다.
- 데이터에는 source와 observation timestamp를 보존한다.
- missing data를 임의로 0으로 대체하지 않는다.
- stale data는 최신 데이터처럼 조용히 사용하지 않는다.
- 주문 어댑터와 시장데이터 어댑터는 같은 KIS를 사용하더라도 별도 모듈로 유지한다.

## 기존 투자 프레임
- Graham → Value / Margin of Safety
- Buffett/Munger → Quality
- Lynch → GARP
- Marks → Cycle / Risk
- Dalio → Macro Regime
- Soros → Trend / Reflexivity
- Druckenmiller → Regime / Conviction
- Thorp → Position Sizing / Expected Value

Stock Score: Quality 25, Growth 20, Value 20, Momentum 15, Revision 10, Macro Fit 10.

## 다음 작업
### P3 Data Layer
1. KIS MarketDataProvider adapter
2. provider 공통 오류/재시도 정책
3. MacroDataProvider interface + FRED adapter
4. KOSPI/KOSDAQ/S&P500/Nasdaq 및 VIX/VKOSPI 입력 연결
5. 실제 데이터로 Market Regime 계산

### 이후
- Fundamentals / valuation / EPS Revision 공급자 확정
- Backtest / Walk-forward
- Portfolio / Thesis persistence
- Mobile UI
- 알림
- KIS Broker adapter

## 운영 원칙
- Chat: 요구사항, 설계, 작업분해, 검수
- Work: 저장소 탐색, 다단계 실행, 테스트/빌드
- Codex: 실제 코드 수정, 오류 해결
- GitHub: Issue/PR/버전/진행상태
- GitHub Actions: 자동 테스트/빌드

중요 변경 시 `DECISIONS.md`, `PROJECT_STATUS.md`, `TASKS.md`를 함께 업데이트한다.

## 재개 순서
`README.md` → `PROJECT_STATUS.md` → `DECISIONS.md` → `ARCHITECTURE.md` → `ROADMAP.md` → `TASKS.md` → 최근 PR/CI.
