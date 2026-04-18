---
description: 현재 브랜치의 변경사항을 논리 단위로 쪼개어 Conventional Commits 한국어 메시지로 커밋한다
argument-hint: "[선택: 커밋 단위에 대한 힌트]"
allowed-tools: Bash, AskUserQuestion, Read, Skill
---

# /done — 커밋 마무리 커맨드

너는 현재 브랜치의 변경사항을 분석해 **논리 단위로 쪼개고**, 각 단위마다 **Conventional Commits 기반 한국어 메시지**를 생성·미리보기·확정·커밋하는 Claude다. 푸시·PR 생성은 범위 밖이다.

> **선행 로드**: 이 커맨드는 `docs/conventions/commit-message.md`의 규칙을 따른다. 커밋 메시지 생성 단계(5)에 들어가기 전에 **반드시** 해당 파일을 Read해 최신 컨벤션·type 목록·예시를 확보한다. 커맨드 안에서 규칙을 재정의하지 않는다.

---

## 0. 사전 검증

### 0-1. 변경사항 존재 확인
```sh
git status --porcelain
```
출력이 비어있으면 다음 메시지 출력 후 **즉시 종료**:

> ✅ 커밋할 변경사항이 없습니다.

### 0-2. 브랜치 경고 (차단 아님)
```sh
current=$(git symbolic-ref --short HEAD)
```
`current`가 `main` 또는 `develop`이면 **경고 출력 후 계속 진행**:

> ⚠️ 현재 브랜치가 `<main|develop>` 입니다. 보호 브랜치에 직접 커밋은 권장되지 않습니다. 그대로 진행합니다.

---

## 1. 변경사항 수집

```sh
git status --porcelain
git diff --stat
git diff --cached --stat
```

staged(`git diff --cached`)가 비어있고 unstaged/untracked만 있으면 **자동으로 `git add -A`** 실행:

```sh
git add -A
```

이후 staged 전체를 대상으로 분석 진행.

---

## 2. 코드 정리 (`simplify` Skill 실행, 필수)

논리 단위 분석 직전에 **반드시** `simplify` Skill을 호출하여 staged 코드의 재사용성·품질·효율성을 점검하고 필요한 개선을 반영한다. **건너뛸 수 없다.**

### 2-1. 실행
`simplify` Skill을 호출. 대상 범위는 **현재 staged 변경사항**. Skill이 파일을 수정하면 working tree에 반영된다.

### 2-2. 변경 재수집
Skill 실행 후 staged + unstaged 상태를 다시 확인:
```sh
git status --porcelain
```
simplify로 인한 수정이 있으면 `git add -A`로 다시 staged에 포함한다.

> 주의: simplify가 의도한 기능 범위 밖의 변경을 만들 수 있다. 다음 단계(논리 단위 분석)에서 해당 수정을 **별도 커밋**(`refactor:` 또는 `chore:`)으로 분리할지 판단한다. 원래 기능 변경과 단순 정리는 섞지 않는 것을 우선한다.

---

## 3. 논리 단위 분석 & 쪼개기 제안

staged 파일 목록과 diff 내용을 기반으로 **논리 단위**를 추출한다. 분리 기준:

- 기능 추가 ↔ 리팩토링
- 프로덕션 코드 ↔ 테스트
- 모듈·패키지별 경계
- 의존성 업그레이드 ↔ 실제 기능 변경
- 문서 ↔ 코드

### 3-1. 단일 단위 판정
모든 변경이 **하나의 의도**로 묶인다고 판단되면 쪼개지 않고 단일 커밋으로 진행.

### 3-2. 다중 단위 판정
논리 단위가 여러 개라 판단되면, 다음처럼 **쪼개기 계획**을 사용자에게 제시:

```
제안된 커밋 분할 (N개):

[1/N] feat: 로그인 API 연동
      - app/src/main/.../LoginRepository.kt
      - app/src/main/.../LoginUseCase.kt

[2/N] test: 로그인 UseCase 테스트 추가
      - app/src/test/.../LoginUseCaseTest.kt

[3/N] chore: OkHttp 4.12 업그레이드
      - gradle/libs.versions.toml
```

AskUserQuestion으로 선택지 제공:
- `이 계획대로 진행` (Recommended)
- `단일 커밋으로 합치기`
- `취소`

---

## 4. 커밋 메시지 컨벤션 로드

`docs/conventions/commit-message.md`를 Read (아직 안 읽었다면). type 목록·예시·규칙을 반드시 참고하여 다음 단계에서 메시지를 생성한다.

---

## 5. 각 커밋 단위 순차 처리

쪼개기 계획의 각 단위(또는 단일 단위)에 대해 아래를 반복:

### 5-1. 메시지 생성
변경 내용을 기반으로 Conventional Commits 한국어 메시지 생성:
- `<type>: <한국어 제목>` (scope 없음, 마침표 없음)
- 필요 시 한 줄 공백 후 한국어 본문 (왜 바꿨는지 중심)
- `Co-Authored-By` 꼬리표 **붙이지 않음**

### 5-2. 미리보기 & 확인
사용자에게 현재 단위의 대상 파일과 메시지를 함께 보여주고 AskUserQuestion:
- `이 메시지로 커밋` (Recommended)
- `메시지 수정` — 사용자가 직접 입력한 텍스트로 교체
- `이 단위 건너뛰기` — 해당 파일들을 `git reset HEAD <files>`로 staged에서 제외하고 다음 단위로
- `전체 취소` — 남은 단위 모두 중단

### 5-3. 커밋 실행
확정 시:

```sh
# 해당 단위의 파일만 staged 유지하도록 reset 후 재-add
git reset HEAD
git add <files-of-this-unit>

# 커밋 (multiline 메시지는 -F 사용)
TMP=$(mktemp -t commit-msg)
cat > "$TMP" <<'EOF'
<type>: <한국어 제목>

<본문 (있을 때만)>
EOF

git commit -F "$TMP"
rm -f "$TMP"
```

Pre-commit hook 실패 시 실패 이유를 그대로 출력하고 **해당 단위에서 중단** (이미 완료된 이전 커밋은 유지). 사용자에게 정리 후 `/done` 재실행을 안내.

---

## 6. 결과 요약

모든 단위 처리 후 최종 요약 출력:

```
✅ 커밋 N개 생성 완료

[1] <hash> feat: 로그인 API 연동
[2] <hash> test: 로그인 UseCase 테스트 추가
[3] <hash> chore: OkHttp 4.12 업그레이드

현재 브랜치: <branch>
```

취소·스킵된 단위가 있었다면 그 내역도 함께 표기.

---

## 규칙 요약 (Claude에게)

- 커밋 메시지 규칙(type 목록·포맷·Co-Authored-By 정책 등)은 **[`docs/conventions/commit-message.md`](../../docs/conventions/commit-message.md)** 를 단일 출처로 한다. 커맨드 초기에 Read하고 재정의하지 않는다.
- 언어는 **한국어**. scope는 **사용 안 함**. `Co-Authored-By` 꼬리표는 **붙이지 않음**.
- **변경사항이 많을 때 자동 쪼개기 제안**, 단일 단위면 그대로 진행.
- 각 커밋은 **미리보기 → 사용자 확인 → 실행** 순서로만 진행. 자동 커밋 금지.
- **푸시·PR 생성은 하지 않는다**. 커밋까지가 전부.
- 보호 브랜치(`main`/`develop`)에서 실행 시 **경고만** 출력하고 진행.
- staged가 비어있으면 `git add -A`로 자동 포함. staged가 있으면 그대로 사용.
- **논리 단위 분석(3단계) 직전에 `simplify` Skill을 필수 실행**. 건너뛰기 불가. simplify로 인한 수정은 기능 변경과 섞지 않고 별도 커밋(`refactor:`/`chore:`)으로 분리하는 것을 우선.
