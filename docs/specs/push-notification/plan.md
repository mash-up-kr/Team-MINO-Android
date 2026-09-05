# 구현 계획: 푸시 알림 (Push Notification)

**대상 스펙 경로**: `docs/specs/push-notification`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 1.0.3

**최초 작성일**: 2026-09-03

**최종 수정일**: 2026-09-04

**버전**: 1.2.2

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

앱은 세션 확보 직후 FCM 등록 토큰을 조회해 서버(`PUT /api/v1/users/me/push-token`)에 등록하고(FR-001~005), 서버가 보낸 data-only FCM 메시지를 앱 상태와 무관하게 받아 단일 채널의 시스템 알림으로 표시한다(FR-006~008, FR-011). 알림을 누르면 payload의 유형·대상 식별자로 도착지(장소 상세·방 상세·알림 탭 목록)를 정해 이동한다(FR-009~010, FR-012~013).

기술적 접근은 세 갈래다 — (1) FCM SDK 통합·알림 빌드·딥링크 조립은 새 모듈 `:core:notification`에 둔다([research.md D4](research.md#d4-fcm-sdk-통합은-새-모듈-corenotification에-둔다)). (2) 토큰 조회는 `core:data`의 기존 Firebase 인증 제공자 패턴을 그대로 재사용하고, 서버 등록은 그 엔드포인트가 속한 OpenAPI `user` 태그의 소유자 `UserApiService`·`UserRemoteDataSource`를 넓힌다(D5, [ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md)). (3) 알림 탭 시 도착지 진입은 이미 존재하는 탭 간 장소 상세 진입 인프라(`PlaceDetailRequestHolder`)를 그대로 재사용하고, 대칭되는 `RoomDetailRequestHolder`를 새로 만든다(D2·D8). 알림은 `MainActivity`(`singleTask`)를 겨냥해 앱이 살아 있으면 `onNewIntent`로 스플래시 없이 이동하고, 프로세스가 죽어 있으면 `MainActivity`가 `setContent` 전에 `SplashActivity`로 우회해 시작 경로를 그대로 지난다(D13~D15).

> **1.0.1 (2026-09-03, PATCH)**: `/mino-task`가 [ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md)과의 충돌을 발견해 보정했다. 1.0.0은 `PushApiService`·`PushRemoteDataSource`·`PushDataSourceModule`을 신설했으나, `PUT /api/v1/users/me/push-token`은 OpenAPI `user` 태그(`uH_updatePushToken`)라 그 태그의 소유자를 넓혀야 한다. 요구사항·도메인 계약·`:core:notification` 설계는 그대로다 — 바뀐 곳은 §프로젝트 구조의 `core/data` 블록과 `core/notification/README.md` 추가, 헌법 게이트 V, [research.md D5](research.md#d5-토큰-조회등록은-coredata에-기존-인증-제공자-패턴을-그대로-재사용한다), [contracts/push-token-api.md §2](contracts/push-token-api.md)다. 같은 날 spec 1.0.2가 FR-013(`pinId`)·§4 가정(토큰 API 존재)을 반영해, 게이트 IV와 요약이 "제안"으로 남겨 둔 spec 개정은 닫혔다.
>
> **1.2.2 (2026-09-04, PATCH)**: D13·계약 §4의 "최근 앱 복원 경로의 세션 미확보 결함" 표현을 정정했다 — 코드 재확인 결과 세션은 SDK 복원과 ViewModel의 `ensureSession()` 선행으로 확보되며, 건너뛰는 것은 그 실행의 토큰 재등록뿐이다. 별도 이슈를 만들지 않는다. 설계 변경 없음.
>
> **1.2.1 (2026-09-04, PATCH)**: 3차 `/mino-analyze` F2 — §복잡도 추적에 `SplashActivity` `singleTop` 행을 더했다. 설계 변경 없음.
>
> **1.2.0 (2026-09-04, MINOR)**: 2차 `/mino-analyze` C1(스플래시 진행 중 알림 탭 → 스플래시 중복)을 닫았다 — `SplashActivity` `singleTop` + 우회 Intent `CLEAR_TOP` + `SplashActivity.onNewIntent`로 extra 갱신([D16](research.md#d16-스플래시가-진행-중일-때의-알림-탭은-그-스플래시가-이어받는다), [계약 §2·§3·§4](contracts/push-deeplink-contract.md)). 함께 C2(계약 다이어그램 셋째 가지)·F1(§저장소에 `MainEntryGate`)·B1(§4 헬퍼 풀어씀)을 보정했다. 스플래시 spec 요구사항은 바뀌지 않는다.
>
> **1.1.0 (2026-09-04, MINOR)**: 알림 탭 진입 경로를 바꿨다 — 항상 `SplashActivity` 경유(D6·D7)에서 `MainActivity` 겨냥 + 탭 시점 게이트 판정(D13·D14·D15)으로. `/mino-analyze` 발견 C2(웜 탭마다 3초 스플래시·상태 소실)를 사용자 결정으로 닫은 결과다. 요구사항·도메인 모델·`:core:notification`의 알림 빌드·토큰 등록은 그대로이고, 바뀐 곳은 [딥링크 계약 §2~§5](contracts/push-deeplink-contract.md), §프로젝트 구조의 `core/navigation`·`feature/main`·`feature/splash` 블록, [quickstart.md §2.1](quickstart.md#21-시나리오)이다. spec 본문은 바뀌지 않는다(SC-004 그대로 만족).
>
> **1.0.2 (2026-09-04, PATCH)**: `/mino-analyze`가 지적한 문구 오류 셋을 고쳤다 — 확장 모듈 개수(4→6), `PushDetailRequestHolder`라는 없는 이름, 오퍼레이션 개수(28→29). 설계 변경 없음. 같은 날 spec 1.0.3(PATCH — 입력 필드 주석·§4 가정 1건)이 나와 기준 spec 버전만 따라 올렸다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin (프로젝트 표준 버전 그대로, 신규 언어 도입 없음)

**주요 의존성**: Firebase Cloud Messaging SDK(`firebase-messaging`, 신규 카탈로그 항목) — 기존 `firebase-bom`·`google-services`(`AndroidFirebaseConventionPlugin`, `:app`에 이미 적용)에 편승한다. Hilt(`mino.android.hilt`). 신규 외부 라이브러리 도입은 이것뿐이다.

**저장소**: 없음 — 등록 토큰을 로컬에 저장하지 않는다(spec §4 가정: 앱 시작마다 재조회·재등록, 비교·캐시 불필요). `PlaceDetailRequestHolder`·`RoomDetailRequestHolder`는 메모리 상태(`ActivityRetainedScoped`)이고 `MainEntryGate`는 프로세스 스코프 메모리 값(`@Singleton`)일 뿐, 어느 것도 영속 저장이 아니다.

**테스트**: JUnit(JVM) — `core:domain`의 `ParsePushMessageUseCase`·`ResolvePushDestinationUseCase`(순수 함수)만 유닛 테스트 대상이다. 알림 표시·딥링크 탭은 [quickstart.md](quickstart.md) §2·§3이 정한 대로 수동/계측 검증으로 남긴다.

**대상 플랫폼**: Android(기존 `minSdk`/`targetSdk` 그대로, 이 feature가 별도로 올리지 않는다)

**프로젝트 유형**: mobile-app — 기존 다중 Gradle 모듈 구조에 신규 모듈 `:core:notification` 하나를 추가한다.

**성능 목표**: SC-003 — 서버가 보낸 알림이 표시되기까지 앱이 자체 대기·지연을 추가하지 않는다(`onMessageReceived`에서 즉시 알림 빌드).

**제약 조건**: FR-004·UX-002 — 토큰 등록 실패가 어떤 화면 요소로도 새지 않는다(도메인 예외를 삼키고 재시도 큐를 두지 않음).

**규모/범위**: 알림 유형 7종([data-model.md](data-model.md) §1), 도착지 3종([data-model.md](data-model.md) §3), 신규 모듈 1개(`:core:notification`), 기존 모듈 6개 확장(`core:domain`·`core:data`·`core:navigation`·`feature:main`·`feature:splash`·`feature:room`).

**참조 API 문서**: `https://api.gguk.org/api-docs-json` (Team MINO API 1.0.0, 오퍼레이션 29개), 조회 시점 2026-09-04. 푸시 토큰 등록(`user` 태그)은 이 문서에 있고, FCM data 페이로드 스키마는 없다 — 근거는 [contracts/push-payload-contract.md](contracts/push-payload-contract.md)가 소유(사용자 제공 서버 설계 캡처, 실기기 재확인 필요).

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| 원칙 | 판정 | 근거 |
|---|---|---|
| I. SSOT | 조건부 통과 | `:core:notification` 신설·새 Intent extra 키는 이 문서·`data-model.md`·`contracts/`에만 정의하고 코드 주석에 다시 풀어쓰지 않는다. 단 `docs/architecture/modularization.md`(모듈 목록 SSOT)는 `/mino-plan`의 범위 가드가 편집을 금지해 이번 실행에서 갱신하지 못한다 — **완료 보고의 후속 작업**으로 남긴다(위반이 아니라 유예) |
| II. 레이어 경계 | 통과 | `:core:notification`은 `core:domain`·`core:navigation`만 의존하고 `core:data`·feature 모듈을 의존하지 않는다(연구 D4). DI 바인딩은 구현을 소유한 모듈(`core:data`·`feature:splash`·`feature:main`)이 갖는다(D5·계약 §2). 1.1.0의 `MainEntryGate`·`MainDeepLinkIntentFactory`도 인터페이스는 `:core:navigation`, 구현은 대상 feature라 feature 간 직접 의존이 생기지 않는다 |
| III. 결정 기록 | 조건부 통과 | 신규 모듈 추가(`:core:notification`)는 다른 feature에도 구속력을 갖는 구조 결정이라 ADR 승격 대상이다 — **완료 보고에서 제안** |
| IV. Spec-First | 조건부 통과 | 이 plan은 spec.md의 요구사항을 새로 만들지 않는다. 다만 D2(`pinId` 재사용)가 FR-013의 문언과 갈려 **완료 보고에서 spec.md PATCH 개정을 제안**한다 — 이 문서 자체는 spec을 고치지 않는다(범위 가드 준수) |
| V. 컨벤션 게이트 | 통과 | 새로 추가하는 예외 매핑 지점(D10)은 `docs/conventions/error_handling.md` §3의 기존 정책("원천마다 매핑 지점 하나")을 그대로 따른다. 그 표에 새 행을 추가하는 문서 편집은 이 plan의 범위 밖이라 후속 작업으로 남긴다. **1.0.1**: 토큰 등록 엔드포인트는 [ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md)(태그당 `ApiService`·`DataSource` 하나)대로 `UserApiService`·`UserRemoteDataSource`를 넓힌다 — 1.0.0의 `PushApiService` 신설은 이 ADR 대조 누락이었다 |

Phase 1 설계 후 재확인: 위 판정은 Phase 0·1을 거치며 바뀌지 않았다 — 새로 드러난 위반이나 게이트 실패가 없다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/{feature-name}/
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 (/mino-plan)
├── data-model.md        # Phase 1 산출물 (/mino-plan)
├── quickstart.md        # Phase 1 산출물 (/mino-plan)
├── contracts/           # Phase 1 산출물 (/mino-plan)
└── tasks.md             # /mino-task 산출물 (/mino-plan 이 생성하지 않음)
```

### 소스 코드 (Repository Root 기준)

기존 다중 Gradle 모듈 구조([`docs/architecture/modularization.md`](../../architecture/modularization.md))를 그대로 따른다. 신규 모듈 하나(`core:notification`)와 기존 모듈 다섯 곳의 확장으로 구성된다. — feature 모듈이 다른 feature 모듈을 의존하는 경우는 없다(원칙 II).

```text
core/notification/                                          # 신규 (D4)
├── README.md                                        # 모듈 사용·확장 규칙 (1.0.1 — 다른 core 모듈과 같은 구조, CLAUDE.md 표에 행 추가)
├── build.gradle.kts                                # mino.android.library + mino.android.hilt
├── src/main/AndroidManifest.xml                     # <service> MinoFirebaseMessagingService 등록
└── src/main/java/team/mino/core/notification/
    ├── MinoFirebaseMessagingService.kt               # onNewToken → RegisterPushTokenUseCase, onMessageReceived → 알림 빌드
    ├── PushNotificationChannel.kt                     # 단일 채널 멱등 생성 (D3)
    └── PushNotificationBuilder.kt                     # PushMessage + PendingIntent → Notification

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/
│   ├── PushMessageType.kt                            # 신규 — data-model.md §1
│   ├── PushMessage.kt                                # 신규 — data-model.md §2
│   └── PushDestination.kt                            # 신규 — data-model.md §3
├── repository/
│   └── PushRegistrationRepository.kt                 # 신규 — data-model.md §4
└── usecase/
    ├── RegisterPushTokenUseCase.kt                    # 신규 (D5)
    ├── ParsePushMessageUseCase.kt                      # 신규
    └── ResolvePushDestinationUseCase.kt                # 신규

core/data/src/main/java/team/mino/core/data/
├── push/
│   ├── PushTokenProvider.kt · PushTokenProviderImpl.kt  # 신규 — FirebaseMessaging SDK 래핑 (D5)
│   ├── extension/Task.kt                                # 신규 — 전용 매핑 지점 (D10)
│   └── di/{FirebaseMessagingModule,PushProviderModule}.kt
├── datasource/
│   └── UserRemoteDataSource.kt · UserRemoteDataSourceImpl.kt   # 확장 — registerPushToken (D5, ADR 2026-08-28). DI 모듈은 기존 UserDataSourceModule 그대로
├── network/
│   ├── service/UserApiService.kt                       # 확장 — updatePushToken: PUT /api/v1/users/me/push-token (user 태그 소유자)
│   └── dto/request/PushTokenRequest.kt                 # 신규
└── repository/
    ├── PushRegistrationRepositoryImpl.kt               # 신규
    └── di/PushRegistrationRepositoryModule.kt

core/navigation/src/main/java/team/mino/core/navigation/
├── entry/RoomDetailRequestHolder.kt                   # 신규 — 계약 §6 (D8)
├── entry/MainEntryGate.kt                              # 신규 (1.1.0) — 계약 §3, 프로세스 스코프 게이트 플래그 (D13)
├── activity/launcher/ExtraTag.kt                       # 확장 — EXTRA_PUSH_* (계약 §1)
├── deeplink/MainDeepLinkIntentFactory.kt               # 신규 (1.1.0) — 계약 §2, :core:notification이 쓴다
└── deeplink/SplashDeepLinkIntentFactory.kt             # 신규 — 계약 §2, :feature:main의 콜드 우회가 쓴다

feature/splash/src/main/AndroidManifest.xml             # 확장 (1.2.0) — SplashActivity launchMode="singleTop" (D16)
feature/splash/src/main/java/team/mino/feature/splash/
├── SplashActivity.kt                                  # 확장 — 계약 §3 (extra 전달 + MainEntryGate.markPassed + onNewIntent로 extra 갱신, D16)
├── main/vm/SplashViewModel.kt                          # 확장 — 세션 확보 직후 RegisterPushTokenUseCase 호출(fire-and-forget)
└── di/SplashDeepLinkIntentFactoryImpl.kt · SplashDeepLinkModule.kt   # 신규 — 계약 §2

feature/main/src/main/AndroidManifest.xml               # 확장 (1.1.0) — MainActivity launchMode="singleTask" (D14)
feature/main/src/main/java/team/mino/feature/main/
├── MainActivity.kt                                    # 확장 — 계약 §4 (onCreate 콜드 우회 + onNewIntent 웜 소비)
├── MainShell.kt · MainNavHost.kt                       # 확장 — startTab(D9) + pendingTab 소비(D15) (계약 §5)
├── di/MainDeepLinkIntentFactoryImpl.kt · MainDeepLinkModule.kt         # 신규 (1.1.0) — 계약 §2

feature/room/src/main/java/team/mino/feature/room/main/vm/
└── RoomListViewModel.kt                                # 확장 — RoomDetailRequestHolder 소비 (계약 §6)

app/build.gradle.kts                                    # 확장 — implementation(project(":core:notification"))
settings.gradle.kts                                      # 확장 — include(":core:notification")
```

**구조 결정**: 기존 "core 모듈 = SDK/인프라 하나를 감싼다"는 선례(`core:map`·`core:analytics`)를 따라 `core:notification`을 신설했다(D4). 토큰 조회·등록은 새 모듈을 만들지 않고 기존 `core:data`의 Firebase 인증 제공자 패턴을 그대로 잇고, HTTP 등록은 `user` 태그의 기존 소유자에 함수 하나를 더한다(D5) — 같은 feature 안에서도 "SDK를 감싸는 방식"이 데이터 계층 성격(토큰 등록)과 시스템 UI 계층 성격(알림 표시)으로 갈리기 때문에 모듈도 그 경계를 따라 나뉜다.

## 복잡도 추적 (Complexity Tracking)

| 위반 사항 | 필요한 이유 | 더 단순한 대안을 기각한 이유 |
|---|---|---|
| 신규 Gradle 모듈 `core:notification` 추가 | `FirebaseMessagingService`(Android `Service` 컴포넌트)·알림 채널·`PendingIntent` 조립은 기존 어느 `core:*` 모듈의 책임 범위에도 들지 않는다(`core:data`=Repository·DataSource, `core:common:android`=범용 유틸리티) — D4 | `core:data`에 얹기: Firebase Auth SDK를 감싼 선례와 겹쳐 보이지만 그쪽은 순수 데이터 조회고 이쪽은 `Service` 컴포넌트·알림 UI 조립이라 책임이 다르다. `core:common:android`에 얹기: 범용 유틸리티 모듈이 FCM 전용 로직을 갖게 되어 "공용"의 의미가 흐려진다 |
| `MainActivity`의 `launchMode`를 `singleTask`로 바꾸는 전역 변경(1.1.0) | 알림 탭 시 살아 있는 `MainActivity`가 `onNewIntent`를 받아야 스플래시·재생성 없이 도착지로 갈 수 있다(D13·D14). `standard` + `CLEAR_TOP`은 재생성이라 상태 보존이 성립하지 않는다 | `singleTop`: 폼이 위에 떠 있으면 Main이 새로 쌓여 두 인스턴스가 된다. D6 유지: 웜 탭마다 3초 대기·상태 소실 — 요구사항 위반은 아니지만 사용자가 비용을 받아들이지 않았다 |
| `SplashActivity`의 `launchMode`를 `singleTop`으로 바꾸는 변경(1.2.0) | 스플래시가 진행 중일 때 알림을 누르면 우회용 `MainActivity`가 그 위에 쌓인다. `CLEAR_TOP`으로 Main을 걷어낸 뒤 기존 스플래시가 재생성 없이 `onNewIntent`로 extra를 이어받아야 스플래시·세션 확보가 한 번으로 끝난다(D16) | `singleTask`: 재사용은 되지만 런처 진입 Activity의 태스크 규칙이 바뀌어 영향이 이 feature를 넘는다. 아무것도 하지 않기: 두 스플래시가 겹치고 세션 확보 요청이 중복된다 |
| `PlaceDetailRequestHolder`와 거의 같은 형태의 `RoomDetailRequestHolder` 신설(코드 중복처럼 보임) | 장소·방 두 도메인의 진입 규칙이 다르다 — 장소 상세만 진입 출처(`origin`)에 따라 [나가기] 도착지가 갈린다(PRD 13.0.0) | 제네릭 홀더로 통합: 방 쪽 소비자가 쓰지 않는 `origin` 필드를 강제로 갖게 되고, 두 도메인의 규칙이 갈릴 때마다(이미 한 번 갈렸다) 제네릭 타입을 다시 쪼개야 해 장기적으로 더 복잡하다 |
