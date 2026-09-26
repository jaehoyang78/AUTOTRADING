# Tasks — AUTOTRADING

현재 목표는 **실제 한국·미국 종목을 평가하고 추천을 보여주는 v1**이다. 아래 v1 항목을 순서대로 작은 PR로 진행한다. v2+ 항목은 기록만 유지하며 v1 완료 조건에 넣지 않는다.

## v1 — 지금 필요한 작업

1. [ ] **재무·밸류에이션 공급자 설계와 구현 — 최우선**: KR/US 지원 범위와 접근 조건을 확인하고, 공통 모델·provider·정규화 경계를 구현한다. 매출/EPS 성장률, ROE/ROIC, PER/PBR, 부채비율, FCF를 우선 연결하고 출처·기준시점·결측을 보존한다. fixture 기반 테스트와 CI를 통과한다.
2. [ ] 전략 엔진 입력 정리: 재무·가격·거시 입력을 기존 100점 계산에 연결한다. 누락 항목은 임의 값으로 채우지 않으며 기존 Coverage/위험 가드를 유지한다. 신뢰할 수 있는 EPS Revision이 없으면 missing으로 둔다.
3. [ ] 실제 시세·거시 연결 검증: 기존 KIS KR/US 시세, KOSPI/KOSDAQ/S&P500, FRED 경로를 확인한다. 실제 credential smoke 성공을 별도로 기록하고, 키가 없으면 미검증 상태를 명시한다.
4. [ ] 한국·미국 각각 20~50개 실제 종목 평가: 재현 가능한 종목 목록으로 가격·재무 입력을 결합하고 점수, 등급, 판단, 추천비중, 이유·위험, Coverage를 생성한다.
5. [ ] 추천 TOP 10: 데이터·위험 기준을 통과한 후보를 순위화한다. 적격 후보가 10개 미만이면 그 수만 표시하며 데이터 부족 종목을 추천으로 채우지 않는다.
6. [ ] 최소 UI 3개 화면: 오늘 시장상태, 추천 TOP 10, 종목 상세점수. 데이터 기준시점·미확보 항목과 추천 이유·위험을 함께 보여준다.
7. [ ] 간단한 검증과 v1 완료 확인: 핵심 계산/정규화/결측 테스트, 샘플 종목 수동 대조, 추천 생성 smoke, CI를 통과한다. 네트워크 없는 테스트와 실제 데이터 연결 결과를 구분한다.

v1에는 주문·broker 구현, 모의매매, 스케줄러·알림을 추가하지 않는다. KIS는 읽기전용 시세 공급자로 사용한다. v1 추천 결과를 투자성과가 검증된 전략으로 표현하지 않는다.

## v2+ — 나중에 순차 진행

- 시장 데이터 확장: VIX/VKOSPI, market breadth 고도화, Nasdaq Composite 공식 코드, 미국 일봉 OHLC/거래량 추가 검증, 신뢰 가능한 컨센서스/EPS Revision 공급자.
- 심화 검증: durable snapshot 저장소(SQLite/DuckDB/Parquet 등), deterministic replay 확장, 벤치마크·체결·비용/슬리피지 모델, walk-forward, 성과 리포트.
- 전략 비교·포트폴리오: Buffett/Graham/Lynch/Momentum/Hybrid 가상포트폴리오, 실제 보유종목 관리, Strategy Lab, 검증에 따른 가중치 조정.
- 자동화: 장 시작 30분 후/마감 30분 전 스캔, 추천·점수·thesis·EPS Revision 변화 감지, 리밸런싱 알림, 서버·푸시.
- 매매: paper trading 이후 사용자 승인형 KIS 실주문. 별도 broker 모듈, Paper/Live 분리, 보안저장소, fresh quote, 중복주문 방지, audit log, timeout 재주문 금지를 함께 검증한다.

## 이미 확보한 기반

- Market Regime, 100점 Stock Score, Investor Fit, Thesis/Decision, Coverage, 비중·현금·집중도·낙폭 가드와 단위테스트.
- 공통 시세·지수·거시 모델, 가격 feature, KIS/FRED gateway·adapter, Macro overlay, 공식 KOSPI/KOSDAQ/S&P500 매핑.
- GitHub Actions CI와 수동 읽기전용 live smoke 경로. **실제 KIS/FRED credential 성공은 미확인**이다.
- Historical snapshot의 point-in-time 검증, in-memory append-only store와 SHA-256 테스트. 기존 구현은 보존하며 추가 저장소/백테스트 개발은 v2+로 미룬다.

상세 현황은 [PROJECT_STATUS.md](PROJECT_STATUS.md), 단계별 완료 조건은 [ROADMAP.md](ROADMAP.md)를 따른다.

## 재개 순서

`README.md` → `PROJECT_STATUS.md` → `DECISIONS.md` → `ARCHITECTURE.md` → `ROADMAP.md` → `TASKS.md` → 최근 PR/CI를 읽고, v1의 가장 높은 미완료 항목부터 이어간다. 한 PR에는 하나의 검토 가능한 목적만 담는다.
