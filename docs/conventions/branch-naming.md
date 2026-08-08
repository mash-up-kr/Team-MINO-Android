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

## 워크플로우 하위 작업의 base 브랜치

spec/plan/task 등 하위 작업이 `develop` 대신 상위 이슈 브랜치를 base로 삼는 규칙은 별도 문서 [`base-branch.md`](base-branch.md)를 단일 출처로 한다.
