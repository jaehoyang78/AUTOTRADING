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

## Core Engine v2 — main 반영 완료
PR #1이 CI 성공 후 `main`에 squash merge 됐다.

완료 기능:
- Market Regime / 100점 Stock Score
- Data Coverage gate
- Investor Fit
- Thesis/Decision Engine
- 개별종목·섹터·포트폴리오 위험 가드
- 시장국면별 Cash Target / Max Equity Exposure
- GitHub Actions CI 및 핵심 단위테스트

## 2026-09-26 Data Foundation v1
작업 브랜치: `feature/data-foundation-v1`

### 완료
- Quality/Growth/Value/Momentum/Revision 경계값 테스트 추가
- 비정상적으로 큰/작은 입력에서도 factor score와 total score가 0~100 범위를 벗어나지 않는 extreme-value 테스트 추가
- 데이터 공급자 비교 및 우선순위 문서 `docs/data-sources.md` 작성
- provider-agnostic 데이터 모델 작성:
  - Quote
  - DailyBar
  - IndexBar
  - VolatilityPoint
  - MacroPoint
  - ProviderMeta / DataFreshness
- 시장/거시 공급자 인터페이스 작성:
  - MarketDataProvider
  - MacroDataProvider
- Gradle main source에 `data/` 포함

### 데이터 공급자 우선순위 v1
1. KIS: 한국/미국 시세·일봉 우선
2. 지수 데이터 어댑터
3. FRED: 정책금리/국채금리/인플레이션/일부 유동성·신용 지표
4. VIX/VKOSPI
5. 저장된 종목 universe 기반 breadth 자체 계산
6. 재무/밸류에이션
7. Consensus/EPS Revision

원칙:
- 외부 vendor 응답 스키마는 adapter 내부에 격리
- 코어는 AUTOTRADING 공통 모델만 사용
- 모든 데이터는 provider/observedAt/freshness 메타데이터를 가짐
- 신뢰할 수 없는 EPS Revision 데이터는 임의 proxy로 채우지 않고 Coverage 부족으로 표시
- Broker 주문 API와 MarketDataProvider를 분리

### 공식 출처 확인
- KIS Developers: REST 시세/기간별시세 및 WebSocket 기능 제공
- FRED API: 경제 시계열 observation 조회 제공
- Cboe: VIX historical closing data 제공

## 다음 작업
1. KIS market-data adapter skeleton + HTTP transport 분리
2. FRED macro adapter skeleton
3. DailyBar 기반 20/60/200일 추세·수익률·52주 낙폭 계산
4. Market Regime 입력 생성기
5. provider adapter mock tests

## 이후
- 실제 API credential을 저장소에 넣지 않는 로컬/서버 설정
- 실제 데이터 smoke test
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
