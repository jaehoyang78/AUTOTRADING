# AUTOTRADING Development Workflow

## Branches

- `main`: 항상 재개 가능한 기준 브랜치. 검증되지 않은 직접 개발 금지.
- `feature/<name>`: 기능 개발.
- `fix/<name>`: 오류 수정.
- `docs/<name>`: 문서 중심 변경.

## Pull Requests

의미 있는 코드 변경은 feature/fix 브랜치에서 작업하고 PR로 `main`에 합친다.

PR 전 확인:
1. `gradle test` 통과
2. 전략/위험 규칙 변경 시 `DECISIONS.md` 갱신
3. 진행상태 변경 시 `PROJECT_STATUS.md`, `TASKS.md` 갱신
4. 실주문 관련 변경은 Paper/Live 경계를 침범하지 않는지 확인
5. 비밀키, 계좌번호, 토큰을 코드/Issue/로그에 남기지 않음

## Commit prefixes

- `feat:` 기능
- `fix:` 버그
- `test:` 테스트
- `docs:` 문서
- `build:` 빌드/의존성
- `ci:` GitHub Actions
- `refactor:` 동작 변경 없는 구조개선

## Definition of Done

코어 전략 기능은 다음을 만족해야 완료로 본다.
- 순수 계산 로직에 단위테스트가 있음
- Missing data와 extreme value 시나리오를 고려함
- 사용자 승인 없이 주문을 발생시키지 않음
- 변경 이유가 문서화됨
- GitHub Actions CI가 성공함
