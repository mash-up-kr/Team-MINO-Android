# 브랜치 네이밍 컨벤션

## 포맷
```
<prefix>/<issue-number>-<english-slug>
```
- 예: `feature/12-login-api`, `docs/15-design-system-kdocs`
- `#` 문자는 **포함하지 않는다**
- slug는 **반드시 영어 kebab-case**

## prefix 가이드 (이슈 종류 기준)

| 이슈 종류 | 참고 prefix |
|---|---|
| New Feature | `feature` |
| Improvement | `improve` |
| Bug Fix, Bug 제보 | `fix` |
| Refactoring | `refactor` |
| Chore | `chore` |
| Docs | `docs` |

> 고정 매핑이 아닌 **참고용**. 이슈 성격에 따라 조정 가능.

## slug 작성 규칙
- 영어 kebab-case (소문자, 하이픈 구분)
- 한국어 제목이면 **의미 번역 후** slug화
  - "로그인 API 연동 개발" → `login-api`
  - "디자인 시스템 KDoc 작성" → `design-system-kdocs`
- 이슈 번호·접속사·불필요한 단어 제외, 핵심 명사/동사 위주

## Git Flow 전략

- `main` — 릴리스 브랜치
- `develop` — 통합 개발 브랜치 (작업 브랜치의 base)
- 작업 브랜치 — 위 컨벤션에 따라 원칙상 `develop`에서 분기

> **보호 브랜치 직접 작업에 대해**
> `main`/`develop`에 직접 커밋하는 것은 **가급적 지양**하되, 긴급 수정·릴리스 메타 변경 등 상황에 따라 허용될 수 있다. 커맨드(`/done` 등)는 보호 브랜치에서 실행 시 **경고만** 출력하고 작업을 막지 않는다. 판단 책임은 개발자에게 있다.

### 새 브랜치 생성 절차 (원칙)
1. 워킹 트리 clean 확인 (`git status --porcelain` 비어있어야 함)
2. 현재 브랜치가 `develop`이 아니면 `develop`으로 checkout
3. `git pull --ff-only origin develop`으로 최신화
4. `git checkout -b <prefix>/<issue-number>-<slug>`

워킹 트리에 커밋되지 않은 변경사항이 있으면 먼저 커밋 또는 스태시 후 진행.

예외 상황(예: hotfix를 `main`에서 분기)은 상황에 맞게 위 절차를 조정한다.
