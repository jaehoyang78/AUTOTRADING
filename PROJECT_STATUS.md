# Project Status — AUTOTRADING

Canonical repository: `jaehoyang78/AUTOTRADING`

## 현재 목표 — 추천 앱 v1

2026-09-27 기준 범위를 **실제 종목 추천에 필요한 기능**으로 좁혔다. 최우선 작업은 재무·밸류에이션 공급자 설계와 구현이다. 이후 기존 엔진 입력 정리, 실제 데이터 연결 검증, 한국·미국 각각 20~50개 종목 점수화, TOP 10, 최소 UI와 간단한 검증을 순차 진행한다.

v1은 아직 완료되지 않았다. 재무 연결·추천 목록·UI가 남아 있으며 실제 KIS/FRED credential 성공도 확인되지 않았다. v1에는 주문/broker 코드, paper trading, 스케줄러·알림을 추가하지 않는다.

## 확보한 구현과 테스트

| 영역 | 현재 구현 |
|---|---|
| 전략 엔진 | 5단계 Market Regime, 100점 Stock Score, Data Coverage, Investor Fit, Thesis/Decision, 추천비중·현금·집중도·낙폭 가드 |
| 데이터 기반 | Quote/DailyBar/Index/Volatility/Macro 공통 모델, PriceFeatureCalculator, MarketInputFactory, provider/gateway 경계 |
| KIS 읽기전용 데이터 | KR/US 현재가, KR 일봉 OHLC/거래량, US 일봉 종가, KOSPI `0001`, KOSDAQ `1001`, S&P500 `SPX` 공식 매핑 |
| FRED / Macro | DFF, DGS2, DGS10, CPIAUCSL, DEXKOUS, WALCL, BAMLH0A0HYM2. Macro feature와 base 85% + macro 15% overlay |
| 검증 경로 | 단위테스트, GitHub Actions CI, 수동 `Live Credential Smoke` workflow |
| 기존 backtest 기반 | Historical snapshot schema, `availableAt <= asOf` 검증, universe membership 시점, in-memory append-only store, SHA-256 integrity hash와 테스트 |

기존 구현 이력: PR #1 Core Engine, #2 Data Foundation, #3 Provider Adapters, #4 FRED Live Gateway, #7 Macro Integration, #8 KOSPI/KOSDAQ 매핑, #9 읽기전용 live smoke, #10 S&P500 매핑. 코드/테스트 구현 이력은 실계정 연결 성공을 의미하지 않는다.

## 검증 상태와 알려진 빈칸

- **실제 KIS/FRED credential smoke 성공은 미확인.** 키는 GitHub Secrets/환경변수로만 주입하며 코드·fixture·문서에 저장하지 않는다. 실행 방법은 [docs/live-smoke.md](docs/live-smoke.md)를 따른다.
- 재무·밸류에이션 provider와 실제 종목 점수 입력 연결이 아직 필요하다.
- US 일봉 OHLC/거래량은 검증된 필드만 사용하는 현재 동작을 유지한다.
- Nasdaq Composite는 공식 코드 확인 전 fail-closed, VIX/VKOSPI는 미지원이다. 이 기능들과 breadth 고도화는 v2+이다.
- 신뢰 가능한 컨센서스/EPS Revision이 없으면 missing으로 유지한다. 확보하지 않은 값을 채워 Coverage를 높이지 않는다.
- 기존 snapshot 코드는 보존하지만 durable persistence, backtest runner, walk-forward와 투자자별 가상포트폴리오는 v2+로 미룬다.

## 다음 작은 PR

1. 재무·밸류에이션 공급자: KR/US 지원 범위·필드 의미·접근 조건, 공통 모델과 정규화 구현, fixture 테스트와 CI.
2. 재무·가격·거시 → 실제 종목 점수 연결 및 샘플 평가. 실제 credential 검증은 결과를 별도로 기록한다.
3. 추천 TOP 10 → 세 화면 UI → 간단한 v1 검증을 각각 분리해서 진행한다.

현재 작업은 [TASKS.md](TASKS.md), v2+ 항목은 [ROADMAP.md](ROADMAP.md)에 기록한다. 문서의 기존 “Core Engine v2” 등은 모듈 이력이며, 현재 추천 앱의 v1/v2 출시 범위와 구분한다.

## 운영·재개 원칙

한 PR에 하나의 목적을 담고 테스트/CI 결과를 확인한다. 중요한 결정과 상태 변경은 `DECISIONS.md`, `PROJECT_STATUS.md`, `TASKS.md`에 반영한다.

재개 시 `README.md` → `PROJECT_STATUS.md` → `DECISIONS.md` → `ARCHITECTURE.md` → `ROADMAP.md` → `TASKS.md` → 최근 PR/CI 순서로 읽는다.
