# Project Status — AUTOTRADING

## Canonical Repository

`jaehoyang78/AUTOTRADING`

이 저장소가 하이브리드 투자전략 프로젝트의 정식 기준 저장소다. 기존 `tradingbot`의 임시 브랜치는 과거 보관 위치이며 신규 개발 기준으로 사용하지 않는다.

## 현재 목표

유명 투자자들의 장점을 조합해 시장상황별로 전략 가중치를 바꾸는 개인용 하이브리드 투자 의사결정 시스템을 구축한다.

핵심 목표:
1. 시장 국면 자동 판단
2. 한국/미국 종목 스크리닝
3. Quality / Growth / Value / Momentum / Earnings Revision / Macro Fit 점수화
4. 투자자별 적합도 계산
5. 종목별 추천 비중 및 행동 계산
6. 가상 포트폴리오 병렬 운용
7. 사용자 승인형 실제 주문
8. 장기적으로 알림/자동감시/조건부 주문 자동화

## 2026-09-26 Core Engine v2 진행현황

작업 브랜치: `feature/core-engine-v2`

### 완료
- Kotlin/JVM Gradle 프로젝트 초기화
- GitHub Actions `Core CI` 생성
- CI에서 Gradle 테스트 자동 실행
- `CONTRIBUTING.md`에 feature/fix 브랜치 + PR 운영규칙 기록
- Stock Data Coverage 구현
- Coverage 부족 시 신규 추천비중 0 처리
- Investor Fit 구현: Buffett / Graham / Lynch / Soros-Momentum / Druckenmiller
- ThesisState 구현: STRONG / INTACT / WEAKENING / BROKEN / UNKNOWN
- DecisionAction 구현: BUY / ADD / HOLD / REDUCE / SELL / AVOID / WATCH
- Thesis BROKEN → 즉시 SELL / 목표 0%
- 섹터비중 25% 이상 → 해당 종목 위험 증액 제한
- 포트폴리오 drawdown -15% 이하 → 신규 위험 확대 제한
- 개별 종목 추천비중 7% 상한 유지
- Market data coverage 표시
- 단위테스트 추가
- CI 테스트 성공 확인 (Core CI run #2)

### v2 Data Coverage 규칙

24개 종목 입력 기준:
- 전체 필드 50% 이상 확보
- 6개 핵심 factor group 중 4개 이상 확보
- Quality와 Momentum 그룹 필수

미충족 시 점수 자체는 참고용으로 계산할 수 있지만 `recommendedPositionPct = 0`으로 두고 `데이터 보강`으로 표시한다.

### v2 Decision Engine 우선순위

1. Thesis BROKEN → SELL
2. Coverage 부족 → WATCH
3. 종합점수/추천비중 계산
4. 섹터 집중도 제한
5. 포트폴리오 drawdown gate
6. Thesis WEAKENING 시 목표비중 축소
7. 현재비중과 목표비중 차이로 BUY/ADD/HOLD/REDUCE 판단

## 기존 완료 항목

### 투자 철학
- Benjamin Graham → 안전마진 / Value
- Warren Buffett / Charlie Munger → Quality
- Peter Lynch → GARP / Growth
- Howard Marks → Cycle / Risk
- Ray Dalio → Macro Regime
- George Soros → Trend / Reflexivity
- Stanley Druckenmiller → Regime / Conviction
- Edward Thorp → Position Sizing / Expected Value

### Market Regime
- 80~100 RISK_ON_STRONG
- 65~79 RISK_ON_NORMAL
- 40~64 NEUTRAL
- 20~39 RISK_OFF
- 0~19 PANIC

### Stock Score
- Quality 25
- Growth 20
- Value 20
- Momentum 15
- Earnings Revision 10
- Macro/Sector Fit 10

### Position Sizing
- 90+ 7%
- 85~89 5%
- 80~84 3%
- 75~79 2%
- 70~74 1%
- 70 미만 신규진입 없음
- Market Regime risk multiplier 적용

## 현재 미완료 / 다음 작업

### P1 Core Engine
1. 포트폴리오 전체 집중도 규칙
2. Cash target rules
3. 섹터/종목 상관관계 기반 위험조정(후속)

### P2 Tests
- Quality/Growth/Value/Momentum/Revision 경계값 테스트
- extreme value 테스트
- 추가 decision matrix 테스트

### P3 Data Layer
- 데이터 공급자 비교 문서
- KR/US 시세·일봉
- KOSPI/KOSDAQ/S&P500/Nasdaq
- VIX/VKOSPI
- 재무/밸류에이션
- EPS Revision
- Macro data

### 이후
- Backtest / Walk-forward
- Portfolio / Thesis persistence
- Mobile UI
- 알림
- KIS Broker adapter

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

## 재개 순서

1. `README.md`
2. `PROJECT_STATUS.md`
3. `DECISIONS.md`
4. `ARCHITECTURE.md`
5. `ROADMAP.md`
6. `TASKS.md`
7. 최근 PR/CI

그 후 `TASKS.md`의 가장 높은 우선순위 미완료 작업부터 이어간다.
