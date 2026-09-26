# Project Status — AUTOTRADING

## Canonical Repository
`jaehoyang78/AUTOTRADING`

## main 기준 완료
- PR #1 Core Engine v2
- PR #2 Data Foundation v1
- PR #3 Provider Adapters v1
- PR #4 FRED Live Data Gateway v1

핵심 완료 기능:
- Market Regime / 100점 Stock Score / Data Coverage
- Investor Fit / Thesis & Decision Engine
- 종목·섹터·포트폴리오 위험 가드와 Cash Target
- 공통 Quote/DailyBar/Index/Volatility/Macro 모델
- PriceFeatureCalculator / MarketInputFactory
- KIS/FRED gateway 경계와 정규화 provider adapter
- FRED live HTTP gateway
- GitHub Actions CI 및 단위테스트

## 2026-09-26 KIS Live Market v1
작업 브랜치: `feature/kis-live-market-v1`

### 완료
- `Venue` 모델 추가: KRX / NASDAQ / NYSE / AMEX
- `MarketDataProvider`를 venue-aware 인터페이스로 변경
- KR은 KRX를 기본 venue로 강제
- US는 NASDAQ/NYSE/AMEX 명시 없이는 조회 금지
- `KisInstrument` / `KisVenue` 도입
- `HttpTransport` POST 지원 추가
- 비밀값을 출력하지 않는 `KisCredentials` 추가
- 환경변수 이름 확정: `KIS_APP_KEY`, `KIS_APP_SECRET`
- `KisHttpGateway` OAuth token 발급/캐시 구현
- token 만료 60초 전 갱신
- 국내 현재가: `inquire-price` / `FHKST01010100`
- 미국 현재가: overseas price / `HHDFS00000300`
- 미국 venue → KIS EXCD: NASDAQ=NAS, NYSE=NYS, AMEX=AMS
- 국내 일봉: `inquire-daily-itemchartprice` / `FHKST03010100`
- 미국 일봉 종가: `dailyprice` / `HHDFS76240000`
- 국내 일봉 OHLC/거래량은 확인된 필드만 파싱
- 미국 일봉은 검증된 `xymd`/`clos`만 사용하고 미검증 OHLC/거래량은 null 유지
- KIS `rt_cd != 0`, HTTP 비정상 응답은 fail-closed
- OAuth token 재사용 / venue 매핑 / 일봉 정렬 / 오류 / secret redaction 단위테스트
- 기존 provider/price feature 테스트를 venue 모델에 맞게 갱신
- 브랜치 CI 성공 확인

### 의도적으로 미구현
- KIS index API mapping: 정확한 엔드포인트/TR ID 검증 전 미구현
- KIS volatility API mapping: 정확한 VKOSPI/VIX 경로 검증 전 미구현
- Broker 주문: 이 gateway에 절대 포함하지 않음
- 미국 일봉 OHLC/volume: 필드 검증 전 임의 파싱 금지

### 보안
- AppKey/AppSecret은 코드/커밋/Issue/fixture에 저장하지 않는다.
- `KisCredentials.toString()`은 항상 마스킹한다.
- HTTP 오류/업무 오류 메시지에 credential을 포함하지 않는다.
- 실계좌 주문 모듈과 시세 gateway는 독립 유지한다.

## 2026-09-26 Macro Regime Integration v1
작업 브랜치: `feature/data-layer-v1`

### 추가
- `MacroFeatureCalculator`
  - 정책금리 6개월 변화
  - 10Y-2Y yield curve
  - CPI YoY 및 3개월 방향
  - Fed total assets 13주 변화
  - KR용 USD/KRW 3개월 변화
  - High Yield OAS 기반 credit stress
- 누락 데이터는 0점 처리하지 않고 사용 가능한 macro component만 재정규화
- KR/US macro coverage 계산
- `MacroRegimeOverlay`
  - 기존 Market Regime 점수 85% + Macro Tailwind 15%
  - macro가 없으면 기존 MarketAssessment를 그대로 유지
- `MarketRegimeComposer`
  - Price trend + supplementary breadth/volatility + credit stress + macro overlay를 한 경로로 조립
- favorable/adverse/missing macro 단위테스트 추가

### 설계 의도
기존 Market Regime 엔진을 직접 크게 변경하지 않고 매크로를 독립 overlay로 둔다. 매크로 산식은 향후 백테스트에서 교체/조정하기 쉽고, 데이터 누락 시 기존 엔진이 그대로 작동한다.

## 다음 작업
1. KOSPI/KOSDAQ/S&P500/Nasdaq index mapping 검증
2. 실제 KIS/FRED credential smoke-test 실행 경로
3. VIX/VKOSPI + market breadth
4. 미국 일봉 거래량/OHLC 추가 검증
5. 재무/밸류에이션 데이터 공급자 연결
6. Historical snapshot schema / Backtest 시작

## 운영 원칙
- Chat: 요구사항·설계·작업분해·검수
- Work: 저장소 탐색·다단계 실행·테스트/빌드
- Codex: 실제 코드 수정·오류 해결
- GitHub: Issue/PR/버전/진행상태
- GitHub Actions: 자동 테스트/빌드

중요 변경 시 `DECISIONS.md`, `PROJECT_STATUS.md`, `TASKS.md`를 함께 업데이트한다.

## 재개 순서
`README.md` → `PROJECT_STATUS.md` → `DECISIONS.md` → `ARCHITECTURE.md` → `ROADMAP.md` → `TASKS.md` → 최근 PR/CI.
