# AUTOTRADING — Hybrid Investor Engine

개인 겸업 투자자를 위한 한국·미국 주식 의사결정 앱입니다. 현재 목표는 **실제 데이터로 종목을 평가하고 추천을 보여주는 v1**입니다.

```text
시세·거시·재무 데이터 → 시장상태 → 종목 100점 평가 → 위험·Coverage 확인 → 추천 TOP 10
```

v1 화면은 세 가지입니다: **오늘 시장상태, 추천 TOP 10, 종목 상세점수**. 종목별 등급, 판단, 추천비중, 투자 스타일 적합도, 추천 이유·위험과 데이터 기준시점/부족 항목을 보여줍니다. 주문/broker 코드, paper trading, 스케줄러·알림은 v2+ 범위입니다.

## 현재 진행상황

전략 엔진, 읽기전용 KIS KR/US 시세와 KOSPI/KOSDAQ/S&P500 매핑, FRED 거시 연결 코드, 단위테스트와 CI 기반이 있습니다. **실제 KIS/FRED credential 성공은 아직 확인되지 않았고, 추천 앱 v1은 개발 중입니다.**

다음 최우선 작업은 **재무·밸류에이션 공급자 설계와 구현**입니다. 매출/EPS 성장률, ROE/ROIC, PER/PBR, 부채비율, FCF를 우선 연결합니다. 이후 한국·미국 각각 20~50개 종목으로 점수를 계산하고 TOP 10과 최소 UI를 완성합니다.

- [TASKS.md](TASKS.md): 지금 할 v1 작업과 v2+ 보류 항목
- [ROADMAP.md](ROADMAP.md): 출시 범위와 완료 조건
- [PROJECT_STATUS.md](PROJECT_STATUS.md): 구현 및 실제 연결 검증 상태

## 전략의 핵심

시장 국면과 종목의 매력을 분리해서 평가합니다. Graham의 가치, Buffett/Munger의 퀄리티, Lynch의 성장, Marks/Dalio의 사이클·거시, Soros/Druckenmiller의 추세·국면 대응, Thorp의 비중 원칙을 모듈로 조합합니다.

| 종목 점수 구성 | 가중치 |
|---|---:|
| Quality | 25 |
| Growth | 20 |
| Value | 20 |
| Momentum | 15 |
| Earnings Revision | 10 |
| Macro / Sector Fit | 10 |

시장점수는 0~100이며 강한 상승/정상 상승/중립/방어/패닉의 5단계로 분류합니다. 점수별 기본비중에 시장 위험배수를 반영하고 기존 집중도·현금·낙폭 가드를 적용합니다. PANIC 자체는 매수 신호가 아니며 투자논리와 퀄리티를 함께 확인합니다.

데이터가 없으면 0점으로 평가하거나 임의 proxy로 채우지 않습니다. 확보한 항목끼리 재정규화하고 Coverage를 별도 표시하며, 핵심 데이터가 부족하면 신규매수 추천비중을 0으로 제한합니다. EPS Revision 데이터가 없으면 missing으로 남깁니다. v1의 단위테스트·샘플 검증은 투자성과 검증을 뜻하지 않습니다.

상세 전략은 [docs/hybrid-strategy-v1.md](docs/hybrid-strategy-v1.md), 계산·데이터 결정은 [DECISIONS.md](DECISIONS.md)를 참고합니다. 기존 전략 문서와 [ARCHITECTURE.md](ARCHITECTURE.md)의 자동화·주문·가상포트폴리오는 장기 설계이며 현재 출시 범위는 [ROADMAP.md](ROADMAP.md)가 정합니다.

## v2+ 기록

VIX/VKOSPI, breadth 고도화, Nasdaq Composite, 심화 백테스트 저장소/walk-forward, 투자자별 가상포트폴리오, 자동 스캔·알림, paper trading과 사용자 승인형 KIS 실주문은 v1 이후 순차 진행합니다. 기존 historical snapshot/store 구현은 보존합니다. 향후 주문은 읽기전용 데이터 공급자와 분리된 broker 모듈에서 다룹니다.

## 개발과 검증

Kotlin/JVM 프로젝트이며 JDK 17과 Gradle을 사용합니다.

```sh
gradle test
```

단위테스트는 실제 네트워크와 비밀키 없이 실행합니다. 실제 연결 확인은 수동 읽기전용 [Live Credential Smoke](docs/live-smoke.md)로 별도 수행합니다. credential은 GitHub Secrets/환경변수로 주입하고 코드·fixture·로그에 저장하지 않습니다.

저장소 주요 경로: `core/` 전략 계산, `data/` 공급자·정규화, `tests/` 테스트, `tools/` 검증 도구, `.github/workflows/` CI. UI 경로는 아직 없습니다. 공급자 설계는 [docs/data-sources.md](docs/data-sources.md), 개발 규칙은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참고합니다.

작은 PR을 순차적으로 진행하고, 한 PR에 하나의 검토 가능한 목적만 담습니다. 재개 시 README → PROJECT_STATUS → DECISIONS → ARCHITECTURE → ROADMAP → TASKS → 최근 PR/CI를 읽고 v1의 가장 높은 미완료 항목부터 진행합니다.

**Canonical repository: `jaehoyang78/AUTOTRADING`**
