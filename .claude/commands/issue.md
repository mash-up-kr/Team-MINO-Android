---
description: 대화형으로 GitHub 이슈를 생성하고 develop에서 작업 브랜치를 분기한다
argument-hint: "[--worktree|-w] [선택: 이슈에 대한 간단한 설명]"
allowed-tools: Bash, AskUserQuestion, Write, Read
---

# /issue — 대화형 이슈 생성 커맨드

너는 사용자와 **단계별 대화**로 GitHub 이슈를 생성하고, Git Flow 전략에 따라 `develop`에서 작업 브랜치까지 분기하는 Claude다. 아래 플로우를 **순서대로** 따른다. 한 단계라도 실패·취소되면 즉시 중단한다.

> **선행 로드**: 이 커맨드는 `docs/conventions/branch-naming.md`의 규칙을 따른다. 브랜치 구성 단계(8)에 들어가기 전에 **반드시** 해당 파일을 Read해 최신 포맷·prefix·slug 규칙을 확보한다. 커맨드 안에서 해당 규칙을 재정의하지 않는다.

## 실행 모드 판별 (가장 먼저)

`$ARGUMENTS`에 `--worktree` 또는 `-w` 토큰이 있으면 **worktree 모드**, 없으면 **기본(제자리) 모드**다.

- 해당 토큰을 `$ARGUMENTS`에서 제거한 나머지를 이슈 설명(자유 입력)으로 쓴다.
- 두 모드는 **이슈 생성(1~7단계)까지 완전히 동일**하고, 브랜치 분기(8단계)와 그 전제인 clean 검증(0-2)만 다르다.
- worktree 모드의 워크트리 생성 절차는 [`docs/conventions/worktree.md`](../../docs/conventions/worktree.md)의 `/issue --worktree 절차`를 단일 출처로 따른다. 8단계 진입 전 해당 문서를 Read한다.

---

## 0. 사전 조건 검증 (실패 시 즉시 중단)

아래 검증을 **순차적으로** 수행한다. 하나라도 실패하면 이슈 생성을 시작하지 말고 안내 메시지만 출력 후 종료.

### 0-1. `develop` 브랜치 존재 확인 (Git Flow 전제)

```sh
git show-ref --verify --quiet refs/heads/develop \
  || git show-ref --verify --quiet refs/remotes/origin/develop
```

없으면 아래 메시지 출력 후 중단:

> ❌ `develop` 브랜치가 없습니다.
> 이 커맨드는 Git Flow 브랜치 전략을 전제로 합니다. 먼저 `develop` 브랜치를 생성해주세요.
> 예: `git checkout -b develop main && git push -u origin develop`

### 0-2. 워킹 트리 clean 확인

> **worktree 모드에서는 이 검증을 생략한다** — 새 워크트리는 현재 작업 트리를 건드리지 않아 미커밋 변경이 있어도 안전하다.

기본(제자리) 모드에서만 수행:

```sh
git status --porcelain
```

출력이 비어있지 않으면 중단:

> ❌ 커밋되지 않은 변경사항이 있습니다.
> 커밋하거나 스태시(`git stash`)한 후 다시 `/issue`를 실행해주세요.

### 0-3. 라벨 존재 확인 (없으면 자동 생성)

```sh
gh label list --json name -q '.[].name' | grep -qx feature \
  || gh label create feature --color a2eeef --description "개발 작업 티켓"
gh label list --json name -q '.[].name' | grep -qx bug \
  || gh label create bug --color d73a4a --description "버그 제보"
```

### 0-4. `gh` 인증 확인

```sh
gh auth status
```

실패 시 중단 안내: "GitHub CLI 인증이 필요합니다. `gh auth login` 실행 후 다시 시도해주세요."

---

## 1. 자유 입력 수집

커맨드 호출 시 인자($ARGUMENTS)가 비어있으면, 사용자에게 **한 번만** 자유 입력으로 질문:

> "어떤 이슈를 만들까요? 자유롭게 설명해주세요."

인자가 있으면 그것을 초기 설명으로 사용한다.

---

## 2. 이슈 유형 자동 판정 (Feature vs Bug)

사용자의 설명을 분석하여 **자동 결정** (사용자에게 묻지 않음):

- **Bug 제보**: "크래시", "에러", "안 됨", "오류", "버그" 등 이미 발생 중인 문제에 대한 제보 톤
- **Feature 개발 작업**: 앞으로 할 일(개발/개선/리팩토링/문서화) 톤

매우 애매한 경우에 **한 번만** AskUserQuestion으로 확인.

---

## 3. (Feature 경로에서만) 종류 자동 결정

Feature로 판정된 경우, 설명을 기반으로 아래 6가지 중 하나를 **완전 자동**으로 결정 (사용자에게 묻지 않음):

- `New Feature` — 신규 기능 추가
- `Improvement` — 기존 기능 개선/UX 개선
- `Bug Fix` — 알려진 버그 수정 작업 (Bug 제보가 아닌 수정 티켓)
- `Refactoring` — 동작 변화 없는 코드 구조 개선
- `Chore` — 빌드·설정·의존성·CI 등 부수 작업
- `Docs` — 문서화 / KDoc / README

---

## 4. 섹션별 순차 정보 수집

사용자 설명에서 이미 커버된 내용은 **건너뛰고**, 빠진 항목만 순차적으로 AskUserQuestion (또는 자유 입력)으로 질문한다.

### Feature 경로 — 수집 순서

**사용자에게 직접 질문하는 항목 (이 2개만):**
1. **작업 목표** — 이 작업이 완료되면 달성되는 상태
2. **상세 설명** — 배경, 맥락, 구현 방향

**Claude가 위 2개 입력과 사용자 초기 설명을 바탕으로 직접 작성하는 항목:**
- **제목** — 한국어 OK, 간결하게 요약 (이슈 리스트에서 읽히기 좋은 형태)
- **영향 모듈** — 프로젝트 구조(Gradle 모듈, 패키지)를 추론하여 기재. 근거 부족하면 생략
- **완료 조건** — 작업 목표/상세 설명에서 도출되는 체크리스트. 최소 2~4개 항목
- **참고 사항** — 사용자가 언급한 링크·문서만 포함. 없으면 생략

Claude가 자동 작성한 항목은 6단계 "본문 미리보기"에서 한 번에 확인받는다. 중간에 개별적으로 묻지 않는다.

### Bug 경로 — 수집 순서
1. **제목**
2. **요약** (현상 한 줄)
3. **크래시 발생 히스토리**
4. **개선 후 예상 동작**
5. **실행 환경** (Device, OS 버전, 앱 버전)
6. **추가 설명** (생략 가능)

---

## 5. 중복 이슈 체크

제목에서 핵심 키워드 2~3개를 추출해 검색:

```sh
gh issue list --search "<키워드>" --state all --limit 5 \
  --json number,title,state,url
```

결과가 있으면 아래처럼 표로 제시하고 AskUserQuestion:

```
| # | 제목 | 상태 |
|---|---|---|
| 12 | 로그인 API 연동 | OPEN |
```

옵션: `계속 진행` / `취소`

---

## 6. 본문 미리보기 & 확인

구성된 Markdown 본문을 사용자에게 보여주고 확정/수정 확인.

### Feature 본문 포맷
```markdown
## 종류
<New Feature | Improvement | Bug Fix | Refactoring | Chore | Docs>

## 작업 목표
...

## 상세 설명
...

## 영향 모듈
- ...

## 완료 조건
- [ ] ...
- [ ] ...

## 참고 사항
- ...
```

### Bug 본문 포맷
```markdown
## 요약
...

## 크래시 발생 히스토리
1. ...
2. ...

## 개선 후 예상 동작
...

## 실행 환경
- Device:
- OS 버전:
- 앱 버전:

## 추가 설명
...
```

---

## 7. 이슈 생성

본문을 임시 파일에 쓴 뒤 `gh`로 생성 (multiline escape 회피):

```sh
TMP=$(mktemp -t issue-body)
cat > "$TMP" <<'EOF'
<여기에 위에서 구성한 Markdown 본문>
EOF

gh issue create \
  --title "<title>" \
  --body-file "$TMP" \
  --label <feature|bug> \
  --assignee @me

rm -f "$TMP"
```

생성된 이슈 번호와 URL을 출력.

---

## 8. 브랜치 제안 & 체크아웃 (develop에서 분기)

**브랜치 네이밍 규칙·prefix 가이드·slug 작성 규칙·Git Flow 절차는 [`docs/conventions/branch-naming.md`](../../docs/conventions/branch-naming.md)을 단일 출처로 따른다.** 아직 읽지 않았다면 여기서 먼저 Read. 이 섹션은 규칙에 맞게 브랜치를 **구성·실행하는 절차**만 정의한다.

### 8-1. 브랜치 이름 구성
- `docs/conventions/branch-naming.md`의 prefix 가이드에 따라 prefix 결정
- 이슈 제목을 영어 kebab-case slug로 변환 (한국어면 의미 번역 후)
- 최종 포맷: `<prefix>/<issue-number>-<slug>/base` — 이슈 생성 시 만드는 브랜치는 항상 `/base` 접미사를 붙인다. 이 브랜치가 곧 그 이슈의 base 브랜치([`base-branch.md`](../../docs/conventions/base-branch.md) 참조)이며, git은 같은 이름을 leaf 브랜치와 상위 경로로 동시에 쓸 수 없으므로(`feature/N-slug`가 이미 브랜치면 그 아래 `feature/N-slug/spec`을 만들 수 없음) `/base`가 반드시 별도 세그먼트여야 하위 작업(`.../spec`, `.../plan` 등)과 형제로 공존할 수 있다.

### 8-2. 분기 실행 (승인 절차 없이 바로 진행)

실행 모드에 따라 분기한다. 별도의 사용자 확인 없이 진행.

#### 기본(제자리) 모드

```sh
# 1) 현재 브랜치가 develop이 아니면 develop으로 이동
current=$(git symbolic-ref --short HEAD)
if [ "$current" != "develop" ]; then
  git checkout develop
fi

# 2) 원격 최신 develop으로 동기화
git pull --ff-only origin develop

# 3) develop에서 새 브랜치 분기
git checkout -b "<prefix>/<issue-number>-<slug>/base"
```

완료 후 최종 요약 출력:

```
✅ 이슈 #<번호> 생성 완료 — <URL>
✅ 브랜치 <prefix>/<번호>-<slug>/base 체크아웃 완료 (develop 기준)
```

#### worktree 모드

현재 작업 트리는 건드리지 않고, `origin/develop`에서 분기한 새 워크트리를 만든다. 절차·근거는 [`docs/conventions/worktree.md`](../../docs/conventions/worktree.md)의 `/issue --worktree 절차`를 따른다.

```sh
git fetch origin
git worktree add ".claude/worktrees/<issue-number>-<slug>" -b "<prefix>/<issue-number>-<slug>/base" origin/develop
git config "branch.<prefix>/<issue-number>-<slug>/base.merge" "refs/heads/<prefix>/<issue-number>-<slug>/base"
for f in local.properties keystore.properties app/google-services.json; do
  [ -f "$f" ] && cp "$f" ".claude/worktrees/<issue-number>-<slug>/$f"
done
```

> `git config` 라인은 `worktree add`가 `origin/develop`으로 잡은 upstream을 새 브랜치로 교정하기 위한 것이다(생략 시 첫 push에 `-u` 필요). 이유·상세는 [`docs/conventions/worktree.md`](../../docs/conventions/worktree.md) 참조.

완료 후 최종 요약 출력:

```
✅ 이슈 #<번호> 생성 완료 — <URL>
✅ 워크트리 생성 완료: .claude/worktrees/<번호>-<slug> (<prefix>/<번호>-<slug>/base, develop 기준)
▶ 다음: cd .claude/worktrees/<번호>-<slug> && claude
```

---

## 취소 처리

어느 단계에서든 사용자가 "취소"를 선택하면 **이슈·브랜치 생성 없이** 대화 종료. 이미 생성된 임시 파일이 있으면 정리.

---

## 규칙 요약 (Claude에게)

- 질문은 필요한 것만 최소로. 사용자 설명에서 이미 답이 나왔으면 다시 묻지 않는다.
- 유형(Feature/Bug)과 종류(New Feature 등)는 **자동 결정** (묻지 않음).
- **Feature 경로에서 사용자에게 묻는 항목은 "작업 목표" + "상세 설명" 2개만**. 제목·영향 모듈·완료 조건·참고 사항은 Claude가 직접 작성 후 6단계에서 일괄 확인.
- Bug 경로는 제보성이므로 기존 6개 항목 모두 사용자 입력 (제목/요약/히스토리/예상/환경/추가).
- 브랜치 네이밍·prefix·slug 규칙 및 Git Flow 전략은 **[`docs/conventions/branch-naming.md`](../../docs/conventions/branch-naming.md)** 를 단일 출처로 한다. 커맨드 실행 초기에 해당 파일을 Read하고, 커맨드 안에서 규칙을 재정의하지 않는다.
- 브랜치 이름은 **별도 승인 없이** Claude가 규칙에 맞게 구성하여 바로 체크아웃한다. 최종 이름은 항상 `/base` 접미사로 끝난다(`<prefix>/<issue-number>-<slug>/base`) — git ref 네임스페이스 제약(leaf 브랜치는 그 아래에 자식 ref를 가질 수 없음)때문에 하위 작업(`.../spec`, `.../plan` 등)과 형제로 공존하려면 base 쪽에 별도 세그먼트가 필요하다. 개념은 [`base-branch.md`](../../docs/conventions/base-branch.md) 참조.
- `--worktree`/`-w` 플래그가 있으면 **worktree 모드**: 이슈 생성(1~7)은 동일하고, 0-2 clean 검증은 생략, 8단계는 제자리 checkout 대신 워크트리 생성으로 분기한다. 워크트리 생성 절차는 **[`docs/conventions/worktree.md`](../../docs/conventions/worktree.md)** 를 단일 출처로 하며 커맨드 안에서 재정의하지 않는다.
