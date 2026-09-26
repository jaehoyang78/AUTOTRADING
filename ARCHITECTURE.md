# Architecture — AUTOTRADING Hybrid Investor Engine

## 1. 목표 구조

```text
[Market Data]     [Fundamentals]     [Consensus]     [Macro]
      \                 |                 |             /
       \                |                 |            /
                [Data Normalization Layer]
                          |
             +------------+------------+
             |                         |
     [Market Regime Engine]      [Stock Scoring Engine]
             |                         |
             +------------+------------+
                          |
                [Decision Engine]
                          |
        +-----------------+-----------------+
        |                 |                 |
 [Position Sizing] [Investor Fit] [Risk / Thesis Rules]
        |                 |                 |
        +-----------------+-----------------+
                          |
                 [Recommendation]
                          |
             [Portfolio / Backtest]
                          |
                    [Mobile UI]
                          |
                 [User Approval]
                          |
                    [Broker API]
```

## 2. 핵심 모듈

### data
외부 데이터를 내부 공통 형식으로 변환한다.

예:
- DailyBar
- Quote
- Fundamentals
- ConsensusEstimate
- MacroSnapshot
- MarketBreadth

원칙: 계산 엔진이 특정 데이터 공급자 형식에 직접 의존하지 않도록 한다.

### market_regime
시장 상태를 0~100 점수와 regime으로 반환한다.

출력 예:

```json
{
  "score": 72,
  "regime": "RISK_ON_NORMAL",
  "riskMultiplier": 0.9,
  "summary": "정상 상승 · 퀄리티+성장 중심"
}
```

### stock_scoring
개별 종목의 6개 component와 total score를 계산한다.

### investor_fit
종목 특성이 어떤 투자 철학과 잘 맞는지 계산한다.

예:

```json
{
  "buffett": 87,
  "graham": 62,
  "lynch": 91,
  "momentum": 83,
  "druckenmiller": 88
}
```

### decision
Buy / Add / Hold / Reduce / Sell / Avoid 후보를 만든다.

### position_sizing
종목점수, 시장위험, 포트폴리오 집중도, 현금비중을 반영해 추천 비중을 결정한다.

향후 Kelly 원리는 그대로 사용하지 않고 보수적인 fractional Kelly 또는 상한 기반 방식만 검토한다.

### thesis
매수 당시 투자논리와 이후 변경사항을 기록한다.

목표: 가격만 떨어졌다는 이유로 추가매수하는 행동을 막는다.

### portfolio
- 실제 보유종목
- 목표비중
- 현재비중
- 현금
- 실현/미실현 손익
- 리밸런싱 후보

### strategy_lab
전략별 가상포트폴리오를 병렬 운용한다.

### backtest
과거 데이터를 이용해 전략을 검증한다.

필수 고려:
- survivorship bias
- look-ahead bias
- 거래비용
- 슬리피지
- 리밸런싱 빈도

### broker
브로커 주문 API 어댑터.

초기 대상:
- 한국투자증권 Open API

원칙: 추천 로직과 주문 로직을 분리한다.

## 3. 저장소 구조

```text
AUTOTRADING/
├─ README.md
├─ PROJECT_STATUS.md
├─ DECISIONS.md
├─ ARCHITECTURE.md
├─ ROADMAP.md
├─ TASKS.md
├─ docs/
│  ├─ hybrid-strategy-v1.md
│  ├─ data-sources.md
│  └─ backtest.md
├─ core/
│  ├─ market_regime/
│  ├─ stock_scoring/
│  ├─ investor_fit/
│  ├─ decision/
│  ├─ position_sizing/
│  ├─ portfolio/
│  └─ backtest/
├─ data/
│  ├─ models/
│  ├─ providers/
│  └─ normalization/
├─ apps/
│  └─ mobile/
├─ server/
├─ tests/
└─ .github/workflows/
```

## 4. 자동화 단계

### Level 1
데이터 수집 자동화

### Level 2
시장/종목 점수 자동화

### Level 3
추천 및 알림 자동화

### Level 4
사용자 승인형 주문

### Level 5
조건부 자동주문

Level 5는 충분한 테스트, 주문중복 방지, 장애대응 체계가 검증된 이후에만 고려한다.

## 5. 데이터 품질 설계

각 종목 평가에 `dataCoverage`를 둔다.

예:

```text
Quality data 5/5
Growth data 4/4
Value data 3/5
Momentum data 6/6
Revision data 0/2
Macro 1/1
Coverage = 79%
```

원칙:
- coverage가 낮으면 등급 옆에 경고
- 특정 핵심 데이터가 없으면 자동 주문 대상에서 제외
- stale data 여부를 반드시 기록

## 6. 상태 저장

기록해야 할 것:
- 날짜/시간
- 사용 데이터 시점
- Market Score
- Regime
- 종목 component score
- Total Score
- 추천 액션
- 추천 비중
- 당시 투자논리
- 실제 사용자 결정
- 실제 주문/체결
- 이후 성과

이 기록은 나중에 전략의 실제 의사결정 품질을 분석하는 기반이 된다.
