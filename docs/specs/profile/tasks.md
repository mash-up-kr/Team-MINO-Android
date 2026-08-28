# 작업 목록: 프로필 설정 및 수정 (Profile Setup & Edit)

**대상 스펙 경로**: `docs/specs/profile`

**기준 plan 버전**: 3.0.0

**최초 작성일**: 2026-08-24

**최종 수정일**: 2026-08-25

**사전 조건**: [plan.md](plan.md) · [spec.md](spec.md) · [research.md](research.md) · [data-model.md](data-model.md) · [contracts/](contracts/) · [quickstart.md](quickstart.md)

**테스트**: 포함한다. [repository 계약 §테스트 계약](contracts/profile-repository-contract.md)이 덮을 대상 5종을 지정했고, [research.md D12](research.md)가 범위를 JVM 단위 테스트로 한정했다. Compose UI 테스트는 만들지 않는다.

**구성 방식**: 화면 하나를 세 스토리가 공유하므로(FR-001) 화면이 서기 위한 배관 전체가 Phase 2에 들어가고, 각 스토리는 그 화면에 자기 동작을 얹는다. 스토리별 독립 검증 기준은 각 Phase 머리에 있다.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. **한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.**
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리 (US1 · US2 · US3)
- 설명에는 정확한 파일 경로를 포함한다. UI를 만드는 작업에는 대조할 Figma 노드 ID를 함께 적는다.

## 경로 규칙

모바일(Android) 다중 모듈. 경로는 저장소 루트 기준이며 [plan.md §프로젝트 구조](plan.md)를 그대로 따른다. `:core:domain`은 `src/main/kotlin/`, 나머지 모듈은 `src/main/java/`다.

---

## Phase 1: 셋업 (모듈 골격)

**목적**: `:feature:profile`을 빌드에 등록하고, 이후 단계의 테스트가 실행될 수 있게 만든다.

- [X] T001 `settings.gradle.kts`에 `include(":feature:profile")` 추가
- [X] T002 `feature/profile/build.gradle.kts` 신설 — `alias(libs.plugins.mino.android.feature)` + `namespace = "team.mino.feature.profile"` + `testImplementation(libs.kotlinx.coroutines.test)`(T038의 실행 전제). 버전 카탈로그에 새 항목을 만들지 않는다([plan.md §기술 컨텍스트](plan.md))
- [X] T003 `app/build.gradle.kts`의 진입형 feature 목록에 `implementation(project(":feature:profile"))` 추가
- [X] T004 `core/domain/build.gradle.kts`에 `testImplementation(libs.junit)` 추가 — T007·T010의 실행 전제. **plan이 열거한 변경 대상 세 곳 밖이다**(아래 [미결 사항](#미결-사항) 1)

**체크포인트**: `./gradlew :app:assembleQaDebug`가 통과한다. 여기서부터 다른 작업을 시작할 수 있다.

---

## Phase 2: 기반 작업 (세 스토리가 공유하는 인프라)

**목적**: 두 진입점이 공유하는 화면 한 벌(FR-001)이 실제로 서기까지의 배관. 스토리별 동작은 얹지 않는다.

**⚠️ 실행 순서는 Phase가 아니라 아래 소그룹 간 의존이 정한다.** 2-A·2-B·2-C·2-D는 서로를 기다리지 않고, 2-E만 이들의 산출물을 읽는다.

### 2-A. 도메인 계약 (`:core:domain`) — US1·US2·US3 전부가 쓴다

- [X] T005 [P] `Profile` 모델 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/model/Profile.kt` ([data-model.md §1](data-model.md))
- [X] T006 [P] `ProfileRepository` 인터페이스 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/repository/ProfileRepository.kt` ([repository 계약](contracts/profile-repository-contract.md))
- [X] T007 [P] `ValidateNicknameUseCaseTest` 작성 후 실패 확인 — `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ValidateNicknameUseCaseTest.kt`. 케이스는 repository 계약 §테스트 계약이 지정한 `민`·`abc1`·`  민호  `·공백만·한글 30자·낱자 `ㄱㄱ`
- [X] T008 `ValidateNicknameUseCase` 구현 — `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ValidateNicknameUseCase.kt` (T007을 통과시킨다)
- [X] T009 `FakeProfileRepository`(도메인 테스트용) 작성 — `core/domain/src/test/kotlin/team/mino/core/domain/repository/FakeProfileRepository.kt`. 저장 값 보관과 예외 주입을 지원한다(T010이 쓴다)
- [X] T010 `SaveProfileUseCaseTest` 작성 후 실패 확인 — `core/domain/src/test/kotlin/team/mino/core/domain/usecase/SaveProfileUseCaseTest.kt`. trim된 값 저장 · 무효 입력 차단 · Repository 예외 전파 3건
- [X] T011 `SaveProfileUseCase` 구현 — `core/domain/src/main/kotlin/team/mino/core/domain/usecase/SaveProfileUseCase.kt` (T010을 통과시킨다)

### 2-B. 로컬 저장 (`:core:data`) — US1의 저장과 US2의 프리필이 쓴다

- [X] T012 `ProfileLocalDataSourceImplTest` 작성 후 실패 확인 — `core/data/src/test/java/team/mino/core/data/datasource/ProfileLocalDataSourceImplTest.kt`. 저장→`observeProfile()` 왕복 · 키가 하나만 있을 때 `null` · 두 키가 한 번에 쓰이는지
- [X] T013 `ProfileLocalDataSource` 인터페이스와 `ProfileLocalDataSourceImpl` 구현 — `core/data/src/main/java/team/mino/core/data/datasource/ProfileLocalDataSource.kt`. 기존 공유 `DataStore<Preferences>`(`core/data/src/main/java/team/mino/core/data/storage/DataStoreModule.kt`)를 주입받고 새 인스턴스를 만들지 않는다. 키와 미저장 판정은 [data-model.md §3](data-model.md)
- [X] T014 `ProfileDataSourceModule` 작성 — `core/data/src/main/java/team/mino/core/data/datasource/di/ProfileDataSourceModule.kt` ([DI 규칙](../../conventions/dependency-injection.md))
- [X] T015 [P] `ProfileRepositoryImplTest` 작성 후 실패 확인 — `core/data/src/test/java/team/mino/core/data/repository/ProfileRepositoryImplTest.kt`. DataSource를 Fake로 두고 위임이 그대로인지
- [X] T016 `ProfileRepositoryImpl` 구현 — `core/data/src/main/java/team/mino/core/data/repository/ProfileRepositoryImpl.kt`. 매퍼를 만들지 않는다([repository 계약 §저장 계층](contracts/profile-repository-contract.md))
- [X] T017 `ProfileRepositoryModule` 작성 — `core/data/src/main/java/team/mino/core/data/repository/di/ProfileRepositoryModule.kt`

### 2-C. 디자인 시스템 (`:core:design-system`) — US1의 썸네일·그리드와 화면 상단 바가 쓴다

- [X] T018 아바타 12종 에셋을 `core/design-system/src/main/res/drawable-{mdpi,xhdpi,xxhdpi}/`에 밀도별 WebP로 배치 — 배경 원과 캐릭터가 래스터로 합성돼 있어 벡터 대상이 아니다([ADR](../../adr/2026-08-01-webp-for-raster-images.md)). 배치 근거는 [아바타 소유 ADR](../../adr/2026-08-25-profile-avatar-assets-in-design-system.md). Figma 노드 `2314-95672`
- [X] T019 `MinoProfileAvatar` enum 12항목과 `token/ProfileAvatarTokens.kt` 작성 — `core/design-system/src/main/java/team/mino/core/designsystem/component/profileavatar/`. enum은 그림과 크기만 안다([data-model.md §4](data-model.md)). Figma 노드 `2314-95672`
- [X] T020 `MinoProfileAvatarImage`와 `MinoProfileAvatarDefaults` 구현 — `core/design-system/src/main/java/team/mino/core/designsystem/component/profileavatar/`. 공개 API는 [design-system 계약 §1](contracts/design-system-contract.md). **선택 상태의 시각 표시는 그리지 않는다**([research.md D28](research.md#d28-아바타-선택-상태의-시각-표시를-만들지-않는다)) — `selected`는 접근성 시맨틱만 싣는다. 테두리는 에셋에 없어 컴포넌트가 그린다. Figma 노드 `2314-95672`
- [X] T021 `ProfileAvatarPreview.kt` 작성 — `core/design-system/src/main/java/team/mino/core/designsystem/component/profileavatar/ProfileAvatarPreview.kt` ([design-system README §6.4](../../../core/design-system/README.md))
- [X] T022 [P] `MinoTopNavigation`·`MinoTopNavigationDefaults`·`token/TopNavigationTokens.kt` 구현 — `core/design-system/src/main/java/team/mino/core/designsystem/component/topnavigation/`. **`Platform=iOS` variant를 구현한다**([D27](research.md#d27-상단-바는-화면-목업이-쓰는-ios-variant를-따른다)) — 바 44dp·가운데 제목·`Headline2Bold`·`MinoIcons.ChevronLeft`. 상태 표시줄 인셋을 갖지 않는다. `backEnabled` 파라미터를 두지 않는다([D34](research.md#d34-minotopnavigation에-backenabled-파라미터를-두지-않는다)). 대조 노드는 화면 인스턴스 `2314-95704`(= DS `16215-20433`)
- [X] T023 `TopNavigationPreview.kt` 작성 — `core/design-system/src/main/java/team/mino/core/designsystem/component/topnavigation/TopNavigationPreview.kt`
- [X] T056 `MinoIcons.ChevronLeft` 추가 — `core/design-system/src/main/java/team/mino/core/designsystem/foundation/icons/icons/ChevronLeft.kt` + `MinoIconsPreview.kt` 카탈로그 등록. 절차는 [design-system README §5.2](../../../core/design-system/README.md). T057이 쓴다. Figma 노드 `2314-95704`의 Back 서브트리
- [X] T057 `MinoTopNavigation`을 `Platform=iOS` variant로 전환 — `component/topnavigation/` 4파일. 바 56→44dp, 제목 좌측→가운데, `Heading2Bold`→`Headline2Bold`, `ArrowLeft`→`ChevronLeft`, `ContentSpacing` 토큰 제거, `backEnabled`·`DisabledBackIconColor` 제거([D27](research.md#d27-상단-바는-화면-목업이-쓰는-ios-variant를-따른다)·[D34](research.md#d34-minotopnavigation에-backenabled-파라미터를-두지-않는다)). T056에 의존

### 2-D. 전환 계약 (`:core:navigation`) — US1의 결과 반환과 진입점 해석이 쓴다

- [X] T024 [P] `ProfileLauncher` 인터페이스와 **진입점 값 상수 2개**(`PROFILE_ENTRY_POINT_ONBOARDING`·`PROFILE_ENTRY_POINT_EDIT`) 생성 — `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/ProfileLauncher.kt`. 값이 계약 파일에 오는 이유는 [D33](research.md#d33-진입점-값-상수는-extratagkt가-아니라-전환-계약-파일이-갖는다)
- [X] T025 [P] `ExtraTag.kt`에 **키 `EXTRA_PROFILE_ENTRY_POINT`만** 추가 — `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/ExtraTag.kt`. 이 파일은 키 전용이라 값 상수는 T024가 갖는다

### 2-E. feature 골격 (`:feature:profile`) — 세 스토리가 공유하는 화면 자체

- [X] T026 [P] `ProfileEntryPoint` 작성 — `feature/profile/src/main/java/team/mino/feature/profile/main/model/ProfileEntryPoint.kt`. Intent extra 문자열 ↔ enum 해석을 갖고, 알 수 없는 값은 `MyPage`로 읽는다([data-model.md §5](data-model.md))
- [X] T027 [P] enum ↔ `avatarId` 매핑 작성 — `feature/profile/src/main/java/team/mino/feature/profile/main/model/ProfileAvatarId.kt`. 선언 순서를 1부터 매기는 임시 매핑이라는 사실을 이 파일에 주석으로 남긴다([research.md D18](research.md))
- [X] T028 [P] `ProfileDestinations.kt` 작성 — `feature/profile/src/main/java/team/mino/feature/profile/ProfileDestinations.kt`. `internal data class ProfileMain(val entryPoint: String) : Route`, `typeMap` 없음([screen 계약 §Route](contracts/profile-screen-contract.md))
- [X] T029 `ProfileUiState`·`ProfileIntent`·`ProfileSideEffect` 선언 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/`. 필드는 [data-model.md §5](data-model.md), 인텐트·사이드이펙트 축은 [screen 계약](contracts/profile-screen-contract.md)
- [X] T030 `ProfileViewModel` 골격 작성 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/ProfileViewModel.kt`. MVI 컨테이너 배선과 `savedStateHandle.toRoute<ProfileMain>()`로 진입점 복원까지만. 인텐트 처리는 US1~US3이 얹는다
- [X] T031 `ProfileAvatarGrid` 작성 — `feature/profile/src/main/java/team/mino/feature/profile/main/component/ProfileAvatarGrid.kt`. 4열 × 3행 고정 `Column`+`Row`, `LazyVerticalGrid` 금지([research.md D26](research.md)). Figma 노드 `2314-95672`
- [X] T032 `ProfileScreen` 작성 — `feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileScreen.kt`. stateless이며 [screen 계약 §화면 구성](contracts/profile-screen-contract.md)의 6자리를 배치한다. **상단 바와 액션 영역은 고정이고 본문만 스크롤한다**([D32](research.md#d32-화면은-상단-바와-액션-영역을-고정하고-본문만-스크롤한다)). 자체 `Scaffold`를 열지 않는다. Figma 노드 `2314-95662`
- [X] T033 `ProfileRoute` 작성 — `feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileRoute.kt`. 상태 수집과 인텐트 전달까지만. 실패 통로는 T042가 얹는다
- [X] T034 `ProfileShell.kt`와 `ProfileNavHost.kt` 작성 — `feature/profile/src/main/java/team/mino/feature/profile/`. `MinoScaffold` + `rememberNavController` + `TrackScreenViews`, `screen<ProfileMain>` 등록([research.md D11](research.md), UX-006)
- [X] T035 `ProfileActivity.kt`와 `feature/profile/src/main/AndroidManifest.xml` 작성 — extra를 읽어 시작 라우트에 싣고 셸을 호스팅한다. Activity는 `exported="false"`. 결과 반환은 T043이 얹는다
- [X] T036 `ProfileLauncherImpl`과 `ProfileNavigationModule` 작성 — `feature/profile/src/main/java/team/mino/feature/profile/di/` ([launcher 계약 §구현](contracts/profile-launcher-contract.md))

**체크포인트**: `./gradlew :core:domain:test :core:data:test :app:assembleQaDebug`가 통과하고, `adb`로 화면이 뜬다([quickstart.md §3](quickstart.md)). 아직 어떤 조작도 동작하지 않는다.

---

## Phase 3: 사용자 스토리 1 - 온보딩에서 프로필을 처음 만든다 (우선순위 P1 · MVP)

**목표**: 프로필이 없는 사용자가 닉네임과 아바타를 정해 저장하고, 호출자가 다음 온보딩 스텝으로 넘어갈 근거(`RESULT_OK`)를 받는다.

**독립 테스트**: `adb ... --es profile_entry_point onboarding`으로 열어 [quickstart.md §3](quickstart.md)의 확인 항목 1·2·6·7·10을 통과한다. 저장 실패·중복 저장은 눈으로 볼 수 없으므로 T038의 단위 테스트가 그 자리를 대신한다([research.md D25](research.md)).

### 사용자 스토리 1 테스트

- [X] T037 [US1] `FakeProfileRepository`(feature 테스트용) 작성 — `feature/profile/src/test/java/team/mino/feature/profile/fake/FakeProfileRepository.kt`. `observeProfile()`에 값을 미리 채우고 `saveProfile()`이 예외를 던지게 할 수 있어야 한다
- [X] T038 [US1] `ProfileViewModelTest` 작성 후 실패 확인 — `feature/profile/src/test/java/team/mino/feature/profile/main/vm/ProfileViewModelTest.kt`. 닉네임 입력 → 저장 활성 · 아바타 단일 선택 · 저장 성공 시 `SaveCompleted` · 저장 중 두 번째 `SaveClicked` 무시(UX-003, EC-004) · 저장 실패 시 입력값 보존(FR-012, SC-006)

### 사용자 스토리 1 구현

- [X] T039 [US1] `ProfileViewModel`에 `NicknameChanged`·`AvatarSelected` 처리 구현 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/ProfileViewModel.kt`. 판정은 `ValidateNicknameUseCase`가 하고(FR-002, UX-002) 아바타는 항상 단일 선택으로 교체한다(FR-003, TS-004)
- [X] T040 [US1] `ProfileViewModel`에 `SaveClicked` 처리 구현 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/ProfileViewModel.kt`. `isSaving` 가드 → `launchSafely` + `runCatchingDomain`으로 `SaveProfileUseCase(nickname, selectedAvatar ?: 기본 아바타의 id)` 호출 → 성공 시 `SaveCompleted`, 실패 시 `emitDomainError`, 어느 쪽이든 `isSaving=false`([에러 처리 규약](../../conventions/error_handling.md) §4, EC-002)
- [X] T041 [US1] `ProfileScreen`에 썸네일·그리드 선택 반영과 `저장` 활성 조건 배선 — `feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileScreen.kt`. 파생 값 계산은 [data-model.md §5](data-model.md), 비활성 버튼은 인텐트를 만들지 않는다(UX-004, EC-011). Figma 노드 `2314-95709`
- [X] T042 [US1] [US1] `ProfileRoute`에 실패 통로 배선 — `feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileRoute.kt`. `CollectDomainError`로 받아 `LocalSnackbarHostState`에 띄우고, 리프 → 문구 매핑을 이 파일의 `messageResOf`에 둔다. **문구는 `feature/profile/src/main/res/values/strings.xml`의 문자열 리소스다**([feature-module.md](../../architecture/feature-module.md) 4장 스켈레톤, 에러 처리 규약 §8)
- [X] T043 [US1] `ProfileActivity`에 결과 반환 배선 — `feature/profile/src/main/java/team/mino/feature/profile/ProfileActivity.kt`. `SaveCompleted`를 받아 `setResult(RESULT_OK)` 후 `finish()`([launcher 계약 §결과](contracts/profile-launcher-contract.md), FR-008의 이동 부분)
- [X] T044 [US1] [US1] 온보딩 진입의 뒤로가기 차단 배선 — `ProfileScreen.kt`(**`onBackClick = null`로 버튼을 그리지 않는다** — 비활성이 아니라 숨김, [D29](research.md#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다))와 `ProfileRoute.kt`(`BackHandler(enabled = !state.isBackEnabled) {}`). 판정 기준은 `entryPoint == Onboarding`(FR-010, EC-001)

**체크포인트**: 온보딩 진입으로 프로필을 만들고 화면이 `RESULT_OK`로 닫힌다. 이것이 MVP다.

---

## Phase 4: 사용자 스토리 2 - 마이페이지에서 프로필을 수정한다 (우선순위 P2)

**목표**: 이미 프로필이 있는 사용자가 같은 화면에서 값을 고쳐 저장하거나, 저장하지 않고 돌아간다.

**독립 테스트**: `adb ... --es profile_entry_point edit`으로 열어 [quickstart.md §3](quickstart.md)의 확인 항목 11·12·13·14·16·17을 통과한다. 프리필은 T045의 단위 테스트로도 독립 검증된다(Fake Repository에 값을 미리 채운다).

### 사용자 스토리 2 테스트

- [X] T045 [US2] `ProfileViewModelTest`에 프리필 케이스 추가 — `feature/profile/src/test/java/team/mino/feature/profile/main/vm/ProfileViewModelTest.kt`. `observeProfile()`의 첫 값이 `nickname`·`selectedAvatar`를 채우고 `isNicknameValid=true`·`isNicknameTouched=false`가 되는지(FR-006, TS-008)

### 사용자 스토리 2 구현

- [X] T046 [US2] `ProfileViewModel`에 `observeProfile()` 구독 프리필 구현 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/ProfileViewModel.kt`. 화면이 따로 조회를 걸지 않는다([screen 계약 §Intent](contracts/profile-screen-contract.md)). 목록 밖 `avatarId`는 기본 아바타로 대체한다([data-model.md §4](data-model.md))
- [X] T047 [US2] 마이페이지 진입의 뒤로가기 활성 배선 — `feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileScreen.kt`(상단 바 `onBackClick` 노출)·`feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileRoute.kt`·`feature/profile/src/main/java/team/mino/feature/profile/ProfileActivity.kt`. 확인 모달 없이 `finish()`하고 수정 내용을 버린다(FR-010, FR-013, EC-005)

**체크포인트**: 두 진입점이 같은 화면에서 서로 다른 진입·이탈 동작을 보인다.

---

## Phase 5: 사용자 스토리 3 - 닉네임 입력 오류를 고치고 입력을 초기화한다 (우선순위 P3)

**목표**: 규칙에 맞지 않는 닉네임을 화면이 알려 주고, `지우기`로 입력을 한 번에 되돌린다.

**독립 테스트**: 진입점과 무관하게 [quickstart.md §3](quickstart.md)의 확인 항목 3·4·5·8·9를 통과한다.

### 사용자 스토리 3 테스트

- [X] T048 [US3] `ProfileViewModelTest`에 오류 표시·지우기 케이스 추가 — `feature/profile/src/test/java/team/mino/feature/profile/main/vm/ProfileViewModelTest.kt`. 진입 직후 오류 미표시(TS-001) · 무효 입력 후 오류(TS-012·TS-013) · 해소(TS-014) · `지우기` 후 초기 상태 복귀(TS-015)

### 사용자 스토리 3 구현

- [X] T049 [US3] `ProfileViewModel`에 `ClearClicked` 처리와 `isNicknameTouched` 전이 구현 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/ProfileViewModel.kt` (FR-005, TS-015)
- [X] T050 [US3] `ProfileScreen`에 닉네임 오류 표시 배선 — `feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileScreen.kt`. `MinoTextField`의 `status`·`helperText`로만 표시하고 팝업·토스트를 쓰지 않는다(FR-011, UX-001). Figma 노드 `2314-95754`
- [X] T051 [US3] `ProfileScreen`에 `지우기` 활성 조건 배선 — `feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileScreen.kt`. 닉네임 유효 **그리고** 아바타 선택일 때만 활성(FR-005, EC-012, TS-016)

**체크포인트**: 세 스토리가 모두 동작한다.

---

## Phase 6: 마무리 및 공통 관심사

- [X] T052 [P] `ProfileScreenPreview.kt` 작성 — `feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileScreenPreview.kt`. [quickstart.md §2](quickstart.md)의 4가지 상태와 진입점별 뒤로가기 2장을 `@UiModePreviews`로 둔다
- [X] T053 Figma 대조 수행 — 010-1 `2314-95662` · 010-2 `2314-95709` · 010-3 `2314-95754`. 절차와 토큰/실측 판정 기준은 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md), 대상은 신설한 `MinoProfileAvatarImage`·`MinoTopNavigation`과 화면 배치다
- [X] T054 [quickstart.md §3](quickstart.md)의 기기 확인 18항목 수행 — 확인할 수 없는 항목은 같은 문서 §4가 이미 열거했다. 통과하지 못한 항목은 사유와 함께 남긴다
- [X] T055 품질 게이트 실행 — `./gradlew :core:domain:test :core:data:test :feature:profile:testDebugUnitTest :app:assembleQaDebug` ([헌법](../../constitution.md) §품질 게이트). 로컬 `lintDebug`는 데몬이 죽을 수 있고, 죽었다고 검증이 수행된 것으로 보지 않는다
- [X] T058 `:feature:profile`을 모듈 목록 SSOT에 등록 — `docs/architecture/modularization.md` §모듈 구성. [헌법](../../constitution.md) §기술 표준이 이 문서를 모듈 목록의 단일 출처로 지정한다. **신규 모듈을 만드는 작업에는 항상 이 등록이 딸린다**
- [X] T059 아바타 12종 소유 결정을 ADR로 승격 — `docs/adr/2026-08-25-profile-avatar-assets-in-design-system.md` + [ADR README](../../adr/README.md) 인덱스. 헌법 Governance의 "기록 없는 예외는 없다"를 충족한다([research.md D35](research.md#d35-아바타-12종의-소유-결정을-adr로-승격했다))

---

## 미결 사항

plan 3.0.0 기준으로 갱신했다. **구현이 끝나며 닫힌 것은 지웠고, 남은 것과 새로 드러난 것만 적는다.**

1. **MUST 위반 1건이 남는다 — 규약과 계약이 정면 충돌한다.** [`core:data` README](../../../core/data/README.md) §5·§2가 "DataSource는 DTO만 반환하고 변환하지 않는다"로 정하는데, [repository 계약](contracts/profile-repository-contract.md) §저장 계층이 `ProfileLocalDataSourceImpl`이 `Preferences`에서 `Profile`을 직접 조립하도록 명시 지시했다. Preferences에는 자연적 DTO가 없고 키 상수가 DataSource에 묶여 있어 **어느 쪽으로도 규약을 다 지킬 수 없다.** 해소는 (a) README에 "DTO 없는 로컬 DataSource" 갈래 보완 (b) 계약을 바꿔 `ProfileEntry` 도입 중 하나이며 **사용자 판단이 필요하다.** plan 3.0.0의 Constitution Check 원칙 V가 이 때문에 FAIL이다.
2. **spec FR-003의 칸 단위 선택 표시가 충족되지 않는다.** 원본에 선택된 칸을 구별하는 표현이 없어 만들지 않았다(사용자 확정, [D28](research.md#d28-아바타-선택-상태의-시각-표시를-만들지-않는다)). 디자인이 생기면 `MinoProfileAvatarImage` 한 곳만 고치면 된다.
3. **spec FR-010의 "노출하되 비활성"과 구현이 다르다.** 온보딩에서 뒤로가기를 숨긴다(사용자 확정, [D29](research.md#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다)). 화면을 벗어나지 못하게 한다는 목적은 그대로다 — spec 문구 정리가 필요하다.
4. **spec FR-008의 개인방 생성과 §4의 "저장은 서버 반영을 포함한다"는 이번 범위가 충족하지 않는다.** 의도된 이연이며 근거는 [D22·D17](research.md)이다.
5. **저장 실패 경로(FR-012 등)에 발화 원천이 없다.** 통로는 배선했지만 로컬 저장에는 도메인 예외로 변환되는 지점이 없다([D30](research.md#d30-로컬-저장-실패용-도메인-예외-리프를-추가하지-않는다)). 원격 연동에서 매핑 지점이 생길 때 닫힌다.
6. **닉네임 규칙이 서버 스키마와 어긋난다**(spec: 공백 불가·상한 없음 / 서버: 공백 허용·15자). 이번 범위에는 서버 거절 경로가 없어 드러나지 않는다([D19](research.md)). 원격 연동 전에 `/mino-spec` 개정이 필요하다.
7. **`AndroidFeatureConventionPlugin`에 `testOptions` 승격 제안.** `toRoute`를 쓰는 모든 feature의 ViewModel 테스트가 같은 벽을 만난다([D31](research.md#d31-viewmodel-단위-테스트는-isreturndefaultvalues로-열고-진입점은-통제하지-않는다)). 이번 범위 밖이라 작업으로 만들지 않았다.

### 이번 개정에서 닫힌 미결 사항

- ~~`core/domain/build.gradle.kts` 변경이 plan 열거 밖~~ — plan 3.0.0이 "기존 파일 변경 여섯 곳"으로 정정했다
- ~~`MinoTopNavigation`의 Figma 컴포넌트 정의 노드 ID 미확인~~ — 사용자가 링크를 제공했고, 이후 대조 대상이 화면 인스턴스 `2314-95704`로 확정됐다
- ~~앱 전체 반영(TS-011·SC-003) 확인 불가~~ — 성격이 바뀌지 않아 [quickstart.md §4](quickstart.md)가 계속 든다. 작업 목록의 미결로는 닫는다

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **Phase 1 (셋업)**: 의존성 없음 — 즉시 시작. T002는 T001 이후.
- **Phase 2 (기반)**: T002 완료에 의존한다(`:feature:profile` 작업만). 2-A·2-B·2-C·2-D는 서로 독립이고 동시에 진행할 수 있다.
  - 2-B는 2-A의 T005·T006을 컴파일 대상으로 삼는다.
  - 2-E는 2-A(T005~T008·T011)·2-C(T019·T020·T022)·2-D(T024·T025)의 산출물을 읽는다. **2-B는 읽지 않는다** — `:feature:profile`은 `:core:domain`만 의존한다(헌법 원칙 II).
- **Phase 3~5 (스토리)**: 2-E 완료에 의존한다.
- **Phase 6 (마무리)**: 목표한 스토리의 완료에 의존한다. T055는 전부 이후.
- **T056 → T057**: 아이콘이 있어야 상단 바가 컴파일된다. 둘 다 `:core:design-system`이고 T057이 T022의 산출물을 고친다 — **같은 파일이라 한 전문가에게 순차 배정한다.**
- **T058·T059는 문서 작업이라 코드에 의존하지 않는다.** 다만 T058은 T001(모듈 등록)이, T059는 T018~T020(에셋·컴포넌트)이 무엇을 만들었는지 확정된 뒤에 쓸 수 있다.

### 사용자 스토리 간 의존성

화면이 하나뿐이라 스토리는 완전히 독립적이지 않다. 아래가 실제 관계다.

- **US1**: 2-E가 끝나면 시작 가능. 다른 스토리에 의존하지 않는다. **US1만으로 배포 가능한 증분이 나온다.**
- **US2**: US1의 저장 경로(T040)를 전제로 기기에서 검증된다 — 프리필할 값이 있어야 하기 때문이다. 단위 테스트(T045)는 Fake에 값을 채우므로 US1 없이도 독립 검증된다.
- **US3**: US1의 `NicknameChanged` 처리(T039)를 전제로 오류 표시를 얹는다. US2와는 서로 독립이다.

### 각 사용자 스토리 내부

- 테스트를 먼저 쓰고 실패를 확인한 뒤 구현한다.
- 모델 → UseCase → Repository → ViewModel → 화면 순서.
- 같은 파일을 건드리는 작업은 개수와 무관하게 한 사람이 순차로 처리한다.

### 병렬 처리 기회

- Phase 2의 네 소그룹(2-A · 2-B · 2-C · 2-D)은 서로 다른 모듈이라 통째로 병렬이다.
- `[P]`가 붙은 작업은 같은 소그룹 안에서도 병렬이다.
- US2(T045~T047)와 US3(T048~T051)은 US1이 끝난 뒤 병렬로 진행할 수 있다 — 단 `ProfileViewModel.kt`·`ProfileScreen.kt`를 양쪽이 함께 건드리므로 **같은 사람이 순차로 처리해야 한다.** 파일이 겹치지 않는 것은 T045·T048(테스트 파일도 같다)까지 포함해 사실상 없다.

---

## 병렬 실행 예시: Phase 2

```bash
# 서로 다른 모듈이라 네 갈래를 동시에 연다:
Task: "core/domain/src/main/kotlin/.../model/Profile.kt 에 Profile 모델 생성"          # T005
Task: "core/data/src/test/java/.../ProfileLocalDataSourceImplTest.kt 작성"             # T012 (T005·T006 이후)
Task: "core/design-system/.../topnavigation/ 에 MinoTopNavigation 구현"                # T022
Task: "core/navigation/.../launcher/ProfileLauncher.kt 생성"                           # T024

# 도메인 계약 두 개를 함께 만든다:
Task: "core/domain/.../model/Profile.kt 에 Profile 모델 생성"                          # T005
Task: "core/domain/.../repository/ProfileRepository.kt 에 인터페이스 생성"             # T006
```

---

## 구현 전략

### MVP 우선 (US1만)

1. Phase 1 셋업 완료 → `:app:assembleQaDebug` 통과
2. Phase 2 기반 완료 → `adb`로 화면이 뜬다(조작은 아직 없음)
3. Phase 3 US1 완료 → 온보딩 진입으로 저장하고 `RESULT_OK`로 닫힌다
4. **중단하고 검증**: quickstart §3 항목 1·2·6·7·10
5. 여기까지가 배포 가능한 최소 증분이다

### 점진적 전달

1. 셋업 → 기반은 끝나는 소그룹부터 아래로 공급
2. US1 추가 → 독립 검증 (MVP)
3. US2 추가 → 독립 검증
4. US3 추가 → 독립 검증
5. Phase 6에서 프리뷰·Figma 대조·기기 검증·품질 게이트

### 팀 병렬 전략

1. Phase 1은 한 사람이 끝낸다(빌드가 서기 전에는 나눌 것이 없다)
2. Phase 2를 네 갈래로 나눈다 — 도메인 / 데이터 / 디자인 시스템 / 네비게이션+feature 골격
3. Phase 3~5는 `ProfileViewModel.kt`·`ProfileScreen.kt`를 공유하므로 **나누지 않고 한 사람이 US1 → US2 → US3 순으로 잇는다.** 나누면 서로의 파일을 덮어쓴다

---

## 참고 사항

- 규약·설계 내용을 이 문서에 옮겨 적지 않았다. 각 작업 줄의 링크와 섹션 번호가 원문을 가리킨다(헌법 원칙 I).
- 커밋 단위는 [`commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다. 모듈 경계가 커밋 경계와 대체로 일치한다.
