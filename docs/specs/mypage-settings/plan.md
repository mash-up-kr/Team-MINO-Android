# 구현 계획: 마이페이지 & 환경설정

**대상 스펙 경로**: `docs/specs/mypage-settings`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 3.0.0

**최초 작성일**: 2026-08-17

**최종 수정일**: 2026-08-19

**버전**: 3.0.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

바텀 네비게이션 4번째 탭 `:feature:mypage`(신규, 탭 feature)를 만들어 프로필 조회·수정, 알림·위치 권한 스위치, 약관·앱 리뷰 진입점을 제공한다. `core:domain`에 Repository 인터페이스 4종(`ProfileRepository`·`PushNotificationRepository`·`AppSettingsRepository`·`PermissionRepository`)을 둔다. 백엔드 `swagger.yaml`(Team-MINO-Node)에 실제 프로필 API(`GET`/`PATCH /api/v1/users/me`)와 푸시 토큰 등록 API(`PUT .../push-token`)가 문서화되어, 프로필은 로컬 저장이 아니라 원격 API로 구현한다(research.md D1 재검토 → D9). 알림 스위치는 OS 권한과 분리된 앱 자체 발송 플래그를 로컬(DataStore)에 두되, 서버에 구독 해제 API가 없어 토큰은 등록해 두고 클라이언트 수신 단계에서 표시만 억제한다(D10). 위치 스위치는 OS 권한 상태를 그대로 반영한다. `core:design-system`에 `MinoDialog`·`MinoSwitch` 2개 컴포넌트를 신설한다. 다크모드는 PRD 4.1.0이 비목표로 확정해 범위에서 완전히 빠졌다(spec 3.0.0, research.md D4·D6 재검토). **실서버 baseUrl·Bearer 인증 부착은 이 plan의 범위 밖 선행 의존성이다**(D11 — 완료 보고 참조). 상세 설계 근거는 [`research.md`](research.md), 데이터·상태 계약은 [`data-model.md`](data-model.md)·[`contracts/`](contracts/) 참조.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin 2.2.10, Jetpack Compose (프로젝트 표준 버전 카탈로그 그대로)

**주요 의존성**: Hilt 2.59.2, Navigation Compose, `androidx-datastore-preferences` 1.2.1, `androidx-activity-compose`(권한 런처), Ktor(`core:data` 기존) — 기존 카탈로그에 있음(research.md D5·D7이 새 라이브러리 도입을 각각 기각). **신규**: `firebase-messaging`(FCM 푸시 토큰 발급, research.md D10) — 버전 카탈로그에 없어 추가 필요

**저장소**: 프로필은 원격 API(`GET`/`PATCH /api/v1/users/me`, research.md D9) — 로컬 캐시 없음. 알림 발송 설정·권한 요청 이력만 공유 `mino_preferences` DataStore(`core:data/storage/DataStoreModule`)에 남는다

**테스트**: JUnit4 + Fake Repository(JVM 단위 테스트) — `core:data`의 기존 `FakeDeviceInfoProvider` 패턴을 따른다. Compose UI 테스트는 이 저장소에 선례가 없어 이번 feature도 도입하지 않는다(범위 밖)

**대상 플랫폼**: Android minSdk 29 / targetSdk 36. `POST_NOTIFICATIONS`는 API 33+ 런타임 권한이므로 29~32에서는 요청 없이 항상 허용 상태로 취급(OS 표준 동작)

**프로젝트 유형**: mobile-app, 다중 Gradle 모듈 (신규 모듈 `:feature:mypage` 1개 추가)

**성능 목표**: spec SC-001(프로필 수정 10초 이내) — UI 반응성 목표이며 별도 성능 계측 인프라는 두지 않는다

**제약 조건**: 알림·위치 스위치 표시는 캐시 없이 화면 재진입마다 OS를 재조회(FR-009). 노션 문서·Play 스토어는 외부 브라우저/스토어 앱에 위임하고 오프라인 처리는 그쪽 책임(가정, spec §4). **원격 API 호출은 baseUrl·Bearer 인증이 이미 배선돼 있음을 전제한다 — 이 feature가 만들지 않는다(research.md D11)**

**규모/범위**: 화면 2개(`MyPageMain`·`MyPageProfile`), 신규 Repository 4종, 신규 design-system 컴포넌트 2종, 신규 원격 API 소비 2건(`GET`/`PATCH users/me`, `PUT push-token`)

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| 원칙/기준 | 판정 | 근거 |
|---|---|---|
| I. 단일 출처 문서화 | PASS | 이 plan은 규약 문서를 링크로만 지목한다(위 각 절 참조). 규칙 본문을 복제하지 않았다 |
| II. 레이어 경계와 의존 방향 | PASS | `:feature:mypage`는 `core:domain`만 의존(탭 feature 컨벤션 그대로). `ProfileRepositoryImpl`·`PushNotificationRepositoryImpl`은 `core:data`에 있고 domain은 도메인 모델만 반환받는다(D9·D10). `core:design-system`은 여전히 domain·data를 의존하지 않는다. DI 바인딩은 구현을 소유한 모듈(`core:data`)의 `di/`에 둔다 |
| III. 결정과 실패는 기록으로 남는다 | PASS, 승격 대상 없음(차단 의존성 1건 별도 보고) | research.md D1·D9~D11은 이 feature 로컬 결정. D11(실서버 baseUrl·Bearer 인증)은 다른 feature도 공유할 인프라지만 이 plan이 "결정"하는 게 아니라 "아직 없다"고 기록하는 것이라 ADR 승격 대상이 아니다 — 완료 보고에서 선행 과제로 별도 표시한다 |
| IV. 명세가 구현에 선행한다 | PASS | 이 plan의 모든 요구사항은 spec.md의 FR-*·UX-*·EC-*에서 도출했다. spec에 없는 요구사항(예: 닉네임 상한 15자)은 백엔드 계약 반영일 뿐 spec 요구사항으로 추가하지 않았다 — `/mino-spec` 개정 필요 여부는 완료 보고에서 사용자 판단에 맡긴다 |
| V. 컨벤션은 게이트다 | PASS | 브랜치는 이미 `feature/152-mypage-settings-screen/base`(develop 기준)로 분기돼 있다. 새 컴포넌트는 M3 패턴(Defaults·Colors·token)을 따른다 |
| 기술 표준 — 디자인 토큰 | PASS(조건부) | `MinoDialog`·`MinoSwitch`의 토큰 대조는 구현 단계에서 Figma 노드를 열어 판정한다(`figma-design-fidelity.md`). 이 plan은 존재·역할만 결정했다 |

**Phase 1 설계 후 재평가(plan 3.0.0)**: 프로필을 원격 API로 전환하면서도 도메인 모델·Repository 인터페이스 경계는 그대로 유지돼(D9) 원칙 II를 새로 건드리지 않는다. `Complexity Tracking`에 기록할 항목 없음 — 원격 전환은 헌법이 이미 예정해 둔 표준 패턴(`core:data/README.md`)을 따른다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/mypage-settings/
├── plan.md              # 이 파일
├── research.md          # Phase 0 산출물
├── data-model.md         # Phase 1 산출물
├── quickstart.md         # Phase 1 산출물
├── contracts/             # Phase 1 산출물
│   ├── mypage-main-contract.md
│   ├── profile-setup-contract.md
│   └── repository-contracts.md
└── tasks.md               # /mino-task 산출물 (이 실행이 만들지 않음)
```

### 소스 코드 (Repository Root 기준)

Option 3(모바일)의 Android 구조를 그대로 따른다. 신규·변경 모듈만 나열한다 — 손대지 않는 모듈은 생략.

```text
feature/mypage/                                    # 신규 — 탭 feature (feature-module.md 골격)
├── build.gradle.kts                                # alias(mino.android.feature) + namespace만
└── src/main/java/team/mino/feature/mypage/
    ├── MyPageNavigation.kt                          # MyPageGraph(public) + mypageGraph() + 내부 Route
    ├── main/
    │   ├── screen/  MyPageRoute.kt · MyPageScreen.kt
    │   ├── vm/      MyPageViewModel · MyPageUiState · MyPageIntent · MyPageSideEffect
    │   └── component/  (권한 스위치 행 · 서비스정보 행 등 화면 조립 조각)
    └── profile/
        ├── screen/  ProfileRoute.kt · ProfileScreen.kt
        ├── vm/      ProfileViewModel · ProfileUiState · ProfileIntent · ProfileSideEffect
        └── component/  (아바타 그리드)

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/  Profile.kt(avatarId: Int) · PermissionType.kt        # 신규
└── repository/  ProfileRepository.kt · PushNotificationRepository.kt · AppSettingsRepository.kt · PermissionRepository.kt   # 신규

core/data/src/main/java/team/mino/core/data/
├── network/dto/response/UserResponse.kt                                                       # 신규 — User·Avatar DTO
├── network/service/UserApiService.kt                                                          # 신규 — GET/PATCH users/me, PUT push-token
├── datasource/  UserRemoteDataSource(+Impl) · AppSettingsLocalDataSource(+Impl) · PermissionLocalDataSource(+Impl)   # 신규. Profile은 원격, 나머지는 공유 DataStore
├── repository/mapper/UserMapper.kt                                                             # 신규 — UserResponse.toDomain()
├── device/  PermissionRepositoryImpl.kt + di/PermissionRepositoryModule.kt                    # 신규 — 기존 device/ 패키지에 추가
├── device/  PushTokenProvider(+Impl) + di/PushTokenProviderModule.kt                           # 신규 — FCM SDK 래퍼
└── repository/  ProfileRepositoryImpl.kt · PushNotificationRepositoryImpl.kt · AppSettingsRepositoryImpl.kt (+ di/*Module.kt)     # 신규

core/design-system/src/main/java/team/mino/core/designsystem/component/
├── dialog/       MinoDialog.kt · MinoDialogDefaults.kt · token/DialogTokens.kt                 # 신규
└── switch/       MinoSwitch.kt · MinoSwitchDefaults.kt · MinoSwitchColors.kt · token/SwitchTokens.kt  # 신규

feature/main/src/main/java/team/mino/feature/main/
├── MainDestinations.kt      # 변경 — MyPage data object 제거(모듈 소유로 이동)
├── MainNavHost.kt           # 변경 — MyPage placeholder screen<> 등록을 mypageGraph(...) 호출로 교체
├── MainTab.kt                # 변경 — MY_PAGE의 route를 MyPageGraph로 교체
└── build.gradle.kts          # 변경 — implementation(project(":feature:mypage")) 추가

app/src/main/AndroidManifest.xml   # 변경 — POST_NOTIFICATIONS · ACCESS_FINE_LOCATION · ACCESS_COARSE_LOCATION 추가

settings.gradle.kts   # 변경 — include(":feature:mypage") 추가
```

**구조 결정**: 마이페이지는 탭 feature이므로 `XActivity`·`XShell`·`XNavHost`·`di/`(Launcher)를 두지 않는다(`feature-module.md` 1장 — 진입형/탭 비교표). 프로필 설정은 별도 모듈로 분리하지 않고 `:feature:mypage` 내부 Route로 둔다 — 온보딩 모듈이 아직 없어 지금 재사용 지점이 하나뿐이고, 두 번째 사용처(온보딩)가 생기는 시점에 분리·공유 방식을 다시 설계한다(research.md에 남기지 않은 이유: spec §3.2가 이미 온보딩 통합을 비목표로 명시했으므로 이 plan의 범위 밖).

## 복잡도 추적 (Complexity Tracking)

해당 없음 — Constitution Check에서 정당화가 필요한 위반이 발견되지 않았다.
