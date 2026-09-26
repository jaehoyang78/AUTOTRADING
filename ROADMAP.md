# Roadmap — AUTOTRADING

## v1 — 실제 종목 추천이 나오는 앱

**목표:** 한국·미국 실제 종목을 평가하고 시장상태, 추천 TOP 10, 상세점수를 보여준다. 주문과 자동화는 포함하지 않는다.

진행 순서:

1. 재무·밸류에이션 데이터 공급자를 설계·구현한다. 매출/EPS 성장률, ROE/ROIC, PER/PBR, 부채비율, FCF를 우선 확보하며 공급자가 제공하지 않는 값은 결측으로 남긴다.
2. 기존 전략 엔진을 정리하고 재무·가격·거시 입력을 연결한다. 100점 점수, 등급, 판단, 추천비중, Investor Fit, 이유·위험과 Coverage를 일관되게 반환한다.
3. 기존 KIS 시세·KOSPI/KOSDAQ/S&P500, FRED 거시 연결을 실제 credential로 검증한다. 구현 완료와 실제 연결 성공은 별도로 기록한다.
4. 한국·미국 각각 20~50개 실제 종목으로 추천 TOP 10을 만든다. 적격 후보가 부족하면 부족한 수와 이유를 그대로 표시한다.
5. 최소 UI를 만든다: 오늘 시장상태 / 추천 TOP 10 / 종목 상세점수.
6. 단위테스트, 샘플 수동 대조, 추천 smoke와 CI로 v1을 검증한다.

완료 조건:

- 실제 데이터의 출처·기준시점·결측/신선도를 확인할 수 있다.
- 미확보 EPS Revision 등은 임의 proxy로 채우지 않으며 Coverage와 기존 위험 가드가 추천에 반영된다.
- 세 화면에서 실제 평가 결과와 추천 이유·위험을 확인할 수 있다.
- 테스트/CI가 통과하고 실제 연결 검증 결과를 기록했다. credential이 없으면 해당 live 검증은 미완료로 남긴다.
- 주문·broker·모의매매·스케줄러·알림 없이 수동 실행으로 사용할 수 있다.

v1의 간단한 검증은 추천 계산과 데이터 연결의 정확성을 확인한다. 전략 수익성 검증, 가중치 최적화와 walk-forward는 다음 단계에서 수행한다.

## v2+ — v1 이후 차근차근

| 단계 | 범위 | 시작/완료 기준 |
|---|---|---|
| A. 데이터 확장·심화 검증 | VIX/VKOSPI, breadth 고도화, Nasdaq Composite, 미국 일봉 OHLC/거래량, 컨센서스/EPS Revision. Durable snapshot 저장소, 벤치마크·체결·비용/슬리피지, walk-forward와 성과 리포트 | v1 추천 흐름이 동작한 뒤 필요한 항목부터 진행. 공급자 접근 조건과 시점 정합성을 검증 |
| B. 전략·포트폴리오 비교 | Buffett/Graham/Lynch/Momentum/Hybrid 가상포트폴리오, 보유종목/thesis 관리, Strategy Lab, 제한적 가중치 조정 | 동일 조건의 전략 비교와 위험/성과 근거를 확보한 뒤 변경 |
| C. 자동화·알림 | 장 시작 30분 후/마감 30분 전 스캔, 추천·점수·thesis·Revision 변화, 리밸런싱, 서버·푸시 | 수동 추천 흐름의 신뢰성을 확인한 뒤 스케줄·장애 대응 검증 |
| D. 매매 | Paper trading → 사용자 승인형 KIS 주문 → 충분한 검증 이후 제한적 Live 자동화 검토 | 별도 broker 모듈, Paper/Live 분리, fresh quote, 승인, 중복주문 방지, timeout 재시도 금지 검증 |

Historical snapshot schema, point-in-time validator, in-memory append-only store는 이미 있는 기반으로 보존한다. 이를 확장하는 작업은 v1을 막지 않는다.

## 범위와 진행 관리

[TASKS.md](TASKS.md)는 당장 할 일, [PROJECT_STATUS.md](PROJECT_STATUS.md)는 구현/검증 현황, [DECISIONS.md](DECISIONS.md)는 결정 근거를 기록한다. [ARCHITECTURE.md](ARCHITECTURE.md)와 기존 전략 문서의 포트폴리오·자동화·주문 구조는 장기 설계이며 이 로드맵이 현재 출시 범위를 정한다.

작은 PR을 순차적으로 진행한다. 범위 정리, 재무 공급자, 점수 연결, 추천, UI를 각각 검토 가능한 단위로 나눈다.
