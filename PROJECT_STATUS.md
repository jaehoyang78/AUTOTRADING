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
작업 브랜치: `feature/core-engine-v2`, PR #1

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
### P2 Tests
- factor별 경계값 테스트
- extreme value 테스트
- decision matrix 확대

### P3 Data Layer
- 데이터 공급자 비교 문서
- KR/US 시세·일봉 provider interface
- KOSPI/KOSDAQ/S&P500/Nasdaq
- VIX/VKOSPI
- 재무/밸류에이션/EPS Revision/Macro

### 이후
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
