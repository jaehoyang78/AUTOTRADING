# Project Status — AUTOTRADING

## Canonical Repository
`jaehoyang78/AUTOTRADING`

## main 기준 완료
- PR #1 Core Engine v2
- PR #2 Data Foundation v1
- PR #3 Provider Adapters v1
- PR #4 FRED Live Data Gateway v1
- PR #7 Macro Regime Integration v1
- PR #8 Verified KOSPI/KOSDAQ Index Mapping v1
- PR #9 Secure read-only live credential smoke workflow

핵심 완료 기능:
- Market Regime / 100점 Stock Score / Data Coverage
- Investor Fit / Thesis & Decision Engine
- 종목·섹터·포트폴리오 위험 가드와 Cash Target
- 공통 Quote/DailyBar/Index/Volatility/Macro 모델
- PriceFeatureCalculator / MarketInputFactory
- KIS/FRED gateway 경계와 정규화 provider adapter
- FRED live HTTP gateway
- MacroFeatureCalculator / MacroRegimeOverlay / MarketRegimeComposer
- KOSPI/KOSDAQ verified KIS index history
- secret-backed manual read-only live smoke workflow
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
### 공식 KIS 매핑
- KOSPI `0001`
- KOSDAQ `1001`
- `/uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice`
- TR ID `FHKUP03500100`
- `FID_COND_MRKT_DIV_CODE=U`
- date `stck_bsop_date`
- close `bstp_nmix_prpr`

### 구현
- KOSPI/KOSDAQ `indexBars()` 지원
- 200일 추세 확보를 위한 날짜 커서 반복 조회
- 중복 날짜 제거 및 oldest-to-newest 정렬

## 2026-09-26 Live Credential Smoke Path v1
### 구현
- `tools/LiveSmoke.kt`
- Gradle `liveSmoke` verification task
- GitHub Actions 수동 workflow: `Live Credential Smoke`
- 실행 대상: `KIS`, `FRED`, `BOTH`
- KIS: 삼성전자 현재가 + KOSPI 5일봉 읽기 전용 확인
- FRED: DFF 최근 observations 확인

### 상태
- smoke 실행 경로 구축 완료
- 실제 credential을 이용한 live smoke 성공 확인은 아직 미완료
- 키는 source/fixture/Issue/PR/chat에 남기지 않는다.

## 2026-09-26 U.S. Index Mapping v1
작업 브랜치: `feature/us-index-mapping-v1`

### 공식 KIS 자료에서 확인
- 해외 종목/지수/환율 기간별 시세 API path: `/uapi/overseas-price/v1/quotations/inquire-daily-chartprice`
- TR ID: `FHKST03030100`
- 해외지수 구분: `FID_COND_MRKT_DIV_CODE=N`
- 일봉 날짜: `stck_bsop_date`
- 해외지수 종가: `ovrs_nmix_prpr`
- 공식 해외지수 예제에서 S&P500 지수 코드 `SPX` 사용 확인

### 구현
- `MarketIndex.SP500` → `SPX`
- 해외지수 일봉 조회 및 날짜 커서 반복 조회
- 중복 날짜 제거, oldest-to-newest 정렬
- S&P500 URL/TR-ID/구분코드/지수코드/응답 필드 단위테스트

### Fail-closed 유지
- `MarketIndex.NASDAQ_COMPOSITE`는 공식 `frgn_code.mst`의 정확한 심볼을 아직 확정하지 못해 미지원
- 일반적인 외부 서비스의 `IXIC`, `CCMP`, `COMP` 등을 KIS 코드라고 추측하지 않는다.
- VIX/VKOSPI도 공식 매핑 검증 전 미지원

## 다음 작업
1. Repository Secrets 설정 후 KIS/FRED live smoke 성공 확인
2. Nasdaq Composite 공식 KIS master 코드 확정
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
