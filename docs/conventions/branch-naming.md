# 브랜치 네이밍 컨벤션

## 포맷
```
<prefix>/<issue-number>-<english-slug>
```
- 예: `feature/12-login-api`, `feature/15-design-system-kdocs`
- `#` 문자는 **포함하지 않는다**
- slug는 **반드시 영어 kebab-case**

## prefix 가이드 (Git Flow)

| prefix | base 브랜치 | 머지 대상 | 용도 |
|---|---|---|---|
| `feature` | `develop` | `develop` | 신규 기능 추가, 개선, 버그 수정, 리팩토링, 문서, 부수 작업 등 일반 개발 작업 |
| `release` | `develop` | `main` + `develop` | 릴리스 준비 (버전 번호 갱신·릴리스 노트·릴리스 직전 자잘한 수정) |
| `hotfix` | `main` | `main` + `develop` | 운영 릴리스의 긴급 수정 |

> 고정 매핑이 아닌 **참고용**. 상황에 따라 조정 가능.

## 브랜치 prefix vs 커밋 메시지 type

브랜치 prefix(릴리스 흐름)와 커밋 type(변경 성격)은 별개. 일반 개발 작업은 type과 무관하게 모두 `feature/`로 분기한다. 예: `docs:` 커밋이라도 브랜치는 `feature/15-design-system-kdocs`.

커밋 type은 [`commit-message.md`](commit-message.md) 참조.

## slug 작성 규칙
- 영어 kebab-case (소문자, 하이픈 구분)
- 한국어 제목이면 **의미 번역 후** slug화 — 예: "로그인 API 연동 개발" → `login-api`
- 이슈 번호·접속사·불필요한 단어 제외, 핵심 명사/동사 위주

## Git Flow 전략

- `main` — 릴리스 브랜치
- `develop` — 통합 개발 브랜치 (일반 작업의 base)

보호 브랜치(`main`/`develop`) 직접 커밋은 지양하되, 긴급 수정·릴리스 메타는 허용. 커맨드(`/done` 등)는 경고만 출력하고 차단하지 않는다.

### `feature` 분기 절차

1. 워킹 트리 clean
2. `develop` checkout → `git pull --ff-only origin develop`
3. `git checkout -b feature/<issue-number>-<slug>`

### `release` / `hotfix` 분기 절차

- `release/<version>` — `develop`에서 분기. 머지 후 `main`에 버전 태그
- `hotfix/<issue-number>-<slug>` — `main`에서 분기

base·머지 대상은 위 표 참조.

## base 브랜치 (워크플로우 통합)

워크플로우(SDD: spec → plan → task 등) 도입으로 하나의 이슈 아래 여러 PR이 생기는 경우를 위한 동작 규칙. **새 prefix나 네이밍 규칙을 추가하는 게 아니다** — 기존 동작을 재해석하는 것뿐이다.

- 에픽/상위 이슈에 대해 `/issue`가 만드는 브랜치(`feature/<issue-number>-<slug>`)는 **그 자체로 하위 작업의 base 역할을 겸한다.** 별도 플래그·prefix 불필요 — 이슈 생성 시 브랜치가 만들어지는 기존 동작 그대로.
- 하위 작업(spec/plan/task 등) 브랜치를 만들 때는 `develop`이 아니라 이 base 브랜치에서 분기한다.
- 하위 작업 브랜치의 PR은 `develop`이 아니라 base 브랜치를 타겟한다. [`/pr`](../../.claude/commands/pr.md)이 이를 **브랜치명이 아니라 실제 git 조상 관계로 자동 판단**한다 — 현재 브랜치가 `develop`이 아닌 다른 열린 브랜치에서 갈라져 나온 것이면 그 브랜치를 base로 쓴다. 이름 규칙을 강제하지 않으므로 하위 브랜치명은 무엇이든 상관없다.
- base 브랜치가 모든 하위 작업을 흡수한 뒤에는, base 브랜치에서 `/pr`을 실행해 `develop`으로 머지한다. 이때는 더 가까운 조상 브랜치가 없으므로 기존 로직대로 자동으로 `develop`이 default가 된다.

**범위**: spec 브랜치 생성은 spec-center 대시보드가, plan/task 등 하위 브랜치 생성 자동화는 향후 별도 hook이 담당한다. 이 문서는 "base를 기준으로 분기·타겟한다"는 규칙만 규정하며, 생성 자동화 자체는 다루지 않는다.
