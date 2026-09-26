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
- Quality/Growth/Value/Momentum/Revision 경계값 테스트
- extreme-value 테스트: factor/total score 0~100 범위 보장
- 데이터 공급자 비교/우선순위 `docs/data-sources.md`
- provider-agnostic 모델:
  - Quote / DailyBar / IndexBar / VolatilityPoint / MacroPoint
  - ProviderMeta / DataFreshness
- MarketDataProvider / MacroDataProvider 인터페이스
- Gradle main source에 `data/` 포함
- `PriceFeatureCalculator` 구현:
  - 20/60/120 trading-session 수익률
  - 20/60/200일 이동평균 상·하 여부
  - 252거래일 종가 기준 고점 대비 거리/낙폭
  - 당일 거래량 / 직전 20거래일 평균 거래량
  - 일봉 정렬/중복일 검증
  - 데이터 부족 시 신호를 추정하지 않고 null 반환
- `MarketInputFactory` 구현:
  - Index trend feature를 `HybridStrategyEngine.MarketInputs`로 변환
  - breadth/VIX/revision breadth/credit stress는 신뢰 가능한 데이터가 있을 때만 추가
  - 비율성 보조입력 0~100 경계 처리
- 가격특징/입력조립 단위테스트 추가

### 데이터 파이프라인 현재 구조
`외부 API → Provider Adapter → AUTOTRADING Data Models → Price/Market Feature Calculator → MarketInputs/StockInputs → Strategy Engine`

### 데이터 공급자 우선순위 v1
1. KIS: 한국/미국 시세·일봉
2. 지수 데이터 어댑터
3. FRED: 정책금리/국채금리/인플레이션/일부 유동성·신용 지표
4. VIX/VKOSPI
5. 저장된 종목 universe 기반 breadth 자체 계산
6. 재무/밸류에이션
7. Consensus/EPS Revision

원칙:
- 외부 vendor 응답 스키마는 adapter 내부에 격리
- 모든 데이터에 provider/observedAt/freshness 기록
- 신뢰할 수 없는 EPS Revision proxy 금지
- Market-data와 Broker order adapter 분리
- 데이터 부족은 false/zero 신호가 아니라 missing으로 보존

### 공식 출처 확인
- KIS Developers: REST 시세/기간별시세 및 WebSocket 기능 제공
- FRED API: 경제 시계열 observation 조회 제공
- Cboe: VIX historical closing data 제공

## 다음 작업
1. KIS market-data adapter skeleton + mock tests
2. FRED macro adapter skeleton + mock tests
3. KOSPI/KOSDAQ/S&P500/Nasdaq 실제 index mapping
4. 실제 데이터 smoke test → Market Regime 생성
5. 이후 market breadth / 재무 / valuation / consensus 순으로 확장

## 운영 원칙
- Chat: 요구사항, 설계, 작업분해, 검수
- Work: 저장소 탐색, 다단계 실행, 테스트/빌드
- Codex: 실제 코드 수정, 오류 해결
- GitHub: Issue/PR/버전/진행상태
- GitHub Actions: 자동 테스트/빌드

중요 변경 시 `DECISIONS.md`, `PROJECT_STATUS.md`, `TASKS.md`를 함께 업데이트한다.

## 재개 순서
`README.md` → `PROJECT_STATUS.md` → `DECISIONS.md` → `ARCHITECTURE.md` → `ROADMAP.md` → `TASKS.md` → 최근 PR/CI.
