# 검증 가이드: 앱 온보딩 플로우

**대상 스펙 경로**: `docs/specs/onboarding-flow` · **부속 문서**: [plan.md](./plan.md)

이 기능이 **엔드투엔드로 동작함을 증명하는 절차**다. 계약과 데이터 모델의 내용은 복제하지 않고 지목한다.

---

## 1. 선행 조건

**1.0.x가 든 네 조건이 전부 충족됐다(2026-08-29 확인).** 이 feature는 더 기다릴 것이 없다.

| # | 무엇 | 상태 |
|---|---|---|
| 1 | `:feature:profile`이 develop에 있다 | 충족 — 서버 연동(`POST /api/v1/users`)까지 |
| 2 | `:feature:roomform`이 develop에 있다 | 충족 — 온보딩 4갈래 결과 계약까지 구현 |
| 3 | `MinoTopNavigation`이 `:core:design-system`에 있다 | 충족. 다만 **우측 아이콘 액션 축이 없어 이 계획이 넓힌다**([design-system-additions.md §4](./contracts/design-system-additions.md)) |
| 4 | `:feature:splash`가 develop에 있다 | 충족. 다만 **온보딩이 아니라 프로필을 직접 열고 있어 이 계획이 배선을 바꾼다**([onboarding-launcher.md §8](./contracts/onboarding-launcher.md)) |

**착수 순서에 강제되는 것은 하나뿐이다** — `SplashActivity`의 전환을 `OnboardingLauncher`로 바꾸는 것은 `:feature:onboarding`이 그래프에 들어간 뒤여야 한다. 그 전까지는 프로필 직접 호출을 그대로 두어도 앱이 빌드된다.

---

## 2. 셋업

```bash
# 모듈 등록 확인
grep -n 'feature:onboarding' settings.gradle.kts app/build.gradle.kts

# 빌드 — 이 저장소의 확인 최소선
./gradlew :app:assembleQaDebug

# 단위 테스트 (회귀 포함)
./gradlew :core:domain:test :core:data:test \
  :feature:splash:test :feature:profile:test :feature:roomform:test
```

> 로컬 `./gradlew lintDebug`는 JBR JIT 이슈로 데몬이 죽을 수 있다. **이 저장소에는 PR을 검증하는 CI가 없다** — 헌법 §검증 장치의 한계. 아래 수동 시나리오가 실질적인 게이트다.

**앱 상태 초기화** (온보딩을 처음부터 다시 보려면)

```bash
adb shell pm clear team.mino.qa      # 설치 데이터 전체 삭제 = 재설치와 같은 상태 (EC-020)
```

---

## 3. 자동 검증 — JVM 단위 테스트

| 대상 | 무엇을 덮는가 | 계약 |
|---|---|---|
| `ResolveOnboardingStepUseCase` | 재개 지점 5갈래(`INVITE` + `roomId` 없음 → `TUTORIAL` 포함) | [onboarding-progress.md §3](./contracts/onboarding-progress.md) |
| `OnboardingProgressRepositoryImpl` | 기본값 · 세 쓰기 왕복 · 알 수 없는 스텝 문자열의 떨어짐 | [onboarding-progress.md §2](./contracts/onboarding-progress.md) |
| `GetInviteLinkUseCase` | 성공 · 실패 전파(`null`로 뭉개지 않음) | [invite-link.md §3](./contracts/invite-link.md) |
| `RoomInvitationRepositoryImpl` | 코드 전달 · 예외 전파 | [invite-link.md §2](./contracts/invite-link.md) |
| `InviteLinkBuilderImpl` | 코드 → 링크 형식 1건 | [invite-link.md §4](./contracts/invite-link.md) |
| `ResolveSplashEntryUseCase` | **3갈래**(미등록 / 등록+미완료 / 등록+완료) — 기존 2갈래 테스트의 회귀 | [onboarding-progress.md §4.2](./contracts/onboarding-progress.md) |
| `OnboardingFlowViewModel` | 전이 표 7줄 + 같은 Intent 두 번의 중복 전이 없음(EC-003) | [onboarding-flow-ui.md §2.4](./contracts/onboarding-flow-ui.md) |

Fake `OnboardingProgressRepository`·`RoomInvitationRepository`·`InviteLinkBuilder`로 덮는다. Compose UI 테스트는 이 저장소에 선례가 없어 도입하지 않는다.

**회귀 대상**: `ResolveSplashEntryUseCaseTest`(`:core:domain`)와 `MinoTopNavigation`을 쓰는 두 feature의 테스트 — `./gradlew :core:domain:test :core:data:test :feature:splash:test :feature:profile:test :feature:roomform:test`.

---

## 4. 수동 검증 — 시나리오

**임시 진입점을 두지 않는다.** `SplashActivity`의 전환을 `OnboardingLauncher`로 바꾸고 나면 앱 실행이 곧 온보딩 진입이다([onboarding-launcher.md §8](./contracts/onboarding-launcher.md)). 그 배선 전에 화면만 먼저 보려면 `OnboardingActivity`를 `adb shell am start -n team.mino.qa/team.mino.feature.onboarding.OnboardingActivity`로 직접 띄운다 — 매니페스트를 고치지 않는다.

### 4.1 전 구간 완주 (유저 플로우 1)

| # | 조작 | 기대 | spec |
|---|---|---|---|
| 1 | 앱 실행 | 프로필 설정 화면. 바텀 네비 없음 | TS-001·TS-006 |
| 2 | 닉네임 입력 후 [저장] | 공동방 생성 폼. 좌상단 뒤로가기 없음, 우상단 [건너뛰기] 있음 | TS-002·TS-011·TS-026 |
| 3 | 방 정보 입력 후 [방 생성하기] → [저장하기] | 친구 초대 화면. 참여자 목록 없음 | TS-003·TS-015 |
| 4 | 화면 확인 | 제목 `친구들을 초대해볼까요?`와 본문 노출 | TS-016 |
| 5 | [초대 링크 복사] | 토스트가 하단 40dp에 뜨고 **화면이 그대로 머무른다** | TS-018·TS-019·TS-023 |
| 6 | 5초 기다린다 | 여전히 친구 초대 화면이다 | TS-004 |
| 7 | 클립보드 붙여넣기(다른 앱) | 방금 만든 방을 가리키는 링크다 | TS-020 |
| 8 | [친구 초대하기] → 시트 취소 | 친구 초대 화면 유지 | TS-017·TS-021 |
| 9 | 우상단 [X] | 튜토리얼 스텝 1. dot 5개, 하단 CTA 없음 | TS-022·TS-025·TS-029 |
| 10 | 스텝 5까지 넘긴다 | 문구·이미지·dot이 함께 바뀌고, 스텝 5에서 [건너뛰기]가 사라지고 `꾹 시작하기`가 뜬다 | TS-024·TS-026·TS-030 |
| 11 | `꾹 시작하기` | 홈 탭 + 바텀 네비 노출 | TS-005 |
| 12 | 앱 종료 후 재실행 | 온보딩이 아니라 메인 탭 | TS-036·TS-039 |

### 4.2 최단 경로 (유저 플로우 2)

| # | 조작 | 기대 | spec |
|---|---|---|---|
| 1 | 프로필 저장 후 공동방 폼에서 방 이름을 입력해 둔다 | — | — |
| 2 | [건너뛰기] | 확인 모달 없이 **친구 초대를 거르고** 튜토리얼 스텝 1 | TS-012·EC-006 |
| 3 | 튜토리얼 [건너뛰기] | 홈 탭. 온보딩 완료 기록 | TS-014·TS-031 |
| 4 | 방 목록 확인 | 공동방 0개 | TS-013 |

### 4.3 뒤로가기 (FR-006·FR-007)

| # | 지점 | 조작 | 기대 | spec |
|---|---|---|---|---|
| 1 | 친구 초대 | 시스템 뒤로가기 | **앱이 백그라운드로.** 앞 스텝으로 가지 않는다 | FR-007·EC-004 |
| 2 | 최근 앱에서 복귀 | — | 친구 초대 화면 그대로 | EC-005 |
| 3 | 튜토리얼 스텝 3 | 시스템 뒤로가기 | 스텝 2 | TS-034·EC-014 |
| 4 | 튜토리얼 스텝 1 | 시스템 뒤로가기 | 앱이 백그라운드로. 홈 탭이 열리지 않는다 | TS-035·EC-015 |
| 5 | 튜토리얼 스텝 4 → dot으로 스텝 1 → 시스템 뒤로가기 | — | 앱이 백그라운드로. 스텝 4로 되짚지 않는다 | EC-016 |
| 6 | 각 스텝 상단 | 눈으로 확인 | 뒤로가기 컨트롤이 없다 | TS-007 |

> **1~2를 프로필·공동방 스텝에서도 확인하려 하지 말 것.** 그 두 화면의 뒤로가기는 각자의 spec이 `무반응`으로 정의하고 **그대로 구현되어 있다** — `ProfileRoute`의 `BackHandler(enabled = !state.isBackEnabled) {}`, `RoomFormRoute`의 `state.isOnboarding -> Unit`. 제스처가 먹지 않는 것이 지금의 정상 동작이다([열린 항목 A](./research.md#열린-항목) · [R-026](./research.md)).

### 4.4 중단과 재개 (유저 플로우 5)

| # | 조작 | 기대 | spec |
|---|---|---|---|
| 1 | 프로필 저장 직후 공동방 폼에서 앱을 강제 종료 | — | — |
| 2 | 앱 재실행 | **공동방 생성 스텝**이 열린다. 프로필 화면이 다시 나오지 않는다 | TS-037·TS-038 |
| 3 | 방을 만들어 친구 초대 스텝에서 강제 종료 → 재실행 | 친구 초대 스텝. 초대 링크를 다시 확보하고 방을 새로 만들지 않는다 | EC-021 |
| 4 | 방 목록 확인 | 공동방 1개 (2개가 아니다) | SC-008·EC-019 |
| 5 | 튜토리얼 스텝 4에서 강제 종료 → 재실행 | 튜토리얼이 **스텝 1**로 열린다 | EC-022 |
| 6 | 완주 후 `adb shell pm clear` → 앱 실행 | 온보딩을 처음부터 다시 거친다 | EC-020 |

### 4.5 실패 경로

| # | 상황 | 만드는 법 | 기대 | spec |
|---|---|---|---|---|
| 1 | 초대 링크 확보 실패 | 친구 초대 진입 직전 비행기 모드 | 화면은 그대로. 두 액션을 누르면 실패 안내가 뜨고 잘못된 링크가 나가지 않는다 | EC-008 |
| 2 | 〃 | 위 상태에서 [X] | 튜토리얼로 넘어간다 | EC-011 |
| 3 | 연달아 복사 | [초대 링크 복사] 빠르게 3회 | 토스트가 쌓이지 않고 스텝도 넘어가지 않는다 | EC-009 |
| 4 | 연타로 스텝 전환 | 프로필 [저장]·[건너뛰기]·[X]를 각각 빠르게 2회 | 같은 스텝이 두 번 열리지 않는다 | EC-003 |
| 5 | 튜토리얼 예시 이미지 탭 | 스텝 1~5에서 이미지를 누른다 | 아무 반응 없음. 인스타그램·공유 시트가 열리지 않는다 | EC-013 |
| 6 | 튜토리얼 완주 후 홈 | 저장된 장소 확인 | 0개 | TS-033·SC-006 |

---

## 5. 이 범위가 확인할 수 없는 것

정직하게 적는다. 아래는 이 계획의 결함이 아니라 **다른 작업이 닫는 구멍**이며, 근거는 [research.md 열린 항목](./research.md#열린-항목)이다.

**1.0.x의 여섯 줄 중 넷이 사라졌다.** 선행 feature 셋이 서버 연동까지 구현되면서 확인 가능해졌거나, 이 계획이 그 변경을 가져왔다.

| spec 항목 | 왜 확인할 수 없는가 | 닫히는 조건 |
|---|---|---|
| **FR-014 스텝 5의 예시 이미지** | Figma에 자리표시자만 있다 | 디자인 ([열린 항목 C](./research.md#열린-항목)) |
| **TS-020의 링크 호스트** | 코드는 실제 서버 값이지만, **QA 빌드가 어느 호스트를 써야 하는지 미정**이다. 링크를 눌러 방에 들어가 보는 것까지는 확인할 수 없다 | [SYS-010] 또는 서버팀 확정 ([열린 항목 D](./research.md#열린-항목)·협의 항목 S-1) |
| **FR-007 프로필·공동방 스텝의 백프레스** | 두 화면이 `무반응`으로 구현되어 있고 이 계획이 고치지 않는다 | 두 spec의 개정 ([열린 항목 A](./research.md#열린-항목)) |

### 2.0.0에서 확인 가능해진 것

| spec 항목 | 어떻게 확인하는가 |
|---|---|
| **SC-002** — 프로필·개인방 없이 홈에 도착하는 경우 0건 | 서버가 `POST /api/v1/users`에서 개인방을 함께 만든다([R-027](./research.md)). 응답이 개인방을 싣지 않으므로 **방 목록에 `내 장소`가 보이는 것으로 간접 확인한다** — §4.2의 4번, §4.4의 4번 |
| **TS-020** — 링크가 방금 만든 방을 가리킨다 | 발급 API가 실제 서버 코드를 준다. 같은 방에서 두 번 눌러 **같은 코드가 오는지**(서버 멱등)까지 확인할 수 있다 |
| **TS-038·TS-039** — 완료 표시로 진입 화면이 갈린다 | 이 계획이 `ResolveSplashEntryUseCase`를 넓힌다([onboarding-progress.md §4](./contracts/onboarding-progress.md)). §4.4의 2번이 그 확인이다 |
| **UX-003 토스트 40dp** | 이 계획이 `MinoScaffold`를 고친다. §4.1의 5번 |

**SC-007(완주율 90%)** 은 이 범위가 계측 인프라를 두지 않는다. 온보딩 퍼널 이벤트는 spec §3.2가 비목표로 뒀다.

---

## 6. 리뷰 체크리스트

이 저장소의 경계·규약 게이트는 리뷰다(헌법 §검증 장치의 한계). 각 계약 문서의 "이 계약이 지켜지는지 보는 법" 절을 그대로 쓴다.

- [onboarding-launcher.md §7](./contracts/onboarding-launcher.md) — feature 간 의존·결과 계약
- [onboarding-progress.md §5](./contracts/onboarding-progress.md) — 도메인 순수성·저장 순서·DataStore 단일 인스턴스
- [invite-link.md §6](./contracts/invite-link.md) — URL 소유 위치·방 중복 생성 없음
- [onboarding-flow-ui.md §5](./contracts/onboarding-flow-ui.md) — 바텀 네비·백스택·자동 전환 없음·진행률 없음
- [design-system-additions.md §6](./contracts/design-system-additions.md) — 컴포넌트·에셋 배치
