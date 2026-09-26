# Hybrid Strategy Engine v1

목표: 특정 투자자 한 명을 복제하지 않고, 시장 국면에 따라 가치·퀄리티·성장·모멘텀·리스크 관리의 가중치를 바꾸는 개인 투자 의사결정 시스템을 만든다.

## 투자 철학 모듈
- Graham: 안전마진 / 가치
- Buffett & Munger: 퀄리티 / 장기 경쟁우위
- Peter Lynch: 합리적 가격의 성장(GARP)
- Howard Marks: 사이클 / 시장 온도 / 리스크
- Ray Dalio: 성장·물가·유동성 환경
- George Soros: 추세 / 재귀성 / 가격 확인
- Stanley Druckenmiller: 국면 대응 / 확신도 기반 비중
- Edward Thorp: 기대값 / 포지션 사이징

## 1. 시장 국면 엔진

Market Score 0~100:
- 80~100: RISK_ON_STRONG
- 65~79: RISK_ON_NORMAL
- 40~64: NEUTRAL
- 20~39: RISK_OFF
- 0~19: PANIC

가중치:
- 20일 추세 10%
- 60일 추세 15%
- 200일 추세 20%
- 52주 고점 대비 낙폭 15%
- 시장 breadth 15%
- VIX/VKOSPI 10%
- EPS revision breadth 10%
- 신용스프레드 스트레스 5%

위험배수:
- RISK_ON_STRONG 1.00
- RISK_ON_NORMAL 0.90
- NEUTRAL 0.70
- RISK_OFF 0.55
- PANIC 0.65

PANIC은 공격매수 신호가 아니다. Quality가 낮거나 투자논리가 훼손된 종목은 제외한다.

## 2. 종목 점수

100점 목표 가중치:
- Quality 25
- Growth 20
- Value 20
- Momentum 15
- Earnings Revision 10
- Macro/Sector Fit 10

데이터가 없는 항목은 0점 처리하지 않고 확보된 항목끼리 재정규화한다. 별도 `dataCoverage`를 표시하고 핵심 데이터가 부족한 종목은 자동주문 대상에서 제외한다.

## 3. 하드 리스크 패널티

초기 규칙:
- EPS 성장률 < -20%: -8
- 3개월 EPS Revision < -15%: -8
- Debt/Equity > 250%: -6

향후 추가:
- 감사의견/거래정지/관리종목
- 유상증자/CB/BW 희석 위험
- 연속 적자 / FCF 구조적 악화
- 급격한 내부자 매도
- 실적 발표 직전 이벤트 리스크

## 4. 등급과 기본 포지션

- S (90+): 7%
- A+ (85~89): 5%
- A (80~84): 3%
- B+ (75~79): 2%
- B (70~74): 1%
- 70 미만: 신규 진입 없음

실제 추천 비중 = 기본 비중 × 시장 위험배수

## 5. 매수 규칙

기본 분할:
- 1차 40%
- 2차 30%
- 3차 30%

추가매수 조건:
- 투자논리 유지
- EPS Revision 심각한 하향 없음
- Quality 유지
- 재무구조 유지

단순 가격 하락만으로 추가매수 금지.

## 6. 매도 규칙

1. Thesis Sell — 투자논리 훼손
2. Valuation Sell — 과도한 고평가
3. Trend Sell — 실적과 모멘텀 동시 훼손
4. Portfolio Sell — 더 높은 기대값 후보 등장

## 7. 가상 포트폴리오

동일 후보군을 아래 전략으로 병렬 기록한다.
- Buffett
- Graham
- Lynch
- Momentum
- Hybrid

비교:
- 1/3/6/12개월
- CAGR
- MDD
- 변동성
- Sharpe/Sortino
- 승률
- 평균 손익비
- turnover
- 거래비용/슬리피지

Hybrid 가중치는 백테스트와 워크포워드 검증 후 제한 범위 내에서만 조정한다.

## 8. 자동화 원칙

- 데이터 수집 자동화
- 시장 분석 자동화
- 종목 점수 자동화
- 추천/알림 자동화
- 사용자 승인형 주문
- 충분한 검증 이후에만 제한적 조건부 자동주문

## 9. 검증 원칙

전략 개선은 수익률 하나로 판단하지 않는다.

필수:
- CAGR
- Max Drawdown
- Sharpe/Sortino
- 변동성
- 승률
- 평균 손익비
- turnover
- 거래비용/슬리피지 포함
- 학습/검증 구간 분리
- walk-forward validation
