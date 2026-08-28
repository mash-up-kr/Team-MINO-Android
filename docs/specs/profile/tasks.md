# 작업 목록: 프로필 설정 및 수정 (Profile Setup & Edit)

**대상 스펙 경로**: `docs/specs/profile`

**기준 plan 버전**: 5.0.0

**최초 작성일**: 2026-08-24

**최종 수정일**: 2026-08-28

**사전 조건**: [plan.md](plan.md) · [spec.md](spec.md) · [research.md](research.md) · [data-model.md](data-model.md) · [contracts/](contracts/) · [quickstart.md](quickstart.md)

**테스트**: 포함한다. [repository 계약 §테스트 계약](contracts/profile-repository-contract.md)이 덮을 대상 7종을 지정했고, [research.md D12](research.md)가 범위를 JVM 단위 테스트로 한정했다. plan 4.0.0에서 `MockEngine` 기반 원격 경로 테스트가 더해졌다([D43](research.md#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)). Compose UI 테스트는 만들지 않는다.

**구성 방식**: 화면 하나를 세 스토리가 공유하므로(FR-001) 화면이 서기 위한 배관 전체가 Phase 2에 들어가고, 각 스토리는 그 화면에 자기 동작을 얹는다. 스토리별 독립 검증 기준은 각 Phase 머리에 있다.

> **이 문서는 두 개의 작업 물결을 담고 있다.**
>
> - **Phase 1~6 — plan 3.0.0 물결(완료).** 화면·도메인·로컬 저장까지 59개 작업이 모두 끝났다. 체크 상태는 그 기록이므로 손대지 않는다.
> - **Phase 7 — plan 4.0.0 물결(대부분 완료).** 실서버를 연결한다. 설계 자체는 4.0.0이 정했고, plan 4.2.0(`prefill()` 재읽기)·4.3.0(봉투는 이미 있는 `MinoResponse<T>`를 쓴다)·4.4.0(문서의 빈틈 셋)이 그 위에 보정을 얹었다. 코드 작업은 끝났고 기기 검증(T082·T083)과 디자인 확인(T086)이 남아 있다.
> - **Phase 7-E — plan 5.0.0 물결(미착수).** **남은 코드 작업은 전부 여기에 있다.** develop을 반영하고 `:core:data`를 전수 확인하니 splash-screen이 `UserApiService`로 같은 `user` 엔드포인트를 이미 쓰고 있었다. 7-B가 만든 `ProfileApiService`·`ProfileRemoteDataSource`는 **같은 서버 리소스의 두 번째 소유자**였고, 그것을 걷어내고 기존 소유자를 넓힌다([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다) · [ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md)).
>
> 4.0.0이 이미 끝난 작업의 산출물을 고치는 곳에는 완료 작업 줄 끝에 `→ 4.0.0: T0xx`로 후속 작업을 지목해 두었다. 그 작업들의 체크는 "그때 그 설계대로 만들었다"는 사실이므로 되돌리지 않는다.
>
> **5.0.0이 폐기한 완료 작업 넷(T069·T070·T071·T074)은 지우지 않고 [폐기된 작업](#폐기된-작업)으로 옮겼다.** 코드가 이미 들어갔다는 뜻이므로, 그 코드를 정리하는 범위를 그 섹션이 든다.

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

- [X] T005 [P] `Profile` 모델 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/model/Profile.kt` ([data-model.md §1](data-model.md)) → **4.0.0: T061**이 아바타 필드 타입을 바꾼다
- [X] T006 [P] `ProfileRepository` 인터페이스 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/repository/ProfileRepository.kt` ([repository 계약](contracts/profile-repository-contract.md)) → **4.0.0: T062**가 `refreshProfile()`을 더한다
- [X] T007 [P] `ValidateNicknameUseCaseTest` 작성 후 실패 확인 — `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ValidateNicknameUseCaseTest.kt`. 케이스는 repository 계약 §테스트 계약이 지정한 `민`·`abc1`·`  민호  `·공백만·한글 30자·낱자 `ㄱㄱ`
- [X] T008 `ValidateNicknameUseCase` 구현 — `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ValidateNicknameUseCase.kt` (T007을 통과시킨다)
- [X] T009 `FakeProfileRepository`(도메인 테스트용) 작성 — `core/domain/src/test/kotlin/team/mino/core/domain/usecase/SaveProfileUseCaseTest.kt` 안의 `private class FakeProfileRepository`. 저장 값 보관과 예외 주입을 지원한다(T010이 쓴다). **경로 정정(4.2.0)**: 이 줄은 별도 파일(`repository/FakeProfileRepository.kt`)을 적고 있었으나 그 파일은 만들어진 적이 없다([D46](research.md#d46-develop-대조로-드러난-사실--도메인-테스트-fake는-별도-파일이-아니다)). 산출물은 그대로이고 기록만 고쳤다 → **4.2.0: T064**가 이어받는다
- [X] T010 `SaveProfileUseCaseTest` 작성 후 실패 확인 — `core/domain/src/test/kotlin/team/mino/core/domain/usecase/SaveProfileUseCaseTest.kt`. trim된 값 저장 · 무효 입력 차단 · Repository 예외 전파 3건 → **4.0.0: T064**
- [X] T011 `SaveProfileUseCase` 구현 — `core/domain/src/main/kotlin/team/mino/core/domain/usecase/SaveProfileUseCase.kt` (T010을 통과시킨다) → **4.0.0: T065**

### 2-B. 로컬 저장 (`:core:data`) — US1의 저장과 US2의 프리필이 쓴다

- [X] T012 `ProfileLocalDataSourceImplTest` 작성 후 실패 확인 — `core/data/src/test/java/team/mino/core/data/datasource/ProfileLocalDataSourceImplTest.kt`. 저장→`observeProfile()` 왕복 · 키가 하나만 있을 때 `null` · 두 키가 한 번에 쓰이는지 → **4.0.0: T073**
- [X] T013 `ProfileLocalDataSource` 인터페이스와 `ProfileLocalDataSourceImpl` 구현 — `core/data/src/main/java/team/mino/core/data/datasource/ProfileLocalDataSource.kt`. 기존 공유 `DataStore<Preferences>`(`core/data/src/main/java/team/mino/core/data/storage/DataStoreModule.kt`)를 주입받고 새 인스턴스를 만들지 않는다. 키와 미저장 판정은 [data-model.md §3](data-model.md) → **4.0.0: T072**가 반환 타입·키·`clearProfile()`을 바꾼다
- [X] T014 `ProfileDataSourceModule` 작성 — `core/data/src/main/java/team/mino/core/data/datasource/di/ProfileDataSourceModule.kt` ([DI 규칙](../../conventions/dependency-injection.md)) → **4.0.0: T074**가 원격 바인딩을 더한다
- [X] T015 [P] `ProfileRepositoryImplTest` 작성 후 실패 확인 — `core/data/src/test/java/team/mino/core/data/repository/ProfileRepositoryImplTest.kt`. DataSource를 Fake로 두고 위임이 그대로인지 → **4.0.0: T075**
- [X] T016 `ProfileRepositoryImpl` 구현 — `core/data/src/main/java/team/mino/core/data/repository/ProfileRepositoryImpl.kt`. 매퍼를 만들지 않는다([repository 계약 §저장 계층](contracts/profile-repository-contract.md)) → **4.0.0: T076**이 원격을 앞에 두고 매퍼를 도입한다. "매퍼를 만들지 않는다"는 3.0.0 시점의 지시다
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
- [X] T027 [P] enum ↔ `avatarId` 매핑 작성 — `feature/profile/src/main/java/team/mino/feature/profile/main/model/ProfileAvatarId.kt`. 선언 순서를 1부터 매기는 임시 매핑이라는 사실을 이 파일에 주석으로 남긴다([research.md D18](research.md)) → **4.0.0: T077**이 통째로 대체한다. 임시 `Int` 매핑은 근거였던 서버 스키마가 실재하지 않아 폐기됐다([D37](research.md#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열))
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

- [X] T037 [US1] `FakeProfileRepository`(feature 테스트용) 작성 — `feature/profile/src/test/java/team/mino/feature/profile/fake/FakeProfileRepository.kt`. `observeProfile()`에 값을 미리 채우고 `saveProfile()`이 예외를 던지게 할 수 있어야 한다 → **4.0.0: T078**
- [X] T038 [US1] `ProfileViewModelTest` 작성 후 실패 확인 — `feature/profile/src/test/java/team/mino/feature/profile/main/vm/ProfileViewModelTest.kt`. 닉네임 입력 → 저장 활성 · 아바타 단일 선택 · 저장 성공 시 `SaveCompleted` · 저장 중 두 번째 `SaveClicked` 무시(UX-003, EC-004) · 저장 실패 시 입력값 보존(FR-012, SC-006) → **4.0.0: T079**

### 사용자 스토리 1 구현

- [X] T039 [US1] `ProfileViewModel`에 `NicknameChanged`·`AvatarSelected` 처리 구현 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/ProfileViewModel.kt`. 판정은 `ValidateNicknameUseCase`가 하고(FR-002, UX-002) 아바타는 항상 단일 선택으로 교체한다(FR-003, TS-004)
- [X] T040 [US1] `ProfileViewModel`에 `SaveClicked` 처리 구현 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/ProfileViewModel.kt`. `isSaving` 가드 → `launchSafely` + `runCatchingDomain`으로 `SaveProfileUseCase(nickname, selectedAvatar ?: 기본 아바타의 id)` 호출 → 성공 시 `SaveCompleted`, 실패 시 `emitDomainError`, 어느 쪽이든 `isSaving=false`([에러 처리 규약](../../conventions/error_handling.md) §4, EC-002) → **4.0.0: T080**
- [X] T041 [US1] `ProfileScreen`에 썸네일·그리드 선택 반영과 `저장` 활성 조건 배선 — `feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileScreen.kt`. 파생 값 계산은 [data-model.md §5](data-model.md), 비활성 버튼은 인텐트를 만들지 않는다(UX-004, EC-011). Figma 노드 `2314-95709`
- [X] T042 [US1] `ProfileRoute`에 실패 통로 배선 — `feature/profile/src/main/java/team/mino/feature/profile/main/screen/ProfileRoute.kt`. `CollectDomainError`로 받아 `LocalSnackbarHostState`에 띄우고, 리프 → 문구 매핑을 이 파일의 `messageResOf`에 둔다. **문구는 `feature/profile/src/main/res/values/strings.xml`의 문자열 리소스다**([feature-module.md](../../architecture/feature-module.md) 4장 스켈레톤, 에러 처리 규약 §8)
- [X] T043 [US1] `ProfileActivity`에 결과 반환 배선 — `feature/profile/src/main/java/team/mino/feature/profile/ProfileActivity.kt`. `SaveCompleted`를 받아 `setResult(RESULT_OK)` 후 `finish()`([launcher 계약 §결과](contracts/profile-launcher-contract.md), FR-008의 이동 부분)
- [X] T044 [US1] 온보딩 진입의 뒤로가기 차단 배선 — `ProfileScreen.kt`(**`onBackClick = null`로 버튼을 그리지 않는다** — 비활성이 아니라 숨김, [D29](research.md#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다))와 `ProfileRoute.kt`(`BackHandler(enabled = !state.isBackEnabled) {}`). 판정 기준은 `entryPoint == Onboarding`(FR-010, EC-001)

**체크포인트**: 온보딩 진입으로 프로필을 만들고 화면이 `RESULT_OK`로 닫힌다. 이것이 MVP다.

---

## Phase 4: 사용자 스토리 2 - 마이페이지에서 프로필을 수정한다 (우선순위 P2)

**목표**: 이미 프로필이 있는 사용자가 같은 화면에서 값을 고쳐 저장하거나, 저장하지 않고 돌아간다.

**독립 테스트**: `adb ... --es profile_entry_point edit`으로 열어 [quickstart.md §3](quickstart.md)의 확인 항목 11·12·13·14·16·17을 통과한다. 프리필은 T045의 단위 테스트로도 독립 검증된다(Fake Repository에 값을 미리 채운다).

### 사용자 스토리 2 테스트

- [X] T045 [US2] `ProfileViewModelTest`에 프리필 케이스 추가 — `feature/profile/src/test/java/team/mino/feature/profile/main/vm/ProfileViewModelTest.kt`. `observeProfile()`의 첫 값이 `nickname`·`selectedAvatar`를 채우고 `isNicknameValid=true`·`isNicknameTouched=false`가 되는지(FR-006, TS-008) → **4.0.0: T079**

### 사용자 스토리 2 구현

- [X] T046 [US2] `ProfileViewModel`에 `observeProfile()` 구독 프리필 구현 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/ProfileViewModel.kt`. 화면이 따로 조회를 걸지 않는다([screen 계약 §Intent](contracts/profile-screen-contract.md)). 목록 밖 `avatarId`는 기본 아바타로 대체한다([data-model.md §4](data-model.md)) → **4.0.0: T081**이 진입 시 갱신을 더한다
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
- [X] T053 Figma 대조 수행 — 010-1 `2314-95662` · 010-2 `2314-95709` · 010-3 `2314-95754`. 절차와 토큰/실측 판정 기준은 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md), 대상은 신설한 `MinoProfileAvatarImage`·`MinoTopNavigation`과 화면 배치다 → **4.1.0: T086**이 아바타 색 대응 한 칸을 더한다
- [X] T054 [quickstart.md §3](quickstart.md)의 기기 확인 18항목 수행 — 확인할 수 없는 항목은 같은 문서 §4가 이미 열거했다. 통과하지 못한 항목은 사유와 함께 남긴다 → **4.0.0: T082·T083**
- [X] T055 품질 게이트 실행 — `./gradlew :core:domain:test :core:data:test :feature:profile:testDebugUnitTest :app:assembleQaDebug` ([헌법](../../constitution.md) §품질 게이트). 로컬 `lintDebug`는 데몬이 죽을 수 있고, 죽었다고 검증이 수행된 것으로 보지 않는다 → **4.0.0: T084**
- [X] T058 `:feature:profile`을 모듈 목록 SSOT에 등록 — `docs/architecture/modularization.md` §모듈 구성. [헌법](../../constitution.md) §기술 표준이 이 문서를 모듈 목록의 단일 출처로 지정한다. **신규 모듈을 만드는 작업에는 항상 이 등록이 딸린다**
- [X] T059 아바타 12종 소유 결정을 ADR로 승격 — `docs/adr/2026-08-25-profile-avatar-assets-in-design-system.md` + [ADR README](../../adr/README.md) 인덱스. 헌법 Governance의 "기록 없는 예외는 없다"를 충족한다([research.md D35](research.md#d35-아바타-12종의-소유-결정을-adr로-승격했다))

---

## Phase 7: 원격 연동 (plan 4.0.0 개정분)

**목적**: 프로필의 원천을 로컬 DataStore에서 꾹 서버로 옮긴다. 로컬은 캐시로 내려간다([D36](research.md#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)). 화면은 바뀌지 않는다 — 이 물결이 손대는 `:feature:profile` 파일은 다섯이고 UiState·Intent·SideEffect·컴포저블은 그대로다.

**닫히는 것**: spec §4의 "저장은 서버 반영을 포함한다" 가정, FR-008의 개인방 생성, FR-012의 저장 실패 발화 원천, 그리고 plan 3.0.0의 원칙 V FAIL(아래 [미결 사항](#미결-사항) 1번).

**⚠️ 손대는 파일은 plan이 고정한다** — **이미 있는 파일의 변경**은 [§규모/범위](plan.md)의 두 표(프로덕션 **열한 줄** · 테스트 **일곱 줄**, plan 5.0.0에서 늘었다)가, **신규 파일**은 [§프로젝트 구조](plan.md)의 트리가 든다. 어느 쪽에도 없는 파일을 고치게 되면 설계에서 벗어난 신호이므로 작업을 멈추고 보고한다.

### 7-A. 도메인 계약 (`:core:domain`) — 7-B·7-C 전부가 쓴다

- [X] T060 [P] `ProfileAvatar` enum 12항목 신설 — `core/domain/src/main/kotlin/team/mino/core/domain/model/ProfileAvatar.kt`. 선언 순서는 `MinoProfileAvatar`와 같게 두고(T077의 전제), 그림·서버 문자열을 갖지 않는다([data-model.md §1](data-model.md), [D37](research.md#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열))
- [X] T061 `Profile`의 필드를 `avatarId: Int` → `avatar: ProfileAvatar`로 변경 — `core/domain/src/main/kotlin/team/mino/core/domain/model/Profile.kt`. KDoc의 `Avatar { id: integer }` 근거 서술을 걷어낸다(그 스키마는 배포 문서에 없다). T060에 의존
- [X] T062 [P] `ProfileRepository`에 `suspend fun refreshProfile()` 추가 — `core/domain/src/main/kotlin/team/mino/core/domain/repository/ProfileRepository.kt`. **미등록은 실패가 아니라는 계약**과 `saveProfile`의 "원격 성공 → 캐시 갱신" 순서를 KDoc에 싣고, 3.0.0이 남긴 "지금은 발화 원천이 없다" 단서를 제거한다([repository 계약](contracts/profile-repository-contract.md), [D39](research.md#d39-repository-표면--observeprofile--refreshprofile--saveprofile-세-멤버))
- [X] T064 `SaveProfileUseCaseTest`를 새 타입으로 갱신하고 실패 확인 — `core/domain/src/test/kotlin/team/mino/core/domain/usecase/SaveProfileUseCaseTest.kt`. 검증 대상 3건(trim 저장·무효 입력 차단·예외 전파)은 그대로다. **이 파일 안의 `private class FakeProfileRepository`에 `refreshProfile()`과 아바타 타입을 함께 반영한다** — 별도 fake 파일은 없다([D46](research.md#d46-develop-대조로-드러난-사실--도메인-테스트-fake는-별도-파일이-아니다)). T061·T062에 의존
- [X] T065 `SaveProfileUseCase`의 파라미터를 `avatarId: Int` → `avatar: ProfileAvatar`로 변경 — `core/domain/src/main/kotlin/team/mino/core/domain/usecase/SaveProfileUseCase.kt` (T064를 통과시킨다). `ValidateNicknameUseCase`는 건드리지 않는다
- [X] T087 [P] `ValidateNicknameUseCaseTest`에 **중간 공백** 케이스 1건 추가 — `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ValidateNicknameUseCaseTest.kt`. 검증 대상은 [repository 계약 §테스트 계약](contracts/profile-repository-contract.md)이 소유한다. **구현은 이미 옳으므로 red를 얻지 못한다** — 회귀 방지용이며 통과하는 것이 정상이다([D48](research.md#d48-분석이-드러낸-문서의-빈틈--계약이-요구한-검증이-작업이-되지-못했다)). 이 물결의 어느 작업에도 의존하지 않는다

**체크포인트**: `./gradlew :core:domain:test`가 통과한다. 이 시점에 `:core:data`·`:feature:profile`은 컴파일되지 않는다 — 7-B·7-C가 뒤따라야 한다.

### 7-B. 데이터 레이어 (`:core:data`) — 이번 물결의 무게중심

> 원문 스키마·협의 항목·레이어 구성은 [API 계약](contracts/profile-api-contract.md)이 소유한다. 작업 줄에 옮겨 적지 않는다.

- [X] T066 [P] 원격 DTO 3종 신설 — `core/data/src/main/java/team/mino/core/data/network/dto/request/ProfileRequest.kt`(+`AvatarRequest`) · `dto/response/ProfileResponse.kt`(+`AvatarResponse`, `avatar`는 nullable) · `dto/response/ErrorResponse.kt`. `ErrorResponse`는 **공용 타입**이라 프로필 이름을 갖지 않는다. **봉투는 만들지 않는다** — `dto/response/MinoResponse.kt`가 이미 있고 [ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 그것을 지배한다([D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다), [API 계약 §3](contracts/profile-api-contract.md))
- [X] T067 [P] `ProfileMapperTest` 작성 후 실패 확인 — `core/data/src/test/java/team/mino/core/data/repository/mapper/ProfileMapperTest.kt`. **아바타 색 왕복 12종**([API 계약 §2 아바타 값 표](contracts/profile-api-contract.md)의 값을 그대로 쓴다) · `gray` → 기본 아바타(보내지 않는 값이지만 받을 수는 있다) · 모르는 문자열 → 기본 아바타 · `avatar == null` → 기본 아바타. **12종을 하나씩 다 적는다** — 대응이 선언 순서와 어긋나 루프로 돌리면 검증이 자기 자신을 증명하는 꼴이 된다([D44](research.md#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)). T060·T066에 의존
- [X] T068 `ProfileMapper` 구현 — `core/data/src/main/java/team/mino/core/data/repository/mapper/ProfileMapper.kt`. **아바타 색 표를 이 파일 하나가 소유한다** — 값은 [API 계약 §2 아바타 값 표](contracts/profile-api-contract.md)가 소유하며(`Person1`→`red` … `Person12`→`violet`) 서버 `enum` 13개 중 `gray`만 쓰지 않는다. **표를 `ordinal`로 파생하지 말 것** — 대응이 `RoomColor` 선언 순서와 어긋나 조용히 틀린 값이 나간다([D44](research.md#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)). `ProfileEntry` ↔ `Profile` 변환도 여기다(T072와 짝). (T067을 통과시킨다)
- [X] T072 `ProfileEntry` DTO 신설과 `ProfileLocalDataSource`(+`Impl`) 변경 — `core/data/src/main/java/team/mino/core/data/datasource/ProfileEntry.kt`(신설)와 같은 디렉터리의 `ProfileLocalDataSource.kt`(+`Impl`). **`network/dto/`에 두지 않는다** — 서버 계약이 아니라 캐시 표현이며, 로컬 DataSource의 자리는 [`core/data/README.md`](../../../core/data/README.md) §5가 정한다. 반환 타입 `Profile` → `ProfileEntry`, `clearProfile()` 추가, 아바타 키 `profile_avatar_id`(Int) → `profile_avatar`(String, enum 이름). **키 상수는 구현체 안에 남긴다**([data-model.md §3](data-model.md), [D42](research.md#d42-로컬-캐시-datasource는-profileentry-dto를-반환한다)). 마이그레이션은 두지 않는다
- [X] T073 `ProfileLocalDataSourceImplTest` 갱신 — `core/data/src/test/java/team/mino/core/data/datasource/ProfileLocalDataSourceImplTest.kt`. 기존 3건을 `ProfileEntry`로 옮기고 `clearProfile()` 후 `null` 1건을 더한다. **타입이 바뀌어 red를 컴파일 실패로만 얻으므로 T072와 한 묶음으로 처리한다**
- [X] T075 `ProfileRepositoryImplTest` 재작성 후 실패 확인 — `core/data/src/test/java/team/mino/core/data/repository/ProfileRepositoryImplTest.kt`. Fake DataSource 둘로 ① 캐시 비었을 때 등록 호출 ② 캐시 있을 때 수정 호출 ③ **원격 실패 시 캐시 불변**(FR-012·SC-006) ④ `refreshProfile()`이 미등록에서 캐시를 비우고 예외를 던지지 않는지 ⑤ `409`의 전파. T071·T072에 의존
- [X] T076 `ProfileRepositoryImpl` 재구현 — `core/data/src/main/java/team/mino/core/data/repository/ProfileRepositoryImpl.kt`. 원격을 앞에 두고 로컬을 캐시로 내린다. **분기는 여기 하나뿐이고**([D38](research.md#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)) 변환의 경계도 여기다 — DTO가 밖으로 나가지 않는다. 불변식 4건은 [repository 계약 §저장의 불변식](contracts/profile-repository-contract.md). (T075를 통과시킨다)

**체크포인트**: `./gradlew :core:domain:test :core:data:test`가 통과한다. **여기까지는 서버 없이 검증된다** — 원격 경로 테스트가 `MockEngine`으로 돌기 때문이다.

### 7-C. feature 배선 (`:feature:profile`) — 다섯 파일

- [X] T077 [P] [US1] [US2] `ProfileAvatarId.kt` → `ProfileAvatarMapping.kt` 재작성 — `feature/profile/src/main/java/team/mino/feature/profile/main/model/ProfileAvatarMapping.kt`. `Int` 매핑을 걷어내고 `ProfileAvatar` ↔ `MinoProfileAvatar`를 **전수 `when`**으로 잇는다 — `ordinal`은 목록이 어긋나도 컴파일이 통과한다([data-model.md §4](data-model.md)). `DefaultProfileAvatar`는 유지하되 값은 `ProfileAvatar.Default`에서 가져온다. T060에 의존
- [X] T078 [US1] feature 테스트용 `FakeProfileRepository`에 `refreshProfile()` 구현 — `feature/profile/src/test/java/team/mino/feature/profile/fake/FakeProfileRepository.kt`. 갱신 호출 횟수 관측과 예외 주입을 지원한다(T079가 쓴다)
- [X] T079 [US1] [US2] [US3] `ProfileViewModelTest` 갱신 후 실패 확인 — `feature/profile/src/test/java/team/mino/feature/profile/main/vm/ProfileViewModelTest.kt`. 기존 케이스(T038·T045·T048이 만든 것)를 새 타입으로 옮기고 **① 마이페이지 진입 시 `refreshProfile()`이 한 번 불리는지 ② 갱신이 미등록(예외 없음)일 때 오류가 방출되지 않는지 ③ 갱신 실패 시 화면이 캐시 값으로 계속 서는지 ④ 갱신이 새 값을 캐시에 쓰면 화면이 그 값으로 다시 채워지는지, 단 사용자가 이미 입력했거나(`isNicknameTouched`) 저장 중이면 덮어쓰지 않는지** 4건을 더한다. ④가 [D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)의 가드다. 온보딩 진입이 갱신하지 않는다는 것은 진입점 단위로 고정한다([D50](research.md#d50-진입-시-갱신--마이페이지-진입에서만-건다)). T078에 의존
- [X] T080 [US1] `ProfileViewModel`의 `SaveClicked` 처리에서 넘기는 아바타 타입 변경 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/ProfileViewModel.kt`. `selectedAvatar ?: 기본 아바타`를 `ProfileAvatar`로 옮겨 넘긴다. `isSaving` 가드·`launchSafely`·`runCatchingDomain` 배선은 그대로 둔다(3.0.0이 미리 뚫어 둔 통로가 이제 실제로 발화한다)
- [X] T081 [US2] `ProfileViewModel`에 **마이페이지 진입 시** `refreshProfile()` 호출과 **갱신 성공 시 조건부 재프리필** 배선 — `feature/profile/src/main/java/team/mino/feature/profile/main/vm/ProfileViewModel.kt`. `launchSafely` + `runCatchingDomain`으로 감싸고 **별도 로딩 상태를 두지 않는다**([data-model.md §5](data-model.md), [screen 계약 §Intent](contracts/profile-screen-contract.md)). **기존 `prefill()`이 `observeProfile().first()`라 갱신된 값이 저절로 들어오지 않는다** — 갱신이 성공하면 `isNicknameTouched == false && !isSaving`일 때만 한 번 더 채운다([D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)). **흐름을 계속 구독하지는 않는다**([screen 계약 §Intent](contracts/profile-screen-contract.md)). T080과 같은 파일이라 순차 처리한다

**체크포인트**: `./gradlew :feature:profile:testDebugUnitTest :app:assembleQaDebug`가 통과한다. 앱이 실서버에 붙는다.

### 7-E. `user` 태그 통합 (plan 5.0.0 개정분)

> **7-B가 만든 것 중 넷을 걷어내고 기존 소유자를 넓힌다.** 폐기 사유와 정리 범위는 [폐기된 작업](#폐기된-작업)이 든다. 소유 규칙 자체는 [ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md)이, 레이어 표면은 [API 계약 §3](contracts/profile-api-contract.md)이 소유한다.
>
> **이 소그룹은 이미 머지된 splash-screen의 파일을 고친다.** 프로필이 잘 돌아도 진입 게이트가 깨지면 앱을 켜는 모든 사용자가 영향을 받는다 — T096이 그것을 받는다.

- [X] T088 [P] `UserApiServiceTest` 작성 후 실패 확인 — `core/data/src/test/java/team/mino/core/data/network/UserApiServiceTest.kt`. `MockEngine`으로 ① `MinoResponse<T>` 봉투(`{data}`) 해제 ② `401` + `USER_NOT_REGISTERED` → `getMe()`가 `null` · `hasProfile()`이 `false` ③ `401` + `UNAUTHORIZED`의 전파 ④ `409`의 전파 ⑤ **`hasProfile()`이 성공 본문 스키마에 의존하지 않는지** — `{"data":{"id":1}}`처럼 `ProfileResponse`를 만족하지 않는 본문에도 `true`여야 한다. ⑤는 T091이 넘겨주는 검증이며 **이 물결에서 가장 잃기 쉬운 성질**이다([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)). 폐기된 T069의 케이스 넷을 이 파일이 흡수한다
- [X] T089 `UserApiService` 확장 — `core/data/src/main/java/team/mino/core/data/network/service/UserApiService.kt`. 기존 `getMe()`(Unit)를 **`hasProfile(): Boolean`으로 바꾸고 본문 미역직렬화를 유지**한 뒤, `getMe(): ProfileResponse?`·`register()`·`updateMe()` 셋을 더한다. `401` 판정을 **`ErrorResponse` DTO를 쓰는 private 헬퍼 하나**로 두고 `hasProfile()`·`getMe()`가 공유한다([API 계약 §3](contracts/profile-api-contract.md), [ADR 2026-08-28](../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)). 봉투 해제는 `body<MinoResponse<T>>().data`이고 반환 타입에 `MinoResponse`가 드러나지 않는다. (T088을 통과시킨다)
- [X] T090 `UserRemoteDataSource`(+`Impl`) 확장 — `core/data/src/main/java/team/mino/core/data/datasource/UserRemoteDataSource.kt`(+`Impl`). `getMe()`·`register()`·`updateMe()` 셋을 더하고 기존 `isRegistered()`의 **시그니처와 KDoc 계약은 그대로 둔다**(splash-screen이 쓴다). **`Impl`의 `Json.parseToJsonElement` 수동 파싱과 `USER_NOT_REGISTERED` 상수를 걷어내고 위임 네 줄만 남긴다** — Ktor 타입이 `network/` 밖으로 나가지 않아야 [`core/data/README.md`](../../../core/data/README.md) §5를 지킨다. T089에 의존
- [X] T091 `UserRemoteDataSourceImplTest` 정리 — `core/data/src/test/java/team/mino/core/data/datasource/UserRemoteDataSourceImplTest.kt`. `401` 판정 케이스는 T088로 옮겼으므로 **여기 남기지 않는다.** 남는 것은 네 함수가 서비스로 위임하는지다. **`{"data":{"id":1}}` 픽스처가 지키던 성질을 버리는 것이 아니라 T088로 옮기는 것이다** — 옮겨졌는지 확인하고 지운다. T088·T090에 의존
- [X] T092 `ProfileDataSourceModule`에서 원격 바인딩 제거 — `core/data/src/main/java/team/mino/core/data/datasource/di/ProfileDataSourceModule.kt`. 로컬 바인딩 하나만 남긴다. 원격은 `UserDataSourceModule`이 이미 갖고 있어 **더할 것이 없다**(폐기된 T074를 되돌린다). 중복 바인딩을 남기면 Hilt가 컴파일 단계에서 잡는다
- [X] T093 `ProfileRepositoryImplTest`의 Fake를 `UserRemoteDataSource`로 교체 — `core/data/src/test/java/team/mino/core/data/repository/ProfileRepositoryImplTest.kt`. **검증 대상 5건(T075가 정한 것)은 그대로다** — Fake가 구현하는 인터페이스만 바뀌고, 쓰지 않는 `isRegistered()`는 기본 구현으로 채운다. T090에 의존
- [X] T094 `ProfileRepositoryImpl`의 주입 대상을 `UserRemoteDataSource`로 교체 — `core/data/src/main/java/team/mino/core/data/repository/ProfileRepositoryImpl.kt`. **생성자 파라미터 타입 한 줄이고 본문 로직은 손대지 않는다** — 등록/수정 분기와 "원격 성공 → 캐시 갱신" 순서는 T076이 이미 옳게 만들었다. (T093을 통과시킨다)
- [X] T095 폐기된 산출물 삭제 — `core/data/src/main/java/team/mino/core/data/network/service/ProfileApiService.kt` · `datasource/ProfileRemoteDataSource.kt` · `datasource/ProfileRemoteDataSourceImpl.kt` · `core/data/src/test/java/team/mino/core/data/network/ProfileApiServiceTest.kt`. **T089~T094가 모두 끝난 뒤에 지운다** — 먼저 지우면 컴파일이 서지 않아 중간 상태를 확인할 수 없다. 지운 뒤 `grep -rn "ProfileApiService\|ProfileRemoteDataSource" core/ feature/`가 비어야 한다

**체크포인트**: `./gradlew :core:domain:test :core:data:test :feature:profile:testDebugUnitTest :feature:splash:testDebugUnitTest :app:assembleQaDebug`가 통과한다. **`:feature:splash`가 이 물결의 회귀 지표다.**

### 7-D. 검증 및 마무리

- [X] T082 [quickstart.md §4-1·4-2](quickstart.md)의 등록·수정 경로 기기 확인 17항목 수행 — 요청 본문의 `avatar.color`가 **고른 아바타의 색**인지(6번 `orange` · 14번 `red`) 로그캣으로 확인하고, 등록/수정 엔드포인트 선택도 함께 본다. 10번은 spec 2.0.0이 신설한 **TS-019**(선택 아바타가 보조 수단에 전달되는지)를 겸한다. 개인방 생성(8번)은 서버 쪽에서 확인한다. **Firebase 익명 세션이 먼저 확보돼 있어야 한다**(같은 문서 §선행 조건)
- [X] T083 [quickstart.md §4-3](quickstart.md)의 실패 경로 기기 확인 5항목 수행 — 이번 물결에서 **처음 기기로 확인되는 구간**이다(FR-012·SC-006·UX-003). 20번은 서버가 긴 닉네임을 거절하는 **상태 코드를 실측해 [API 계약 §2](contracts/profile-api-contract.md) 4번에 적는다** — 문서에 없는 값이라 실측이 유일한 근거다
- [X] T084 품질 게이트 실행 — `./gradlew :core:domain:test :core:data:test :feature:profile:testDebugUnitTest :app:assembleQaDebug` ([헌법](../../constitution.md) §품질 게이트). 로컬 `lintDebug`는 데몬이 죽을 수 있고, 죽었다고 검증이 수행된 것으로 보지 않는다
- [X] T096 [quickstart.md §4-4](quickstart.md)의 스플래시 회귀 기기 확인 3항목 수행 — 미등록 콜드 스타트가 온보딩으로, 등록 후 재시작이 메인으로 가는지, 그리고 **비행기 모드 콜드 스타트가 온보딩으로 떨어지지 않는지**. 셋째가 핵심이다 — `401`이 아닌 실패를 미등록으로 뭉개면 세션이 깨진 기존 사용자가 온보딩에 떨어진다. 7-E 완료에 의존한다
- [ ] T097 품질 게이트 재실행 — `./gradlew :core:domain:test :core:data:test :feature:profile:testDebugUnitTest :feature:splash:testDebugUnitTest :app:assembleQaDebug`. **T084와 달리 `:feature:splash`를 포함한다** — 7-E가 그 feature의 데이터 계층을 고쳤기 때문이다([헌법](../../constitution.md) §품질 게이트)
- [X] T098 [P] `ApiService`의 소유 단위를 ADR로 승격 — [`docs/adr/2026-08-28-api-service-owned-per-server-tag.md`](../../adr/2026-08-28-api-service-owned-per-server-tag.md) + [ADR README](../../adr/README.md) 인덱스. `ApiService`를 feature가 아니라 **OpenAPI 태그** 단위로 두고 태그당 소유자를 하나로 한다는 규칙은 **서버를 소비하는 모든 feature를 구속한다**([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)). 같은 작업에서 [에러 본문 ADR](../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)의 "소비 지점은 `ProfileApiService.getMe()` 하나"라는 **거짓이 된 서술도 함께 바로잡았다**(결정은 유지, `Accepted` 그대로)
- [X] T086 [P] `Person10` ↔ `brown` 대응을 디자인에 확인 — 확정되면 [API 계약 §2](contracts/profile-api-contract.md)의 디자인 확인 항목을 닫고, 다른 색이면 `ProfileMapper`(T068이 만든다)의 표 한 줄만 고친다. **나머지 11종은 배경 원 색이 디자인 시스템 토큰과 hex 단위로 일치해 확인이 필요 없다** — `Person10`만 대응 토큰이 없어 남은 색으로 소거 배정했다([D44](research.md#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)). 코드에 의존하지 않으므로 언제든 진행할 수 있다
- [X] T085 `ErrorResponse`와 **`errorCode` 취급**의 소유 결정을 ADR로 승격 — `docs/adr/` + [ADR README](../../adr/README.md) 인덱스. `errorCode`를 도메인 예외로 올리지 않고 필요한 곳에서 지역 처리한다는 판단은 **서버를 소비하는 모든 feature를 구속한다**([D40](research.md#d40-응답-봉투와-에러-코드--공용-dto를-신설한다) 보정, [D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)). **봉투는 범위에서 빠진다** — [ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 이미 승격해 두었다. `/adr-writer`를 쓴다

**체크포인트**: 온보딩 진입이 서버에 유저를 등록하고 개인방이 생기며, 마이페이지 진입이 서버 값을 프리필해 수정한다. 저장 실패가 화면을 지키는 것이 기기에서 확인된다.

---

## 폐기된 작업

**plan 5.0.0이 폐기한 완료 작업 넷이다.** 넷 다 `- [X]`였다 — **코드가 이미 들어갔다는 뜻**이므로 지우지 않고 여기로 옮겼다. 정리는 T095가 한 번에 한다.

폐기 사유는 넷 다 하나다: `POST /api/v1/users`·`GET /api/v1/users/me`·`PATCH /api/v1/users/me`는 전부 OpenAPI `user` 태그이고, 그 태그의 소유자는 splash-screen이 만든 `UserApiService`·`UserRemoteDataSource`가 이미 있었다. 7-B는 **같은 서버 리소스의 두 번째 소유자**를 만든 것이다([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다) · [ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md)).

| 폐기 | 무엇이었나 | 대체 | 정리할 코드 |
|---|---|---|---|
| T069 | `ProfileApiServiceTest` 작성 | **T088** (`UserApiServiceTest`) | `core/data/src/test/java/.../network/ProfileApiServiceTest.kt` 삭제 |
| T070 | `ProfileApiService` 구현 | **T089** (`UserApiService` 확장) | `core/data/src/main/java/.../network/service/ProfileApiService.kt` 삭제 |
| T071 | `ProfileRemoteDataSource`(+`Impl`) 신설 | **T090** (`UserRemoteDataSource` 확장) | `datasource/ProfileRemoteDataSource.kt`·`ProfileRemoteDataSourceImpl.kt` 삭제 |
| T074 | `ProfileDataSourceModule`에 원격 바인딩 추가 | **T092** (바인딩 제거) | 모듈에서 `bindProfileRemoteDataSource` 한 줄 제거 |

**폐기된 것은 작업이지 그 안의 판단이 아니다.** T070이 정한 봉투 해제 방식·`401` 지역 처리·`ErrorResponse` 사용은 그대로 **T089로 옮겨간다.** 옮겨가지 않는 것은 **어느 타입이 그 일을 하느냐** 하나다.

**살아남은 7-B 산출물** — T066(DTO 3종)·T067·T068(`ProfileMapper`)·T072·T073(`ProfileEntry`·로컬 캐시)·T075·T076(`ProfileRepositoryImpl`+테스트)은 폐기 대상이 아니다. T075·T076은 **파일도 산출물도 그대로**이고 주입 대상 타입만 바뀌므로 T093·T094가 그 한 줄씩을 고친다.

<details>
<summary>폐기된 작업의 원문 (인용이 깨지지 않도록 보존)</summary>

- [X] T069 `ProfileApiServiceTest` 작성 후 실패 확인 — `core/data/src/test/java/team/mino/core/data/network/ProfileApiServiceTest.kt`. `MockEngine`으로 ① `MinoResponse<T>` 봉투(`{data}`) 해제 ② `401` + `USER_NOT_REGISTERED` → `null` ③ `401` + `UNAUTHORIZED`의 전파 ④ `409`의 전파. 기존 [`DomainExceptionMappingTest`](../../../core/data/src/test/java/team/mino/core/data/network/DomainExceptionMappingTest.kt) 방식을 따른다
- [X] T070 `ProfileApiService` 구현 — `core/data/src/main/java/team/mino/core/data/network/service/ProfileApiService.kt`. 세 오퍼레이션과 **`body<MinoResponse<T>>().data` 봉투 해제**(반환 타입에 `MinoResponse`가 드러나서는 안 된다, [ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)), 그리고 **`getMe()`의 지역 catch 한 곳**([API 계약 §3](contracts/profile-api-contract.md)). 엔드포인트를 붙이는 절차는 [`core/data/README.md`](../../../core/data/README.md) §8을 따른다. 그 밖의 예외를 잡지 않는다 — 매핑은 `convertDomainException`이 전역 수행한다([`core/data/README.md`](../../../core/data/README.md) §4). `MinoIdentityProofPlugin`이 이미 Bearer를 붙이므로 헤더를 손으로 달지 않는다. (T069를 통과시킨다)
- [X] T071 `ProfileRemoteDataSource` 인터페이스와 `Impl` 신설 — `core/data/src/main/java/team/mino/core/data/datasource/ProfileRemoteDataSource.kt`. DTO만 반환하고 변환하지 않는다([`core/data/README.md`](../../../core/data/README.md) §5)
- [X] T074 `ProfileDataSourceModule`에 `ProfileRemoteDataSource` 바인딩 추가 — `core/data/src/main/java/team/mino/core/data/datasource/di/ProfileDataSourceModule.kt` ([DI 규칙](../../conventions/dependency-injection.md)). T071에 의존

</details>

---

## 미결 사항

plan 4.3.0 기준으로 갱신했다. **닫힌 것은 아래 "닫힌 미결 사항"으로 옮겼고, 남은 것과 새로 드러난 것만 여기 적는다.**

> **2026-08-28 — 남은 항목의 처리 방침을 사용자와 확정했다.** 1·5·6·8번은 **의도적으로 지금 움직이지 않는다**(놓친 것이 아니다). 각 항목에 결정과 재개 조건을 적어 두었으니, 다음에 이 목록을 읽는 사람은 같은 논의를 다시 열지 않는다.

1. **서버 문서의 빈틈 2건 — 협의가 필요하다.** 표와 잠정 처리는 [API 계약 §2](contracts/profile-api-contract.md)가 소유한다. ⑤ 미등록이 `404`가 아니라 `401`이라 본문 `errorCode`를 읽어야 구분된다 ⑥ 응답 `avatar`가 nullable인데 언제 `null`인지 불명. **plan 4.0.0 시점의 6건에서 2건으로 줄었다** — ④(닉네임 거절 상태 코드)는 **T083이 `400 Bad Request`로 실측해 닫았다**(2026-08-28).
   → **결정(2026-08-28): 문서에만 남기고 진행한다.** 이슈를 따로 올리지 않는다. 세 건 모두 **잠정 처리가 이미 서 있어 구현을 막지 않기** 때문이다 — ⑤는 `UserApiService`의 `401` 판정 헬퍼가 닫아 두었고(T089), ⑥은 기본 아바타로 읽는다. [API 계약 §2](contracts/profile-api-contract.md)의 표가 근거이므로 필요할 때 그것을 들고 구두로 맞춘다.
   → **재개 조건**: ⑤가 `404`로 바뀌면 지역 catch가 통째로 사라지므로, 서버가 그 변경을 알려 오면 즉시 반영한다.
2. ~~**`Person10` ↔ `brown` 대응이 미검증이다.**~~ → **닫혔다(2026-08-28, T086).** 디자인 확인 결과 소거법 배정이 맞았다. 아바타 12종의 서버 문자열 대응이 전부 근거를 갖게 됐고, [API 계약 §2](contracts/profile-api-contract.md)의 디자인 확인 항목도 0건이 됐다.
3. **서버 문서가 구현 도중에도 바뀔 수 있다.** plan 4.0.0과 4.1.0 사이 **약 3시간 만에** `avatar.color`가 자유 문자열에서 13개 `enum`으로 좁혀졌다. 그 사이에 T068을 구현했다면 서버가 거절할 값을 내보내고 있었을 것이다. **7-B 착수 직전에 [quickstart.md §2](quickstart.md)의 재조회를 한 번 돌린다.** 저장이 이유 없이 실패하면 코드보다 이 조회를 먼저 의심한다.
   → **plan 4.3.0에서 대상이 하나 늘었다: `:core:data` 트리도 함께 다시 본다.** 4.2.0의 대조가 옛 트리를 근거로 삼는 바람에 `MinoResponse<T>`를 못 보고 `ApiEnvelope`를 신설할 뻔했다. 여러 feature가 `:core:data`를 동시에 넓히는 국면이라 **대조에도 유효기간이 있다**([D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)).
4. ~~**`ErrorResponse`와 `errorCode` 취급이 다른 feature를 구속하는데 ADR이 아직 없다.**~~ → **닫혔다(2026-08-28).** T085가 [에러 본문 ADR](../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)로, T098이 [`ApiService` 소유 단위 ADR](../../adr/2026-08-28-api-service-owned-per-server-tag.md)로 승격했다. 봉투는 [ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 먼저 닫아 두었다.
5. **방은 절반만 실서버다.** 방 목록(`getRooms()`)은 이미 실서버를 보고 `:feature:sharereceiver`의 방 선택 시트가 `RoomType.PERSONAL`을 알지만, `getRoom`·`createRoom`·`updateRoom`은 아직 `RoomMockRemoteDataSourceImpl`이다.
   → **4.1.0이 적었던 "서버가 만든 개인방이 앱에 보이지 않는다"는 더 이상 사실이 아니다**([D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)). 다만 T082 8번은 그대로 서버 쪽에서 확인한다 — 목록 화면은 이 spec의 검증 대상이 아니다.
   → **결정(2026-08-28): 문서에 드러내고 둔다.** 남은 mock 3종의 실서버 전환은 [group-room-form](../group-room-form/contracts/room-api-mock.md)의 소관이고 프로필 범위가 아니다([D41](research.md#d41-목-엔진을-만들지-않는다)). 프로필이 실서버를 붙였다는 이유로 그쪽 계획을 대신 개정하지 않는다.
   → **재개 조건**: group-room-form이 남은 셋을 전환하면 이 항목은 저절로 닫힌다.
6. **`AndroidFeatureConventionPlugin`에 `testOptions` 승격 제안.** `toRoute`를 쓰는 모든 feature의 ViewModel 테스트가 같은 벽을 만난다([D31](research.md#d31-viewmodel-단위-테스트는-isreturndefaultvalues로-열고-진입점은-통제하지-않는다)). 이번 범위 밖이라 작업으로 만들지 않았다.
   → **결정(2026-08-28): 미결로 그대로 둔다.** 이슈도 작업도 만들지 않는다. 지금 막고 있는 것이 없고(`:feature:profile`이 개별로 들고 있어 테스트가 돈다), 컨벤션 플러그인은 모든 feature가 쓰는 빌드 로직이라 프로필 범위에서 고치면 영향이 이 spec 밖으로 번진다.
   → **재개 조건**: `toRoute`를 쓰는 **두 번째** feature가 같은 벽에 막히는 순간. 그때는 사례가 둘이라 승격 근거가 선다.
7. **`409 USER_ALREADY_REGISTERED`를 기기에서 재현할 수 없다.** 캐시가 비었는데 서버에는 유저가 있는 상태를 앱 조작만으로 만들 수 없다(앱 데이터를 지우면 익명 세션도 함께 사라진다). T075의 단위 테스트가 그 자리를 대신한다([quickstart.md §5](quickstart.md)).
8. **PRD `[SYS-011]` Flow D가 spec과 어긋난다.** spec 2.0.0이 온보딩 뒤로가기를 "노출하지 않는다"로 확정했는데 PRD는 아직 "비활성"이다. 같은 PRD의 `[SYS-001]`은 "표출하지 않는다"라 PRD 내부에서도 갈린다.
   → **결정(2026-08-28): 구현이 끝난 뒤에 모아서 고친다.** 지금 `/mino-prd`를 돌리지 않는다. **PRD 버전이 오르면 다른 모든 spec의 기준 PRD 버전이 일제히 뒤처지므로**, 문구 한 줄 때문에 그 파장을 지금 만들 이유가 없다. 코드는 spec을 따르므로 동작에는 영향이 없다.
   → **재개 조건**: Phase 7 완료. 그때 이 건과 3차 검증에서 보고된 `프로필 이미지를 꾸며보세요.` 인용 오류를 **함께** 고친다 — 둘 다 `[SYS-011]` 한 섹션이라 한 번의 PRD 개정으로 닫힌다.
11. **T097이 develop의 기존 파손으로 통과하지 못한다.** `:feature:splash:testDebugUnitTest`가 컴파일되지 않는다 — `SplashViewModelTest`의 `FakeAnonymousAuthRepository`가 `AnonymousAuthRepository.currentSession()`을 구현하지 않았다. **7-E가 만든 것이 아니다**: `feature/splash/`와 `AnonymousAuthRepository.kt` 둘 다 working tree 무수정이며, develop `93317be`가 인터페이스를 넓히면서 그 fake를 갱신하지 않은 것이다.
   → **tasks.md가 그것을 7-E의 회귀 지표로 지목한 것도 틀렸다.** `SplashViewModelTest`는 `FakeProfileRegistrationRepository`로 **도메인 레벨을 fake**해 `:core:data`를 한 줄도 실행하지 않는다. 고쳐도 7-E를 검증하지 못한다. **실질 회귀 근거는 `UserApiServiceTest`(16건)·`UserRemoteDataSourceImplTest`(4건)·`:app:assembleQaDebug`이고 전부 green이다.**
   → **결정(2026-08-28, 사용자): 고치지 않고 미완료로 남긴다.** plan 5.0.0의 변경 대상 표에도 tasks.md에도 없는 splash 소유 파일이라 프로필 범위에서 손대지 않는다(미결 6·9번과 같은 성격).
   → **재개 조건**: splash 쪽에서 fake에 `currentSession()`을 더하는 순간 저절로 닫힌다. 그때 T097을 그대로 다시 돌리면 된다.
9. ~~**`core/data/README.md` §4의 문구가 ADR과 어긋난다.**~~ → **닫혔다(2026-08-28, 사용자 승인).** 아래 "plan 5.0.0에서 닫힌 미결 사항" 참조. **코드 쪽 정리는 T090이 그대로 맡는다.**
10. ~~**`ProfileRegistrationRepository`와 `ProfileRepository`가 같은 엔드포인트를 두 계약으로 쓴다.**~~ → **미결이 아니다(2026-08-28).** 합치지 않기로 확정됐고, 근거는 [ADR §결과](../../adr/2026-08-28-api-service-owned-per-server-tag.md)와 [D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)의 기각한 대안 4가 소유한다. 여기 다시 적지 않는다.

### plan 5.0.0에서 닫힌 미결 사항

- ~~`ErrorResponse`와 `errorCode` 취급의 ADR이 없다~~ — T085·T098이 ADR 둘로 승격했다(위 4번)
- ~~두 도메인 Repository가 같은 엔드포인트를 쓴다(미결 10)~~ — **미결로 세운 것 자체가 분류 오류였다.** 결정은 이미 [ADR §결정·§결과](../../adr/2026-08-28-api-service-owned-per-server-tag.md)·[D49 기각 대안 4](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)·[repository 계약](contracts/profile-repository-contract.md) 네 곳이 소유하고 있었고, 미결 항목은 **다섯 번째 재서술**이라 [헌법 원칙 I](../../constitution.md)에 걸렸다. 후속 작업도 재개 조건도 없으므로 참조 한 줄로 줄였다
- ~~`core/data/README.md` §4의 문구가 ADR과 어긋난다~~ — **사용자 승인으로 §4를 정리했다(2026-08-28).** 원래 결정은 "규약 문서는 이 spec에서 고치지 않는다"였으나, 세 번째 사례를 기다리지 않고 지금 닫기로 했다. **단순 치환이 아니다** — `ApiService`로 몰지 않고 **자리를 무엇을 보는지로 갈랐다**: 상태 코드만 보는 정책(§4의 원래 예시 "404를 빈 결과로")은 `DataSource`에 그대로 두고, **실패 본문(`errorCode`)을 읽어야 하는 정책만** `ApiService`로 보낸다. 두 경우가 요구하는 것이 달라 한쪽으로 몰면 멀쩡한 선례가 규약 위반이 된다
- ~~[에러 본문 ADR](../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)이 기각 사유로 든 `§5·§9` 인용이 넓다~~ — **§9를 인용에서 뺐다.** §9가 금지하는 것은 `DataSourceImpl`이 DTO를 **노출**하는 것이고, 이 건에서 Ktor 타입은 반환되지 않고 안에서만 쓰인다. 기각 사유를 지탱하는 것은 §5의 **책임 범위** 하나이며 결정은 바뀌지 않는다. [D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)의 같은 인용도 함께 정정했다
- ~~`:core:data`를 다른 feature가 동시에 넓히고 있어 이미 있는 타입을 다시 만들 수 있다(미결 3번의 확장)~~ — **실제로 일어났고 이번에 잡혔다.** `MinoResponse<T>`는 4.3.0이 잡았고(타입), `UserApiService`는 5.0.0이 잡았다(**엔드포인트 자체**). [quickstart.md §2](quickstart.md)의 재조회 절차에 "그 경로를 이미 부르는 코드를 `grep`한다"가 추가돼, 다음에는 대조 단계에서 걸린다

### plan 4.4.0에서 닫힌 미결 사항

- ~~`ValidateNicknameUseCase`의 중간 공백 케이스가 계약에는 있고 plan의 파일 표에는 없다~~ — **plan 4.4.0이 그 파일을 변경 대상 표에 올렸다**(다섯 줄 → 여섯 줄). 근거가 생겨 **T087**이 되었다. 계약이 요구한 검증이 작업으로 옮겨지지 못한 이유와 그것을 `/mino-analyze`가 잡은 경위는 [D48](research.md#d48-분석이-드러낸-문서의-빈틈--계약이-요구한-검증이-작업이-되지-못했다)에 있다
- ~~T068의 선행 작업에 `ProfileEntry`를 만드는 T072가 빠져 있다~~ — 의존성 표를 `T068은 T067·T072 이후`로 고쳤다. 표대로 착수하면 컴파일되지 않던 유일한 순서 결함이었다
- ~~신규 테스트 파일 둘이 plan의 어느 표·트리에도 없어 정당한 작업이 범위 이탈로 보인다~~ — plan 4.4.0이 `ProfileApiServiceTest.kt`·`ProfileMapperTest.kt`를 구조 트리에 올렸고, Phase 7 머리말도 "변경 파일은 두 표, 신규 파일은 구조 트리"로 바로잡혔다

### plan 4.1.0에서 닫힌 미결 사항

- ~~아바타 식별자 문자열이 잠정이다(`"person_01"`~`"person_12"`)~~ — **서버가 13개 `enum`으로 값 도메인을 확정했고**, 아바타 12종의 배경 원 색 실측으로 12색에 1대1 대응이 확인됐다. 값 표는 [API 계약 §2](contracts/profile-api-contract.md)가 소유한다. 남은 것은 `Person10` 한 칸뿐이라 위 2번으로 좁혔다
- ~~닉네임 상한·공백이 서버와 어긋나 `/mino-spec` 개정이 필요하다~~ — **[spec 2.0.0](spec.md) §5가 확정했다.** 상한은 두지 않고(16자 이상은 서버가 거절해 저장 실패로 보인다, 신설 EC-014), 공백은 불가를 유지한다(클라이언트가 더 좁아 실패가 없다). 알고 받아들이는 어긋남이라 협의 항목이 아니다
- ~~spec FR-003의 칸 단위 선택 표시가 충족되지 않는다~~ — **spec 2.0.0이 요구를 걷어냈다.** 대신 보조 수단 전달을 명시하고 TS-019를 신설했으며, 그것은 이미 구현돼 있다(T020). 검증은 T082 10번이 겸한다
- ~~spec FR-010의 "노출하되 비활성"과 구현이 다르다~~ — **spec 2.0.0이 "노출하지 않는다"로 정정했다.** 구현과 일치한다. PRD 쪽 정리만 남아 위 8번으로 옮겼다

### plan 4.0.0에서 닫힌 미결 사항

- ~~MUST 위반 1건 — `core:data` README와 repository 계약의 정면 충돌~~ — 사용자가 `ProfileEntry` 도입을 골랐다. **T072가 해소하며 원칙 V가 FAIL → PASS로 돌아온다**([D42](research.md#d42-로컬-캐시-datasource는-profileentry-dto를-반환한다))
- ~~spec FR-008의 개인방 생성과 §4의 "저장은 서버 반영을 포함한다"가 충족되지 않는다~~ — 이연이 예정대로 닫혔다. `POST /api/v1/users`가 등록과 개인방 생성을 함께 처리한다(T076·T082)
- ~~저장 실패 경로에 발화 원천이 없다~~ — HTTP 비2xx·네트워크 실패가 `convertDomainException`을 거쳐 올라온다. T083이 기기에서 처음 확인한다

### plan 3.0.0에서 닫힌 미결 사항

- ~~`core/domain/build.gradle.kts` 변경이 plan 열거 밖~~ — plan 3.0.0이 "기존 파일 변경 여섯 곳"으로 정정했다
- ~~`MinoTopNavigation`의 Figma 컴포넌트 정의 노드 ID 미확인~~ — 사용자가 링크를 제공했고, 이후 대조 대상이 화면 인스턴스 `2314-95704`로 확정됐다
- ~~앱 전체 반영(TS-011·SC-003) 확인 불가~~ — 성격이 바뀌지 않아 [quickstart.md §5](quickstart.md)가 계속 든다. 작업 목록의 미결로는 닫는다

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **Phase 1 (셋업)**: 의존성 없음 — 즉시 시작. T002는 T001 이후.
- **Phase 2 (기반)**: T002 완료에 의존한다(`:feature:profile` 작업만). 2-A·2-B·2-C·2-D는 서로 독립이고 동시에 진행할 수 있다.
  - 2-B는 2-A의 T005·T006을 컴파일 대상으로 삼는다.
  - 2-E는 2-A(T005~T008·T011)·2-C(T019·T020·T022)·2-D(T024·T025)의 산출물을 읽는다. **2-B는 읽지 않는다** — `:feature:profile`은 `:core:domain`만 의존한다(헌법 원칙 II).
- **Phase 3~5 (스토리)**: 2-E 완료에 의존한다.
- **Phase 6 (마무리)**: 목표한 스토리의 완료에 의존한다. T055는 전부 이후.
- **Phase 7 (원격 연동)**: Phase 1~6 완료에 의존한다 — 고칠 대상이 이미 서 있어야 한다. 내부 순서는 아래 별도 항목이 정한다.
- **T056 → T057**: 아이콘이 있어야 상단 바가 컴파일된다. 둘 다 `:core:design-system`이고 T057이 T022의 산출물을 고친다 — **같은 파일이라 한 전문가에게 순차 배정한다.**
- **T058·T059는 문서 작업이라 코드에 의존하지 않는다.** 다만 T058은 T001(모듈 등록)이, T059는 T018~T020(에셋·컴포넌트)이 무엇을 만들었는지 확정된 뒤에 쓸 수 있다.

### Phase 7 내부 의존성

**7-A → 7-B → 7-C → 7-D의 큰 순서는 뒤집을 수 없다.** 도메인 타입이 바뀌는 순간 `:core:data`와 `:feature:profile`이 함께 컴파일되지 않으므로, 이 물결에는 **중간에 빌드가 서지 않는 구간이 있다.** 7-C가 끝나야 앱이 다시 선다.

- **7-A**: T060 → T061(T060 필요) · T062는 T060·T061과 독립 `[P]`. T064는 T061·T062 이후(테스트와 그 안의 fake를 한 파일에서 함께 고친다), T065는 T064 이후. **T087은 이 물결과 무관하다** — 다른 파일이고 아무것에도 의존하지 않아 언제든 열 수 있다.
- **7-B**: T066·T067은 서로 독립 `[P]`이고 T060·T061 이후. **T068은 T067·T072 이후** — `ProfileEntry` ↔ `Profile` 변환을 함께 소유하므로 그 타입이 먼저 서야 한다.
  - **T072와 T073은 한 묶음이다** — 반환 타입이 바뀌어 테스트가 컴파일되지 않으므로 red를 따로 얻을 수 없다.
  - **T076은 T068·T072·T075 전부 이후**이며 이 물결에서 가장 늦은 데이터 레이어 작업이다.
  - T069·T070·T071·T074는 [폐기](#폐기된-작업)됐다 — 7-E가 대체한다.
- **7-C**: T077은 T060 이후이고 `[P]`. T078 → T079 → (T080 → T081). **T080·T081은 `ProfileViewModel.kt` 한 파일이라 반드시 순차다.**
- **7-E**: **7-C 완료 이후에 시작한다** — 고칠 대상이 서 있어야 한다. 내부는 거의 사슬이다: T088 → T089 → T090 → (T091 · T092 · T093 → T094) → **T095는 맨 마지막**. T088만 `[P]`로 먼저 열 수 있다(새 파일이라 아무것도 깨뜨리지 않는다).
  - **T095를 앞당기지 않는다** — 폐기 파일을 먼저 지우면 컴파일이 서지 않아 T089~T094의 중간 상태를 확인할 수 없다.
  - **T090은 `UserRemoteDataSourceImpl` 한 파일에서 함수 추가와 파싱 제거를 함께 한다** — 나누면 그 사이에 컴파일이 서지 않는다.
- **7-D**: T082·T083은 7-C 완료와 **실서버·익명 세션**에 의존한다(코드가 아니라 환경 의존이다). T084는 T082·T083과 독립이며 코드 작업 전부 이후. T085는 T066 이후면 언제든 쓸 수 있었다 `[P]`. **T086은 코드에 아무것도 의존하지 않는다** — 디자인에 묻는 일이라 이 물결이 시작되기 전에 던져 두면 T068 착수 시점에 답이 와 있을 수 있다.
  - **T096·T097은 7-E 완료 이후**다. T096은 기기와 실서버가, T097은 코드 전부가 필요하다.
  - **T098은 이미 끝났다** — 코드에 의존하지 않는 문서 작업이라 7-E보다 먼저 처리됐다.

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

**Phase 7의 병렬 여지는 좁다.** 레이어가 순서대로 무너지고 복구되는 물결이라 갈래를 많이 열 수 없다.

- 7-B 안에서 **T066·T067은 서로 다른 파일이라 동시에 연다.**
- **7-E는 사실상 사슬이라 갈래가 T088 하나다** — 새 테스트 파일이므로 7-C가 끝나기 전에도 써 둘 수 있다.
- **T077은 T060만 끝나면 7-B 전체와 병렬로 진행할 수 있다** — `:feature:profile`의 매핑 파일 하나라 `:core:data`와 겹치지 않는다.
- **T085·T098(ADR)은 코드 컴파일 상태와 무관하다.** 둘 다 완료됐다.
- **T087(중간 공백 테스트)도 의존이 아예 없다** — `:core:domain` 테스트 파일 하나이고 원격 물결이 건드리는 어느 파일과도 겹치지 않는다.
- **T086(디자인 확인)은 의존이 아예 없다.** 가장 먼저 던져 두는 편이 낫다 — 답이 늦어도 T068은 소거법 값으로 진행할 수 있고, 답이 오면 표 한 줄만 고친다.
- 반대로 `ProfileViewModel.kt`(T080·T081)·`ProfileLocalDataSource`(T072·T073)·`UserRemoteDataSourceImpl`(T090의 추가와 제거)은 **절대 나누지 않는다.**

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

## 병렬 실행 예시: Phase 7

```bash
# 7-B에서 열리는 갈래 — 서로 다른 파일이다:
Task: "core/data/.../network/dto/ 에 원격 DTO 3종 신설"                                 # T066
Task: "core/data/src/test/.../mapper/ProfileMapperTest.kt 작성"                          # T067 (T060·T066 이후)

# 7-B와 겹치지 않는 갈래:
Task: "feature/profile/.../main/model/ProfileAvatarMapping.kt 재작성"                    # T077 (T060 이후)

# 의존이 없어 물결 시작 전에 던져 두는 것:
Task: "Person10 ↔ brown 대응을 디자인에 확인"                                             # T086
Task: "core/domain/src/test/.../ValidateNicknameUseCaseTest.kt 에 중간 공백 케이스 추가"     # T087

# 7-E에서 유일하게 미리 열 수 있는 것 — 새 파일이라 아무것도 깨뜨리지 않는다:
Task: "core/data/src/test/.../network/UserApiServiceTest.kt 작성"                        # T088
```

**열지 말아야 할 갈래**: `ProfileViewModel.kt`(T080·T081)·`ProfileLocalDataSource`(T072·T073)·7-E 전체(T089~T095)는 순차다. 7-E는 레이어를 갈아끼우는 사슬이라 갈래를 열면 서로의 컴파일 실패를 구분할 수 없다.

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

### Phase 7 전략 — 한 사람이 잇는다

**이 물결은 나누지 않는 편이 낫다.** 7-A가 도메인 타입을 바꾸는 순간 두 모듈이 함께 깨지고, 그 상태에서 여러 사람이 각자의 모듈을 고치면 서로의 컴파일 실패를 구분할 수 없다. 순서는 이렇다.

1. **7-A를 끝까지** — `:core:domain:test`가 통과할 때까지. 다른 모듈이 깨져 있는 것은 정상이다
2. **7-B를 끝까지** — `:core:data:test`가 통과하면 **서버 없이도 원격 경로가 검증된 것이다.** 이 물결의 무게중심이자 가장 값어치 있는 체크포인트다
3. **7-C** — 앱이 다시 선다
4. **7-E** — `user` 태그 통합. ~~`:feature:splash` 테스트가 함께 통과해야 끝난 것이다~~ → **이 지표 지목은 틀렸다**(미결 11번). `SplashViewModelTest`는 도메인 레벨을 fake해 `:core:data`를 실행하지 않는다. 실질 지표는 `UserApiServiceTest`·`UserRemoteDataSourceImplTest`·`:app:assembleQaDebug`다
5. **7-D** — 실서버·익명 세션이 있는 환경에서 기기 확인. T083의 실측 결과를 API 계약에 되먹인다

### 7-E 전략 — 걷어내는 순서가 전부다

**7-E는 새로 만드는 물결이 아니라 갈아끼우는 물결이다.** 두 벌이 공존하는 중간 상태를 지나야 하고, 그 구간에서 **컴파일은 계속 서 있어야 한다** — 그래야 무엇이 깨졌는지 알 수 있다. 그래서 순서가 곧 전략이다.

1. **T088을 먼저 쓴다** — 새 파일이라 아무것도 깨뜨리지 않고, 7-C가 끝나기 전에 써 둬도 된다. 이 시점에 폐기될 `ProfileApiServiceTest`는 아직 그대로 돌고 있다
2. **T089 → T090** — `UserApiService`가 네 함수를 갖고 `UserRemoteDataSource`가 그것을 위임한다. **`ProfileApiService`는 아직 살아 있다** — 두 벌이 공존하지만 둘 다 컴파일된다
3. **T091 → T092 → T093 → T094** — 소비 지점을 새 쪽으로 옮긴다. 이 단계가 끝나면 `ProfileApiService`·`ProfileRemoteDataSource`를 **아무도 부르지 않는다**
4. **T095로 지운다** — 참조가 0인 것을 `grep`으로 확인한 뒤에 지운다. 여기서 처음으로 파일이 사라진다

**되돌아가는 지점은 3번이다.** 3번까지 끝냈는데 `UserApiServiceTest`의 401·본문 독립성 케이스가 깨지면 T089·T090에서 진입 판정의 성질을 잃은 것이다 — **`hasProfile()`이 성공 본문을 역직렬화하고 있지 않은지** 가장 먼저 본다([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)). 4번을 지나면 되돌리기가 훨씬 비싸진다.

**시작 전에 두 가지를 먼저 한다.** ① [quickstart.md §2](quickstart.md)의 재조회 — **API 문서와 `:core:data` 트리를 함께 본다.** 값 도메인이 3시간 만에 바뀐 적이 있고(미결 3번), 다른 feature가 넓혀 둔 `:core:data`를 못 보면 이미 있는 타입을 다시 만들게 된다([D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)). ② **T086을 디자인에 던져 둔다** — 답을 기다리지 않고 진행하지만, 일찍 물을수록 표를 고칠 일이 줄어든다.

여유가 있으면 **T086(디자인 확인)·T087(중간 공백 테스트)을 다른 사람에게 떼어 준다.** 이 물결의 어느 파일과도 겹치지 않는다. T085·T098(ADR 둘)은 이미 끝났다.

---

## 참고 사항

- 규약·설계 내용을 이 문서에 옮겨 적지 않았다. 각 작업 줄의 링크와 섹션 번호가 원문을 가리킨다(헌법 원칙 I).
- 커밋 단위는 [`commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다. 모듈 경계가 커밋 경계와 대체로 일치한다.
