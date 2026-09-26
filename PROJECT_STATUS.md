# Project Status — AUTOTRADING

## Canonical Repository
`jaehoyang78/AUTOTRADING`

## main 기준 완료
- PR #1 Core Engine v2
- PR #2 Data Foundation v1
- PR #3 Provider Adapters v1
- PR #4 FRED Live Data Gateway v1
- PR #7 Macro Regime Integration v1

핵심 완료 기능:
- Market Regime / 100점 Stock Score / Data Coverage
- Investor Fit / Thesis & Decision Engine
- 종목·섹터·포트폴리오 위험 가드와 Cash Target
- 공통 Quote/DailyBar/Index/Volatility/Macro 모델
- PriceFeatureCalculator / MarketInputFactory
- KIS/FRED gateway 경계와 정규화 provider adapter
- FRED live HTTP gateway
- MacroFeatureCalculator / MacroRegimeOverlay / MarketRegimeComposer
- GitHub Actions CI 및 단위테스트

## 2026-09-26 KIS Live Market v1
### 완료
- `Venue`: KRX / NASDAQ / NYSE / AMEX
- venue-aware `MarketDataProvider`
- KIS OAuth token 발급/캐시
- 국내/미국 현재가
- 국내 일봉 OHLC/거래량
- 미국 일봉 종가
- market-data gateway와 broker 분리
- KIS 업무/HTTP 오류 fail-closed
- secret redaction

### 보안
- AppKey/AppSecret은 코드/커밋/Issue/fixture에 저장하지 않는다.
- 서버/CLI 환경변수: `KIS_APP_KEY`, `KIS_APP_SECRET`
- 실계좌 주문 모듈과 시세 gateway는 독립 유지한다.

## 2026-09-26 Macro Regime Integration v1
### 완료
- 정책금리 6개월 변화
- 10Y-2Y yield curve
- CPI YoY 및 방향
- Fed total assets 13주 변화
- KR용 USD/KRW 3개월 변화
- HY OAS → credit spread stress
- base Market Regime 85% + Macro Tailwind 15%
- missing macro → 기존 MarketAssessment 유지
- 합성 경로 `MarketRegimeComposer`

## 2026-09-26 Domestic Index Mapping v1
작업 브랜치: `feature/index-mapping-v1`

### 공식 KIS 매핑 확인
한국투자증권 공식 `open-trading-api` 예제 기준:
- KOSPI 업종코드: `0001`
- KOSDAQ 업종코드: `1001`
- Path: `/uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice`
- TR ID: `FHKUP03500100`
- `FID_COND_MRKT_DIV_CODE=U`
- 날짜 필드: `stck_bsop_date`
- 종가 필드: `bstp_nmix_prpr`

### 구현
- `KisHttpGateway.indexBars()`에서 KOSPI/KOSDAQ 지원
- 장기 추세(200일) 계산을 위해 날짜 커서를 이동하며 최대 10페이지 반복 조회
- 결과는 중복 날짜 제거 후 oldest-to-newest 정렬
- `limit`에 맞춰 최근 데이터만 반환
- KOSPI/KOSDAQ URL/TR-ID/index-code 테스트 추가

### Fail-closed 유지
- `SP500`, `NASDAQ_COMPOSITE`는 공식 KIS 지수 mapping이 검증되지 않아 여전히 `UnsupportedOperationException`
- VIX/VKOSPI도 검증된 KIS endpoint 전까지 미구현
- 추측한 심볼/TR-ID를 운영 코드에 넣지 않는다.

## 다음 작업
1. KIS/FRED 실제 credential smoke-test 실행 경로
2. S&P500/Nasdaq 공식 지수 mapping 추가 조사
3. VIX/VKOSPI + market breadth
4. 미국 일봉 거래량/OHLC 추가 검증
5. Historical snapshot schema / Backtest 시작
6. 재무/밸류에이션/EPS Revision 데이터 공급자 연결

## 운영 원칙
- Chat: 요구사항·설계·작업분해·검수
- Work: 저장소 탐색·다단계 실행·테스트/빌드
- Codex: 실제 코드 수정·오류 해결
- GitHub: Issue/PR/버전/진행상태
- GitHub Actions: 자동 테스트/빌드

중요 변경 시 `DECISIONS.md`, `PROJECT_STATUS.md`, `TASKS.md`를 함께 업데이트한다.

## 재개 순서
`README.md` → `PROJECT_STATUS.md` → `DECISIONS.md` → `ARCHITECTURE.md` → `ROADMAP.md` → `TASKS.md` → 최근 PR/CI.
