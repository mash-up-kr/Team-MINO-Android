# Git Worktree 병렬 작업 컨벤션

멀티모듈 + Git Flow 구조에서 경계가 다른 작업(예: `core:map` ↔ `feature:sample`)을 **메인 작업 트리를 점유하지 않고** 충돌 없이 병렬로 굴리기 위한 worktree 운영 규칙이다.

> 브랜치 이름·prefix·slug·Git Flow 절차는 [`branch-naming.md`](branch-naming.md)를 단일 출처로 따른다. 이 문서는 worktree **운영 방식**만 다룬다.

## 전제 (팀원 각자 1회)

1. **Claude Code 업데이트** — 네이티브 worktree(`--worktree` 플래그, `.worktreeinclude`)는 최신 버전에서만 동작한다. `claude --help`에 `--worktree`가 보이지 않으면 업데이트한다.
2. **워크트리 base를 develop으로 고정** — 네이티브 worktree는 기본적으로 `origin/HEAD`(보통 `main`)에서 분기한다. Git Flow의 base인 `develop`으로 맞추려면 클론마다 한 번 실행한다:
   ```sh
   git remote set-head origin develop
   ```
   이후 `claude -w`로 만든 워크트리는 `develop`에서 분기된다.
3. **워크스페이스 신뢰 승인** — 해당 디렉터리에서 `claude`를 한 번 실행해 신뢰 다이얼로그를 수락해야 `--worktree`가 동작한다.

## 두 가지 흐름 (하이브리드)

| 용도 | 방식 | 브랜치 | base |
|---|---|---|---|
| 즉석 실험·짧은 작업 | 네이티브 `claude -w <name>` | `worktree-<name>` | `develop`(위 전제 2 적용 시) |
| 정식 이슈 작업 | `/issue --worktree` | `feature/<issue#>-<slug>` | `origin/develop` |

- **즉석 흐름**: 컨벤션 브랜치명이 필요 없는 짧은 실험에 쓴다. `worktree-*` 브랜치명은 Git Flow 정식 브랜치가 아니므로, 계속 이어갈 작업이라면 정식 흐름으로 옮긴다.
- **정식 흐름**: 이슈 번호가 있는 작업은 [`/issue --worktree`](../../.claude/commands/issue.md)로 만든다. 기존 `/issue`(제자리 checkout) 위에 워크트리 생성만 얹은 변형으로, `feature/<issue#>-<slug>` 컨벤션을 그대로 지킨다. 아래 [절차](#issue---worktree-절차)가 그 동작의 단일 출처다.

## 워크트리 위치와 로컬 파일

- 네이티브 워크트리는 저장소 루트 하위 `.claude/worktrees/<name>/`에 생성된다. 이 경로는 `.gitignore`에 등록되어 있다.
- `local.properties`·`keystore.properties`·`app/google-services.json`은 gitignore 대상이라 새 워크트리에 복사되지 않으면 첫 Gradle Sync가 깨진다. 루트의 [`.worktreeinclude`](../../.worktreeinclude)가 네이티브 워크트리 생성 시 이 파일들을 자동 복사한다.
  - **주의**: `.worktreeinclude`는 네이티브 `claude -w`(및 서브에이전트 워크트리)에만 적용된다. 수동 `git worktree add`에는 적용되지 않으므로 직접 복사해야 한다.

## `/issue --worktree` 절차

`/issue`에 `--worktree`(별칭 `-w`)를 주면, 이슈 생성까지는 기본 흐름과 동일하고 **브랜치 분기만 워크트리 생성으로 바뀐다**. 커맨드는 이 절차를 단일 출처로 따른다.

- **워킹 트리 clean 검증 생략**: 새 워크트리는 현재 작업 트리를 건드리지 않으므로, 미커밋 변경이 있어도 진행한다.
- **base는 `origin/develop`**: 네이티브 `claude -w`와 달리 수동 `git worktree add`로 만들어 컨벤션 브랜치명(`feature/<issue#>-<slug>`)을 지킨다.
- **로컬 파일 직접 복사**: 수동 생성이라 `.worktreeinclude`가 적용되지 않으므로, [`.worktreeinclude`](../../.worktreeinclude)에 등록된 로컬 파일들(`local.properties`·`keystore.properties`·`app/google-services.json`)을 새 워크트리의 같은 경로로 복사한다.
- **upstream을 자기 브랜치로 교정**: `worktree add -b ... origin/develop`은 새 브랜치의 upstream을 `origin/develop`으로 잡는다. 이대로면 `push.default=simple`에서 브랜치 이름이 달라 `git push`가 거부되고 매번 `-u`가 필요하다(`upstream`/`tracking` 모드였다면 develop에 직접 push될 수도 있다). merge ref를 자기 브랜치로 덮어쓰면 첫 `git push`가 별도 플래그 없이 `origin/feature/<issue#>-<slug>`를 생성한다.

```sh
git fetch origin
git worktree add ".claude/worktrees/<issue#>-<slug>" -b "feature/<issue#>-<slug>" origin/develop
git config "branch.feature/<issue#>-<slug>.merge" "refs/heads/feature/<issue#>-<slug>"
for f in local.properties keystore.properties app/google-services.json; do
  [ -f "$f" ] && cp "$f" ".claude/worktrees/<issue#>-<slug>/$f"
done
```

생성 후 사용자에게 새 워크트리에서 세션을 여는 방법을 안내한다: `cd .claude/worktrees/<issue#>-<slug> && claude`

같은 절차를 커맨드 없이 손으로 실행해도 결과는 동일하다.

## 목록·정리

```sh
git worktree list                       # 현재 워크트리 목록
git worktree remove <경로>              # 워크트리 제거 (변경사항 있으면 --force)
git worktree prune                      # 삭제된 워크트리 레코드 정리
```

네이티브 세션 종료 시 정리 동작:

| 상태 | 동작 |
|---|---|
| 변경·미추적·신규 커밋 없음 | 워크트리·브랜치 자동 삭제 (세션에 이름이 있으면 유지 여부를 물음) |
| 변경/미추적/커밋 있음 | 유지·삭제 여부를 물음 |
| `-p` 비대화형 실행 | 자동 정리 안 함 → `git worktree remove`로 수동 정리 |

수동 `git worktree add`로 만든 워크트리는 자동 정리 대상이 아니다.

## 빌드 비용

워크트리마다 별도 `build/`·`.gradle/`가 생긴다. `gradle.properties`의 `org.gradle.caching=true`로 빌드 캐시를 `~/.gradle`에서 공유해 재빌드 비용을 완화한다.

## 주의

- 워크트리는 **파일만 격리**한다. Bash 명령·Gradle 데몬·에뮬레이터 등은 시스템 전역에 영향을 준다.
- 같은 브랜치를 두 워크트리에서 동시에 체크아웃할 수 없다.
