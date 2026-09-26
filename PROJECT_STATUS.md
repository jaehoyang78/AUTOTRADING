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
- S&P500/Nasdaq/VIX/VKOSPI는 공식 매핑 검증 전 fail-closed

## 2026-09-26 Live Credential Smoke Path v1
작업 브랜치: `feature/live-smoke-v1`

### 구현
- `tools/LiveSmoke.kt`
- Gradle `liveSmoke` verification task
- GitHub Actions 수동 workflow: `Live Credential Smoke`
- 실행 대상: `KIS`, `FRED`, `BOTH`

### KIS smoke — 읽기 전용
- `KIS_APP_KEY`, `KIS_APP_SECRET`을 환경변수/GitHub Secrets에서만 읽음
- 삼성전자 `005930` 현재가 조회
- KOSPI 최근 5개 지수 일봉 조회
- 양수 가격, non-empty index, 시간순 정렬 확인
- 계좌번호/주문 메서드 사용 없음

### FRED smoke — 읽기 전용
- `FRED_API_KEY`를 환경변수/GitHub Secrets에서만 읽음
- DFF 최근 3개 observation 조회
- non-empty 확인

### 중요한 상태 구분
- smoke **실행 경로 구축은 완료**
- 실제 credential을 이용한 live smoke **성공 확인은 아직 미완료**
- 키는 source/fixture/Issue/PR/chat에 남기지 않는다.

## 다음 작업
1. Repository Secrets 설정 후 KIS/FRED live smoke 성공 확인
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
