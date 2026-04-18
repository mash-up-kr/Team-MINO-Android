---
description: 지정한 PR의 변경사항을 분석하고 인라인 코멘트로 코드 리뷰를 게시한다
argument-hint: <PR 번호>
allowed-tools: Bash, Read
---

# /review — PR 코드 리뷰 커맨드

`$ARGUMENTS` 번호의 PR 코드 변경사항을 분석하고 코드 리뷰를 수행한다.

> **선행 로드**: 리뷰 시작 전 다음 3개 컨벤션 문서를 **반드시** Read한다. 1번 리뷰 기준(컨벤션)의 단일 출처다.
> - `docs/conventions/branch-naming.md`
> - `docs/conventions/commit-message.md`
> - `docs/conventions/pull-request.md`

## 진행 순서

1. **PR 정보 수집 및 브랜치 체크아웃**
   - `gh pr view $ARGUMENTS --json title,body,state,files,headRefName,headRefOid,baseRefName,author` — PR 메타데이터
   - `gh pr diff $ARGUMENTS` — 코드 변경사항
   - 워킹 트리가 dirty(`git status --porcelain` 비어있지 않음)이면 **중단**하고 정리 후 재실행 안내
   - PR 브랜치를 로컬에 가져와 체크아웃: `git fetch origin <headRefName> && git checkout <headRefName>`
     - 이렇게 해야 PR에서 새로 추가된 파일을 직접 읽을 수 있다.

2. **프로젝트 구조 파악**
   변경된 파일이 속한 모듈과 주변 코드를 읽어서 기존 패턴·컨벤션을 파악한다.

3. **컨벤션 준수 확인**
   - 브랜치 이름이 `docs/conventions/branch-naming.md` 규칙(`<prefix>/<N>-<english-slug>`, `#` 없음)에 부합하는가
   - 커밋 메시지들이 `docs/conventions/commit-message.md` 규칙(Conventional Commits 한국어, scope 없음, Co-Authored-By 없음)에 부합하는가
   - PR 제목·본문이 `docs/conventions/pull-request.md` 규칙(이슈 제목 그대로, `Closes #N`, 6섹션 구조)에 부합하는가

4. **코드 리뷰 수행**
   아래 리뷰 기준에 따라 변경사항을 분석한다.

5. **리뷰 결과 출력**
   발견된 사항을 카테고리별로 정리하여 사용자에게 보여준다.
   - 직접 수정 가능한 항목은 🔧 태그를 붙이고, 구체적인 수정 코드를 코드 블록으로 함께 제시한다.

6. **코드 수정 제안**
   🔧 항목이 있으면 "수정 가능한 항목이 N개 있습니다. 코드를 직접 수정할까요?"라고 사용자에게 확인한다.
   - 사용자가 승인하면 PR 브랜치에서 코드를 수정한다.
   - 수정 후 변경사항을 사용자에게 보여주고 **`/done`으로 커밋**하도록 안내 (자동 커밋하지 않음).

7. **PR 리뷰 제출**
   리뷰 결과 출력 후 자동으로 `gh api`로 발견 사항 하나당 하나의 인라인 코멘트를 해당 파일의 해당 라인에 게시한다.
   - `gh api repos/{owner}/{repo}/pulls/$ARGUMENTS/comments` 사용
   - 각 코멘트의 필수 필드: `body`, `commit_id`(PR의 HEAD commit SHA), `path`, `line`(diff 내 라인 번호), `side`("RIGHT")
   - `commit_id`는 `gh pr view $ARGUMENTS --json headRefOid --jq '.headRefOid'`로 조회
   - PR 작성자는 `gh pr view $ARGUMENTS --json author --jq '.author.login'`으로 조회
   - 각 코멘트 본문은 첫 줄에 `@{작성자}` 멘션, 줄바꿈(`\n\n`) 후 심각도 태그 + 설명, 끝에 `\n\n> 🤖 Claude Code가 작성한 리뷰입니다.` 추가
   - 👏 Good 항목은 인라인 대신 `gh pr review $ARGUMENTS --comment --body "내용"`로 전체 코멘트로 남김
   - **Approve / Request Changes 결정은 사용자 몫**. Claude는 `--comment`까지만 자동 게시하며, `--approve`/`--request-changes`는 사용자가 명시적으로 요청할 때만 실행.

## 리뷰 기준

### 1. 컨벤션 (Convention) — 프로젝트 룰
- 브랜치 네이밍 / 커밋 메시지 / PR 템플릿이 각 컨벤션 문서에 부합하는지
- `Closes #N` 이슈 연결 누락 여부
- PR 자동 작성 섹션(요약 / 관련 이슈 / 변경 내용) 누락·미기재 여부
- PR 제목이 연결 이슈 제목과 동일한지 (prefix·번호 없음)

### 2. 정확성 (Correctness)
- 버그 또는 잠재적 버그
- 엣지 케이스 미처리
- 널 안전성 (nullable 처리 누락)
- 오프바이원 에러
- 에러 핸들링 누락

### 3. 설계 (Design)
- 단일 책임 원칙 위반
- 불필요한 결합도
- 의존성 방향 (상위 레이어가 하위 레이어에 의존하는지)
- 인터페이스/추상화 적절성

### 4. 프로젝트 구조 일관성 (Consistency)
- 모듈 구조 및 패키지 컨벤션 준수
- 아키텍처 레이어 규칙 (data/domain/presentation 등) 준수
- 기존 DI / Repository / UseCase 패턴 일관성
- 네이밍 컨벤션 (파일명, 클래스명, 함수명) 일관성
- 새 파일/클래스 위치가 기존 구조와 일치하는지

### 5. 성능 (Performance)
- 불필요한 리컴포지션 (Compose)
- N+1 쿼리 또는 불필요한 반복 호출
- 메모리 누수 가능성 (`CoroutineScope`, `Flow` 구독, 리스너 미해제, Context 참조)
- 메인 스레드 블로킹

### 6. Android 특화
- **Compose**: `remember` / `derivedStateOf` 누락, state hoisting, `LaunchedEffect` key 부적절
- **Lifecycle**: `repeatOnLifecycle` / `collectAsStateWithLifecycle` 사용 여부 (UI Flow 구독 시)
- **CoroutineScope**: `viewModelScope` / `lifecycleScope` 사용 적절성, `GlobalScope` 사용 여부
- **Configuration Change**: 회전 등 구성 변경 시 상태 보존 처리
- **Activity/Fragment 생명주기**: 초기화 타이밍, `onDestroyView`에서 리소스 해제

### 7. 가독성 (Readability)
- 불명확한 네이밍
- 과도한 복잡도 (깊은 중첩, 긴 함수)
- 매직 넘버 / 매직 스트링
- 코드 중복

### 8. 보안 (Security)
- 하드코딩된 시크릿 / API 키
- 입력 검증 누락
- `SharedPreferences`에 민감 정보 평문 저장 (→ `EncryptedSharedPreferences` 권장)
- 로그에 PII / 토큰 노출

## 리뷰 결과 출력 형식

각 발견 사항을 아래 형식으로 정리한다:

```
## 리뷰 결과

### 요약
- 전체적인 변경사항에 대한 한줄 평가
- 주요 발견 사항 개수: N개

### 발견 사항

#### [심각도] 카테고리 — 파일명:라인
설명과 개선 제안

#### 🔧 [심각도] 카테고리 — 파일명:라인
설명과 수정 코드 (직접 수정 가능한 항목)
```

### 심각도 구분
- 🔴 **Must Fix**: 버그, 보안 이슈, 데이터 손실 가능성
- 🟡 **Should Fix**: 설계 문제, 성능 이슈, 구조 불일치, **컨벤션 위반**
- 🟢 **Suggestion**: 가독성, 스타일 개선, 미세 개선 제안
- 👏 **Good**: 잘 작성된 코드, 좋은 설계 판단, 모범 사례 적용

## 규칙
- 자명한 변경(import 정리, 포맷팅, 설정값)에는 코멘트하지 않는다.
- 문제를 지적할 때는 반드시 **개선 방안**을 함께 제시한다.
- 칭찬할 만한 좋은 코드가 있으면 언급한다.
- 리뷰는 **한국어**로 작성한다.
- PR의 맥락(제목, 본문, 관련 이슈)을 고려하여 리뷰한다.
- **Approve / Request Changes 결정은 Claude가 자동으로 하지 않는다.** 자동 게시는 `--comment`까지만.
- 리뷰 후 코드 수정이 이뤄지면 **`/done`으로 커밋**한다. 커맨드 내에서 직접 커밋하지 않는다.
