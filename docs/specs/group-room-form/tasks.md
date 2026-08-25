# 작업 목록: 공동방 생성 및 편집 폼 (Group Room Form)

**대상 스펙 경로**: `docs/specs/group-room-form`

**기준 plan 버전**: 1.4.1

**최초 작성일**: 2026-08-25

**최종 수정일**: 2026-08-25

**사전 조건**: [plan.md](./plan.md) · [spec.md](./spec.md) · [data-model.md](./data-model.md) · [research.md](./research.md) · [contracts/](./contracts/) · [quickstart.md](./quickstart.md)

**테스트**: 포함한다. [plan.md](./plan.md) §기술 컨텍스트가 JVM 단위 테스트를 명시하고, 대상 목록은 [contracts/room-repository.md](./contracts/room-repository.md) §2 · [contracts/room-form-ui.md](./contracts/room-form-ui.md) §5가 소유하며, [quickstart.md](./quickstart.md) §4가 통과 판정에 넣었다. Compose UI 테스트는 범위 밖이다.

**구성 방식**: 작업을 [spec.md](./spec.md) §1의 유저 플로우 5개(US1~US5)에 대응시켜 묶는다.

**이번 범위의 데이터 레이어는 인메모리 mock이다.** DTO·Mapper·Repository 체인은 실제와 동일하게 세우되 `DataSource` 구현만 mock으로 채운다 — 실서버 전환 시 바뀌는 세 지점은 [contracts/room-api-mock.md](./contracts/room-api-mock.md) §4가 소유하며 이 목록에 없다.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. **한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.** 개정으로 추가되는 작업은 문서에 존재하는(폐기 섹션 포함) 최대 번호 + 1부터 부여하므로, 개정을 거치면 문서 순서와 ID 순서는 어긋날 수 있다. 실행 순서는 Phase 순서와 "의존성 및 실행 순서" 섹션이 말한다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리 (US1~US5)
- 설명에는 정확한 파일 경로를 포함할 것

## 경로 규칙

다중 Gradle 모듈이다. 경로는 저장소 루트 기준이며, 모듈별 소스 루트는 아래를 따른다.

| 모듈 | 소스 루트 |
|---|---|
| `:core:domain` | `core/domain/src/main/kotlin/team/mino/core/domain/` · 테스트 `core/domain/src/test/kotlin/team/mino/core/domain/` |
| `:core:data` | `core/data/src/main/java/team/mino/core/data/` |
| `:core:navigation` | `core/navigation/src/main/java/team/mino/core/navigation/` |
| `:core:design-system` | `core/design-system/src/main/java/team/mino/core/designsystem/` |
| `:feature:roomform` | `feature/roomform/src/main/java/team/mino/feature/roomform/` · 테스트 `feature/roomform/src/test/java/team/mino/feature/roomform/` |

---

## Phase 1: 셋업 (모듈 등록)

**목적**: 진입형 feature 모듈 `:feature:roomform`을 빌드 그래프에 올린다. 구조 결정의 근거는 [plan.md](./plan.md) §프로젝트 구조.

- [X] T001 `settings.gradle.kts`에 `include(":feature:roomform")` 추가
- [X] T002 `feature/roomform/build.gradle.kts` 생성 — `alias(libs.plugins.mino.android.feature)` · `namespace = "team.mino.feature.roomform"` · `testImplementation(libs.kotlinx.coroutines.test)` · `testOptions.unitTests.isReturnDefaultValues = true`(ViewModel이 `SavedStateHandle.toRoute`로 진입 인자를 복원하므로 Bundle 스텁이 필요하다). 구성은 `feature/profile/build.gradle.kts`와 같다
- [X] T003 `app/build.gradle.kts`의 진입형 feature 블록에 `implementation(project(":feature:roomform"))` 추가

**체크포인트**: `./gradlew :app:assembleQaDebug`가 성공하고 빈 모듈이 그래프에 들어간다

---

## Phase 2: 기반 작업 (US1~US5 공통 인프라)

**목적**: 다섯 스토리가 공통으로 쓰는 진입 계약 · 도메인 · mock 데이터 레이어 · 디자인 시스템 · feature 골격을 세운다.

**⚠️ 중요**: 각 작업 줄에 어떤 스토리가 그것을 쓰는지 적었다. 스토리 작업은 자신이 쓰는 산출물이 나온 시점부터 시작할 수 있고, 이 단계 전체의 완료를 기다리지 않는다.

### 진입 계약 (`:core:navigation`)

- [X] T004 [P] `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/ExtraTag.kt`에 진입 인자 2개(`EXTRA_ROOM_FORM_ROOM_ID` · `EXTRA_ROOM_FORM_ONBOARDING`)와 결과 상수 5개(`EXTRA_ROOM_FORM_RESULT_OUTCOME` · `EXTRA_ROOM_FORM_RESULT_ROOM_ID` · `ROOM_FORM_OUTCOME_CREATED` · `ROOM_FORM_OUTCOME_UPDATED` · `ROOM_FORM_OUTCOME_SKIPPED`) 추가. 값의 소유자는 [contracts/room-form-launcher.md](./contracts/room-form-launcher.md) §2·§3의 표다 — *US1 진입 · US3·US4·US5 결과*
- [X] T005 [P] `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/RoomFormLauncher.kt` 신규 — `interface RoomFormLauncher : ActivityLauncher` — *US1~US5 전체*

### 도메인 (`:core:domain`)

- [X] T006 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/RoomColor.kt` 신규 — 12색 + `GRAY` enum, 선언 순서는 Figma 칩 그리드 순서, `companion object`에 `selectable`([data-model.md](./data-model.md) §2) — *US1 팔레트 · US3·US4 저장값*
- [X] T007 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/Room.kt` 신규 — `id`·`name`·`description`·`color`·`ownerId`. `type`·`inviteCode`·`createdAt`은 두지 않는다([data-model.md](./data-model.md) §2) — *US3·US4*
- [X] T008 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/RoomDraft.kt` 신규 — `name`·`description`·`color: RoomColor?`(`null` = 미선택) — *US3·US4*
- [X] T009 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/RoomNameValidation.kt` 신규 — `Valid`·`Blank`·`InvalidCharacter` sealed interface. 길이 초과는 이 타입에 없다 — *US1 CTA 판정 · US2 오류 표시*
- [X] T010 `core/domain/src/main/kotlin/team/mino/core/domain/repository/RoomRepository.kt` 신규 — `getRoom`·`createRoom`·`updateRoom` 세 `suspend` 함수. `Result`·`Flow`를 반환하지 않고 실패는 throw다([contracts/room-repository.md](./contracts/room-repository.md) §1) — *US3·US4*
- [X] T011 `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ValidateRoomNameUseCase.kt` 신규 — 앞뒤 공백 제거 후 판정, 허용 문자는 한글(완성형·자모)·영문·숫자·공백. **길이를 판정하지 않고 `suspend`가 아니다**([contracts/room-repository.md](./contracts/room-repository.md) §2) — *US1·US2*
- [X] T012 [P] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ValidateRoomNameUseCaseTest.kt` 신규 — [contracts/room-repository.md](./contracts/room-repository.md) §2 표의 다섯 줄 + 숫자만·영문만·경계값(1자·15자)

### mock 데이터 레이어 (`:core:data`)

- [X] T013 [P] `core/data/src/main/java/team/mino/core/data/network/dto/response/RoomResponse.kt` · `core/data/src/main/java/team/mino/core/data/network/dto/request/RoomRequest.kt` 신규 — 응답 DTO 1종과 요청 DTO 2종(`CreateRoomRequest`·`UpdateRoomRequest`). 필드는 [data-model.md](./data-model.md) §5 — *US3·US4*
- [X] T014 `core/data/src/main/java/team/mino/core/data/repository/mapper/RoomMapper.kt` 신규 — DTO ↔ 도메인. 색상 식별자 문자열 표는 [contracts/room-api-mock.md](./contracts/room-api-mock.md) §2가 소유하며, **서버 표현이 바뀌면 고칠 곳은 이 파일 하나다**. `description`의 `null`은 `.orEmpty()`로 흡수한다 — *US3·US4*
- [X] T015 [P] `core/data/src/main/java/team/mino/core/data/datasource/mock/RoomMockStore.kt` 신규 — `internal @Singleton` 인메모리 저장소. `Mutex`로 맵 접근 보호 · 짧은 읽을 수 있는 `id` 생성 · 고정 `ownerId` · 시드(공동방 `야호`/`야호호`/`red` 1개 + 개인방 1개) · 관측 가능한 지연. 계약은 [contracts/room-api-mock.md](./contracts/room-api-mock.md) §3 — *US3·US4*
- [X] T016 `core/data/src/main/java/team/mino/core/data/datasource/RoomRemoteDataSource.kt` · `RoomMockRemoteDataSourceImpl.kt` 신규 — `internal` 인터페이스와 유일한 mock 구현. 없는 `roomId`는 `MinoDomainException.Http(404, cause)`를 던지고 **403·실패 주입은 두지 않는다**([contracts/room-api-mock.md](./contracts/room-api-mock.md) §3) — *US3·US4*
- [X] T017 `core/data/src/main/java/team/mino/core/data/datasource/di/RoomDataSourceModule.kt` 신규 — `@Binds @Singleton`으로 mock 구현을 인터페이스에 바인딩. **실서버 전환 시 갈아 끼우는 지점이 이 한 줄이다** — *US3·US4*
- [X] T018 `core/data/src/main/java/team/mino/core/data/repository/RoomRepositoryImpl.kt` 신규 — DataSource 호출 + Mapper 변환. DTO가 이 경계를 넘지 않는다 — *US3·US4*
- [X] T019 `core/data/src/main/java/team/mino/core/data/repository/di/RoomRepositoryModule.kt` 신규 — `@Binds`. 바인딩은 구현 소유 모듈이 갖는다([dependency-injection.md](../../conventions/dependency-injection.md)) — *US3·US4*

### 디자인 시스템 (`:core:design-system`)

- [X] T020 [P] `core/design-system/src/main/java/team/mino/core/designsystem/component/topnavigation/MinoTopNavigation.kt`에 **우측 텍스트 액션 축을 더한다** — 이 컴포넌트는 이미 존재하며(좌측 뒤로가기 + 중앙 타이틀), [contracts/design-system-additions.md](./contracts/design-system-additions.md) §1이 요구하는 축 중 우측 슬롯만 비어 있다. **파라미터는 현 구현 방식에 맞춘다** — 기존 `title`·`modifier`·`onBackClick`을 그대로 두고(계약 §1 초안의 `onNavigateBack`으로 이름을 바꾸지 않는다), 우측 액션도 같은 `on<대상>Click` 관례로 더하며, `colors` 파라미터를 새로 열지 않고 현 구현처럼 `MinoTopNavigationDefaults`를 직접 읽는다. 좌측이 그렇듯 우측도 **`null`이면 그리지 않고 자리는 비워 둔다**. `TopNavigationPreview.kt`에 액션 있는 케이스를 더하고 필요한 토큰은 `token/TopNavigationTokens.kt`에 넣는다. 노드는 `2314-95336`·`2542-125957` — *US1 타이틀 · US5 [건너뛰기]*
- [X] T021 [P] `core/design-system/src/main/java/team/mino/core/designsystem/component/roomcolorchip/MinoRoomColor.kt` 신규 — 12항목 enum. 회색·표시 이름·서버 식별자·그리드 배치를 **넣지 않는다**([방 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)) — *US1*
- [X] T022 `core/design-system/src/main/java/team/mino/core/designsystem/component/roomcolorchip/`에 `MinoRoomColorChip.kt`·`MinoRoomColorChipDefaults.kt`·`RoomColorChipPreview.kt`·`token/RoomColorChipTokens.kt` 신규 — 칩 한 칸(70×70)만 안다. 채움·테두리는 `AtomicColorToken`의 `<색>60`·`<색>40`을 참조하되 **brown 2색만 실측 raw**, 선택 상태는 `Modifier.rippleSingleSelectable`로 노출. 노드는 [contracts/design-system-additions.md](./contracts/design-system-additions.md) §2 — *US1*

### 에셋 (`:feature:roomform`)

- [X] T023 [P] 방 썸네일 13종을 `feature/roomform/src/main/res/drawable-{mdpi,xhdpi,xxhdpi}/`에 WebP로 export — 대상은 디자인 시스템 라이브러리 파일 `hkSOCt4kOfyaVWdxybTicF`의 컴포넌트셋 `16765-22588`(`Room Thumbnail_Empty`), variant 12색 + `my room`(= 회색). 절차는 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md) §1.3 · 포맷·밀도는 [component-asset-placement.md](../../conventions/component-asset-placement.md) §1.1 — *US1 미리보기 · US4 편집 미리보기*

### feature 골격 (`:feature:roomform`)

- [X] T024 `feature/roomform/src/main/java/team/mino/feature/roomform/RoomFormDestinations.kt` 신규 — `@Serializable internal data class RoomForm(roomId: String?, isOnboarding: Boolean) : Route` — *US1~US5*
- [X] T025 `feature/roomform/src/main/java/team/mino/feature/roomform/RoomFormActivity.kt`와 `feature/roomform/src/main/AndroidManifest.xml` 신규 — extra 2개를 복원해 시작 라우트에 싣고, `onFinish(outcome)`을 [contracts/room-form-launcher.md](./contracts/room-form-launcher.md) §3의 **네 갈래 결과로 한 곳에서 매핑**해 `setResult` 후 `finish()`한다. Manifest는 `android:exported="false"` — *US1~US5*
- [X] T026 `feature/roomform/src/main/java/team/mino/feature/roomform/RoomFormShell.kt` 신규 — `MinoScaffold` + `rememberNavController` + `TrackScreenViews`. `startDestination`과 `onFinish`를 그대로 흘려보낸다([feature-module.md](../../architecture/feature-module.md) 4장) — *US1~US5*
- [X] T027 `feature/roomform/src/main/java/team/mino/feature/roomform/RoomFormNavHost.kt` 신규 — `screen<RoomForm> { RoomFormRoute(onFinish) }` — *US1~US5*
- [X] T028 `feature/roomform/src/main/java/team/mino/feature/roomform/di/RoomFormLauncherImpl.kt`·`RoomFormNavigationModule.kt` 신규 — `createIntent`가 `RoomFormActivity`만 지목하고, `@Binds`는 `ActivityRetainedComponent` + `@ActivityRetainedScoped`([contracts/room-form-launcher.md](./contracts/room-form-launcher.md) §4) — *US1~US5*
- [X] T029 [P] `feature/roomform/src/main/java/team/mino/feature/roomform/form/model/RoomFormMode.kt`·`RoomFormDialog.kt` 신규 — `Create`/`Edit(roomId)`와 모달 3종 sealed interface([data-model.md](./data-model.md) §4) — *US1~US5*
- [X] T030 `feature/roomform/src/main/java/team/mino/feature/roomform/form/vm/RoomFormUiState.kt` 신규 — `RoomFormValues`와 `RoomFormUiState`의 9개 필드 + 파생 프로퍼티 4개(`canSubmit`·`isBlankForm`·`isChanged`·`needsExitConfirm`). **파생값을 필드로 두지 않는다**([data-model.md](./data-model.md) §4) — *US1~US5*
- [X] T031 [P] `feature/roomform/src/main/java/team/mino/feature/roomform/form/vm/RoomFormIntent.kt` 신규 — [contracts/room-form-ui.md](./contracts/room-form-ui.md) §2 표의 Intent 10종 — *US1~US5*
- [X] T032 [P] `feature/roomform/src/main/java/team/mino/feature/roomform/form/vm/RoomFormSideEffect.kt` 신규 — `Finish(outcome)` 하나와 `RoomFormOutcome` 네 갈래([contracts/room-form-ui.md](./contracts/room-form-ui.md) §3). **스낵바·화면 전환 SideEffect를 두지 않는다** — *US1~US5*
- [X] T033 `feature/roomform/src/main/java/team/mino/feature/roomform/form/vm/RoomFormViewModel.kt` 신규(골격) — `@HiltViewModel` + `MviContainer` + `DomainErrorEmitter` 위임, `init`에서 `savedStateHandle.toRoute<RoomForm>()`로 `mode`·`isOnboarding` 복원, `processIntent`의 `when` 뼈대. 각 분기의 본문은 US1·US3·US4·US5가 채운다([contracts/room-form-ui.md](./contracts/room-form-ui.md) §5) — *US1~US5*
- [X] T034 [P] `feature/roomform/src/main/res/values/strings.xml` 신규 — 타이틀 2종(FR-025) · CTA 2종(FR-009) · 모달 3종의 제목과 버튼(FR-020·FR-021·FR-024) · 필드 라벨·placeholder·helper(FR-002·FR-004·FR-005) · [건너뛰기](FR-017) · 에러 문구. 문구의 소유자는 [spec.md](./spec.md)이며 여기 옮겨 적는 것은 리소스 값뿐이다 — *US1~US5*

### 임시 검증 진입점 (`:feature:main`)

- [X] T035 `:feature:main`에 폼 진입·결과 수신 배선을 더한다 — `feature/main/src/main/java/team/mino/feature/main/MainActivity.kt`가 `RoomFormLauncher`와 결과 `registerForActivityResult`를 갖고, `MainShell.kt`·`MainNavHost.kt`가 콜백을 관통시키며, `placeholder/screen/MainTabPlaceholderScreen.kt`(또는 그 자리의 임시 화면)이 생성·온보딩 생성·시드 방 편집 세 버튼과 결과 표시를 노출한다. 기존 Sample 배선과 같은 모양이다. **실제 진입점 feature가 생기면 걷어낸다**([plan.md](./plan.md) §범위 경계) — *US1~US5 전체의 유일한 실행 경로*

**체크포인트**: 각 기반 작업이 끝날 때마다 그것을 쓰는 스토리 작업을 시작할 수 있다. T035까지 끝나면 빈 폼이 실제로 열린다

---

## Phase 3: 사용자 스토리 1 - 방 정보 입력

**목표**: 진입점과 무관하게 같은 폼이 열리고, 방 이름·설명·색상을 입력하면 미리보기 카드가 그 자리에서 따라오며 방 이름이 채워지는 순간 CTA가 살아난다. (FR-001·FR-002·FR-003·FR-005·FR-006·FR-007·FR-008·FR-009·FR-023·FR-025 · UX-005·UX-007)

**독립 테스트**: [quickstart.md](./quickstart.md) S-1의 1~6단계와 S-7을 그대로 수행한다 — 저장하지 않고도 폼 구성·실시간 반영·CTA 활성 규칙이 전부 확인된다. 단위 테스트는 TS-001·TS-002·TS-003·TS-037·TS-044.

### 사용자 스토리 1 테스트 ⚠️

> **이 테스트들을 먼저 작성하고, 구현 전에 실패하는지 확인한다**

- [X] T036 [P] [US1] `feature/roomform/src/test/java/team/mino/feature/roomform/fake/FakeRoomRepository.kt` 신규 — `RoomRepository`의 Fake. 조회·생성·편집의 성공/실패를 테스트가 지정할 수 있게 한다. `feature/profile/src/test/.../fake/FakeProfileRepository.kt`와 같은 자리·같은 모양
- [X] T037 [US1] `feature/roomform/src/test/java/team/mino/feature/roomform/form/vm/RoomFormViewModelTest.kt` 신규 — 빈 폼 CTA 비활성(TS-001) · 이름만 입력해도 활성(TS-002) · 15자 상한에서 16번째 글자가 반영되지 않음(TS-003·EC-002) · 진입 맥락에 따른 타이틀·CTA 라벨 분기(TS-044·TS-037)

### 사용자 스토리 1 구현

- [X] T038 [P] [US1] `feature/roomform/src/main/java/team/mino/feature/roomform/form/model/RoomColorUiModel.kt` 신규 — `RoomColor` ↔ `MinoRoomColor` ↔ 썸네일 drawable 매핑. **도메인 값과 UI 에셋을 잇는 자리는 feature다**([방 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md) §결과). `GRAY`는 `my room` 에셋에 대응한다(FR-023·TS-029)
- [X] T039 [P] [US1] `feature/roomform/src/main/java/team/mino/feature/roomform/form/component/RoomPreviewCard.kt` 신규 — 썸네일 + 이름·설명. 값이 비면 안내 문구를 대신 그리고(TS-005), 방 이름이 오류 상태여도 현재 입력값을 그대로 반영한다(EC-007)
- [X] T040 [US1] `feature/roomform/src/main/java/team/mino/feature/roomform/form/component/RoomColorPalette.kt` 신규 — `RoomColor.selectable`을 4열×3행으로 배치하고 단일 선택 규칙(재선택으로 해제되지 않음)을 갖는다. 칩 자체는 배치를 모른다(FR-006·TS-006)
- [X] T041 [US1] `feature/roomform/src/main/java/team/mino/feature/roomform/form/screen/RoomFormScreen.kt` 신규 — `MinoTopNavigation` · 세로 스크롤 입력 영역(`RoomPreviewCard` → 방 이름 `MinoTextField`(`showClearButton = false`, 카운터 없음) → 방 설명 `MinoTextArea`(`maxLength = 30`, `showCounter = true`) → `RoomColorPalette`) · 하단 고정 `MinoActionArea` CTA. **`Scaffold`를 열지 않는다.** 구성은 [contracts/room-form-ui.md](./contracts/room-form-ui.md) §1, 스크롤·고정은 UX-005·EC-004
- [X] T042 [US1] `feature/roomform/src/main/java/team/mino/feature/roomform/form/screen/RoomFormRoute.kt` 신규 — state 구독 · 방 설명 `TextFieldState` 소유와 `DescriptionChanged` 전달(**지연 연산자를 붙이지 않는다**) · `CollectSideEffect`로 `Finish` 수집 후 `onFinish` 호출 · `CollectDomainError` → `LocalSnackbarHostState` · `messageResOf`. 책임 표는 [contracts/room-form-ui.md](./contracts/room-form-ui.md) §4
- [X] T043 [US1] `feature/roomform/src/main/java/team/mino/feature/roomform/form/vm/RoomFormViewModel.kt`에 입력 처리 추가 — `NameChanged`(15자로 자른 뒤 `ValidateRoomNameUseCase` 재실행) · `DescriptionChanged`(그대로 반영) · `ColorSelected`(교체만). **방 이름은 ViewModel이, 방 설명은 `MinoTextArea`가 자른다**([contracts/room-form-ui.md](./contracts/room-form-ui.md) §1). 갱신에 비동기 경계를 두지 않는다(SC-002)
- [X] T044 [P] [US1] `feature/roomform/src/main/java/team/mino/feature/roomform/form/screen/RoomFormScreenPreview.kt` 신규 — 빈 폼·입력 완료·편집 진입 세 케이스

**체크포인트**: 폼이 열리고 세 항목의 입력이 미리보기와 CTA에 그대로 반영된다

---

## Phase 4: 사용자 스토리 2 - 방 이름 입력 오류

**목표**: 허용되지 않는 문자가 섞이면 그 필드에서만 알리고, 오류가 남아 있는 한 다른 항목을 아무리 채워도 방을 만들 수 없다. (FR-004·FR-007 · UX-002)

**독립 테스트**: [quickstart.md](./quickstart.md) S-2를 수행한다 — 오류 표시가 필드 아래 문구에만 나타나고(TS-011) 상단 배너·토스트가 없으며, 자모 단독은 오류가 아니다(EC-025). 단위 테스트는 TS-009 + T012의 판정 케이스.

### 사용자 스토리 2 테스트 ⚠️

- [X] T045 [US2] `feature/roomform/src/test/java/team/mino/feature/roomform/form/vm/RoomFormViewModelTest.kt`에 오류 케이스 추가 — 오류 상태에서 설명·색상을 채워도 CTA 비활성(TS-009) · 오류 문자를 지우면 CTA 복구(TS-010). 문자 판정 자체는 T012가 소유하므로 여기서는 **CTA와의 연결만** 검증한다

### 사용자 스토리 2 구현

- [X] T046 [US2] `feature/roomform/src/main/java/team/mino/feature/roomform/form/screen/RoomFormScreen.kt`의 방 이름 필드에 오류 표시 연결 — `nameValidation`을 `MinoTextFieldStatus`에 매핑한다. `InvalidCharacter`만 `Negative`이고 **`Blank`는 오류로 그리지 않는다**([data-model.md](./data-model.md) §2 · TS-001 vs TS-008). helper 문구는 상태에 따라 색만 바뀌고 문자열은 그대로다(UX-002·TS-011)

**체크포인트**: US1과 US2가 함께 동작하고, 오류 상태가 CTA를 정확히 막는다

---

## Phase 5: 사용자 스토리 3 - 공동방 생성 완료

**목표**: CTA만으로는 방이 만들어지지 않고 저장 확인 모달이 한 번 끼어들며, [저장하기]로 확인해야 방이 만들어지고 `created` + `roomId`가 호출자에게 돌아간다. (FR-006 회색 기본값·FR-010·FR-020 · UX-001·UX-003·UX-008·UX-009)

**독립 테스트**: [quickstart.md](./quickstart.md) S-1의 7~9단계와 S-8을 수행한다 — 임시 진입점이 `created`와 `roomId`를 받아 표시하면 통과다. 도착점 이동과 `방 생성 완료!` 스낵바는 이 범위에서 검증하지 않는다([plan.md](./plan.md) §범위 경계).

### 사용자 스토리 3 테스트 ⚠️

- [X] T047 [P] [US3] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/CreateRoomUseCaseTest.kt` 신규 — `color`가 `null`이면 `GRAY`로 확정해 Repository에 넘기고, 값이 있으면 그대로 넘긴다(FR-006·TS-007)
- [X] T048 [US3] `feature/roomform/src/test/java/team/mino/feature/roomform/form/vm/RoomFormViewModelTest.kt`에 생성 케이스 추가 — CTA가 방을 만들지 않고 모달만 띄움(TS-030) · 모달 [취소]가 입력값을 유지(TS-031·TS-034) · 입력값 그대로 생성되고 `Finish(Created)` 방출(TS-012) · 제출 중 재클릭이 요청을 늘리지 않음(UX-001·SC-005·EC-008) · 실패 시 입력값 유지 + 도메인 에러 방출, **모달을 다시 열지 않음**(UX-003·EC-009)

### 사용자 스토리 3 구현

- [X] T049 [P] [US3] `core/domain/src/main/kotlin/team/mino/core/domain/usecase/CreateRoomUseCase.kt` 신규 — `draft.color`가 `null`이면 `RoomColor.GRAY`로 확정한 뒤 `createRoom` 호출. 회색 기본값은 도메인 규칙이라 ViewModel·Mapper가 아니라 여기 있다([contracts/room-repository.md](./contracts/room-repository.md) §3)
- [X] T050 [US3] `feature/roomform/src/main/java/team/mino/feature/roomform/form/component/RoomFormConfirmDialog.kt` 신규 — 모달 3종 공통. **본문 없이 제목 한 줄 + 버튼 2개**이고 컴포저블은 어느 모달인지 모른 채 제목과 라벨만 받는다. 딤 레이어가 하위 터치를 소비하고 바깥 탭·뒤로가기는 `DialogDismissed`로 올라온다(UX-008·UX-009·EC-018)
- [X] T051 [US3] `feature/roomform/src/main/java/team/mino/feature/roomform/form/vm/RoomFormViewModel.kt`에 생성 제출 추가 — `SubmitClicked`(생성이면 `dialog = Save`, `!canSubmit`이면 무시) · `SaveConfirmed`(모달을 닫고 `CreateRoomUseCase` 실행) · `isSubmitting` 게이트 · 성공 시 `Finish(Created(roomId))`. 실패는 `DomainErrorEmitter`로 나가고 코루틴 시작·소비는 [error_handling.md](../../conventions/error_handling.md) §5·§7을 따른다
- [X] T052 [US3] `feature/roomform/src/main/java/team/mino/feature/roomform/form/screen/RoomFormScreen.kt`에 `state.dialog != null`일 때의 모달 오버레이 연결 — 저장 확인 모달의 제목·버튼을 T034의 문자열로 채운다. **둘 이상이 동시에 뜨지 않는 것은 단일 슬롯이 보장한다**(UX-008)

**체크포인트**: 생성 경로가 끝까지 동작하고 결과가 호출자에게 돌아간다 — MVP 완성 지점

---

## Phase 6: 사용자 스토리 4 - 방장의 방 편집

**목표**: `roomId`를 싣고 진입하면 기존 값이 채워진 폼이 열리고, CTA를 누르면 **저장 확인 모달 없이** 곧바로 반영돼 `updated` + `roomId`가 돌아간다. (FR-013·FR-015 수정분·FR-025 · UX-003)

**독립 테스트**: [quickstart.md](./quickstart.md) S-3을 수행한다 — 시드 방(`야호`)으로 열어 값·카운터·타이틀·CTA 라벨을 확인하고, 고친 뒤 다시 열어 반영을 본다. FR-014(방장 전용 노출)·FR-016(다른 화면 반영)은 대상 화면이 없어 검증하지 않는다.

### 사용자 스토리 4 테스트 ⚠️

- [X] T053 [US4] `feature/roomform/src/test/java/team/mino/feature/roomform/form/vm/RoomFormViewModelTest.kt`에 편집 케이스 추가 — 조회 성공 시 `values`·`initial`이 방의 현재 값으로 채워짐(TS-018) · 이름을 모두 지우면 CTA 비활성(TS-020) · **모달 없이 곧바로 제출되고 `Finish(Updated)` 방출**(TS-019) · 아무것도 고치지 않아도 완료 처리(EC-011) · 실패 시 고친 값 유지 + 도메인 에러 방출(EC-014)

### 사용자 스토리 4 구현

- [X] T054 [US4] `feature/roomform/src/main/java/team/mino/feature/roomform/form/vm/RoomFormViewModel.kt`에 편집 진입 로드 추가 — `mode`가 `Edit`이면 `getRoom` 실행, `isLoading` 토글, 성공 시 `values`와 `initial`을 함께 채우고, 실패는 **State의 `loadError`**로 둔다(주 데이터 로드이므로 스낵바가 아니다 — [error_handling.md](../../conventions/error_handling.md) §5). `RetryLoad`로 재시도한다
- [X] T055 [US4] `feature/roomform/src/main/java/team/mino/feature/roomform/form/screen/RoomFormRoute.kt`에 편집 초기값 주입 추가 — `initial`이 `null`에서 **처음** non-null이 될 때만 `TextFieldState`에 넣는다. 재시도로 다시 채워져도 재주입하지 않고, 프로세스 사망 복원 시에는 `TextFieldState`가 복원한 값이 이긴다([contracts/room-form-ui.md](./contracts/room-form-ui.md) §4)
- [X] T056 [US4] `feature/roomform/src/main/java/team/mino/feature/roomform/form/screen/RoomFormScreen.kt`에 로딩·로드 실패 표시 추가 — `isLoading`이면 로딩, `loadError`가 있으면 에러 화면 + 재시도 버튼(`RetryLoad`)
- [X] T057 [US4] `feature/roomform/src/main/java/team/mino/feature/roomform/form/vm/RoomFormViewModel.kt`에 편집 제출 추가 — 편집 경로의 `SubmitClicked`는 모달을 거치지 않고 `roomRepository.updateRoom`을 **직접** 호출한다(전용 UseCase를 두지 않는 근거는 [contracts/room-repository.md](./contracts/room-repository.md) §3). `isSubmitting` 게이트는 생성과 같고, 성공 시 `Finish(Updated(roomId))`

**체크포인트**: 생성과 편집이 같은 폼을 공유한 채 각자의 규칙대로 끝난다

---

## Phase 7: 사용자 스토리 5 - 폼에서 빠져나가기

**목표**: 잃을 것이 있을 때만 이탈 확인 모달이 뜨고, 생성용·편집용 문구가 갈리며, 온보딩에서는 뒤로가기 자체가 없고 [건너뛰기] 하나로만 벗어난다. (FR-017·FR-018·FR-021·FR-022·FR-024 · UX-009)

**독립 테스트**: [quickstart.md](./quickstart.md) S-4·S-5·S-6을 수행한다 — 세 시나리오가 이 스토리의 분기를 전부 지난다. FR-017의 튜토리얼 스텝 이동은 온보딩 feature의 몫이고, 여기서는 `skipped` 결과 반환까지다.

### 사용자 스토리 5 테스트 ⚠️

- [X] T058 [US5] `feature/roomform/src/test/java/team/mino/feature/roomform/form/vm/RoomFormViewModelTest.kt`에 이탈 케이스 추가 — 빈 생성 폼은 모달 없이 즉시 종료(TS-028·EC-021) · 색상만 골라도 모달(EC-020) · 편집에서 값이 같으면 모달 없음(TS-042·TS-043) · 색상만 바뀌어도 모달(EC-023) · CTA 비활성이어도 값이 다르면 모달(EC-024) · `ExitConfirmed`가 `Finish(Cancelled)`, `SkipClicked`가 `Finish(Skipped)`

### 사용자 스토리 5 구현

- [X] T059 [US5] `feature/roomform/src/main/java/team/mino/feature/roomform/form/vm/RoomFormViewModel.kt`에 이탈·건너뛰기 처리 추가 — `BackClicked`(`needsExitConfirm`이면 `dialog = ExitCreate`/`ExitEdit`, 아니면 즉시 `Finish(Cancelled)`) · `ExitConfirmed` · `DialogDismissed`(다른 상태를 건드리지 않는다) · `SkipClicked`(확인 없이 `Finish(Skipped)`). 생성이냐 편집이냐로 모달 종류가 갈린다(FR-021 vs FR-024)
- [X] T060 [US5] `feature/roomform/src/main/java/team/mino/feature/roomform/form/screen/RoomFormRoute.kt`에 `BackHandler` 추가 — **모달이 떠 있으면 `DialogDismissed`가 `BackClicked`보다 먼저다**(이 순서가 없으면 EC-017을 정면으로 어긴다). 온보딩이면 항상 켠 채 아무 일도 하지 않아 제스처를 삼킨다(FR-022·TS-026·EC-015)
- [X] T061 [US5] `feature/roomform/src/main/java/team/mino/feature/roomform/form/screen/RoomFormScreen.kt`의 온보딩 chrome 분기 — `isOnboarding`이면 `MinoTopNavigation`의 좌측 뒤로가기를 비우고 우측에 [건너뛰기]를 노출한다. 그 밖의 진입점은 반대다(FR-017·FR-022·TS-023). T020이 더한 우측 액션 축을 쓴다
- [X] T062 [US5] `feature/roomform/src/main/java/team/mino/feature/roomform/form/screen/RoomFormScreen.kt`의 모달 오버레이에 이탈 2종 문구 연결 — `ExitCreate`·`ExitEdit`가 서로 다른 제목을 쓰고 확인 버튼이 [나가기]다. 생성용 문구가 편집에서 쓰이지 않는다(TS-039)

**체크포인트**: 다섯 스토리가 모두 독립적으로 동작하고, 결과 네 갈래가 전부 호출자에게 도달한다

---

## Phase 8: 마무리 및 공통 관심사

- [X] T063 `./gradlew :app:assembleQaDebug`와 `./gradlew :core:domain:test :feature:roomform:testQaDebugUnitTest` 실행해 전부 통과시킨다([quickstart.md](./quickstart.md) §2). 로컬 `lintDebug` 데몬 사망은 코드 문제가 아니지만 **검증이 수행된 것도 아니다**
- [X] T064 [quickstart.md](./quickstart.md) §3의 S-1~S-8을 임시 진입점으로 직접 눌러 확인한다
- [X] T065 Figma 노드 대조 결과를 `<노드 ID> | 대조: <변수명>=<값> … | <불일치>` 형식으로 제출한다 — 대상은 `2314-95301`·`2314-95339`·`2314-95377`·`2542-125922`·`3798-167701`·`3798-167946`·`3832-213717`. **빌드·테스트 통과를 디자인 일치의 근거로 삼지 않는다**([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md) §6)
- [X] T066 [contracts/room-form-launcher.md](./contracts/room-form-launcher.md) §5의 세 확인을 수행한다 — `feature/roomform/build.gradle.kts`에 다른 `:feature:*`가 없고, `:feature:roomform` 어디에도 다른 feature의 Launcher 주입이 없으며, `setResult` 호출 지점이 한 곳뿐이다
- [X] T067 [P] ADR 승격을 제안한다 — R-002(mock 데이터 레이어 전략) · R-006(디자인 시스템 컴포넌트 판정) · R-022(DS 컴포넌트의 글자 수 단위). 셋 다 다른 feature를 구속한다([plan.md](./plan.md) 헌법 게이트 G4). 각 파일은 `docs/adr/2026-08-25-*.md`로 만든다([작성 규칙](../../adr/README.md))

---

## 커버리지와 미결 사항

### spec 요구사항 대응

| 구분 | 요구사항 | 대응 |
|---|---|---|
| 이번 범위에서 완결 | FR-001·FR-002·FR-003·FR-004·FR-005·FR-006·FR-007·FR-008·FR-009·FR-010·FR-013·FR-018·FR-020·FR-021·FR-022·FR-023·FR-024·FR-025 · UX-001~UX-005·UX-007~UX-009 · SC-001~SC-005·SC-007·SC-008 | T004~T062 |
| 결과 반환까지만 | FR-015(수정·복귀 신호) · FR-017(`skipped` 반환) · FR-019(`roomId` 반환) | T057·T059·T025 |

### 이번 범위에서 닫히지 않는 것

아래는 **작업 누락이 아니라 도착점 feature의 부재**다. 근거는 [plan.md](./plan.md) §범위 경계이고, 각 항목의 검증 자리는 [quickstart.md](./quickstart.md) §5가 소유한다.

| spec 항목 | 남는 이유 | 어디서 닫히는가 |
|---|---|---|
| FR-011 진입점별 도착점 이동 | 온보딩·방 리스트 탭·홈 탭·복제 시트·방 상세가 하나도 없다 | 각 진입점 feature |
| FR-012 `방 생성 완료!` · FR-015 스낵바 | 표출 자리가 도착 화면이다(UX-006) | 도착점 feature |
| FR-014 방장 전용 [편집] 노출 · SC-006 | 방 상세가 없다 | PRD [SCR-005] |
| FR-016 편집 결과의 다른 화면 반영 | 방 목록·지도 마커·방 뱃지가 없다 | PRD [SCR-004] 등 |
| FR-019 복제 시트 두 번째 배치 | 복제 시트가 없다 | PRD [SYS-003] |
| TS-036 진입점이 달라도 폼 구성이 같다 | 임시 검증 진입점이 하나뿐이다 | 각 진입점 feature가 생길 때 |
| UX-006 완료 피드백의 자동 소멸 | 스낵바를 이 feature가 표출하지 않는다 | 도착점 feature |

### 미결 사항

1. **`MinoTopNavigation`이 이미 존재한다.** [plan.md](./plan.md) 1.4.1의 소스 트리는 이 컴포넌트를 `[신규]`로 적었으나, 2026-08-25 `597ea97`(다른 이슈)로 `:core:design-system`에 이미 들어와 develop에 있다. 그래서 T020은 **신설이 아니라 확장**이다. 파라미터 이름·구성이 [contracts/design-system-additions.md](./contracts/design-system-additions.md) §1의 API 초안과 어긋나는 문제는 **현 구현 방식을 따르는 것으로 확정됐다**(2026-08-25 사용자 결정) — 이미 develop에 들어와 다른 화면이 쓰기 시작한 시그니처를 문서 초안에 맞추자고 흔드는 편이 대가가 크다. 그 결정을 T020이 담고 있으므로 착수 시 다시 판단할 것이 없다. 계약 §1의 초안 코드 블록은 이 단계에서 고치지 않았고, 초안과 실제가 갈린 자리는 T020이 단일 출처다.
2. **열린 항목 D(서버 계약)는 그대로다.** swagger 초안이 방 설명 길이·색상 표현·색상 가짓수 세 지점에서 spec과 어긋난다([research.md](./research.md) R-003). 이번 범위는 mock이라 막히지 않고, 서버 확정 시 고칠 곳은 T014의 `RoomMapper` 한 파일이다.
3. **열린 항목 G(`graphemeLength`의 가시성)도 그대로다.** 방 이름을 `length`로 세는 근거는 FR-004의 허용 문자가 전부 코드 유닛 1개라는 것이며([contracts/room-form-ui.md](./contracts/room-form-ui.md) §1), 허용 문자가 넓어지면 `:core:common:kotlin` 승격이 필요해진다. 그 조건은 [plan.md](./plan.md) §복잡도 추적이 추적한다.
4. **`MinoTextArea`의 grapheme 변경은 이 목록에 없다.** plan 1.3.0이 `/mino-task`를 거치지 않고 낸 코드이며 트리에 `[완료]`로 표기됐다([plan.md](./plan.md) §복잡도 추적). 다시 작업으로 만들지 않는다.

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **셋업 (Phase 1)**: 의존성 없음 — 즉시 시작 가능. T001 → T002 → T003 순서다(포함되지 않은 프로젝트를 `:app`이 참조하면 sync가 깨진다)
- **기반 작업 (Phase 2)**: Phase 1 완료에 의존. 내부 의존은 아래 "기반 작업 내부"
- **사용자 스토리 (Phase 3~7)**: 각 작업이 **실제로 읽거나 컴파일 대상으로 삼는** 기반 작업에만 의존한다. Phase 2 전체의 완료를 기다리지 않는다
- **마무리 (Phase 8)**: 목표한 모든 스토리의 완료에 의존. T063은 언제든 중간 확인용으로 돌릴 수 있다

### 기반 작업 내부

```
T004 ─┐
T005 ─┴──────────────────────────► T025 · T028 · T035
T006·T007·T008 ──► T010 ──► T018 ──► T019
T009 ──► T011 ──► T012
T013 ──► T014 ──► T018
T013 ──► T015 ──► T016 ──► T017 · T018
T021 ──► T022
T024 ──► T025 · T026 · T027
T029 · T006 · T009 ──► T030 ──► T033
T031 · T032 ──► T033
T010 · T011 ──► T033
```

- T020·T023·T034는 다른 기반 작업에 의존하지 않는다
- T035는 T005·T004·T028이 끝나야 실제로 폼을 열 수 있다

### 사용자 스토리 간 의존성

- **US1**: T020~T023·T029~T034가 준비되면 시작 가능 — 다른 스토리에 의존하지 않는다. **다른 네 스토리가 US1의 화면 파일을 확장하므로 US1이 가장 먼저다**
- **US2**: US1의 T041(화면)에 의존한다. 판정 로직은 기반의 T011이 이미 갖고 있다
- **US3**: US1의 T041·T043에 의존한다. US2와는 독립이다
- **US4**: US1과 US3의 화면·ViewModel 위에 얹힌다. 모달을 쓰지 않으므로 T050에는 의존하지 않는다
- **US5**: US3의 T050(모달 컴포넌트)과 US1의 화면에 의존한다. 편집 이탈 문구는 US4가 없어도 검증할 수 있다(`initial`을 테스트가 주입한다)

### 각 사용자 스토리 내부

- 테스트를 먼저 작성하고 실패(red)를 확인한 뒤 구현한다
- 모델·컴포넌트 → 화면 → Route → ViewModel 순서다. 화면이 없으면 Route가 붙을 자리가 없다
- **`RoomFormViewModelTest`는 US1이 만들고 US2~US5가 같은 파일에 케이스를 더한다** — 그래서 스토리 간 테스트 작업에는 [P]가 붙지 않는다

### 병렬 처리 기회

- Phase 2에서 [P]가 붙은 T004·T005·T006·T007·T008·T009·T013·T015·T020·T021·T023·T029·T031·T032·T034는 동시에 진행할 수 있다
- 세 모듈(`:core:domain` · `:core:data` · `:core:design-system`)의 기반 작업은 서로를 기다리지 않는다
- 스토리 안에서 [P]가 붙은 컴포넌트 작업(T038·T039 / T044)은 동시에 만들 수 있다
- T047·T049는 `:core:domain`이라 feature 작업과 병렬이다

---

## 병렬 실행 예시: 기반 작업 착수

```bash
# 세 모듈의 기반을 동시에 연다
Task: "core/navigation/.../ExtraTag.kt에 진입·결과 상수 추가"          # T004
Task: "core/domain/.../model/RoomColor.kt 생성"                        # T006
Task: "core/data/.../network/dto/{request,response}/Room*.kt 생성"     # T013
Task: "MinoTopNavigation에 우측 텍스트 액션 축 추가"                    # T020
Task: "방 썸네일 13종 WebP export"                                     # T023
```

## 병렬 실행 예시: 사용자 스토리 1

```bash
# 테스트를 먼저 세운다
Task: "FakeRoomRepository 작성"                                        # T036
Task: "RoomFormViewModelTest에 TS-001·002·003·037·044 작성"            # T037

# 화면 부품을 동시에 만든다
Task: "RoomColorUiModel — RoomColor ↔ MinoRoomColor ↔ drawable 매핑"   # T038
Task: "RoomPreviewCard 작성"                                           # T039
```

---

## 구현 전략

### MVP 우선 (US1 + US3)

1. Phase 1 셋업 완료
2. Phase 2에서 **US1·US3이 쓰는 것만** 먼저 — 진입 계약 · 도메인 · mock 데이터 레이어 · 디자인 시스템 · feature 골격 · 임시 진입점
3. Phase 3(US1) → Phase 5(US3)
4. **중단하고 검증**: [quickstart.md](./quickstart.md) S-1과 S-8을 눌러 본다. 방이 만들어지고 결과가 돌아오면 폼의 존재 이유가 성립한다
5. 여기까지가 데모 가능한 최소 단위다

### 점진적 전달

1. 셋업 → 기반은 끝나는 것부터 스토리에 공급
2. US1 추가 → S-1 1~6단계·S-7 → 데모
3. US3 추가 → S-1 전체·S-8 → **MVP**
4. US2 추가 → S-2
5. US4 추가 → S-3
6. US5 추가 → S-4·S-5·S-6 → 다섯 스토리 완결
7. Phase 8로 빌드·테스트·디자인 대조·경계 확인을 닫는다

### 팀 병렬 전략

개발자가 여러 명인 경우, 스토리가 아니라 **모듈로 가른다** — 다섯 스토리가 같은 세 파일(`RoomFormScreen`·`RoomFormRoute`·`RoomFormViewModel`)을 차례로 확장하므로 스토리별 분담은 충돌한다.

1. 개발자 A: `:core:domain` + `:core:data` (T006~T019 · T047 · T049)
2. 개발자 B: `:core:design-system` + 에셋 (T020~T023)
3. 개발자 C: `:core:navigation` + feature 골격 + 임시 진입점 (T004·T005·T024~T035)
4. 셋이 만나 Phase 3부터는 **한 사람이 US1 → US3 → US2 → US4 → US5 순으로** 화면 세 파일을 확장한다

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 동일 파일 충돌, 독립성을 깨뜨리는 스토리 간 의존성
- 커밋 단위는 [`commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다
- 규약·설계 내용을 이 문서에 다시 풀어쓰지 않는다. 각 작업 줄은 소유 문서의 섹션을 지목만 한다
