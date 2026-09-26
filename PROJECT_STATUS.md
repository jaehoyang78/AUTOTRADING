# Project Status — AUTOTRADING

## Canonical Repository
`jaehoyang78/AUTOTRADING`

이 저장소가 하이브리드 투자전략 프로젝트의 정식 기준 저장소다.

## 완료된 기준선

### Core Engine v2 — main
PR #1 CI 성공 후 main 반영.
- Market Regime / 100점 Stock Score
- Data Coverage gate
- Investor Fit
- Thesis/Decision Engine
- 종목·섹터·포트폴리오 위험 가드
- Cash Target / Max Equity Exposure

### Data Foundation v1 — main
PR #2 CI 성공 후 main 반영.
- provider-agnostic Quote/DailyBar/Index/Volatility/Macro 모델
- MarketDataProvider / MacroDataProvider
- factor boundary/extreme-value tests
- PriceFeatureCalculator
- MarketInputFactory
- KIS/FRED/Cboe 데이터소스 전략 문서

## 2026-09-26 Provider Adapters v1
작업 브랜치: `feature/provider-adapters-v1`

### 완료
- `KisGateway` 저수준 계약 정의
- `KisMarketDataProvider` 정규화 어댑터 구현
- KR/US quote와 daily bar를 공통 AUTOTRADING 모델로 변환하는 경계 확립
- index/volatility gateway 경계 정의
- `FredGateway` 저수준 계약 정의
- `FredMacroDataProvider` 정규화 어댑터 구현
- provider 응답 날짜를 정렬하고 요청 limit을 적용
- 잘못된 limit/series input 방어
- fake KIS/FRED gateway를 이용한 CI 가능한 adapter 단위테스트 추가

### 설계 의도
`실제 OAuth/HTTP/JSON 파싱 → Gateway → Provider Adapter → 공통 데이터 모델 → Feature Calculator → Strategy Engine`

따라서:
- CI는 실제 API 키가 없어도 adapter를 검증할 수 있다.
- 실계좌/AppKey/AppSecret을 GitHub에 저장할 필요가 없다.
- KIS API 변경은 live gateway에서 흡수하고 전략 엔진까지 전파하지 않는다.
- FRED API 키/HTTP 역시 live gateway에서만 처리한다.

## 다음 작업
1. FRED live HTTP gateway + macro series configuration
2. KIS live HTTP/OAuth gateway (시세 전용; broker 주문과 분리)
3. 미국 종목 venue(NASDAQ/NYSE/AMEX) 식별 모델 확정
4. KOSPI/KOSDAQ/S&P500/Nasdaq index mapping
5. 실제 데이터 smoke test → Market Regime 생성

## 이후
- VIX/VKOSPI
- market breadth
- 재무/밸류에이션
- consensus/EPS Revision
- historical snapshot/backtest
- mobile UI/alerts
- KIS broker adapter (최종 사용자 승인형)

## 운영 원칙
- Chat: 요구사항·설계·작업분해·검수
- Work: 저장소 탐색·다단계 실행·테스트/빌드
- Codex: 실제 코드 수정·오류 해결
- GitHub: Issue/PR/버전/진행상태
- GitHub Actions: 자동 테스트/빌드

중요 변경 시 `DECISIONS.md`, `PROJECT_STATUS.md`, `TASKS.md`를 함께 업데이트한다.

## 재개 순서
`README.md` → `PROJECT_STATUS.md` → `DECISIONS.md` → `ARCHITECTURE.md` → `ROADMAP.md` → `TASKS.md` → 최근 PR/CI.
