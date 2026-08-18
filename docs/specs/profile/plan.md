# 구현 계획: 프로필 설정 및 수정 (Profile Setup & Edit)

**대상 스펙 경로**: `docs/specs/profile`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 1.1.0

**최초 작성일**: 2026-08-18

**최종 수정일**: 2026-08-18

**버전**: 1.1.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

온보딩과 마이페이지가 공유하는 프로필 설정 화면을 **진입형 feature 모듈 `:feature:profile`** 로 신설한다. 두 진입점이 서로 다른 feature가 될 예정이고 feature 간 직접 의존이 금지되어 있으므로, 전환은 `:core:navigation`의 `ProfileLauncher` 계약 한 겹으로 받고 진입점은 Intent extra로, 저장 완료는 `RESULT_OK`로 주고받는다 — 다음 목적지는 호출자가 고른다.

데이터의 원천은 서버다. `:core:domain`의 `Profile` 모델과 `ProfileRepository`(`observeProfile` Flow + `register`·`update`·`refresh`)로 경계를 긋고, `:core:data`가 꾹 API의 유저 엔드포인트 3종을 소비하며 공유 Preferences DataStore를 캐시로 쓴다. 저장은 캐시에 프로필이 있으면 `PATCH /users/me`, 없으면 `POST /users`(등록 — 서버가 개인방을 함께 만든다)로 갈리고, 캐시는 원격 성공 뒤에만 갱신된다. 실서버·인증이 아직 없으므로 **qa flavor는 Ktor `MockEngine`으로 목 응답을 낸다** — 엔진만 소스셋으로 갈라 직렬화·에러 매핑·캐시 갱신까지 실제 경로를 그대로 검증한다. 닉네임 규칙(앞뒤 공백 제외, 한글 음절·영문만 2자 이상, 클라이언트 상한 없음)은 화면이 아니라 `ValidateNicknameUseCase`·`SaveProfileUseCase`가 소유한다.

`:core:design-system`에는 아바타 12종(`MinoProfileAvatar` + `MinoProfileAvatarImage`)과 상단 바(`MinoTopNavigation`)를 신설한다. 아바타 소유 위치는 [방 대표 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)이 세운 경계를 그대로 따른다. 설계 근거는 [`research.md`](research.md), 데이터·상태는 [`data-model.md`](data-model.md), API 표면은 [`contracts/`](contracts/), 검증 절차는 [`quickstart.md`](quickstart.md)에 있다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin 2.2.10, Jetpack Compose (버전 카탈로그 그대로)

**주요 의존성**: Hilt 2.59.2, Navigation Compose, Ktor 3.3.0, `androidx-datastore-preferences` 1.2.1 — 모두 기존 카탈로그에 있고 **버전 카탈로그에 새 항목을 추가하지 않는다**. `ktor-client-mock`은 이미 카탈로그에 있으며 지금까지 테스트 전용이던 것을 `:core:data`의 qa 소스셋 의존으로 승격한다(research.md D15)

**저장소**: 원천은 서버(`POST /api/v1/users` · `GET`/`PATCH /api/v1/users/me`), 캐시는 공유 `DataStore<Preferences>`(`core:data/storage/DataStoreModule`) — 닉네임·아바타 id 2개 키. 캐시는 원격 성공 뒤에만 갱신한다(research.md D13)

**외부 계약**: 꾹 API 초안(`openapi 3.0.3`, `0.1.0-draft`). 소비 형태와 목 동작의 스냅숏은 [`contracts/profile-api-contract.md`](contracts/profile-api-contract.md)가 보관한다. 인증(Bearer)은 문서가 미확정으로 남긴 지점이라 이번 범위에서 배선하지 않는다(research.md D20)

**테스트**: JUnit4 + Fake 구현체 JVM 단위 테스트, 데이터 레이어는 `MockEngine` 기반 테스트(기존 `DomainExceptionMappingTest` 방식). Compose UI 테스트는 저장소에 선례가 없어 도입하지 않는다(research.md D12·D21)

**대상 플랫폼**: Android minSdk 29 / targetSdk 36

**프로젝트 유형**: mobile-app, 다중 Gradle 모듈 (신규 모듈 `:feature:profile` 1개)

**성능 목표**: spec SC-001(진입 후 60초 이내 저장 완료)·SC-005(아바타 선택 즉시 썸네일 반영)은 UX 목표이며 별도 계측 인프라를 두지 않는다. SC-003(앱 전체 즉시 반영)은 `observeProfile()` 구독으로 구조적으로 만족시킨다

**제약 조건**: 온보딩 진입에서는 화면을 벗어날 수 없다(FR-010, EC-001). 저장 실패 시 입력값을 보존하고 캐시를 건드리지 않는다(FR-012, SC-006). 프로필 표기는 앱 전체에서 하나다(FR-007). 오프라인 저장·나중에 동기화는 다루지 않는다(spec §4). qa 빌드는 목 응답만 받으므로 실서버 연동은 이 계획의 검증 대상이 아니다

**규모/범위**: 화면 1개, 신규 도메인 모델 1·Repository 1·UseCase 2, 신규 원격 계층(DTO·ApiService·RemoteDataSource·Mapper) 1벌, 신규 design-system 컴포넌트 2, 신규 전환 계약 1, 목 엔진 소스셋 2

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| 원칙/기준 | 판정 | 근거 |
|---|---|---|
| I. 단일 출처 문서화 | PASS | 규약을 링크로만 지목하고 본문을 옮기지 않았다. 화면·계약 정의는 문서마다 소유자를 하나로 두고 서로 참조한다 |
| II. 레이어 경계와 의존 방향 | PASS | `:feature:profile`은 `:core:domain`만 알고 `:core:data`를 의존하지 않는다. feature 간 결합은 `:core:navigation`의 `ProfileLauncher` 한 겹뿐이다(research.md D1). `:core:design-system`은 도메인을 모른다 — 아바타 enum이 서버 식별자를 갖지 않는다(D4). DI 바인딩은 구현을 소유한 모듈의 `di/`에 둔다 |
| III. 결정과 실패는 기록으로 남는다 | PASS, 승격 대상 2건 | research.md D1~D21은 이 feature 로컬 결정이다. 뒤집힌 D3은 지우지 않고 취소선 이력으로 남기고 D13이 대체했다. D13(프로필의 원천과 캐시)·D15(목 엔진을 flavor 소스셋으로 가르는 방식)는 다른 feature도 구속하므로 완료 보고에서 ADR 승격을 제안한다. D4(아바타 12종 소유)도 승격 후보로 유지한다 |
| IV. 명세가 구현에 선행한다 | PASS, 어긋남 1건 보고 | 모든 설계 항목이 spec의 FR-·UX-·EC-·SC- 항목에서 도출됐다. spec이 다른 문서로 넘긴 범위(개인방 생성 규칙·온보딩 나머지 스텝)를 끌어오지 않았다. 서버 `Nickname` 규칙(공백 허용·15자 상한)이 spec §5의 확정(공백 불가·상한 없음)과 어긋나지만, 설계로 봉합하지 않고 클라이언트는 spec을 따르며 어긋난 요청은 저장 실패로 드러낸다(research.md D19) |
| V. 컨벤션은 게이트다 | PASS | 브랜치는 `feature/159-profile-setup/plan`으로 이미 분기돼 있다. 새 컴포넌트는 M3 패턴(Defaults·token)을 따르고, 에러 소비는 `launchSafely`·`runCatchingDomain`·`onDomainFailure`만 쓴다 |
| 기술 표준 — 디자인 토큰·실측 판정 | PASS(조건부) | `MinoProfileAvatarImage`·`MinoTopNavigation`의 값 판정은 구현 단계에서 Figma 원본과 대조해 정한다([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)). 이 plan은 컴포넌트의 존재·역할·공개 API까지만 결정했다 |
| 기술 표준 — 빌드 검증 | PASS | 확인 최소선은 `./gradlew :app:assembleQaDebug`이며 quickstart가 그것을 절차로 담고 있다 |

**Phase 1 설계 후 재평가(1.1.0)**: 원격 계층이 들어오면서 `:core:data`의 표면이 늘었지만 경계는 그대로다 — `:feature:profile`은 여전히 `:core:domain`만 알고, DTO는 데이터 레이어 밖으로 나가지 않는다. 1.0.0이 남긴 "spec 가정과 구현의 시점 차이"는 원격 연동을 실제로 설계하면서 닫혔다. 남은 판정 변화는 원칙 IV의 어긋남 1건(닉네임 규칙)뿐이며 설계로 봉합하지 않고 보고한다. `Complexity Tracking`에 올릴 항목은 없다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/profile/
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물
├── data-model.md        # Phase 1 산출물
├── quickstart.md        # Phase 1 산출물
├── contracts/           # Phase 1 산출물
│   ├── profile-screen-contract.md
│   ├── profile-launcher-contract.md
│   ├── profile-repository-contract.md
│   ├── profile-api-contract.md      # 1.1.0에서 추가 — 서버 계약 스냅숏 + 목 계약
│   └── design-system-contract.md
└── tasks.md             # /mino-task 산출물 (이 실행이 만들지 않음)
```

### 소스 코드 (Repository Root 기준)

모바일(Android) 다중 모듈 구조를 그대로 따른다. **신규·변경 모듈만** 적는다.

```text
feature/profile/                                     # 신규 — 진입형 feature
├── build.gradle.kts                                 # alias(mino.android.feature) + namespace만
└── src/main/java/team/mino/feature/profile/
    ├── ProfileActivity.kt                           # public — extra 해석·셸 호스팅·결과 반환
    ├── ProfileDestinations.kt                       # internal Route(ProfileMain)
    ├── ProfileShell.kt                              # MinoScaffold + navController + TrackScreenViews
    ├── ProfileNavHost.kt                            # screen<ProfileMain> 등록
    ├── di/
    │   ├── ProfileLauncherImpl.kt
    │   └── ProfileNavigationModule.kt
    └── main/
        ├── screen/     ProfileRoute.kt · ProfileScreen.kt
        ├── vm/         ProfileViewModel · ProfileUiState · ProfileIntent · ProfileSideEffect
        ├── model/      ProfileEntryPoint.kt         # 진입 인자이자 UiState 구성요소 → model/ (feature-module.md 2장)
        └── component/  아바타 그리드 · 상단 썸네일 조립 조각

core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/
├── ProfileLauncher.kt                               # 신규 — 전환 계약
└── ExtraTag.kt                                      # 변경 — EXTRA_PROFILE_ENTRY_POINT 및 값 상수 추가

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/Profile.kt                                 # 신규
├── repository/ProfileRepository.kt                  # 신규
├── repository/DeviceRepository.kt                   # 변경 — ensureDeviceId()가 확보한 id를 반환
└── usecase/ValidateNicknameUseCase.kt · SaveProfileUseCase.kt   # 신규 (EnsureDeviceIdUseCase는 반환 타입만 따라 바뀐다)

core/data/src/main/java/team/mino/core/data/
├── network/dto/request/RegisterUserRequest.kt · UpdateProfileRequest.kt          # 신규
├── network/dto/response/UserResponse.kt · AvatarResponse.kt · ApiResponse.kt     # 신규 — ApiResponse는 { data } 공용 봉투
├── network/service/UserApiService.kt                                             # 신규
├── network/di/NetworkModule.kt                                                   # 변경 — HttpClientEngine을 주입받는 형태로
├── datasource/ProfileRemoteDataSource.kt(+Impl) · ProfileLocalDataSource.kt(+Impl) · di/ProfileDataSourceModule.kt   # 신규
└── repository/ProfileRepositoryImpl.kt · mapper/ProfileMapper.kt · di/ProfileRepositoryModule.kt                     # 신규

core/data/src/qa/java/team/mino/core/data/network/di/EngineModule.kt      # 신규 — MockEngine 제공
core/data/src/qa/java/team/mino/core/data/network/mock/                   # 신규 — 유저 엔드포인트 목 핸들러(상태·지연·강제 실패)
core/data/src/prod/java/team/mino/core/data/network/di/EngineModule.kt    # 신규 — OkHttp 제공
core/data/build.gradle.kts                                                # 변경 — qaImplementation(libs.ktor.client.mock)

core/design-system/src/main/java/team/mino/core/designsystem/component/
├── profileavatar/  MinoProfileAvatar.kt · MinoProfileAvatarImage.kt · MinoProfileAvatarDefaults.kt · token/ProfileAvatarTokens.kt   # 신규
└── topnavigation/  MinoTopNavigation.kt · MinoTopNavigationDefaults.kt · token/TopNavigationTokens.kt                              # 신규
core/design-system/src/main/res/                     # 변경 — 아바타 12종 에셋(벡터 또는 밀도별 WebP)

app/build.gradle.kts                                 # 변경 — implementation(project(":feature:profile"))
settings.gradle.kts                                  # 변경 — include(":feature:profile")
```

**데이터 흐름 결정(1.1.0)**: 저장은 `ProfileViewModel → SaveProfileUseCase → ProfileRepository → ProfileRemoteDataSource(서버) → 캐시` 한 방향이다. 등록/수정 갈래는 UseCase가 캐시 상태로 고르고 화면은 어느 쪽인지 모른다. 목은 이 흐름의 **가장 바깥 한 겹**(HTTP 엔진)만 바꾼 것이므로, 실서버 전환 시 갈아 끼우는 파일은 flavor 소스셋의 `EngineModule` 하나다(research.md D13·D14·D15).

**구조 결정**: 프로필 설정은 **진입형** feature다 — 탭 셸의 그래프에 편입되는 화면이 아니라 온보딩·마이페이지 양쪽이 Activity로 여는 독립 플로우이고, 바텀 네비게이션을 노출하지 않는다(UX-006). 따라서 `ProfileActivity`·`ProfileShell`·`ProfileNavHost`·`di/`(Launcher) 골격을 모두 갖는다([feature-module.md](../../architecture/feature-module.md) 1장). 화면이 하나여도 NavHost를 유지하는 이유는 인자 복원과 화면 조회 로깅이 거기 딸려 오기 때문이다(research.md D11). 온보딩·마이페이지 feature는 이번 범위 밖이며, 이 계획은 그들이 호출할 계약([profile-launcher-contract.md](contracts/profile-launcher-contract.md))까지만 확정한다.

## 복잡도 추적 (Complexity Tracking)

해당 없음 — Constitution Check에서 정당화가 필요한 위반이 발견되지 않았다.
