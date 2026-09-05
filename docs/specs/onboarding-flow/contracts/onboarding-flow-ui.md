# 계약: 화면 (`UiState` · `Intent` · `SideEffect`)

**대상 스펙 경로**: `docs/specs/onboarding-flow` · **부속 문서**: [plan.md](../plan.md)

`:feature:onboarding`의 화면 계약이다. MVI 기반 타입(`MviContainer`·`UiState`·`Intent`·`SideEffect`)의 정의는 [`core/common/android/README.md`](../../../../core/common/android/README.md), Route↔Screen 구성 규칙은 [`feature-module.md`](../../../architecture/feature-module.md) 4장이 소유한다.

> **Figma 노드**: 친구 초대 `2314-95550` · 복사 토스트 `2370-67386` · 튜토리얼 `3798-167079`·`3798-167094`·`3798-167109`·`3798-167124`·`4396-184972`.

---

## 1. 화면과 그래프

| Route | 화면 | 진입 인자 | 근거 |
|---|---|---|---|
| `OnboardingRelay` | 배경만 그리는 빈 화면. 위임 스텝(프로필·공동방) 동안 머무른다 | 없음 | [research.md R-005](../research.md) |
| `OnboardingInvite(roomId: String)` | 친구 초대 스텝 | `roomId` — `String`이라 `typeMap` 불필요 | FR-008~FR-013 |
| `OnboardingTutorial` | 공유 방법 튜토리얼 스텝 | 없음 | FR-014~FR-020 |

이 골격은 이미 머지된 `:feature:splash`·`:feature:profile`·`:feature:roomform` 셋과 같다 — `XActivity` → `XShell`(`MinoScaffold` + `TrackScreenViews`) → `XNavHost` → `XRoute`.

**셸(`OnboardingShell`)이 `MinoScaffold`를 열되 `bottomBar`를 넘기지 않는다** — 그것이 FR-005(온보딩 전 구간 바텀 네비 비노출)의 구현이다. `TrackScreenViews(navController)`도 셸이 갖는다.

**전환은 모두 `popUpTo(현재) { inclusive = true }`를 동반한다.** 온보딩 백스택에 앞 스텝이 남지 않는 것이 FR-006·TS-007의 구조적 보장이다([research.md R-006](../research.md)).

---

## 2. 플로우 (`flow/vm/`)

Activity 스코프 ViewModel이다. 스텝 전이 규칙 전체를 여기가 소유한다.

### 2.1 `OnboardingFlowUiState`

| 필드 | 타입 | 초기값 | 설명 | 근거 |
|---|---|---|---|---|
| `isLoading` | `Boolean` | `true` | 저장된 진행 상태를 읽는 동안 참 | FR-023 |
| `step` | `OnboardingStep` | `PROFILE` | 현재 스텝 | FR-001 |
| `createdRoomId` | `String?` | `null` | 이 온보딩에서 만든 공동방 | FR-008·UX-001 |
| `invitedRoomId` | `String?` | `null` | 초대 딥링크(SYS-010)로 자동 참여까지 끝난 방. 튜토리얼을 마쳤을 때 `NavigateToHome`이 아니라 `NavigateToHomeWithRoom`으로 갈지를 가른다 | SYS-010 |

- `isLoading`을 별도 필드로 두는 이유 → [UiState isLoading 분리형 ADR](../../../adr/2026-07-25-uistate-isloading-over-sealed-status.md).
- **진행률을 든 필드가 없다**(UX-006). 남은 스텝 수·전체 스텝 수를 상태에 두지 않는 것이 그 요구사항의 표현이다.

### 2.2 `OnboardingFlowIntent`

| Intent | 발생 시점 | 근거 |
|---|---|---|
| `Start` | Activity 최초 생성 1회 | FR-023 |
| `ProfileSaved` | 프로필 Activity가 `RESULT_OK` | FR-002·TS-002 |
| `RoomCreated(roomId)` | 공동방 폼이 `CREATED` | FR-004·TS-003 |
| `RoomFormSkipped` | 공동방 폼이 `SKIPPED` | FR-003·TS-012 |
| `RoomFormCanceled` | 공동방 폼이 `RESULT_CANCELED` | [research.md R-020](../research.md) |
| `InviteClosed` | 친구 초대 우상단 [X] | FR-013·TS-022 |
| `TutorialFinished` | 튜토리얼 [건너뛰기] 또는 `꾹 시작하기` | FR-019·TS-005·TS-031 |

- **모든 Intent가 사용자 조작 또는 결과 수신에서 온다.** 시간 경과로 발화하는 Intent가 없다 — 그것이 UX-005("시간이 지나 저절로 넘어가는 스텝은 없다")다.
- 프로필 스텝에는 건너뛰기 Intent가 없다(FR-002·EC-007).

### 2.3 `OnboardingFlowSideEffect`

| SideEffect | 페이로드 | Activity가 하는 일 | 근거 |
|---|---|---|---|
| `LaunchProfile` | 없음 | `profileLauncher.launch(…, resultLauncher)` + 온보딩 진입 extra | FR-001 |
| `LaunchRoomForm` | 없음 | `roomFormLauncher.launch(…, resultLauncher)` + 온보딩 extra | FR-003·FR-004 |
| `NavigateToInvite` | `roomId` | `navController.navigate(OnboardingInvite(roomId))` | FR-008 |
| `NavigateToTutorial` | 없음 | `navController.navigate(OnboardingTutorial)` | FR-013·FR-003 |
| `NavigateToHome` | 없음 | `mainLauncher.launch(this, withFinish = true)` | FR-019·FR-021 |

전환을 Activity가 실행하는 이유와 두 `navigate`가 Activity에서 시작하는 형태는 [`feature-navigation.md`](../../../architecture/feature-navigation.md) 1장·[research.md R-003](../research.md).

### 2.4 전이 표 — 이 표가 FR-001·FR-003·FR-004의 단일 출처다

| 현재 `step` | Intent | 저장 (전환 **전**) | 새 `step` | SideEffect |
|---|---|---|---|---|
| — | `Start` | 없음(읽기만) | `ResolveOnboardingStepUseCase(progress)` | 그 스텝에 해당하는 Launch/Navigate |
| `PROFILE` | `ProfileSaved`(초대 코드 없음, 또는 있으나 참여 실패) | `setCurrentStep(ROOM_FORM)` | `ROOM_FORM` | `LaunchRoomForm` |
| `PROFILE` | `ProfileSaved`(초대 코드 보유, 참여 성공 — SYS-010) | `setInvitedRoomId(roomId)` → `setCurrentStep(TUTORIAL)` | `TUTORIAL` | `NavigateToTutorial` |
| `ROOM_FORM` | `RoomCreated(id)` | `setCreatedRoomId(id)` → `setCurrentStep(INVITE)` | `INVITE` | `NavigateToInvite(id)` |
| `ROOM_FORM` | `RoomFormSkipped` | `setCurrentStep(TUTORIAL)` | `TUTORIAL` | `NavigateToTutorial` |
| `ROOM_FORM` | `RoomFormCanceled` | 없음 | `ROOM_FORM` | `LaunchRoomForm` |
| `INVITE` | `InviteClosed` | `setCurrentStep(TUTORIAL)` | `TUTORIAL` | `NavigateToTutorial` |
| `TUTORIAL` | `TutorialFinished`(`invitedRoomId` 없음) | `markCompleted()` | — | `NavigateToHome` |
| `TUTORIAL` | `TutorialFinished`(`invitedRoomId` 있음 — SYS-010) | `markCompleted()` | — | `NavigateToHomeWithRoom(invitedRoomId)` |

**중복 조작 방지(UX-005·EC-003)**: 각 Intent는 **현재 `step`이 표의 왼쪽 칸과 같을 때만** 처리한다. 같은 버튼을 연달아 눌러도 두 번째 Intent는 이미 바뀐 `step`과 맞지 않아 버려진다. 이 가드가 있으므로 화면 쪽에서 버튼을 비활성화하는 처리는 두지 않는다.

**SYS-010(초대 딥링크) 갈래**: 초대 코드로 온보딩에 진입한 신규 유저는 공동방 생성·친구 초대 두 스텝만 건너뛰고 튜토리얼은 그대로 거친다(Figma 스펙 — 온보딩 → 프로필 설정 → 튜토리얼 → 초대받은 방 상세). `invitedRoomId`는 재개 경로에도 필요해 [`OnboardingProgressRepository.setInvitedRoomId`](../../../../core/domain/repository/OnboardingProgressRepository.kt)로 영속화한다 — `createdRoomId`가 친구 초대 스텝 재개를 위해 저장되는 것과 같은 이유다.

**저장이 전환보다 앞선다** — 이 규칙의 단일 출처가 여기다. FR-024가 요구하는 것은 기록 자체이고, **순서**는 이 계획이 정한다: 순서를 뒤집으면 기록 직전에 죽은 프로세스가 같은 스텝을 두 번 실행한다(EC-019·SC-008).

### 2.5 뒤로가기

| 지점 | 처리 | 어디 | 근거 |
|---|---|---|---|
| 튜토리얼 스텝 2~5 | 한 스텝 앞으로 | 튜토리얼 Route의 `BackHandler(enabled = page > 0)` | FR-007·TS-034·EC-014 |
| 그 밖의 온보딩 소유 지점 | `moveTaskToBack(true)` | 셸의 `BackHandler` → Activity 콜백 | FR-007·TS-035·EC-015·EC-016 |
| 프로필·공동방 스텝 | **이 계획이 정하지 않는다** — 그 두 Activity가 소유하고, 현재 둘 다 제스처를 삼켜 `무반응`이다 | — | [열린 항목 A](../research.md#열린-항목) · [R-026](../research.md) |

- `moveTaskToBack`은 온보딩을 끝낸 것으로 보지 않는다. 완료 표시를 기록하지 않고 스텝도 바꾸지 않는다(spec §5).
- 중첩 `BackHandler`는 안쪽의 활성 핸들러가 이긴다 — 튜토리얼 핸들러가 셸 핸들러보다 먼저 먹는다.
- EC-016(dot으로 스텝 1로 옮긴 뒤 뒤로가기)은 `page == 0`이면 튜토리얼 핸들러가 비활성이 되어 셸 핸들러로 내려가는 것으로 성립한다. "거쳐 온 스텝"을 따로 기억하지 않는 것이 요구사항이다.

---

## 3. 친구 초대 (`invite/`)

**Figma**: `2314-95550`(기본) · `2370-67386`(복사 토스트)

### 3.1 화면 구성

| 요소 | 내용 | 근거 |
|---|---|---|
| 상단 바 | `MinoTopNavigation` — 우상단 [X] 아이콘. 제목 없음. **그 축이 지금은 없어 이 계획이 넓힌다** | FR-013 · [design-system-additions.md §4](design-system-additions.md) |
| 제목 | `친구들을 초대해볼까요?` | FR-009 |
| 본문 | `"여기 어때?"는 이제 그만 친구가 들어오면 저장한 장소가 한눈에 모여요. 다음 약속 장소, 여기서 같이 골라요.` | FR-009 |
| 일러스트 | 캐릭터 일러스트 한 장 (feature 소유 에셋) | [research.md R-016](../research.md) |
| 액션 | `MinoActionArea` — [친구 초대하기] · [초대 링크 복사] | FR-011·FR-012 |
| **없는 것** | 참여자 목록 · [건너뛰기] 텍스트 버튼 · 진행 표시 | FR-010·FR-013·UX-006 |

### 3.2 `InviteUiState`

| 필드 | 타입 | 초기값 | 설명 | 근거 |
|---|---|---|---|---|
| `isLoading` | `Boolean` | `true` | 링크 확보 중 | FR-008 |
| `inviteLink` | `String?` | `null` | 확보한 링크. `null`이면 아직 없거나 실패했다 | FR-008·EC-008 |

`roomId`는 `savedStateHandle.toRoute<OnboardingInvite>()`로 복원하며 상태에 두지 않는다 — 화면이 읽지 않는 값이다.

**링크를 얻는 경로는 `GetInviteLinkUseCase(roomId)` 하나다.** 그 뒤에서 발급 API를 부르는 것은 도메인 아래의 일이고, 서버가 멱등을 보장하므로 재개 진입에서도 같은 링크가 온다([invite-link.md §1.1](invite-link.md)). 화면은 코드도 API도 모른다.

**두 액션은 언제나 활성이다**(UX-002·TS-009). `inviteLink == null`이어도 비활성화하지 않고, 눌리면 실패를 알린다(§3.4).

### 3.3 `InviteIntent` · `InviteSideEffect`

| Intent | 발생 시점 | 근거 |
|---|---|---|
| `Load` | 화면 최초 진입 1회 | FR-008·EC-021 |
| `ShareLink` | [친구 초대하기] | FR-011 |
| `CopyLink` | [초대 링크 복사] | FR-012 |

| SideEffect | 페이로드 | Route/Activity가 하는 일 | 근거 |
|---|---|---|---|
| `ShareInviteLink` | `link` | Activity가 `ACTION_SEND` 공유 시트를 연다 | FR-011·TS-017 |
| `CopyInviteLink` | `link` | Route가 클립보드에 쓰고 복사 완료 토스트를 띄운다 | FR-012·TS-018 |

- **[X]는 이 화면의 Intent가 아니다.** 스텝을 넘기는 조작이므로 `onClose` 콜백으로 플로우 ViewModel에 올린다(§2.2 `InviteClosed`).
- 공유 시트를 Activity가 여는 이유: 외부 앱 전환이라 Activity 컨텍스트가 필요하고, [`feature-navigation.md`](../../../architecture/feature-navigation.md) 1장이 "전환은 Activity가 시작하고 화면은 콜백만 올려보낸다"로 정했다. Route는 `onShareInviteLink(link)` 콜백을 올린다.
- **공유 시트의 결과를 읽지 않는다**(FR-011·TS-021·§5 확정). OS가 성공·취소를 구분해 주지 않으므로 `resultLauncher`를 쓰지 않는다.
- **복사도 공유도 스텝을 넘기지 않는다**(FR-012·TS-019·TS-021). 두 SideEffect 어디에도 네비게이션이 없다는 것이 그 표현이다.
- EC-010(복사 후 이어서 공유)·EC-009(연달아 복사)는 두 Intent가 서로에게 아무 조건도 걸지 않는 것으로 성립한다.

### 3.4 실패 처리

| 상황 | 처리 | 근거 |
|---|---|---|
| `Load` 실패 | `isLoading = false`, `inviteLink = null`. 화면은 그대로 | EC-008·UX-002 |
| `inviteLink == null`인 채 `ShareLink`/`CopyLink` | `emitDomainError(리프)` → Route가 스낵바. 공유 시트를 열거나 클립보드에 쓰지 않고, 이어서 재확보를 시도한다 | EC-008 |

판단 근거와 기각한 대안은 [research.md R-012](../research.md), 통로 계약은 [invite-link.md §5](invite-link.md).

### 3.5 복사 완료 토스트

| 항목 | 값 | 근거 |
|---|---|---|
| 문구 | `클립 보드에 초대링크가 복사되었어요` — Figma 원문(`2370-112921`) | spec §4 가정 |
| 컴포넌트 | `MinoSnackbar` | Figma `Snackbar/Snackbar` 인스턴스 |
| 호스트 | 셸의 `LocalSnackbarHostState` | [`error_handling.md`](../../../conventions/error_handling.md) §6 |
| 위치 | 스크린 하단에서 40dp — `MinoScaffold`가 소유한다 | UX-003 · [토스트 소유자 ADR](../../../adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md) |
| 겹침 | 새 토스트가 이전 것을 대체한다. 쌓이지 않는다 | EC-009 |

`MinoScaffold`가 현재 M3 기본 스낵바를 그리고 오프셋도 갖지 않는다는 사실과 그 변경 범위는 [design-system-additions.md §3](design-system-additions.md)이 든다.

---

## 4. 튜토리얼 (`tutorial/`)

**Figma**: `3798-167079`·`3798-167094`·`3798-167109`·`3798-167124`·`4396-184972`

### 4.1 화면 구성

| 요소 | 스텝 1~4 | 스텝 5 | 근거 |
|---|---|---|---|
| 상단 바 | 제목 `튜토리얼` + 우측 [건너뛰기] | 제목 `튜토리얼`, 우측 액션 **없음** | FR-017·FR-018·TS-029·TS-030 |
| 스텝 번호 | `1`~`4` | `5` | FR-014 |
| 안내 문구 | [data-model.md §5](../data-model.md)의 표 | 〃 | FR-014·TS-024 |
| 예시 이미지 | 스텝별 1장 | **에셋 미확정** | FR-014 · [열린 항목 C](../research.md#열린-항목) |
| dot | 5개, 현재 스텝 하나만 선택 | 〃 | FR-015·TS-025 |
| 하단 CTA | **없음** (`Action Area` 숨김) | `꾹 시작하기` | FR-017·FR-018 |

상단 [건너뛰기]와 하단 CTA는 **현재 페이지 인덱스 하나에서 갈린다.** 두 조건을 따로 두지 않는 것이 TS-032(스텝 5에 갔다가 되돌아오면 상태가 되돌아온다)의 보장이다.

### 4.2 상태 — ViewModel을 두지 않는다

스텝 위치는 `rememberPagerState(pageCount = { 5 })` 하나다. 도메인 호출도 비동기 상태도 복원 대상도 없다([research.md R-013](../research.md)). 복원하지 않는 것은 EC-022의 요구다 — 재개 시 항상 스텝 1이다.

| 조작 | 처리 | 근거 |
|---|---|---|
| 좌우 스와이프 | `HorizontalPager` 기본 동작 | spec §4 가정 |
| dot 탭 | `animateScrollToPage(index)` | FR-016·TS-027·TS-028 |
| 양 끝에서 더 넘기기 | Pager가 막는다 — 별도 코드 없음 | EC-017·EC-018 |
| 예시 이미지 탭 | 아무 반응 없음 — 클릭 처리를 붙이지 않는다 | FR-020·EC-013 |
| [건너뛰기] / `꾹 시작하기` | `onFinish()` 콜백 → 플로우 ViewModel | FR-019 |
| 시스템 뒤로가기 | §2.5 | FR-007 |

**튜토리얼은 저장을 일으키지 않는다**(FR-020·TS-033·SC-006). 이 화면에 Repository·UseCase 주입이 없다는 것이 그 확인 방법이다.

---

## 5. 이 계약이 지켜지는지 보는 법

| 확인 | 방법 | 대응 |
|---|---|---|
| 바텀 네비가 어디서도 안 보인다 | `OnboardingShell`이 `MinoScaffold`에 `bottomBar`를 넘기지 않는다 | FR-005·TS-006 |
| 앞 스텝으로 되돌아갈 수 없다 | 모든 `navigate` 호출에 `popUpTo(inclusive = true)`가 있다 | FR-006·TS-007 |
| 저절로 넘어가는 스텝이 없다 | feature 어디에도 `delay(`가 없다 | UX-005·TS-004 |
| 진행률 표시가 없다 | `OnboardingFlowUiState`에 스텝 수를 든 필드가 없다 | UX-006·TS-010 |
| 튜토리얼이 저장을 일으키지 않는다 | `tutorial/` 아래에 Repository·UseCase 주입이 없다 | FR-020·TS-033 |
| 같은 스텝이 두 번 쌓이지 않는다 | 전이 표(§2.4)의 현재 `step` 가드가 모든 Intent에 걸려 있다 | UX-005·EC-003 |
