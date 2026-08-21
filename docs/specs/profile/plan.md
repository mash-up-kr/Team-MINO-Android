# 구현 계획: 프로필 설정 및 수정 (Profile Setup & Edit)

**대상 스펙 경로**: `docs/specs/profile`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 1.3.0

**최초 작성일**: 2026-08-18

**최종 수정일**: 2026-08-21

**버전**: 2.0.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

온보딩과 마이페이지가 공유하는 프로필 설정 화면을 **진입형 feature 모듈 `:feature:profile`** 로 신설한다. 두 진입점이 서로 다른 feature가 될 예정이고 feature 간 직접 의존이 금지되어 있으므로, 전환은 `:core:navigation`의 `ProfileLauncher` 계약 한 겹으로 받고 진입점은 Intent extra로, 저장 완료는 `RESULT_OK`로 주고받는다 — 다음 목적지는 호출자가 고른다.

**이번 범위는 UI와 도메인 모델까지다 — 원격 API는 연결하지 않는다.** 데이터는 `:core:domain`의 `Profile` 모델과 `ProfileRepository`(`observeProfile(): Flow<Profile?>` + `saveProfile(Profile)`) 두 멤버로 경계를 긋고, `:core:data`가 공유 Preferences DataStore 하나로 그것을 구현한다. plan 1.1.0이 설계했던 원격 계층(DTO·`ApiService`·`RemoteDataSource`·flavor 소스셋 목 엔진)은 전부 후속 작업으로 물러난다(research.md D22). 그 이연이 나중에 무엇을 고치고 무엇을 고치지 않는지는 research.md D24가 표로 고정한다. 닉네임 규칙(앞뒤 공백 제외, 한글 음절·영문만 2자 이상, 클라이언트 상한 없음)은 화면이 아니라 `ValidateNicknameUseCase`·`SaveProfileUseCase`가 소유한다.

`:core:design-system`에는 아바타 12종(`MinoProfileAvatar` + `MinoProfileAvatarImage`)과 상단 바(`MinoTopNavigation`)를 신설한다. 아바타 소유 위치는 [방 대표 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)이 세운 경계를 그대로 따른다. 설계 근거는 [`research.md`](research.md), 데이터·상태는 [`data-model.md`](data-model.md), 계약 표면은 [`contracts/`](contracts/), 검증 절차와 **이번 범위가 확인할 수 없는 것**은 [`quickstart.md`](quickstart.md)에 있다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin 2.2.10, Jetpack Compose (버전 카탈로그 그대로)

**주요 의존성**: Hilt 2.59.2, Navigation Compose, `androidx-datastore-preferences` 1.2.1 — 모두 기존 카탈로그에 있고 **버전 카탈로그에 새 항목을 추가하지 않는다**. Ktor는 이번 범위가 쓰지 않는다 — `:core:data`의 기존 네트워크 설정(`NetworkModule`·`HttpClientConfig`)을 건드리지 않는다

**저장소**: 공유 `DataStore<Preferences>`(`core:data/storage/DataStoreModule`) 하나 — `profile_nickname`·`profile_avatar_id` 2개 키를 한 트랜잭션에서 쓴다. 이번 범위에서 이것이 원천이다(research.md D22)

**외부 계약**: 없음. 꾹 API 초안은 존재하지만 이번 범위가 소비하지 않는다 — 계약 스냅숏(plan 1.1.0의 `contracts/profile-api-contract.md`)은 이번 개정에서 지웠고, 원격 연동 작업이 git 이력(`e1ac7a0`)에서 되살린다(research.md D24). 앱 밖으로 여는 표면은 `:core:navigation`의 `ProfileLauncher` 하나뿐이다

**테스트**: JUnit4 + Fake 구현체 JVM 단위 테스트. 검증 규칙·저장 왕복·ViewModel 상태 전이를 덮는다. Compose UI 테스트는 저장소에 선례가 없어 도입하지 않는다(research.md D12)

**대상 플랫폼**: Android minSdk 29 / targetSdk 36

**프로젝트 유형**: mobile-app, 다중 Gradle 모듈 (신규 모듈 `:feature:profile` 1개)

**성능 목표**: spec SC-001(진입 후 60초 이내 저장 완료)·SC-005(아바타 선택 즉시 썸네일 반영)은 UX 목표이며 별도 계측 인프라를 두지 않는다. SC-003(앱 전체 즉시 반영)은 `observeProfile()` 구독으로 구조적으로 만족시킨다

**제약 조건**: 온보딩 진입에서는 화면을 벗어날 수 없고 저장 후 되돌아올 수도 없다(FR-010, EC-001, TS-018, EC-013). 저장 실패 시 입력값을 보존한다(FR-012, SC-006) — 통로는 만들되 로컬 단독 구간에서는 발화 원천이 없다(research.md D25). 프로필 표기는 앱 전체에서 하나다(FR-007). 오프라인 저장·나중에 동기화는 다루지 않는다(spec §4). **spec §4의 "저장은 서버 반영을 포함한다" 가정과 FR-008의 개인방 생성은 이번 범위가 충족하지 않는다** — 부정하지 않고 후속 작업으로 미룬다(research.md D22)

**규모/범위**: 화면 1개, 신규 도메인 모델 1·Repository 1·UseCase 2, 신규 로컬 저장 계층(DataSource·RepositoryImpl) 1벌, 신규 design-system 컴포넌트 2, 신규 전환 계약 1. 기존 파일 변경은 `settings.gradle.kts`·`:app` build 스크립트·`ExtraTag.kt` 세 곳뿐이다

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| 원칙/기준 | 판정 | 근거 |
|---|---|---|
| I. 단일 출처 문서화 | PASS | 규약을 링크로만 지목하고 본문을 옮기지 않았다. 화면·계약 정의는 문서마다 소유자를 하나로 두고 서로 참조한다 |
| II. 레이어 경계와 의존 방향 | PASS | `:feature:profile`은 `:core:domain`만 알고 `:core:data`를 의존하지 않는다. feature 간 결합은 `:core:navigation`의 `ProfileLauncher` 한 겹뿐이다(research.md D1). `:core:design-system`은 도메인을 모른다 — 아바타 enum이 저장 식별자를 갖지 않는다(D4). DI 바인딩은 구현을 소유한 모듈의 `di/`에 둔다 |
| III. 결정과 실패는 기록으로 남는다 | PASS, 승격 대상 1건 | research.md D1~D26은 이 feature 로컬 결정이다. 이번 개정에서 물러난 D13~D16·D21은 지우지 않고 취소선과 함께 남겼고, 대체 결정 D22~D26을 덧붙였다. D22(원격 이연)는 이 feature의 **작업 순서** 결정이라 승격 대상이 아니다 — 원격이 실제로 붙을 때 D13(원천과 캐시)·D15(목 엔진 소스셋 분리)가 되살아나면 그때 승격을 판단한다. D4(아바타 12종 소유)는 승격 후보로 유지한다 |
| IV. 명세가 구현에 선행한다 | PASS, 미충족 2건·어긋남 1건 보고 | 모든 설계 항목이 spec의 FR-·UX-·EC-·SC- 항목에서 도출됐고, plan에만 있고 spec에 근거가 없는 요구사항은 없다. spec이 다른 문서로 넘긴 범위(개인방 생성 규칙·온보딩 나머지 스텝)를 끌어오지 않았다. **이번 범위가 충족하지 않는 것 2건** — spec §4의 "저장은 서버 반영을 포함한다" 가정과 FR-008의 개인방 생성 트리거다. 둘 다 원격 연동이 있어야 성립하며, 설계로 봉합하지 않고 미충족 사실을 여기와 [quickstart.md §4](quickstart.md)에 드러낸다(research.md D22·D17). **어긋남 1건** — 서버 `Nickname` 규칙(공백 허용·15자 상한)이 spec §5의 확정(공백 불가·상한 없음)과 다르다. 이번 범위에는 서버 거절 경로가 없어 어긋남이 드러나지 않으므로, 원격 연동 전에 spec을 정리해야 한다(research.md D19) |
| V. 컨벤션은 게이트다 | PASS | 브랜치는 `feature/159-profile-setup/plan`으로 이미 분기돼 있다. 새 컴포넌트는 M3 패턴(Defaults·token)을 따르고, 에러 소비는 `launchSafely`·`runCatchingDomain`·`onDomainFailure`만 쓴다 |
| 기술 표준 — 디자인 토큰·실측 판정 | PASS(조건부) | `MinoProfileAvatarImage`·`MinoTopNavigation`의 값 판정은 구현 단계에서 Figma 원본과 대조해 정한다([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)). 이 plan은 컴포넌트의 존재·역할·공개 API까지만 결정했다 |
| 기술 표준 — 빌드 검증 | PASS | 확인 최소선은 `./gradlew :app:assembleQaDebug`이며 quickstart가 그것을 절차로 담고 있다. 이번 범위는 flavor별로 코드가 갈리지 않아 qa·prod가 같게 동작한다 |

**Phase 1 설계 후 재평가(1.1.0)**: 원격 계층이 들어오면서 `:core:data`의 표면이 늘었지만 경계는 그대로다 — `:feature:profile`은 여전히 `:core:domain`만 알고, DTO는 데이터 레이어 밖으로 나가지 않는다. 1.0.0이 남긴 "spec 가정과 구현의 시점 차이"는 원격 연동을 실제로 설계하면서 닫혔다. 남은 판정 변화는 원칙 IV의 어긋남 1건(닉네임 규칙)뿐이며 설계로 봉합하지 않고 보고한다. `Complexity Tracking`에 올릴 항목은 없다.

**Phase 1 설계 후 재평가(2.0.0)**: 원격 계층을 들어내면서 `:core:data`의 표면이 줄었고, 경계 판정이 뒤집힌 게이트는 없다 — 오히려 이번 범위가 손대는 기존 파일이 세 곳으로 줄어 원칙 II의 위험이 낮아졌다. 판정이 실질적으로 바뀐 곳은 원칙 IV 하나다: 1.1.0이 "닫혔다"고 적은 **spec 가정과 구현의 시점 차이가 다시 열렸다.** 1.1.0과 다른 점은 이번엔 그 차이가 계약 부재 때문이 아니라 **의도적으로 고른 작업 순서** 때문이라는 것이고, 그래서 미충족 항목(spec §4 서버 반영 가정·FR-008 개인방 생성)과 그것이 닫히는 조건을 문서가 직접 든다(research.md D22·D24, quickstart.md §4). 헌법 원칙 IV의 "근거가 없는 빈틈은 지어내지 않는다"에 따라 없는 것을 있는 것처럼 설계하지 않았으므로 게이트는 PASS다. `Complexity Tracking`에 올릴 항목은 없다.

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
│   └── design-system-contract.md
│                        # profile-api-contract.md는 2.0.0에서 삭제 — 이번 범위가 서버를
│                        # 소비하지 않는다. 원격 연동 작업이 git 이력에서 되살린다
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
├── repository/ProfileRepository.kt                  # 신규 — observeProfile() · saveProfile()
└── usecase/ValidateNicknameUseCase.kt · SaveProfileUseCase.kt   # 신규
                                                     # DeviceRepository·EnsureDeviceIdUseCase는 건드리지 않는다
                                                     # (1.1.0의 반환 타입 확대는 2.0.0에서 철회)

core/data/src/main/java/team/mino/core/data/
├── datasource/ProfileLocalDataSource.kt(+Impl) · di/ProfileDataSourceModule.kt    # 신규 — 공유 DataStore 사용
└── repository/ProfileRepositoryImpl.kt · di/ProfileRepositoryModule.kt            # 신규
                                                     # network/ 아래는 손대지 않는다. DTO·매퍼·목 엔진·
                                                     # flavor 소스셋은 원격 연동 작업의 몫이다(research.md D22·D24)

core/design-system/src/main/java/team/mino/core/designsystem/component/
├── profileavatar/  MinoProfileAvatar.kt · MinoProfileAvatarImage.kt · MinoProfileAvatarDefaults.kt · token/ProfileAvatarTokens.kt   # 신규
└── topnavigation/  MinoTopNavigation.kt · MinoTopNavigationDefaults.kt · token/TopNavigationTokens.kt                              # 신규
core/design-system/src/main/res/                     # 변경 — 아바타 12종 에셋(벡터 또는 밀도별 WebP)

app/build.gradle.kts                                 # 변경 — implementation(project(":feature:profile"))
settings.gradle.kts                                  # 변경 — include(":feature:profile")
```

**데이터 흐름 결정(2.0.0)**: 저장은 `ProfileViewModel → SaveProfileUseCase → ProfileRepository → ProfileLocalDataSource(DataStore)` 한 방향이고, 읽기는 `observeProfile()` Flow 하나로 되돌아온다. 갈래가 없으므로 화면은 물론 UseCase도 조건 분기를 갖지 않는다.

원격이 붙을 때 이 그림에 원격 DataSource가 `ProfileRepositoryImpl`과 로컬 사이로 들어오고 로컬은 캐시로 내려간다. **그때 바뀌는 파일과 바뀌지 않는 파일의 전체 목록은 research.md D24가 표로 들고 있다.** 이 표가 이연을 안전하게 만드는 장치이므로, 이번 범위의 구현이 표를 벗어나면(예: 화면이 저장 경로를 아는 상태를 갖게 되면) 그 자체가 설계 위반이다.

**구조 결정**: 프로필 설정은 **진입형** feature다 — 탭 셸의 그래프에 편입되는 화면이 아니라 온보딩·마이페이지 양쪽이 Activity로 여는 독립 플로우이고, 바텀 네비게이션을 노출하지 않는다(UX-006). 따라서 `ProfileActivity`·`ProfileShell`·`ProfileNavHost`·`di/`(Launcher) 골격을 모두 갖는다([feature-module.md](../../architecture/feature-module.md) 1장). 화면이 하나여도 NavHost를 유지하는 이유는 인자 복원과 화면 조회 로깅이 거기 딸려 오기 때문이다(research.md D11). 온보딩·마이페이지 feature는 이번 범위 밖이며, 이 계획은 그들이 호출할 계약([profile-launcher-contract.md](contracts/profile-launcher-contract.md))까지만 확정한다.

## 복잡도 추적 (Complexity Tracking)

해당 없음 — Constitution Check에서 정당화가 필요한 위반이 발견되지 않았다.
