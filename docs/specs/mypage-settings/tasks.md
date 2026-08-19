# 작업 목록: 마이페이지 & 환경설정

**대상 스펙 경로**: `docs/specs/mypage-settings`

**기준 plan 버전**: 3.0.0

**최초 작성일**: 2026-08-19

**최종 수정일**: 2026-08-19

**사전 조건**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md 모두 사용.

**테스트**: spec.md가 테스트 작업을 명시적으로 요청하지 않아 테스트 작업을 생성하지 않는다. 검증은 Phase 마무리의 `quickstart.md` 수동 시나리오와 빌드 확인으로 대신한다.

**구성 방식**: 각 스토리를 독립적으로 구현하고 테스트할 수 있도록 작업을 사용자 스토리별로 묶는다. spec.md의 세 유저 플로우가 화면 하나(`MyPageMain`)를 공유하므로, Foundational 단계에서 "빈 골격이 동작하는" 워킹 스켈레톤을 먼저 만들고 각 스토리가 그 골격 안의 자기 구역을 채운다.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. 한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: US1(마이페이지 조회·프로필 수정) · US2(알림·위치 권한) · US3(서비스 정보)

## 경로 규칙

Android 다중 모듈. `plan.md` §프로젝트 구조의 경로를 그대로 따른다 — `feature/mypage/`, `core/domain/`, `core/data/`, `core/design-system/`, `feature/main/`, `app/`.

---

## Phase 1: 셋업

**목적**: 모듈·빌드 설정 초기화

- [ ] T001 `settings.gradle.kts`에 `include(":feature:mypage")` 추가, `feature/mypage/build.gradle.kts` 생성(`alias(mino.android.feature)` + `namespace`만, `feature-module.md` 골격)
- [ ] T002 [P] `gradle/libs.versions.toml`에 `firebase-messaging` 의존성 카탈로그 추가(research.md D10)
- [ ] T003 [P] `app/src/main/AndroidManifest.xml`에 `POST_NOTIFICATIONS`·`ACCESS_FINE_LOCATION`·`ACCESS_COARSE_LOCATION` 권한 선언 추가

---

## Phase 2: 기반 작업 (공통 인프라)

**목적**: 세 유저 스토리가 공유하는 화면(`MyPageMain`)의 워킹 스켈레톤을 만든다 — 섹션은 비어 있어도 탭 진입·프로필 화면 왕복 네비게이션은 동작해야 한다.

**⚠️ 중요**: 이 단계 전체의 완료를 기다리지 않는다. 각 사용자 스토리 작업은 자신이 실제로 읽거나 컴파일 대상으로 삼는 기반 작업(T004~T014 중 개별 항목)이 끝나는 대로 시작할 수 있다 — 아래 [의존성 및 실행 순서](#의존성-및-실행-순서) 참조

- [ ] T004 [P] `feature/mypage/.../main/vm/MyPageUiState.kt`·`MyPageIntent.kt`·`MyPageSideEffect.kt`에 `contracts/mypage-main-contract.md`의 전체 계약 선언(필드·sealed 분기 전부, 처리 로직은 아직 없음)
- [ ] T005 `feature/mypage/.../main/vm/MyPageViewModel.kt`에 `MviContainer` 위임 골격 생성(T004 의존)
- [ ] T006 `feature/mypage/.../main/screen/MyPageRoute.kt`·`MyPageScreen.kt`에 프로필 요약·앱 설정·서비스 정보 3섹션 레이아웃 골격(내용은 각 스토리가 채움, T005 의존)
- [ ] T007 [P] `feature/mypage/.../profile/vm/ProfileUiState.kt`·`ProfileIntent.kt`·`ProfileSideEffect.kt`에 `contracts/profile-setup-contract.md`의 전체 계약 선언
- [ ] T008 `feature/mypage/.../profile/vm/ProfileViewModel.kt` 골격 생성(T007 의존)
- [ ] T009 `feature/mypage/.../profile/screen/ProfileRoute.kt`·`ProfileScreen.kt` 골격(T008 의존)
- [ ] T010 `feature/mypage/.../MyPageNavigation.kt`에 `MyPageGraph`(public)·`MyPageMain`·`MyPageProfile` Route 정의 + `mypageGraph()` 등록 함수 — `screen<MyPageMain>{ MyPageRoute(onNavigateToProfileSetup = { navController.navigate(MyPageProfile) }) }`, `screen<MyPageProfile>{ entry -> ProfileRoute(onBack = { navController.popBackStackIfResumed(entry) }) }`(`feature-navigation.md` §2 — `navigate` 호출은 그래프 등록 람다가 갖는다, T006·T009 의존)
- [ ] T011 [P] `feature/main/.../MainDestinations.kt`에서 `MyPage` data object 제거(모듈 소유로 이동)
- [ ] T012 [P] `feature/main/build.gradle.kts`에 `implementation(project(":feature:mypage"))` 추가
- [ ] T013 `feature/main/.../MainTab.kt`의 `MY_PAGE` route를 `MyPageGraph`로 교체(T011·T012 의존)
- [ ] T014 `feature/main/.../MainNavHost.kt`의 `MyPage` placeholder `screen<>` 등록을 `mypageGraph(...)` 호출로 교체(T010·T013 의존)

**체크포인트**: 마이페이지 탭 진입 시 빈 3섹션 레이아웃이 보이고, (아직 미연결이지만) 프로필 화면으로의 네비게이션 배선 자체는 컴파일된다. US1은 T005·T008(각 ViewModel 골격)이 끝나는 대로, US2·US3는 T005가 끝나는 대로 시작할 수 있다 — 전체 완료를 기다릴 필요 없다.

---

## Phase 3: 사용자 스토리 1 — 마이페이지 조회 및 프로필 수정

**목표**: 마이페이지 진입 시 프로필이 표시되고, 편집 화면에서 닉네임·아바타를 바꿔 저장하면 즉시 반영된다.

**독립 테스트**: spec TS-001(부분 — 프로필 섹션만 실제 데이터)·TS-002·TS-003, EC-001·EC-006. 다른 두 섹션은 아직 골격이어도 무방하다.

- [ ] T015 [P] [US1] `core/domain/model/Profile.kt`에 `Profile(nickname, avatarId: Int)` 모델 생성(data-model.md §1)
- [ ] T016 [P] [US1] `core/domain/repository/ProfileRepository.kt` 인터페이스 생성(`getProfile(): Profile`, `saveProfile(): Profile`)
- [ ] T017 [P] [US1] `core/data/network/dto/response/UserResponse.kt`에 `UserResponse`·`AvatarDto` DTO 생성(swagger `User`·`Avatar` 스키마)
- [ ] T018 [US1] `core/data/network/service/UserApiService.kt`에 `getMe()`·`patchMe()` 구현(T017 의존)
- [ ] T019 [US1] `core/data/datasource/UserRemoteDataSource.kt`(+`Impl`)에 `getMe()`·`patchMe()` 추가(T018 의존)
- [ ] T020 [US1] `core/data/repository/mapper/UserMapper.kt`에 `UserResponse.toDomain(): Profile` 작성(T015·T017 의존)
- [ ] T021 [US1] `core/data/repository/ProfileRepositoryImpl.kt` + `di/ProfileRepositoryModule.kt` 생성(T016·T019·T020 의존)
- [ ] T022 [US1] `MyPageViewModel.kt`의 재조회 로직에 `ProfileRepository.getProfile()` 반영 추가(T005·T021 의존)
- [ ] T023 [US1] `MyPageScreen.kt`의 프로필 요약 섹션에 닉네임·아바타·[연필] 아이콘 실제 UI 구현(T006·T022 의존)
- [ ] T024 [US1] `OnEditProfileClick` → `NavigateToProfileSetup` SideEffect를 `MyPageRoute`가 수집해 T010에서 받은 `onNavigateToProfileSetup` 콜백을 호출하도록 배선(`MyPageRoute`는 `navController`를 직접 참조하지 않는다, T010·T022 의존)
- [ ] T025 [US1] `ProfileViewModel.kt`에 초기 프로필 로드(`getProfile()`) + 닉네임 2~15자 검증 + 아바타 선택 여부로 `isSaveEnabled` 계산(EC-001, UX-004) 구현(T008·T021 의존)
- [ ] T026 [P] [US1] `feature/mypage/.../profile/component/AvatarGrid.kt`에 아바타 12종 그리드 컴포저블 작성 — `avatarId` 정수 매핑은 `[TBD]`(`contracts/profile-setup-contract.md` 참조), 확정 전까지 0~11 임시 값 사용
- [ ] T027 [US1] `ProfileScreen.kt`에 닉네임 입력·아바타 그리드·[지우기](`OnClearClick`, EC-006)·[저장] 버튼 실제 UI 구현(T009·T025·T026 의존)
- [ ] T028 [US1] 저장 성공 시 `NavigateBack` → `popBackStackIfResumed` 배선, `MyPageMain` 복귀 시 재조회로 최신 프로필 반영 확인(T022·T027 의존)

**체크포인트**: US1 완결 — 프로필 조회·수정이 독립적으로 동작한다.

---

## Phase 4: 사용자 스토리 2 — 알림/위치 권한 켜기·끄기

**목표**: 알림·위치 스위치가 OS 권한을 요청·반영하고, 이미 허용된 상태에서 끄는 동작이 spec §5 확정 사항대로 동작한다.

**독립 테스트**: spec TS-005~008·011·012, EC-003·EC-007.

- [ ] T029 [P] [US2] `core/domain/model/PermissionType.kt`에 `enum class PermissionType { NOTIFICATION, LOCATION }` 생성
- [ ] T030 [P] [US2] `core/domain/repository/PermissionRepository.kt` 인터페이스 생성
- [ ] T031 [P] [US2] `core/domain/repository/AppSettingsRepository.kt` 인터페이스 생성
- [ ] T032 [P] [US2] `core/domain/repository/PushNotificationRepository.kt` 인터페이스 생성(`syncPushToken()`)
- [ ] T033 [P] [US2] `core/data/datasource/AppSettingsLocalDataSource.kt`(+`Impl`) 생성 — `notification_delivery_enabled` 키(공유 DataStore)
- [ ] T034 [P] [US2] `core/data/datasource/PermissionLocalDataSource.kt`(+`Impl`) 생성 — `notification_permission_requested`·`location_permission_requested` 키
- [ ] T035 [US2] `core/data/device/PermissionRepositoryImpl.kt` + `di/PermissionRepositoryModule.kt` 생성 — `ContextCompat.checkSelfPermission` 래퍼(T030·T034 의존)
- [ ] T036 [US2] `core/data/repository/AppSettingsRepositoryImpl.kt` + `di/AppSettingsRepositoryModule.kt` 생성(T031·T033 의존)
- [ ] T037 [P] [US2] `core/data/device/PushTokenProvider.kt`(+`Impl`) + `di/PushTokenProviderModule.kt` 생성 — FCM SDK 래퍼(T002 의존)
- [ ] T038 [US2] `UserApiService.kt`에 `putPushToken(token, platform)` 메서드 추가(T018 의존)
- [ ] T039 [US2] `UserRemoteDataSource.kt`(+`Impl`)에 `putPushToken` 추가(T019·T038 의존)
- [ ] T040 [US2] `core/data/repository/PushNotificationRepositoryImpl.kt` + `di/PushNotificationRepositoryModule.kt` 생성(T032·T037·T039 의존)
- [ ] T041 [P] [US2] `core/design-system/component/switch/MinoSwitch.kt`·`MinoSwitchDefaults.kt`·`MinoSwitchColors.kt`·`token/SwitchTokens.kt` 생성(M3 패턴)
- [ ] T042 [P] [US2] `core/design-system/component/dialog/MinoDialog.kt`·`MinoDialogDefaults.kt`·`token/DialogTokens.kt` 생성(M3 패턴)
- [ ] T043 [US2] `MyPageViewModel.kt`의 재조회 로직에 `PermissionRepository`·`AppSettingsRepository` 결과 반영(`isNotificationSwitchOn`·`isLocationSwitchOn` 계산) 추가(T029·T030·T031·T035·T036·T022 의존)
- [ ] T044 [US2] `OnNotificationSwitchClick` 분기 로직 구현 — `contracts/mypage-main-contract.md`의 분기표 그대로(T043 의존)
- [ ] T045 [US2] `OnLocationSwitchClick` 분기 로직 구현(T043 의존)
- [ ] T046 [US2] `OnNotificationPermissionResult`/`OnLocationPermissionResult` 콜백 처리 — 알림 허용 시 `PushNotificationRepository.syncPushToken()` 호출(T040·T044·T045 의존)
- [ ] T047 [US2] `MyPageRoute.kt`에 권한 런처 배선 — `rememberLauncherForActivityResult`(단일/`RequestMultiplePermissions`), `shouldShowRequestPermissionRationale` 계산해 Intent에 실어 전달(research.md D3, T006·T044·T045 의존)
- [ ] T048 [US2] `OpenAppSettings` SideEffect 처리 — `core/common/android/extension/Context.kt`에 `Context.openAppSettings()` 확장 추가(T050이 먼저 끝났으면 같은 파일에 이어 추가) 후 `MyPageRoute.kt`에서 호출
- [ ] T049 [US2] `MyPageScreen.kt`에 알림·위치 스위치 행(`MinoSwitch`) + 권한 재요청 불가 안내(`MinoDialog`, EC-003·EC-007) 실제 UI 구현(T023 이후, T041·T042·T046 의존)

**체크포인트**: US1과 US2 모두 독립적으로 동작한다.

---

## Phase 5: 사용자 스토리 3 — 서비스 정보 진입

**목표**: 약관 및 동의·앱 리뷰 진입점이 각각 노션 문서·Play 스토어로 연결된다.

**독립 테스트**: spec TS-009·TS-010, EC-004.

- [ ] T050 [US3] `core/common/android/extension/Context.kt`에 `Context.openUrl(url)`·`Context.openPlayStoreListing()` 확장 추가(research.md D5) — `T048`(US2)과 같은 파일을 만드므로 먼저 끝난 쪽이 파일을 만들고 나머지가 이어 추가한다(병렬 불가)
- [ ] T051 [US3] `MyPageViewModel.kt`에 `OnTermsClick`(`OpenUrl`)·`OnAppReviewClick`(`OpenPlayStoreListing`) 처리 추가(T005 의존)
- [ ] T052 [US3] `MyPageRoute.kt`에서 `OpenUrl`·`OpenPlayStoreListing` SideEffect 수집·실행, Play Store 미설치 시 웹 폴백(EC-004) 처리(T050·T051 의존)
- [ ] T053 [US3] `MyPageScreen.kt`에 서비스 정보 섹션(약관 및 동의·앱 리뷰 남기기) 실제 UI 구현(T006 의존)

**체크포인트**: 세 스토리 모두 독립적으로 동작한다 — spec FR-001의 3섹션이 전부 실제 데이터로 채워진다.

---

## Phase 6: 마무리 및 공통 관심사

- [ ] T054 [P] `quickstart.md`의 실행 시나리오 표 전체를 에뮬레이터/실기기에서 수동 검증
- [ ] T055 `./gradlew :app:assembleQaDebug` 빌드 확인(헌법 §품질 게이트 최소선)

---

## 미결 사항 (구현 착수 전 확인 필요)

- **아바타 `avatarId` 정수 매핑**: 12종 아바타 각각의 실제 `Int` 값이 아직 없다(`contracts/profile-setup-contract.md` `[TBD]`). T026은 임시값(0~11)으로 진행하고, 확정되면 값만 교체한다.
- **실서버 baseUrl·Bearer 인증**: `HttpClient`가 아직 `https://api.github.com/` 임시 baseUrl이고 인증 헤더 주입이 없다(research.md D11). 이 feature 범위 밖 선행 의존성이라 이 문서에 별도 작업으로 넣지 않았다 — T018·T019·T038·T039를 실제로 검증하려면 그 전에 baseUrl·인증이 배선돼 있어야 한다.

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **셋업 (Phase 1)**: 의존성 없음 — 즉시 시작 가능
- **기반 작업 (Phase 2)**: 셋업 완료에 의존. 전체 완료를 기다리지 않는다 — 아래 "사용자 스토리 간 의존성"이 각 스토리가 실제로 쓰는 항목만 짚는다
- **사용자 스토리 (Phase 3~5)**: 각 작업은 자신이 실제로 읽거나 컴파일 대상으로 삼는 기반 작업에만 의존한다(작업 줄의 "T0xx 의존" 표기가 단일 출처). US1·US2·US3는 서로 다른 도메인/데이터 파일을 건드리므로 대부분 병렬 진행 가능하나, US2의 T038·T039가 US1의 T018·T019 파일을 확장하고, US2의 T048과 US3의 T050이 같은 신규 파일(`core/common/android/extension/Context.kt`)을 만들므로 그 두 지점만 순서를 지킨다.
- **마무리 (Phase 6)**: 목표한 모든 사용자 스토리의 완료에 의존

### 사용자 스토리 간 의존성

- **US1**: `T005`(`MyPageViewModel` 골격)·`T008`(`ProfileViewModel` 골격)이 끝나는 대로 시작 가능 — 다른 스토리에 의존하지 않음
- **US2**: `T005`가 끝나는 대로 시작 가능하나, `UserApiService`·`UserRemoteDataSource`에 `putPushToken`을 추가하는 T038·T039는 US1의 T018·T019(같은 파일)가 먼저 존재해야 한다
- **US3**: `T005`가 끝나는 대로 시작 가능 — 다른 스토리에 의존하지 않음. 단 `T050`은 US2의 `T048`과 같은 파일(`core/common/android/extension/Context.kt`)을 만들므로 둘 중 하나가 먼저 끝나야 한다

### 병렬 처리 기회

- Phase 1의 [P] 작업(T002·T003) 병렬 실행 가능
- Phase 2에서 `MyPageMain` 계열(T004~T006)과 `MyPageProfile` 계열(T007~T009)은 서로 다른 파일이라 병렬 가능, 이후 T010이 둘을 묶는다
- US1·US2·US3는 각자 필요한 기반 작업(T005 등)이 끝나는 대로 팀 인원이 되는 대로 병렬 진행 가능 — Phase 2 전체 완료를 기다리지 않는다(단 US2의 T038·T039는 US1의 T018·T019를, US3의 T050은 US2의 T048과의 순서를 지킴)
- 각 스토리 내부에서 `[P]` 표시된 도메인 모델·인터페이스·DTO 생성은 병렬 실행 가능

---

## 병렬 실행 예시: 사용자 스토리 2

```bash
# 도메인 인터페이스 4종을 함께 생성:
Task: "core/domain/model/PermissionType.kt에 PermissionType enum 생성"
Task: "core/domain/repository/PermissionRepository.kt 인터페이스 생성"
Task: "core/domain/repository/AppSettingsRepository.kt 인터페이스 생성"
Task: "core/domain/repository/PushNotificationRepository.kt 인터페이스 생성"

# 로컬 DataSource 2종을 함께 생성:
Task: "core/data/datasource/AppSettingsLocalDataSource.kt(+Impl) 생성"
Task: "core/data/datasource/PermissionLocalDataSource.kt(+Impl) 생성"

# design-system 컴포넌트 2종을 함께 생성:
Task: "core/design-system/component/switch/MinoSwitch.kt 생성"
Task: "core/design-system/component/dialog/MinoDialog.kt 생성"
```

---

## 구현 전략

### MVP 우선 (US1만)

1. Phase 1: 셋업 완료
2. Phase 2: US1이 쓰는 기반 작업(T004~T010) 완료 — 워킹 스켈레톤
3. Phase 3: US1 완료
4. **중단하고 검증**: 프로필 조회·수정만으로 quickstart.md의 관련 행 수동 검증
5. 준비되면 배포/데모

### 점진적 전달

1. 셋업 완료 → 기반 작업은 끝나는 것부터 각 스토리에 공급(빈 3섹션 워킹 스켈레톤은 T004~T014 전부 끝났을 때)
2. US1 추가 → 독립 테스트 → 배포/데모(MVP)
3. US2 추가 → 독립 테스트 → 배포/데모
4. US3 추가 → 독립 테스트 → 배포/데모 — 이 시점에 spec FR-001(3섹션 전부)이 완전히 충족된다

### 팀 병렬 전략

1. 팀이 함께 Phase 1과 T004~T010(`MyPageMain`·`MyPageProfile` 골격)을 완료
2. 각자가 쓰는 기반 산출물이 나오는 대로: 개발자 A → US1, 개발자 B → US2(단 T038·T039는 US1의 T018·T019 대기, T048은 US3의 T050과 순서 조율), 개발자 C → US3
3. 각 스토리는 독립적으로 완료되고 이미 존재하는 `MyPageScreen`·`MyPageViewModel`에 통합됨

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 동일 파일 충돌, 독립성을 깨뜨리는 스토리 간 의존성
- 커밋 단위는 [`docs/conventions/commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다
