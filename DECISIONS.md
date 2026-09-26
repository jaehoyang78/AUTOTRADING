# Architecture & Product Decisions — AUTOTRADING

이 문서는 나중에 프로젝트를 다시 시작해도 왜 현재 구조를 선택했는지 알 수 있도록 결정사항을 기록한다.

## D-001 — 단일 투자 스타일을 사용하지 않는다
결정: 시장 국면과 종목 상태를 분리 평가하고 여러 투자 철학을 조합한다.

이유: 가치, 성장, 모멘텀의 유효 국면이 다르고 전략 전환 기준을 데이터로 고정해 감정 개입을 줄이기 위함이다.

## D-002 — Market Regime과 Stock Score를 분리한다
결정: 좋은 종목인지와 지금 시장에서 얼마나 공격적으로 투자할지를 별도 계산한다.

## D-003 — 종목점수는 6개 컴포넌트로 시작한다
- Quality 25
- Growth 20
- Value 20
- Momentum 15
- Earnings Revision 10
- Macro/Sector Fit 10

## D-004 — 데이터가 없는 항목을 0점 처리하지 않는다
결정: 사용 가능한 항목끼리 재정규화한다. 데이터 미연결을 기업의 약점으로 오인하지 않는다.

## D-005 — PANIC은 무조건 공격매수 신호가 아니다
결정: PANIC에서도 Quality 기준을 통과한 종목만 분할매수 후보로 둔다.

## D-006 — 실제 주문은 초기 버전에서 반드시 사용자 승인형
결정: 전략 엔진은 추천만 생성하고 주문은 별도 확인 절차를 거친다.

## D-007 — 통신 오류 주문 자동 재시도 금지
결정: 브로커 주문 응답이 불명확할 때 동일 주문을 자동 재전송하지 않는다.

## D-008 — 추천비중은 점수와 시장 위험을 동시에 반영한다
기본 비중: 90+ 7%, 85~89 5%, 80~84 3%, 75~79 2%, 70~74 1%.
최종 비중: `base_position × market_risk_multiplier`.

## D-009 — 매수는 분할한다
초기 기본은 40% / 30% / 30%. 추가매수는 가격하락이 아니라 투자논리 유지 여부를 기준으로 한다.

## D-010 — 가상포트폴리오를 병렬 운용한다
Buffett, Graham, Lynch, Momentum, Hybrid 전략을 비교한다.

## D-011 — 전략 변경은 백테스트 + 워크포워드 검증 후 적용한다
CAGR, MDD, Sharpe/Sortino, 승률, 손익비, turnover, 거래비용, 슬리피지를 함께 본다.

## D-012 — AUTOTRADING을 정식 독립 저장소로 사용한다
`jaehoyang78/AUTOTRADING`이 canonical repository다.

## D-013 — 추천 엔진과 주문 엔진을 분리한다
분석 오류가 바로 실주문으로 이어지지 않도록 하고 Paper/Live를 분리한다.

## D-014 — 모든 의미 있는 의사결정을 기록한다
데이터 시점, 시장점수, 컴포넌트, 추천, thesis, 사용자 결정, 주문/체결, 이후 성과를 감사로그 대상으로 한다.

## D-015 — Data Coverage가 부족하면 고득점이어도 신규매수를 막는다
현재 v2 기준:
- 전체 24개 입력 중 50% 이상 확보
- 6개 핵심 그룹 중 4개 이상 확보
- Quality와 Momentum 데이터는 반드시 존재

미충족 시 `recommendedPositionPct = 0`으로 두고 `데이터 보강` 상태로 표시한다.

## D-016 — Investor Fit은 매수점수가 아니라 설명 레이어다
Buffett/Graham/Lynch/Soros-Momentum/Druckenmiller 적합도를 0~100으로 계산하지만, 이것만으로 주문을 허용하지 않는다.

## D-017 — Thesis가 BROKEN이면 점수와 관계없이 SELL 우선
`ThesisState.BROKEN`은 Decision Engine에서 즉시 목표비중 0%를 반환한다.

## D-018 — 포트폴리오 리스크가 커질 때 신규 위험 확대를 제한한다
초기 v2 안전선:
- 동일 섹터 비중 25% 이상이면 해당 종목 증액 금지
- 포트폴리오 낙폭 -15% 이하이면 신규 위험 확대 금지
- 개별 종목 목표비중 상한 7%

## D-019 — 현금비중도 전략 포지션으로 관리한다
초기 v1 현금 목표:
- RISK_ON_STRONG: 10%
- RISK_ON_NORMAL: 15%
- NEUTRAL: 25%
- RISK_OFF: 40%
- PANIC: 35%

PANIC의 현금 목표를 Risk-Off보다 약간 낮게 둔 이유는 고품질 종목에 대한 단계적 매수 여지를 남기기 위함이다. 단, 패닉 자체가 매수 신호는 아니다.

포트폴리오 낙폭이 커지면 현금 버퍼를 추가한다:
- -10% 이하: +5%
- -15% 이하: +10%
- -20% 이하: +15%

최종 현금 목표는 10~60% 범위로 제한한다. 현재 주식비중이 `100 - 현금목표` 이상이면 신규 주식 익스포저 확대를 중지한다.

## D-020 — 외부 데이터 공급자와 코어 전략을 인터페이스로 분리한다
결정: KIS/FRED/Cboe 등 외부 서비스의 응답 스키마를 코어 계산 로직이 직접 사용하지 않는다.

구조:
`외부 API → provider adapter → AUTOTRADING 공통 모델 → feature/market-regime/scoring`

이유:
- API 필드 변경이 전략 로직까지 전파되는 것을 막는다.
- KIS 외 다른 공급자를 추가/대체할 수 있다.
- 테스트에서는 실제 네트워크 없이 fake provider를 사용할 수 있다.

## D-021 — 데이터에는 값뿐 아니라 출처·시점·신선도를 저장한다
`ProviderMeta`에 provider, observedAt, freshness를 기록한다. 오래된 데이터와 나쁜 데이터를 구분하고, 자동화 단계에서 stale critical data를 차단하기 위함이다.

## D-022 — EPS Revision을 신뢰할 수 없는 proxy로 채우지 않는다
컨센서스/추정치 변경 데이터가 확보되지 않았으면 Revision group을 missing으로 유지한다. 임의의 주가·실적 proxy를 EPS Revision이라고 부르지 않는다. Coverage가 이를 그대로 반영한다.

## D-023 — 초기 데이터 공급자 우선순위
- KIS: KR/US 시세·일봉
- FRED: 미국 거시 시계열
- Cboe 또는 검증 가능한 feed: VIX
- VKOSPI: 신뢰 가능한 자동 접근 공급자 별도 선정
- Breadth: 충분한 universe 일봉을 축적한 뒤 내부 계산 우선 검토

주문용 KIS broker adapter는 KIS market-data adapter와 별도 모듈로 유지한다.

## D-024 — Live HTTP/OAuth Gateway와 정규화 Provider Adapter를 한 번 더 분리한다
구조:
`HTTP/OAuth/JSON → KisGateway/FredGateway → KisMarketDataProvider/FredMacroDataProvider → AUTOTRADING models`

이유:
- CI에서 외부 네트워크와 비밀키 없이 정규화 로직을 테스트한다.
- OAuth/token 갱신·rate limit·JSON 필드 변경과 투자 로직을 격리한다.
- 실제 API 장애가 테스트 불안정으로 이어지는 것을 막는다.
- mock gateway로 날짜정렬, limit, 변환 규칙을 결정론적으로 검증한다.

비밀정보 원칙:
AppKey/AppSecret/FRED API key/계좌정보는 코드·커밋·Issue·테스트 fixture에 저장하지 않는다. live gateway는 환경변수 또는 플랫폼 보안 저장소에서만 주입받도록 구현한다.

## D-025 — 미국 종목은 시장(US)만으로 식별하지 않고 venue를 명시한다
KIS 해외 시세는 거래소 코드가 필요하므로 `Venue`를 KRX/NASDAQ/NYSE/AMEX로 모델링한다.

규칙:
- KR → KRX만 허용
- US → NASDAQ/NYSE/AMEX 중 하나를 반드시 지정
- venue를 추측해서 주문/시세 조회하지 않는다

이유: 동일 심볼·거래소 혼동과 잘못된 KIS EXCD 사용을 막기 위함이다.

## D-026 — KIS live gateway는 시세 전용이며 주문 메서드를 넣지 않는다
`KisHttpGateway`에는 OAuth, 현재가, 일봉 같은 market-data 기능만 둔다. 향후 주문은 별도 `KisBrokerAdapter`에서 구현한다.

## D-027 — 검증되지 않은 KIS 응답 필드는 null로 남긴다
국내 일봉은 기존 동작에서 확인된 OHLC/거래량 필드를 파싱한다. 미국 일봉 v1은 확인된 `xymd`와 `clos`만 파싱하며 OHLC/volume을 추정하지 않는다.

이는 데이터 부족을 숨기기보다 Coverage/feature 계산에서 명시적으로 드러내기 위한 결정이다.

## D-028 — KIS credential은 환경변수/보안저장소에서만 주입한다
서버/CLI 기준 환경변수 이름은 `KIS_APP_KEY`, `KIS_APP_SECRET`으로 통일한다. `KisCredentials.toString()`은 값을 마스킹하며 오류 메시지에 secret을 포함하지 않는다.

## D-029 — Macro는 기존 Market Regime을 대체하지 않고 제한된 overlay로 반영한다
결정:
- Price trend / breadth / volatility 중심의 기존 Market Regime을 base로 유지한다.
- FRED 기반 Macro Tailwind Score를 별도 계산하고 base 85% + macro 15%로 합성한다.
- macro 데이터가 없으면 기존 MarketAssessment를 그대로 반환한다.

초기 macro 구성:
- 정책금리 6개월 변화 20%
- 10Y-2Y yield curve 15%
- CPI YoY 및 방향 30%
- Fed total assets 13주 변화 20%
- KR 시장만 USD/KRW 3개월 변화 15%
- High Yield OAS는 별도 credit-spread stress 입력으로 사용

이유:
매크로는 중요하지만 단일 거시모형이 가격·수급보다 시장판단을 과도하게 지배하지 않도록 하고, 향후 백테스트에서 macro 산식과 가중치를 독립적으로 교체/검증하기 위함이다.
