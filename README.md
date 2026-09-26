# AUTOTRADING — Hybrid Investor Engine

개인 겸업 주식투자자를 위한 하이브리드 투자 의사결정 및 자동화 시스템입니다.

이 프로젝트의 목표는 특정 투자자 한 명의 방식을 그대로 복제하는 것이 아니라, 유명 투자자들의 강점을 모듈화하고 시장 국면에 따라 전략 가중치를 바꾸는 것입니다.

핵심 흐름:

```text
Market Data
→ Market Regime
→ Stock Screening
→ Multi-factor Scoring
→ Investor-style Fit
→ Risk / Thesis Check
→ Position Sizing
→ Recommendation
→ User Approval
→ Broker Order
→ Performance Review
→ Strategy Improvement
```

최종 목표는 단순 종목추천 앱이 아니라, 데이터 수집부터 시장판단·종목선별·위험관리·포트폴리오·알림·주문·성과복기·전략개선까지 이어지는 개인 투자 운영체계입니다.

## 투자 철학 모듈

- Benjamin Graham — Value / Margin of Safety
- Warren Buffett & Charlie Munger — Quality / Durable Economics
- Peter Lynch — GARP / Growth at a Reasonable Price
- Howard Marks — Cycle / Market Temperature / Risk
- Ray Dalio — Macro Regime
- George Soros — Trend / Reflexivity
- Stanley Druckenmiller — Regime Adaptation / Conviction
- Edward Thorp — Probability / Position Sizing

## 핵심 투자 원칙

1. 싼 이유만으로 저품질 기업을 사지 않는다.
2. 좋은 기업도 가격이 지나치게 비싸면 기다린다.
3. 성장성 없는 저평가주는 가치함정 가능성을 우선 확인한다.
4. 실적과 가격 추세가 함께 악화되면 위험을 줄인다.
5. 과열장에서는 공격성을 낮춘다.
6. 공포·패닉장에서는 우량주를 더 적극적으로 찾되 저품질주는 제외한다.
7. 가격이 하락했다는 이유만으로 물타기하지 않는다.
8. 추가매수는 투자논리·퀄리티·실적전망 유지가 확인될 때만 한다.
9. 수익 가능성보다 생존과 손실 가능성을 먼저 계산한다.
10. 현금도 하나의 포지션으로 본다.
11. 단일 종목·단일 섹터 과집중을 피한다.
12. 예측보다 대응을 우선한다.
13. 전략 전환은 감정이 아니라 사전 정의된 데이터 조건으로 한다.
14. 주문 자동화보다 분석·추천 자동화를 먼저 완성한다.
15. 전략 변경은 백테스트와 워크포워드 검증 후에만 적용한다.

## Market Regime Engine

Market Score: 0~100

| Score | Regime | 의미 |
|---:|---|---|
| 80–100 | RISK_ON_STRONG | 강한 상승 환경 |
| 65–79 | RISK_ON_NORMAL | 정상 상승 환경 |
| 40–64 | NEUTRAL | 횡보/혼조 |
| 20–39 | RISK_OFF | 방어적 환경 |
| 0–19 | PANIC | 극단적 스트레스 |

초기 입력 가중치:

| 입력 | 가중치 |
|---|---:|
| 20일 추세 | 10 |
| 60일 추세 | 15 |
| 200일 추세 | 20 |
| 52주 고점 대비 낙폭 | 15 |
| Market Breadth | 15 |
| VIX / VKOSPI | 10 |
| EPS Revision Breadth | 10 |
| Credit Spread Stress | 5 |

기본 위험배수:

- RISK_ON_STRONG: 1.00
- RISK_ON_NORMAL: 0.90
- NEUTRAL: 0.70
- RISK_OFF: 0.55
- PANIC: 0.65

PANIC은 무조건 매수 신호가 아닙니다. 퀄리티와 투자논리가 유지되는 우량주만 분할매수 후보가 됩니다.

## Stock Score — 100점

| Component | Weight |
|---|---:|
| Quality | 25 |
| Growth | 20 |
| Value | 20 |
| Momentum | 15 |
| Earnings Revision | 10 |
| Macro / Sector Fit | 10 |
| Total | 100 |

### Quality
ROIC, ROE, 영업이익률, FCF 지속성, Debt/Equity, 이익 안정성

### Growth
매출 성장률, EPS 성장률, 영업이익 성장률, Forward EPS 성장률

### Value
PER vs 업종/자기과거, PBR, EV/EBITDA, FCF Yield, PEG

### Momentum
20/60/120일 수익률, 상대강도, 거래량 증가, 52주 고점과 거리

### Earnings Revision
3개월 EPS 추정치 변화, 상향/하향 추정치 비율

### Macro / Sector Fit
현재 성장·물가·금리·유동성 환경과 업종 민감도의 적합도

## Grade / Position Sizing

| Score | Grade | Base Weight |
|---:|---|---:|
| 90+ | S | 7% |
| 85–89 | A+ | 5% |
| 80–84 | A | 3% |
| 75–79 | B+ | 2% |
| 70–74 | B | 1% |
| <70 | C/D | 0% |

최종 추천비중:

```text
recommended_position = base_position × market_risk_multiplier
```

향후에는 섹터집중, 포트폴리오 상관관계, 기존 비중, 현금목표, 전체 Drawdown도 반영합니다.

## Staged Buying

기본 3회 분할:

- 1차 40%
- 2차 30%
- 3차 30%

추가매수 허용 조건:
- Thesis 유지
- Quality 유지
- EPS Revision 급격한 악화 없음
- 재무구조 훼손 없음

금지 규칙: "주가가 떨어졌으니 더 산다."

## Sell Framework

1. Thesis Sell — 투자논리 훼손
2. Valuation Sell — 과도한 고평가
3. Trend Sell — 실적과 모멘텀 동시 악화
4. Portfolio Sell — 더 높은 기대값 후보로 교체

## Investor Fit

종목별로 다음 적합도를 별도 표시할 예정입니다.

- Buffett Fit
- Graham Fit
- Lynch Fit
- Momentum / Soros Fit
- Druckenmiller Fit

Fit Score는 단순 매수/매도 판단이 아니라 “왜 이 종목이 현재 매력적인가”를 설명하기 위한 도구입니다.

## Virtual Strategy Portfolios

병렬 가상포트폴리오:

- Buffett
- Graham
- Lynch
- Momentum
- Hybrid

비교지표:
- CAGR
- 누적수익률
- MDD
- 변동성
- Sharpe / Sortino
- 승률
- 평균 손익비
- Turnover
- 거래비용
- Slippage

## Automation Roadmap

초기:
- 데이터 수집 자동화
- 시장 분석 자동화
- 종목 점수 자동화
- 후보 추천
- 알림
- 사용자 승인형 주문

다음 단계:
- 장 시작 30분 후 스캔
- 장 마감 30분 전 스캔
- 추천 변경 알림
- Thesis 위험 알림
- EPS Revision 알림
- 리밸런싱 제안

장기:
- 상시 서버
- Push 알림
- 한국투자증권 Open API
- 사용자 승인형 실주문
- 충분한 검증 이후에만 제한적 조건부 자동주문

## Safety / Execution Principles

- 추천 엔진과 주문 엔진을 분리한다.
- 실주문 전 fresh quote를 다시 확인한다.
- 종목, 방향, 수량, 가격, 거래소, 주문금액, 위험요약을 최종 화면에 표시한다.
- 주문 timeout 시 자동 재시도하지 않는다.
- 중복주문 방지 장치를 둔다.
- Paper/Simulation과 Live를 명확히 분리한다.
- 모든 추천·결정·주문은 Audit Log에 남긴다.

## Repository Structure

```text
AUTOTRADING/
├─ README.md
├─ PROJECT_STATUS.md
├─ DECISIONS.md
├─ ARCHITECTURE.md
├─ ROADMAP.md
├─ TASKS.md
├─ docs/
│  └─ hybrid-strategy-v1.md
├─ core/
│  └─ HybridStrategyEngine.kt
├─ data/
├─ apps/
│  └─ mobile/
├─ server/
├─ tests/
└─ .github/workflows/
```

## Project Operating Model

- Chat: 요구사항·설계·작업분해·검수
- Work: 저장소 탐색·다단계 실행·테스트·빌드
- Codex: 실제 코드 수정·오류 해결
- GitHub: Issue / PR / 버전 / 진행상태
- GitHub Actions: 자동 테스트·빌드

중요한 설계 변경 시 반드시 함께 갱신:
- `DECISIONS.md`
- `PROJECT_STATUS.md`
- `TASKS.md`

## Resume Instructions

새 Chat / Work / Codex 세션에서는 다음 순서로 읽고 이어갑니다.

1. `README.md`
2. `PROJECT_STATUS.md`
3. `DECISIONS.md`
4. `ARCHITECTURE.md`
5. `ROADMAP.md`
6. `TASKS.md`
7. 최근 PR / 커밋

그 후 `TASKS.md`의 가장 높은 우선순위 미완료 항목부터 진행합니다.

**Canonical repository: `jaehoyang78/AUTOTRADING`**
