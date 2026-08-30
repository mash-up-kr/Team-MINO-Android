# 작업 목록: 앱 온보딩 플로우 (Onboarding Flow)

**대상 스펙 경로**: `docs/specs/onboarding-flow`

**기준 plan 버전**: 2.1.0

**최초 작성일**: 2026-08-29

**최종 수정일**: 2026-08-29

**사전 조건**: [plan.md](./plan.md) · [spec.md](./spec.md) · [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/](./contracts/) · [quickstart.md](./quickstart.md)

**테스트**: 포함한다. spec §1이 `TS-001`~`TS-039`를 정의하고 [quickstart.md §3](./quickstart.md)이 JVM 단위 테스트를 검증 수단으로 지정한다.

**구성 방식**: spec의 유저 플로우 5개를 사용자 스토리로 삼는다 — US1(전 구간 완주), US2(공동방 건너뛰기), US3(친구 초대), US4(공유 방법 튜토리얼), US5(완료 후 재실행).

> **이 목록은 `docs/specs/splash-screen`에서 이관받은 작업 셋을 포함한다**(T047·T048·T049). 그 스펙의 tasks.md가 같은 셋을 「이관된 작업」으로 표시하고 실행 지시를 넣지 않았다 — `OnboardingProgressRepository`가 여기서 처음 생겨 두 변경을 나누면 어느 브랜치도 빌드되지 않기 때문이다([splash research.md R-019](../splash-screen/research.md)).

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. 한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리

## 경로 규칙

모바일 다중 Gradle 모듈. 신규 모듈은 `feature/onboarding/`, 공유 계층은 `core/`. 전체 트리는 [plan.md §프로젝트 구조](./plan.md)가 소유한다.

---

## Phase 1: 셋업 (공통 인프라)

**목적**: `:feature:onboarding` 모듈을 만들고 앱 그래프에 편입한다. **빌드가 실제로 서는 것**이 이 단계의 완료 근거다.

- [X] T001 `feature/onboarding/build.gradle.kts` 생성과 `settings.gradle.kts`에 `include(":feature:onboarding")` 추가 — `alias(libs.plugins.mino.android.feature)` + `namespace`만 적용한다. 버전 카탈로그에 새 항목을 추가하지 않는다([plan.md §기술 컨텍스트](./plan.md)). 기존 진입형 feature(`feature/splash`)의 빌드 스크립트를 형태의 기준으로 삼는다
- [X] T002 `app/build.gradle.kts`에 `implementation(project(":feature:onboarding"))` 추가 (T001에 의존) — 절차는 [modularization.md §새 feature 모듈 추가 절차](../../architecture/modularization.md)

**체크포인트**: `./gradlew :app:assembleQaDebug`가 빈 모듈과 함께 통과한다.

---

## Phase 2: 기반 작업 (공통 인프라)

**목적**: 다섯 스토리가 공통으로 쓰는 **온보딩 진행 상태**와 **화면 골격·전환 계약**. 초대 링크(US3)·튜토리얼(US4) 전용 자산은 각 스토리가 갖는다.

### 도메인 — 진행 상태

- [X] T003 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/OnboardingStep.kt`에 `OnboardingStep` enum 생성 (`PROFILE`·`ROOM_FORM`·`INVITE`·`TUTORIAL`) — 정의와 "완료를 값으로 두지 않는" 근거는 [data-model.md §2](./data-model.md). T004·T009·T018이 쓴다
- [X] T004 `core/domain/src/main/kotlin/team/mino/core/domain/model/OnboardingProgress.kt`에 `OnboardingProgress` 생성 (`lastStep`·`createdRoomId`·`isCompleted`) (T003에 의존) — 필드·기본값은 [data-model.md §2](./data-model.md)
- [X] T005 `core/domain/src/main/kotlin/team/mino/core/domain/repository/OnboardingProgressRepository.kt`에 4함수 인터페이스 생성 (T003·T004에 의존) — 시그니처와 "쓰기를 한 함수로 합치지 않는" 근거는 [contracts/onboarding-progress.md §1](./contracts/onboarding-progress.md). **`:core:domain` 밖에서도 읽히는 계약이다**(T047)

### 데이터 — 진행 상태

- [X] T006 [P] `core/data/src/main/java/team/mino/core/data/datasource/OnboardingProgressLocalDataSource.kt`와 `Impl`, `datasource/di/OnboardingDataSourceModule.kt` 생성 — 공유 `DataStore<Preferences>`에서 3개 키를 읽고 쓴다. 키 이름·기본값·알 수 없는 `lastStep`의 폴백은 [data-model.md §4.1](./data-model.md). **`preferencesDataStore` delegate를 새로 만들지 않는다**([research.md R-007](./research.md))
- [X] T007 `core/data/src/main/java/team/mino/core/data/repository/OnboardingProgressRepositoryImpl.kt`와 `repository/di/OnboardingProgressRepositoryModule.kt` 생성 (T005·T006에 의존) — DataSource 값을 `OnboardingProgress`로 조립한다. Mapper를 두지 않는다(DTO 없음). 바인딩 배치는 [dependency-injection.md](../../conventions/dependency-injection.md)

### 도메인 — 재개 지점 판정

- [X] T008 [P] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ResolveOnboardingStepUseCaseTest.kt` 작성 — [contracts/onboarding-progress.md §3](./contracts/onboarding-progress.md)의 **다섯 줄 전부**. 특히 `lastStep = INVITE`인데 `createdRoomId == null`이면 `TUTORIAL`로 떨어지는 방어 규칙(FR-004·SC-004)
- [X] T009 `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ResolveOnboardingStepUseCase.kt` 생성 (T003·T004에 의존) — Repository를 주입받지 않는 순수 함수이며 `suspend`가 아니다. `isCompleted`를 보지 않는다

### 전환 계약과 화면 골격

- [X] T010 [P] `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/OnboardingLauncher.kt` 생성 — `interface OnboardingLauncher : ActivityLauncher` 한 줄. **`ExtraTag.kt`를 건드리지 않는다** — 진입 인자가 없다([contracts/onboarding-launcher.md §2](./contracts/onboarding-launcher.md))
- [X] T011 `feature/onboarding/src/main/java/team/mino/feature/onboarding/OnboardingDestinations.kt`에 Route 3종 생성 (`OnboardingRelay`·`OnboardingInvite(roomId)`·`OnboardingTutorial`) — `@Serializable` 필수. `roomId`가 `String`이라 `typeMap`이 필요 없다([contracts/onboarding-flow-ui.md §1](./contracts/onboarding-flow-ui.md))
- [X] T012 `feature/onboarding/src/main/java/team/mino/feature/onboarding/OnboardingShell.kt`·`OnboardingNavHost.kt` 생성 (T001·T011에 의존) — 셸은 `MinoScaffold`를 열되 **`bottomBar`를 넘기지 않는다**(FR-005). `TrackScreenViews(navController)`도 셸이 갖는다. 골격은 [feature-module.md 4장](../../architecture/feature-module.md). **매니페스트는 여기서 만들지 않는다** — Activity 클래스가 T018에서 생기므로 먼저 선언하면 없는 클래스를 가리키게 된다
- [X] T013 `feature/onboarding/src/main/java/team/mino/feature/onboarding/di/OnboardingLauncherImpl.kt`와 `di/OnboardingNavigationModule.kt` 생성 (T010·T012에 의존) — `createIntent`에서 `OnboardingActivity`만 지정한다. 스코프는 [contracts/onboarding-launcher.md §5](./contracts/onboarding-launcher.md)
- [X] T014 `feature/onboarding/src/main/java/team/mino/feature/onboarding/flow/vm/`에 `OnboardingFlowUiState`·`OnboardingFlowIntent`·`OnboardingFlowSideEffect` 정의 (T003에 의존) — 전체 표는 [contracts/onboarding-flow-ui.md §2.1~2.3](./contracts/onboarding-flow-ui.md). **진행률을 든 필드를 두지 않는다**(UX-006)

**체크포인트**: T009까지 끝나면 US5의 재개 판정을 JVM에서 돌릴 수 있고, T014까지 끝나면 US1의 스텝 머신을 시작할 수 있다.

> **이 Phase가 끝나도 앱에 온보딩 진입점이 없다.** 매니페스트는 T018이 만든다 — 클래스보다 선언이 앞서면 Lint `MissingClass`가 뜬다.

---

## Phase 3: 사용자 스토리 1 - 온보딩을 처음부터 끝까지 밟는다

**목표**: 프로필 저장 → 공동방 생성 → 친구 초대 → 튜토리얼 → 홈으로 이어지는 스텝 머신이 돌고, 각 전환이 진행 상태로 기록된다.

**독립 테스트**: 앱을 지우고 실행해 프로필을 저장하면 공동방 폼이 열리고, 방을 만들면 친구 초대 스텝으로 넘어가는지 확인한다. 어느 스텝에서도 바텀 네비게이션이 보이지 않고 뒤로가기 컨트롤이 없어야 한다 ([quickstart.md §4.1](./quickstart.md)).

### 사용자 스토리 1 테스트 ⚠️

> 구현 전에 작성하고 실패를 확인한다.

- [X] T015 [P] [US1] `feature/onboarding/src/test/java/team/mino/feature/onboarding/flow/vm/OnboardingFlowViewModelTest.kt` 작성 — [contracts/onboarding-flow-ui.md §2.4](./contracts/onboarding-flow-ui.md) **전이 표 7줄 전부**와, 같은 Intent를 두 번 보내도 전이가 한 번만 일어나는 것(UX-005·EC-003). Fake `OnboardingProgressRepository`를 쓴다

### 사용자 스토리 1 구현

- [X] T016 [US1] `feature/onboarding/src/main/java/team/mino/feature/onboarding/relay/screen/OnboardingRelayScreen.kt` 생성 — 배경만 그리는 빈 화면. 상태를 갖지 않는다([research.md R-005](./research.md))
- [X] T017 [US1] `feature/onboarding/src/main/java/team/mino/feature/onboarding/flow/vm/OnboardingFlowViewModel.kt`에 스텝 전이 구현 (T005·T009·T014·T015에 의존) — 전이 표가 FR-001·FR-003·FR-004의 단일 출처다. **저장이 전환보다 앞선다**(EC-019·SC-008). 현재 `step` 가드로 중복 전이를 막는다
- [X] T018 [US1] `feature/onboarding/src/main/java/team/mino/feature/onboarding/OnboardingActivity.kt`와 `feature/onboarding/src/main/AndroidManifest.xml` 생성 (T012·T017에 의존) — 매니페스트는 `OnboardingActivity`를 `android:exported="false"`로 선언하고 **LAUNCHER intent-filter를 두지 않는다**([contracts/onboarding-launcher.md §6](./contracts/onboarding-launcher.md)). Activity는 `ActivityResultLauncher` 2개를 등록해 `ProfileLauncher`·`RoomFormLauncher`를 결과 수신 형태로 부르고, 결과를 `OnboardingFlowIntent`로 옮긴다. 넘길 값과 읽을 값은 [contracts/onboarding-launcher.md §4](./contracts/onboarding-launcher.md). **`setResult`를 호출하지 않는다**(§3)
- [X] T019 [US1] `OnboardingActivity`·`OnboardingShell`에 루트 뒤로가기 처리 추가 (T018에 의존) — 셸의 `BackHandler`가 Activity의 `moveTaskToBack(true)`를 부른다. **온보딩을 끝낸 것으로 보지 않는다** — 완료 표시를 기록하지 않고 스텝도 바꾸지 않는다(FR-007·TS-035·EC-015 · [contracts/onboarding-flow-ui.md §2.5](./contracts/onboarding-flow-ui.md))
- [X] T020 [US1] **T057이 만든** `OnboardingNavHost`의 모든 `navigate` 호출에 `popUpTo(현재) { inclusive = true }` 적용 (T057에 의존 — Phase 6에 있다) — 온보딩 백스택에 앞 스텝이 남지 않는 것이 FR-006·TS-007의 구조적 보장이다([research.md R-006](./research.md))
- [X] T021 [US1] `OnboardingActivity`에 홈 진입 배선 추가 (T017에 의존) — `mainLauncher.launch(this, withFinish = true)`. 완료 표시를 기록한 **뒤에** 부른다(FR-019·FR-021·FR-024 · [research.md R-019](./research.md))

**체크포인트**: 프로필·공동방 스텝을 위임으로 거쳐 친구 초대 Route까지 도달한다. 두 화면은 아직 빈 껍데기다.

---

## Phase 4: 사용자 스토리 2 - 공동방 만들기를 건너뛴다

**목표**: 공동방 폼의 [건너뛰기]가 친구 초대를 거르고 튜토리얼로 직행한다.

**독립 테스트**: 프로필 저장 후 공동방 폼에서 방 이름을 입력해 두고 [건너뛰기]를 누르면, 확인 모달 없이 튜토리얼 스텝 1이 열리고 이 설치의 공동방 수가 0개인지 확인한다 ([quickstart.md §4.2](./quickstart.md)).

- [X] T022 [US2] `OnboardingFlowViewModel`에 `RoomFormSkipped`·`RoomFormCanceled` 전이 추가 (T017에 의존) — `SKIPPED`는 `TUTORIAL`로 직행하고(FR-003·TS-012), `RESULT_CANCELED`는 같은 스텝을 다시 연다([research.md R-020](./research.md)). 두 갈래 모두 T015의 전이 표 테스트가 덮는다

**체크포인트**: 최단 경로(프로필 저장 → 건너뛰기 → 건너뛰기)로 홈에 도달한다(SC-001).

---

## Phase 5: 사용자 스토리 3 - 첫 공동방에 친구를 초대한다

**목표**: 방금 만든 공동방의 초대 링크를 확보해 OS 공유 시트와 클립보드로 내보낸다. 두 액션 어느 쪽도 스텝을 넘기지 않는다.

**독립 테스트**: 방을 만들어 친구 초대 스텝에 도달한 뒤 [초대 링크 복사]를 누르면 토스트가 하단 40dp에 뜨고 **화면이 그대로 머무는지**, 5초를 기다려도 넘어가지 않는지 확인한다. 클립보드를 붙여넣으면 방금 만든 방을 가리키는 링크여야 한다 ([quickstart.md §4.3](./quickstart.md)).

### 사용자 스토리 3 테스트 ⚠️

- [X] T023 [P] [US3] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/GetInviteLinkUseCaseTest.kt` 작성 — 성공 1건과 실패 전파 1건. **`null`이나 빈 문자열로 뭉개지지 않는 것**이 핵심이다(EC-008 · [contracts/invite-link.md §3](./contracts/invite-link.md))
- [X] T024 [P] [US3] `core/data/src/test/java/team/mino/core/data/repository/RoomInvitationRepositoryImplTest.kt` 작성 — 코드 전달 1건, DataSource 예외 전파 1건([contracts/invite-link.md §2](./contracts/invite-link.md))
- [X] T025 [P] [US3] `core/data/src/test/java/team/mino/core/data/invite/InviteLinkBuilderImplTest.kt` 작성 — 주어진 코드로 만들어진 문자열이 기대 형식과 일치하는지 1건([contracts/invite-link.md §4](./contracts/invite-link.md))

### 사용자 스토리 3 구현 — 도메인·데이터

- [X] T026 [P] [US3] `core/domain/src/main/kotlin/team/mino/core/domain/repository/RoomInvitationRepository.kt` 생성 — `suspend fun issueInviteCode(roomId: String): String` 하나. **`RoomRepository`에 얹지 않는다**([research.md R-022](./research.md) · [ADR](../../adr/2026-08-28-api-service-owned-per-server-tag.md))
- [X] T027 [P] [US3] `core/domain/src/main/kotlin/team/mino/core/domain/invite/InviteLinkBuilder.kt` 생성 — `fun build(inviteCode: String): String`. **도메인은 호스트도 경로도 모른다**([ADR](../../adr/2026-08-24-invite-link-assembly-domain-interface.md))
- [X] T028 [US3] `core/data/src/main/java/team/mino/core/data/network/service/InvitationApiService.kt`와 `network/dto/response/InvitationResponse.kt` 생성 — `POST /api/v1/rooms/{roomId}/invitations`. 경로 문자열과 서버 코드 상수를 **이 파일 안에** 둔다. 봉투 해제는 ApiService가 한다([ADR](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)). 응답 스키마는 [contracts/invite-link.md §1.1](./contracts/invite-link.md)
- [X] T029 [US3] `core/data/src/main/java/team/mino/core/data/datasource/InvitationRemoteDataSource.kt`와 `Impl`, 그 `di/` 바인딩 생성 (T028에 의존)
- [X] T030 [US3] `core/data/src/main/java/team/mino/core/data/repository/RoomInvitationRepositoryImpl.kt`와 `repository/di/RoomInvitationRepositoryModule.kt` 생성 (T024·T026·T029에 의존) — **DTO가 이 클래스 밖으로 나가지 않는다**
- [X] T031 [US3] `core/data/src/main/java/team/mino/core/data/invite/InviteLinkBuilderImpl.kt`와 그 `di/` 생성 (T025·T027에 의존) — **전 flavor에서 `https://gguk.org/r/{code}`를 만든다 — flavor별 분기를 두지 않는다**([R-021](./research.md)). 이는 서버 문서의 프로덕션 값을 전체에 적용한 **가정**이며, dev·qa 호스트가 다르면 그 빌드의 초대 링크가 열리지 않는다. 되돌릴 때 고칠 자리는 이 파일 하나다
- [X] T032 [US3] `core/domain/src/main/kotlin/team/mino/core/domain/usecase/GetInviteLinkUseCase.kt` 생성 (T023·T026·T027에 의존) — 코드를 받아 링크로 조립한다. 캐시하지 않는다(EC-012)

### 사용자 스토리 3 구현 — 디자인 시스템·화면

- [X] T033 [P] [US3] `core/design-system/src/main/java/team/mino/core/designsystem/component/topnavigation/MinoTopNavigation.kt`에 **우측 아이콘 액션 축 추가** — 요구하는 성질 넷과 기각한 형태는 [contracts/design-system-additions.md §4.2](./contracts/design-system-additions.md). **기존 파라미터의 의미·기본값을 유지해 `:feature:profile`·`:feature:roomform` 호출부가 깨지지 않게 한다**(§4.3). [X] 아이콘이 `MinoIcons`에 없으면 그 모듈에 더한다
- [X] T034 [P] [US3] `core/common/ui/src/main/java/team/mino/core/common/ui/scaffold/MinoScaffold.kt` 변경 — 스낵바 호스트가 `MinoSnackbar`를 그리고 스크린 하단에서 **40dp** 띄운다(UX-003). 소유 근거는 [ADR](../../adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md), 변경 범위는 [contracts/design-system-additions.md §3](./contracts/design-system-additions.md). **이 변경이 앱 전체의 토스트 위치·모양을 바꾼다 — 그것이 의도다**
- [X] T035 [P] [US3] `feature/onboarding/src/main/res/drawable-{mdpi,xhdpi,xxhdpi}/`에 친구 초대 일러스트 배치 (노드 `2314-95551`·`2314-95553`·`2314-95565`)와 `res/values/strings.xml`에 친구 초대 문구·복사 토스트 문구 추가 — 포맷·밀도는 [ADR](../../adr/2026-08-19-raster-image-placement-and-format.md), 문구 원문은 [contracts/onboarding-flow-ui.md §3.1·§3.5](./contracts/onboarding-flow-ui.md)
- [X] T036 [US3] `feature/onboarding/src/main/java/team/mino/feature/onboarding/invite/vm/`에 `InviteUiState`·`InviteIntent`·`InviteSideEffect`·`InviteViewModel` 생성 (T032에 의존) — 표는 [contracts/onboarding-flow-ui.md §3.2·§3.3](./contracts/onboarding-flow-ui.md). **두 액션은 링크가 없어도 언제나 활성이다**(UX-002·TS-009). 실패 통로는 §3.4
- [X] T037 [US3] `feature/onboarding/src/main/java/team/mino/feature/onboarding/invite/screen/InviteScreen.kt`와 `invite/component/` 생성 (T033·T035·T036에 의존) — Figma `2314-95550`. **참여자 목록·[건너뛰기] 텍스트 버튼·진행 표시를 두지 않는다**(FR-010·FR-013·UX-006)
- [X] T038 [US3] `feature/onboarding/src/main/java/team/mino/feature/onboarding/invite/screen/InviteRoute.kt` 생성 (T034·T036·T037에 의존) — 클립보드 쓰기와 복사 토스트는 Route가, OS 공유 시트는 Activity가 연다(콜백으로 올린다). **두 SideEffect 어디에도 네비게이션이 없다**(FR-012·TS-019·TS-021). 우상단 [X]는 이 화면의 Intent가 아니라 `onClose` 콜백이다

- [X] T058 [US3] `feature/onboarding/src/main/java/team/mino/feature/onboarding/OnboardingActivity.kt`에 공유 시트 처리 추가 (T018·T038에 의존) — `InviteRoute`가 올린 `onShareInviteLink(link)`를 받아 `ACTION_SEND` 인텐트로 OS 공유 시트를 연다. **`resultLauncher`를 쓰지 않는다** — OS가 공유 성공·취소를 구분해 주지 않으므로 시트가 닫혀도 스텝을 넘기지 않는다(FR-011·TS-017·TS-021·EC-010·EC-012 · [contracts/onboarding-flow-ui.md §3.3](./contracts/onboarding-flow-ui.md))
  - **T018과 같은 파일을 건드린다.** 두 작업을 한 전문가에게 순차 배정한다

**체크포인트**: 친구 초대 스텝이 눌러지고, 복사·공유 어느 쪽도 스텝을 넘기지 않는다.

---

## Phase 6: 사용자 스토리 4 - 공유 방법 튜토리얼을 본다

**목표**: 5스텝 캐러셀이 dot·스와이프로 오가고, 스텝 5에서만 하단 CTA가 뜬다.

**독립 테스트**: 튜토리얼에 진입해 스텝 1~5를 넘기며 안내 문구·예시 이미지·dot이 함께 바뀌는지, 스텝 5에서 상단 [건너뛰기]가 사라지고 `꾹 시작하기`가 뜨는지, dot으로 스텝 1로 되돌아오면 그 상태가 되돌아오는지 확인한다 ([quickstart.md §4.4](./quickstart.md)).

- [X] T039 [P] [US4] `core/design-system/src/main/java/team/mino/core/designsystem/component/pagination/`에 `MinoPaginationDots`·`Defaults`·`Preview`·`token/` 신설 — 필요한 표면 셋(개수·선택 인덱스·탭 콜백)과 **"컴포넌트는 캐러셀을 모른다"** 는 제약은 [contracts/design-system-additions.md §2](./contracts/design-system-additions.md). M3 패턴은 [design-system README §6.1](../../../core/design-system/README.md)
- [X] T040 [P] [US4] `feature/onboarding/src/main/res/drawable-{mdpi,xhdpi,xxhdpi}/`에 튜토리얼 예시 이미지 **스텝 1~4** 배치와 `res/values/strings.xml`에 안내 문구 5종 추가 — 문구 원문은 [data-model.md §5](./data-model.md). **스텝 5 이미지는 Figma에 자리표시자만 있어 슬롯을 비운 채 둔다**([열린 항목 C](./research.md#열린-항목))
- [X] T041 [US4] `feature/onboarding/src/main/java/team/mino/feature/onboarding/tutorial/model/TutorialStep.kt` 생성 (T040에 의존) — 스텝 번호·안내 문구·예시 이미지 리소스를 **한 값에서** 낸다. 셋이 한 값에서 나오면 UX-004가 구조로 보장된다([data-model.md §5](./data-model.md))
- [X] T042 [US4] `feature/onboarding/src/main/java/team/mino/feature/onboarding/tutorial/screen/TutorialScreen.kt`와 `tutorial/component/` 생성 (T033·T039·T041에 의존) — Figma `3798-167079`~`3798-167139`. 상단 [건너뛰기]와 하단 CTA는 **현재 페이지 인덱스 하나에서 갈린다**(TS-032). 예시 이미지에 클릭 처리를 붙이지 않는다(FR-020·EC-013)
- [X] T043 [US4] `feature/onboarding/src/main/java/team/mino/feature/onboarding/tutorial/screen/TutorialRoute.kt` 생성 (T042에 의존) — **ViewModel을 두지 않는다**([research.md R-013](./research.md)). 스텝 위치는 `rememberPagerState(pageCount = { 5 })` 하나이며 복원하지 않는다(EC-022). `BackHandler(enabled = page > 0)`으로 스텝 2~5에서만 한 스텝 앞으로 간다(FR-007·EC-014·EC-016)

- [X] T057 [US4] `feature/onboarding/src/main/java/team/mino/feature/onboarding/OnboardingNavHost.kt`에 세 화면을 등록하고 `OnboardingShell`·`OnboardingActivity`에 콜백을 잇는다 (T016·T018·T038·T043에 의존) — `screen<OnboardingRelay>`·`screen<OnboardingInvite>`·`screen<OnboardingTutorial>` 셋을 그래프에 올리고, `InviteRoute`의 `onClose`와 `TutorialRoute`의 `onFinish`를 `OnboardingFlowIntent.InviteClosed`·`TutorialFinished`로 잇는다. `NavigateToInvite`·`NavigateToTutorial` SideEffect는 Activity가 `navController.navigate`로 실행한다([contracts/onboarding-flow-ui.md §1·§2.3](./contracts/onboarding-flow-ui.md)). **T020의 `popUpTo`가 여기서 만든 호출에 붙는다** (FR-003·FR-008·FR-013·FR-019)
  - **이 작업 전까지 그래프는 비어 있다.** 빈 `MinoNavHost`도 컴파일되므로 빌드·Lint·단위 테스트가 잡지 못한다 — 앱을 켜야 드러난다

**체크포인트**: 튜토리얼을 완주하거나 건너뛰어 홈에 도달한다. 이 시점에 전 구간이 이어진다.

---

## Phase 7: 사용자 스토리 5 - 온보딩을 끝낸 뒤 앱을 다시 켠다

**목표**: 완료한 설치는 온보딩을 다시 보지 않고, 중단한 설치는 마지막 스텝부터 이어간다.

**독립 테스트**: 프로필 저장 직후 공동방 폼에서 앱을 강제 종료하고 다시 켜면 **공동방 생성 스텝**이 열리는지(프로필 화면이 다시 나오지 않는지), 완주 후 다시 켜면 메인 탭이 열리는지 확인한다 ([quickstart.md §4.5](./quickstart.md)).

- [X] T044 [P] [US5] `core/data/src/test/java/team/mino/core/data/repository/OnboardingProgressRepositoryImplTest.kt` 작성 — 키가 전부 비어 있을 때의 기본값 · 세 쓰기 각각의 왕복 · 알 수 없는 `lastStep` 문자열의 폴백 · `markCompleted` 후 `isCompleted`([contracts/onboarding-progress.md §2](./contracts/onboarding-progress.md))
- [X] T045 [US5] `OnboardingFlowViewModel`에 `Start` 전이 구현 (T009·T017에 의존) — 저장된 진행 상태를 1회 조회해 `ResolveOnboardingStepUseCase`로 재개 지점을 정하고 그 스텝을 연다(FR-023·TS-037). `isLoading`을 상태로 든다([ADR](../../adr/2026-07-25-uistate-isloading-over-sealed-status.md))
- [X] T046 [US5] `feature/onboarding/src/main/java/team/mino/feature/onboarding/invite/screen/InviteRoute.kt`의 재개 경로 확인 (T038·T045에 의존) — 친구 초대 스텝에서 중단한 뒤 다시 켜면 저장된 `createdRoomId`로 초대 링크를 **다시 확보**하고 공동방을 새로 만들지 않는다(EC-021)

### 이관받은 작업 — splash-screen 소유 설계

> 설계는 `docs/specs/splash-screen`이 소유하고 실행만 여기서 한다. 계약은 [splash contracts/splash-entry-decision.md](../splash-screen/contracts/splash-entry-decision.md)가 갖는다.

- [X] T047 [US5] `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ResolveSplashEntryUseCase.kt`에 `OnboardingProgressRepository`를 함께 주입해 두 근거를 조합 (T005에 의존) — 세 갈래는 splash `contracts/splash-entry-decision.md §2`, **`isRegistered()` 선행 호출 제약은 같은 문서 §3**. 순서를 바꾸면 컴파일도 테스트도 통과하면서 프로필 저장이 깨진다([ADR](../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md)). **같은 커밋에서 `core/domain/src/main/kotlin/team/mino/core/domain/model/SplashEntry.kt`의 KDoc을 갱신한다** — 두 리프 주석이 아직 "프로필이 없다/있다"로 낡은 의미를 들고 있다
- [X] T048 [US5] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ResolveSplashEntryUseCaseTest.kt`를 **네 조합 전수**로 확장 (T047에 의존) — `등록+미완료 → Onboarding`과 `미등록+완료 → Onboarding`을 더하고, 두 Fake의 호출 기록으로 순서를 검증한다. 로컬 조회 실패가 `Main`으로 오판되지 않는 것도 함께
- [X] T049 [US5] `feature/splash/src/main/java/team/mino/feature/splash/SplashActivity.kt`의 온보딩 전환을 `ProfileLauncher` 직접 호출에서 `OnboardingLauncher`로 교체 (T010·T013에 의존) — 고정할 성질 셋은 splash `contracts/splash-ui.md §5`, 교체 근거는 [contracts/onboarding-launcher.md §8](./contracts/onboarding-launcher.md). `ProfileLauncher` 주입과 두 extra import가 스플래시에서 빠진다

**체크포인트**: 앱을 껐다 켜도 진행 상태가 유지되고, 완료한 설치는 메인 탭으로 간다.

---

## Phase 8: 마무리 및 공통 관심사

- [X] T050 [P] Figma 대조 — 친구 초대(`2314-95550`·`2370-67386`)·튜토리얼 5종(`3798-167079`~`3798-167139`)·`MinoPaginationDots`·`MinoTopNavigation` 아이콘 액션. 절차는 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md) §2·§6
- [X] T051 [P] `MinoTopNavigation`·`MinoScaffold` 변경의 회귀 확인 — `:feature:profile`·`:feature:roomform`·`:feature:splash`의 화면이 그대로 보이고 테스트가 통과한다. `./gradlew :feature:profile:test :feature:roomform:test :feature:splash:test`
- [X] T052 `./gradlew :app:assembleQaDebug` 빌드 확인 — 헌법 §품질 게이트가 정한 최소선
- [X] T053 `./gradlew :core:domain:test :core:data:test :feature:onboarding:test` 단위 테스트 전량 통과 확인
- [ ] T054 [quickstart.md](./quickstart.md) 검증 실행 — §4.1~§4.5 수동 시나리오 전부
- [X] T055 `./gradlew ktlintCheck` 통과 확인
- [ ] T056 `docs/specs/splash-screen/tasks.md`의 T032·T033·T034를 완료로 닫도록 사용자에게 알린다 (T047·T048·T049 완료에 의존) — **이 목록이 그 파일을 직접 고치지 않는다.** 이관 작업의 체크 상태는 사람이 확인해 닫는다([splash tasks.md 이관된 작업](../splash-screen/tasks.md))

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **Phase 1**: 의존성 없음 — 즉시 시작
- **Phase 2**: T001 완료에 의존(모듈이 있어야 feature 파일을 놓는다). 단 T003~T009는 `core/`라 T001 없이도 가능
- **Phase 3~7**: 각 작업이 실제로 쓰는 기반 산출물에만 의존한다. Phase 2 전체를 기다리지 않는다
- **Phase 8**: 목표한 스토리 완료에 의존

### 작업 단위 의존

```
T001 ──> T002, T011, T012, T035, T040
T003 ─┬> T004 ──> T005 ──> T007, T047
      └> T009, T014
T006 ──> T007
T008 ──> T009
T010 ──> T013, T049
T011 ──> T012 ──> T013, T020
T014 ─┬> T015 ──> T017
      └> T017 ──> T018 ──> T019, T021
T017 ──> T022, T045
T009 ──> T045
T016 ─┐
T018 ─┤
T038 ─┼> T057 ──> T020        # 세 화면이 모두 있어야 그래프를 채울 수 있다
T043 ─┘
T018 ──> T058 <── T038
T026 ─┬> T030, T032
T027 ─┴> T031, T032
T028 ──> T029 ──> T030
T032 ──> T036 ─┬> T037 ──> T038
              └> T038
T033 ──> T037, T042
T034 ──> T038
T035 ──> T037
T039 ─┬> T042 ──> T043
T041 ─┘
T040 ──> T041
T005 ──> T047 ──> T048
T038 ──> T046
T047, T048, T049 ──> T056
```

### 사용자 스토리 간 의존성

- **US1**: 기반 산출물이 준비되면 시작 가능. 이 feature의 backbone이다. **단 T020은 T057(US4)에 의존한다** — 백스택 규칙이 붙을 `navigate` 호출을 T057이 만들고, 그러려면 세 화면이 모두 있어야 한다. US1의 나머지 여섯은 다른 스토리를 기다리지 않는다
- **US2**: **US1의 T017에 의존한다.** 같은 `OnboardingFlowViewModel`에 전이 한 줄을 더하는 것이라 독립 모듈이 아니다
- **US3**: **US1과 병렬 가능하다.** 화면과 도메인·데이터가 겹치지 않는다. 다만 스텝 머신 없이는 눌러볼 수 없으므로 **검증**은 US1 이후다
- **US4**: **US1·US3과 병렬 가능하다.** `MinoTopNavigation`(T033)만 US3와 공유한다. **다만 T057은 세 스토리의 화면이 모두 끝난 뒤에야 착수할 수 있다** — 이 계획에서 세 갈래가 다시 합류하는 유일한 지점이다
- **US5**: **US1의 T017에 의존한다.** 재개는 스텝 머신 위에 얹힌다. 이관 작업(T047~T049)은 T005·T010·T013만 있으면 되므로 더 일찍 시작할 수 있다

### 병렬 처리 기회

- T003·T006·T008·T010은 서로 다른 파일이라 동시 진행 가능
- **US3의 도메인·데이터(T026~T032)와 US4의 디자인 시스템·에셋(T039·T040)은 US1의 스텝 머신(T015~T021)과 완전히 병렬이다** — 이 셋이 이 계획의 가장 넓은 병렬 구간이다
- T023·T024·T025 세 테스트는 서로 다른 모듈이라 동시 진행 가능
- T033·T034는 서로 다른 모듈(`:core:design-system`·`:core:common:ui`)이라 동시 진행 가능
- T050·T051은 구현이 끝나는 대로 병행 가능
- **T057은 병렬 대상이 아니다.** 세 화면(T016·T038·T043)과 Activity(T018)를 모두 요구하는 합류 지점이라, 그 앞의 병렬 구간이 전부 닫힌 뒤에 온다

---

## 병렬 실행 예시: 기반 작업

```bash
# 도메인 모델·로컬 저장·전환 계약을 함께:
Task: "core/domain/.../model/OnboardingStep.kt에 OnboardingStep enum 생성"
Task: "core/data/.../datasource/OnboardingProgressLocalDataSource.kt(+Impl)와 di 생성"
Task: "core/navigation/.../launcher/OnboardingLauncher.kt 생성"
```

## 병렬 실행 예시: 세 스토리 동시 진행

```bash
# US1 스텝 머신 · US3 초대 도메인 · US4 디자인 시스템이 서로를 기다리지 않는다:
Task: "feature/onboarding/.../flow/vm/OnboardingFlowViewModel.kt에 스텝 전이 구현"
Task: "core/domain/.../usecase/GetInviteLinkUseCase.kt 생성"
Task: "core/design-system/.../component/pagination/에 MinoPaginationDots 신설"
```

---

## 구현 전략

### MVP 우선 (US1 + US2)

1. Phase 1 셋업 → 2. US1이 쓰는 기반(T003~T014) → 3. Phase 3 US1 → 4. Phase 4 US2
5. **중단하고 검증**: 프로필 저장 → 공동방 건너뛰기 → (튜토리얼 자리는 빈 화면) 로 이어지는지
6. 이 시점에 스텝 머신이 서고 진행 상태가 기록된다. 두 화면의 내용만 없다

### 점진적 전달

1. US1 완료 → 스텝 전이 데모
2. US3 추가 → 친구 초대 데모 (초대 링크가 실제 서버 코드로 나온다)
3. US4 추가 → 전 구간 완주 데모
4. US5 추가 → 중단·재개와 스플래시 진입 판정
5. Phase 8로 디자인 대조·회귀·빌드를 닫는다

### 팀 병렬 전략

- 개발자 A: `core/` 진행 상태 계층과 스텝 머신 (T003~T009, T014~T022, T044~T045)
- 개발자 B: 초대 도메인·데이터와 친구 초대 화면 (T023~T032, T036~T038)
- 개발자 C: 디자인 시스템과 튜토리얼 (T033·T034·T039~T043)
- 이관 작업(T047~T049)은 T005가 머지된 뒤 누구든 집을 수 있다. **단 T049는 T013 이후여야 한다** — `OnboardingLauncherImpl` 바인딩이 없으면 스플래시가 주입받지 못한다

---

## 커버리지 확인

spec의 요구사항이 모두 작업에 대응한다.

| 요구사항 | 대응 작업 |
|---|---|
| FR-001 | T017, T018 |
| FR-002 | T018 |
| FR-003 | T022, T057 |
| FR-004 | T017, T022 |
| FR-005 | T012 |
| FR-006 | T020, T037, T042 |
| FR-007 | T019, T043 |
| FR-008 | T032, T036, T046, T057 |
| FR-009 | T035, T037 |
| FR-010 | T037 |
| FR-011 | T038, **T058** |
| FR-012 | T038 |
| FR-013 | T033, T037, T038, **T057** |
| FR-014 | T040, T041, T042 |
| FR-015 · FR-016 | T039, T043 |
| FR-017 · FR-018 | T042 |
| FR-019 | T021, **T057** |
| FR-020 | T042 |
| FR-021 | T021, T047 |
| FR-022 | T047, T048 |
| FR-023 | T009, T045, T046 |
| FR-024 | T017, T021 |
| UX-001 | T017, T045 |
| UX-002 | T036 |
| UX-003 | T034 |
| UX-004 | T041 |
| UX-005 | T017 |
| UX-006 | T014, T037 |
| SC-001~SC-006 · SC-008 · SC-009 | T054 (quickstart 검증) |
| SC-007 | **대응 작업 없음** — 완주율 계측은 spec §3.2가 비목표로 뒀다 |

**2026-08-29 보정**: `/mino-analyze`가 찾은 커버리지 공백 둘을 닫았다 — **T057**(NavHost 화면 등록·콜백 배선)과 **T058**(OS 공유 시트). 둘 다 계약이 "A가 B에게 넘긴다"고 적어 둔 자리인데 B 쪽 작업이 없었다. `FR-006`·`FR-008` 행의 누락 작업도 함께 채웠다.

**설계 미확정 없음.** plan 2.0.1의 미확정이 0건이다.

**가정 위에 선 값 1건.** 초대 링크 호스트를 전 flavor에서 프로덕션 값으로 쓴다([R-021](./research.md)). 확인된 사실이 아니라 **답 없이 진행하기로 한 것**이므로, dev·qa 빌드에서 초대 링크가 열리지 않으면 이 가정부터 의심한다. 서버팀 확인은 협의 항목 S-1로 남는다.

**에셋 공백 1건.** 튜토리얼 **스텝 5의 예시 이미지**가 Figma에 자리표시자만 있다(T040·[열린 항목 C](./research.md#열린-항목)). 구조는 만들고 슬롯을 비운 채 둔다.

**이 feature가 고치지 않는 것 1건.** 프로필·공동방 폼의 온보딩 백프레스가 spec FR-007과 어긋난 채 구현되어 있다([열린 항목 A](./research.md#열린-항목) · [R-026](./research.md)). 두 spec의 개정이 선행되어야 하므로 작업으로 세우지 않았다.

---

## 참고 사항

- 커밋 단위는 [`commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다. **T047·T048·T049는 T005와 한 흐름으로 묶는다** — 나누면 `ResolveSplashEntryUseCase`가 존재하지 않는 타입을 참조해 어느 쪽 커밋도 빌드되지 않는다
- T033·T034는 다른 feature가 쓰는 파일을 넓히는 변경이다. **회귀 확인(T051)을 같은 PR에 넣는다**([ADR](../../adr/2026-08-28-api-service-owned-per-server-tag.md) §결과의 공유 파일 규칙과 같은 이유)
- **T018·T058은 같은 파일(`OnboardingActivity.kt`)을 건드린다.** 한 전문가에게 순차 배정하고, 나눠 배정하지 않는다
- **T057이 붙기 전에는 앱을 켜도 온보딩이 동작하지 않는다.** 빈 `MinoNavHost`가 컴파일되므로 T052·T053·T055가 전부 통과한다 — 이 구간의 검증은 T054 수동 시나리오뿐이다
- 이 저장소에는 PR을 검증하는 CI가 없다(헌법 §검증 장치의 한계). T052·T053·T055는 로컬에서 직접 돌린다
