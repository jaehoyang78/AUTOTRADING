# Roadmap — AUTOTRADING

## Phase 0 — Repository Separation

완료 조건:
- [x] 신규 GitHub repository 생성
- [x] AUTOTRADING을 canonical repository로 확정
- [x] 핵심 문서 이전
- [x] 기존 tradingbot과 신규 개발 분리
- [ ] 기본 CI 생성

## Phase 1 — Core Strategy Engine

### 1.1 Market Regime
- Market Score 0~100
- RISK_ON_STRONG
- RISK_ON_NORMAL
- NEUTRAL
- RISK_OFF
- PANIC

### 1.2 Stock Scoring
- Quality
- Growth
- Value
- Momentum
- Revision
- Macro Fit

### 1.3 Position Sizing
- Base allocation
- Market risk multiplier
- Portfolio concentration cap

### 1.4 Investor Fit
- Buffett
- Graham
- Lynch
- Momentum/Soros
- Druckenmiller

완료 조건: 순수 함수 기반 단위테스트가 있고 외부 API 없이 계산 가능.

## Phase 2 — Data Layer

### Market data
- KR: KOSPI/KOSDAQ
- US: S&P 500/Nasdaq
- VIX/VKOSPI
- 20/60/200-day trend
- 52-week drawdown
- breadth

### Company data
- price / volume
- financial statements
- valuation
- estimates/revisions

### Macro data
- policy rates
- bond yields
- inflation
- liquidity proxies
- FX

완료 조건: 각 데이터 필드의 provider, timestamp, stale 상태, coverage 확인 가능.

## Phase 3 — Backtesting & Validation

- historical snapshots
- strategy portfolio construction
- transaction cost
- slippage
- survivorship bias 대응
- walk-forward validation

전략 비교:
- Buffett
- Graham
- Lynch
- Momentum
- Hybrid

평가지표:
- CAGR
- cumulative return
- MDD
- Sharpe
- Sortino
- volatility
- win rate
- payoff ratio
- turnover

완료 조건: Hybrid가 단순 벤치마크보다 수익률뿐 아니라 drawdown/위험조정 관점에서 의미가 있는지 판단할 수 있음.

## Phase 4 — Portfolio & Thesis Engine

- 실제 보유종목 등록
- 매수 당시 thesis 기록
- thesis 상태 변화
- 목표비중
- 현재비중
- 추가매수 조건
- 매도 조건
- 리밸런싱 제안

완료 조건: 모든 추천에 '왜'가 기록됨.

## Phase 5 — Mobile App

### Dashboard
- Market Score
- Regime
- 전략 가중치
- 현금 권장 범위

### Scanner
- 종합점수
- 등급
- component
- 추천비중

### Stock Detail
- investor fit
- thesis
- risks
- buy/add/sell rules

### Portfolio
- current vs target
- rebalance

### Strategy Lab
- virtual portfolio performance

## Phase 6 — Notifications

- 장 시작 30분 후 스캔
- 장 마감 30분 전 스캔
- score 급변
- thesis risk
- earnings revision change
- target rebalance

## Phase 7 — Broker Integration

초기 대상: 한국투자증권 Open API

원칙:
- 실제 주문 전 새 시세 확인
- 주문 요약 표시
- 사용자 최종 승인
- 중복주문 방지
- timeout 자동 재시도 금지
- paper/live 명확히 분리

## Phase 8 — Adaptive Strategy

목표: 시장국면 + 전략별 검증성과를 반영해 Hybrid 가중치를 제한된 범위 내 조정.

안전장치:
- min/max weight
- minimum observation period
- walk-forward validation
- 성과 급변 시 자동 가중치 변경 금지
- 사람 검토 가능한 변경 로그

## 장기 목표

종목을 찍어주는 앱이 아니라 다음을 수행하는 개인 투자 운영체계:

데이터 → 시장판단 → 후보발굴 → 점수화 → 위험관리 → 포트폴리오 → 알림 → 사용자 승인 → 주문 → 성과복기 → 전략개선
