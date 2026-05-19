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

브랜치 prefix는 **릴리스 흐름상의 분류**(어디서 분기해 어디로 머지되는가)이고, 커밋 메시지 type(`feat`/`fix`/`refactor`/`chore`/`docs` 등)은 **변경 성격의 분류**(무엇을 바꿨는가)다. 두 개념은 서로 별개이며, 일반 개발 작업은 성격과 무관하게 모두 `feature/`로 분기한다.

예시:
- 디자인 시스템 KDoc 작성 → 브랜치 `feature/15-design-system-kdocs`, 커밋 `docs: 디자인 시스템 KDoc 작성`
- UseCase 계층 중복 제거 → 브랜치 `feature/22-usecase-dedup`, 커밋 `refactor: UseCase 계층 중복 코드 정리`

커밋 메시지 type 목록·포맷·추가 예시는 [`commit-message.md`](commit-message.md)를 참조.

## slug 작성 규칙
- 영어 kebab-case (소문자, 하이픈 구분)
- 한국어 제목이면 **의미 번역 후** slug화
  - "로그인 API 연동 개발" → `login-api`
  - "디자인 시스템 KDoc 작성" → `design-system-kdocs`
- 이슈 번호·접속사·불필요한 단어 제외, 핵심 명사/동사 위주

## Git Flow 전략

- `main` — 릴리스 브랜치
- `develop` — 통합 개발 브랜치 (일반 작업 브랜치의 base)
- 작업 브랜치 — 위 prefix 가이드에 따라 분기

> **보호 브랜치 직접 작업에 대해**
> `main`/`develop`에 직접 커밋하는 것은 **가급적 지양**하되, 긴급 수정·릴리스 메타 변경 등 상황에 따라 허용될 수 있다. 커맨드(`/done` 등)는 보호 브랜치에서 실행 시 **경고만** 출력하고 작업을 막지 않는다. 판단 책임은 개발자에게 있다.

### `feature` 분기 절차 (일반 작업 — 원칙)
1. 워킹 트리 clean 확인 (`git status --porcelain` 비어있어야 함)
2. 현재 브랜치가 `develop`이 아니면 `develop`으로 checkout
3. `git pull --ff-only origin develop`으로 최신화
4. `git checkout -b feature/<issue-number>-<slug>`

워킹 트리에 커밋되지 않은 변경사항이 있으면 먼저 커밋 또는 스태시 후 진행.

### `release` / `hotfix` 분기 절차 (예외 흐름)
- `release/<version>` — `develop`에서 분기. 머지 후 `main`에 버전 태그를 단다.
- `hotfix/<issue-number>-<slug>` — `main`에서 분기.

base·머지 대상은 위 표 참조.
