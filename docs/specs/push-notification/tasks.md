# 작업 목록: 푸시 알림 (Push Notification)

**대상 스펙 경로**: `docs/specs/push-notification`

**기준 plan 버전**: 1.2.2

**최초 작성일**: 2026-09-03

**최종 수정일**: 2026-09-04

**사전 조건**: [plan.md](plan.md) · [spec.md](spec.md) · [research.md](research.md) · [data-model.md](data-model.md) · [contracts/](contracts/) · [quickstart.md](quickstart.md)

**테스트**: 포함한다. 범위는 [plan.md §기술 컨텍스트](plan.md)가 정한 대로 `:core:domain`의 순수 함수 둘(`ParsePushMessageUseCase`·`ResolvePushDestinationUseCase`)의 JVM 단위 테스트뿐이다. 알림 표시·`PendingIntent`·`FirebaseMessagingService` 콜백은 [quickstart.md §3](quickstart.md)에 따라 실기기 검증으로 남긴다. 기존 테스트(`SplashViewModelTest`·`RoomListViewModelTest`)는 생성자가 바뀌므로 컴파일 유지 수준으로만 손댄다.

**구성 방식**: 네 유저 플로우를 네 스토리로 받는다. US1(토큰 등록)은 데이터 레이어 한 줄기로 독립이고(단, `UserApiService`·`UserRemoteDataSource`를 넓히므로 splash·profile이 회귀 대상이다), US2(앱 밖 수신·탭 진입)가 이 feature의 코드 대부분을 갖는다. US3(사용 중 수신)·US4(대표 알림)는 [research.md D1](research.md#d1-fcm-메시지는-data-only-페이로드를-전제한다)(data-only)과 [data-model.md §3](data-model.md#3-pushdestination)의 라우팅 표가 US2 구현 안에서 함께 성립시키므로 **코드 작업이 따로 생기지 않고 검증 작업만 남는다** — 검증 항목이 스토리마다 다르므로 Phase는 유지한다.

> **개정 이력.** 최초 작성(plan 1.0.0 기준)에서 발견한 [ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md) 충돌을 plan 1.0.1 PATCH로 닫고 같은 날 개정했다 — `PushApiService`·`PushRemoteDataSource`·`PushDataSourceModule` 신설 작업(T020~T022, 미착수)을 지우고 `UserApiService`·`UserRemoteDataSource` 확장 작업(T048·T049)으로 대체했다. 지운 번호는 재사용하지 않는다. plan 1.0.2(2026-09-04, 문구 보정)는 작업 집합을 바꾸지 않아 기준 버전만 올렸다. **plan 1.1.0(2026-09-04, 알림 탭 진입 경로 변경 — D13·D14·D15)** 에서 T051~T054를 추가하고 T030·T032·T033·T036·T037·T038·T041의 대상·문구를 넓혔다. 지운 작업은 없다. **plan 1.2.0(2026-09-04, 스플래시 진행 중 탭 — D16)** 에서 T055를 추가하고 T032·T033·T038·T041 문구를 넓혔다. plan 1.2.1(복잡도 표 보정)·1.2.2(복원 경로 문구 정정, 2026-09-04)는 작업 집합을 바꾸지 않아 기준 버전만 올렸다.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. **한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.** 개정으로 추가되는 작업은 문서에 존재하는(폐기 섹션 포함) 최대 번호 + 1부터 부여하므로, 개정을 거치면 문서 순서와 ID 순서는 어긋날 수 있다. 실행 순서는 Phase 순서와 "의존성 및 실행 순서" 섹션이 말한다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리 (US1 · US2 · US3 · US4)
- 설명에는 정확한 파일 경로를 포함한다. 이 feature는 화면을 만들지 않으므로 Figma 노드가 없다.

## 경로 규칙

모바일(Android) 다중 모듈. 경로는 저장소 루트 기준이며 [plan.md §프로젝트 구조](plan.md)를 그대로 따른다. `:core:domain`은 `src/main/kotlin/`(테스트는 `src/test/kotlin/`), 나머지 모듈은 `src/main/java/`다. 신규 모듈 `:core:notification`의 패키지는 `team.mino.core.notification`이다.

---

## Phase 1: 셋업 (모듈 골격)

**목적**: `:core:notification`을 빌드에 등록하고 FCM SDK를 카탈로그에 올린다. 이 feature가 도입하는 유일한 신규 외부 라이브러리다([plan.md §기술 컨텍스트](plan.md)).

- [X] T001 `settings.gradle.kts`에 `include(":core:notification")` 추가
- [X] T002 [P] `gradle/libs.versions.toml`의 `[libraries]`에 `firebase-messaging = { module = "com.google.firebase:firebase-messaging" }` 추가 — 버전은 기존 `firebase-bom`이 정하므로 `version.ref`를 두지 않는다(`firebase-auth`·`firebase-analytics` 항목과 같은 형태)
- [X] T003 `core/notification/build.gradle.kts` 신설 — `alias(libs.plugins.mino.android.library)` + `alias(libs.plugins.mino.android.hilt)`, `namespace = "team.mino.core.notification"`. 의존은 `project(":core:domain")`·`project(":core:navigation")`·`platform(libs.firebase.bom)`·`libs.firebase.messaging`·`libs.androidx.core.ktx`(`NotificationCompat`)·`libs.kotlinx.coroutines.android`. **`:core:data`·feature 모듈을 의존하지 않는다**([research.md D4](research.md#d4-fcm-sdk-통합은-새-모듈-corenotification에-둔다), 헌법 원칙 II). Compose 플러그인은 붙이지 않는다(non-Compose 모듈)
- [X] T004 `app/build.gradle.kts`의 core 의존 목록에 `implementation(project(":core:notification"))` 추가 — `<service>`가 앱 매니페스트로 병합되려면 `:app`이 이 모듈을 직접 의존해야 한다

**체크포인트**: `./gradlew :core:notification:assembleQaDebug`가 통과한다(빈 모듈). 여기서부터 Phase 2를 시작할 수 있다.

---

## Phase 2: 기반 작업 (스토리들이 공유하는 도메인·서비스 골격)

**목적**: 알림 한 건을 도메인 모델로 읽고 도착지로 바꾸는 순수 함수와, 그것을 호출할 `FirebaseMessagingService`의 빈 골격. 스토리별 동작은 얹지 않는다.

**⚠️ 실행 순서는 Phase가 아니라 아래 소그룹 간 의존이 정한다.** 2-A와 2-B는 서로를 기다리지 않는다. 2-A는 US2·US3·US4가, 2-B는 US1·US2가 쓴다.

### 2-A. 도메인 모델·파싱·라우팅 (`:core:domain`) — US2·US3·US4가 쓴다

- [X] T005 [P] `PushMessageType` 열거 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/model/PushMessageType.kt`. 7종 멤버는 [data-model.md §1](data-model.md#1-pushmessagetype). **`UNKNOWN` 멤버를 두지 않는다** — 모르는 유형은 `PushMessage.type == null`로만 표현한다
- [X] T006 [P] `PushMessage` 모델 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/model/PushMessage.kt`. 필드 5개는 [data-model.md §2](data-model.md#2-pushmessage). `placeId` 필드를 두지 않는다([research.md D2](research.md#d2-장소-대상-알림의-도착지-방은-payload의-pinid로-정한다))
- [X] T007 [P] `PushDestination` sealed interface 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/model/PushDestination.kt`. `PlaceDetail(pinId)`·`RoomDetail(roomId)`·`NotificationTab` 세 갈래([data-model.md §3](data-model.md#3-pushdestination))
- [X] T008 `ParsePushMessageUseCaseTest` 작성 후 실패 확인 — `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ParsePushMessageUseCaseTest.kt`. 케이스는 [contracts/push-payload-contract.md §1](contracts/push-payload-contract.md#1-타입별-data-필드)의 표를 그대로 옮긴다 — 장소 대상 3종은 `targetId = data["pinId"]`이고 `placeId`는 버려진다 · 공동방 2종은 `data["roomId"]` · `SAVE_FAILED`·`NEARBY_PLACE_SUMMARY`는 `null` · 모르는 `type` 문자열은 `type == null`(EC-008) · `title`/`body` 누락은 빈 문자열 · `imageUrl` 누락은 `null`. 입력은 `Map<String, String>`이다(Android 타입 없음)
- [X] T009 `ParsePushMessageUseCase` 구현 — `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ParsePushMessageUseCase.kt`. 시그니처 `operator fun invoke(data: Map<String, String>): PushMessage`, 규칙은 [data-model.md §2 파싱 책임](data-model.md#2-pushmessage). T005·T006에 의존하고 T008을 통과시킨다
- [X] T010 `ResolvePushDestinationUseCaseTest` 작성 후 실패 확인 — `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ResolvePushDestinationUseCaseTest.kt`. 케이스는 [data-model.md §3](data-model.md#3-pushdestination) 라우팅 표의 행 전부 — 장소 3종 `targetId` 있음 → `PlaceDetail` · 공동방 2종 → `RoomDetail` · 위 5종에서 `targetId`가 `null`이거나 빈 문자열 → `NotificationTab`(EC-009) · `SAVE_FAILED` → `NotificationTab`(FR-009) · `NEARBY_PLACE_SUMMARY` → `NotificationTab`(FR-012, US4의 근거). `type == null` 입력은 호출자가 거르므로 케이스로 두지 않는다
- [X] T011 `ResolvePushDestinationUseCase` 구현 — `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ResolvePushDestinationUseCase.kt`. 시그니처 `operator fun invoke(message: PushMessage): PushDestination`. `when(message.type)`이 `null` 분기를 강제하므로 그 분기는 `NotificationTab`으로 둔다(방어적 — 계약상 호출되지 않는다). T005~T007에 의존하고 T010을 통과시킨다

### 2-B. `:core:notification` 골격 — US1·US2가 쓴다

- [X] T012 [P] `core/notification/src/main/AndroidManifest.xml` 작성 — `<service android:name=".MinoFirebaseMessagingService" android:exported="false">`에 `com.google.firebase.MESSAGING_EVENT` intent-filter를 단다. **`POST_NOTIFICATIONS`를 선언하지 않는다** — SDK 매니페스트 병합이 맡는다([research.md D12](research.md#d12-post_notifications-선언은-fcm-sdk의-매니페스트-병합에-맡긴다))
- [X] T013 `MinoFirebaseMessagingService` 골격 작성 — `core/notification/src/main/java/team/mino/core/notification/MinoFirebaseMessagingService.kt`. `@AndroidEntryPoint class ... : FirebaseMessagingService()`까지만 두고 `onNewToken`은 T027이, `onMessageReceived`는 T036이 얹는다. 주입 필드도 그 작업들이 더한다

**체크포인트**: `./gradlew :core:domain:test :app:assembleQaDebug`가 통과한다. 알림은 아직 아무것도 하지 않는다.

---

## Phase 3: 사용자 스토리 1 - 이 설치가 알림을 받을 수 있게 만들기 (FR-001~FR-005·FR-015)

**목표**: 세션 확보 직후와 토큰 갱신 시점에 FCM 등록 토큰을 서버에 등록한다. 성공도 실패도 화면에 남기지 않는다(UX-002).

**독립 테스트**: [quickstart.md §1](quickstart.md#1-토큰-등록-확인-fr-001fr-005-ts-001ts-005)의 절차 1~4로 `PUT /api/v1/users/me/push-token` 200을 Logcat에서 확인하고, 비행기 모드 실패 주입에서 오류 UI가 없음을 확인한다(TS-001~TS-005). 알림 수신(US2)이 없어도 서버 로그로 검증된다.

### 사용자 스토리 1 구현 — 도메인 계약

- [X] T014 [P] [US1] `PushRegistrationRepository` 인터페이스 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/repository/PushRegistrationRepository.kt`. `suspend fun registerCurrentToken()` 하나, 실패를 삼기고 `CancellationException`만 전파한다는 계약은 [data-model.md §4](data-model.md#4-pushregistrationrepository-계약만--구현은-coredata)
- [X] T015 [US1] `RegisterPushTokenUseCase` 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/usecase/RegisterPushTokenUseCase.kt`. 무인자 `suspend operator fun invoke()`가 `registerCurrentToken()`에 위임한다. 호출 지점 둘(Splash·`onNewToken`)이 이 하나를 공유하는 이유는 [research.md D5](research.md#d5-토큰-조회등록은-coredata에-기존-인증-제공자-패턴을-그대로-재사용한다). T014에 의존

### 사용자 스토리 1 구현 — 데이터 레이어 (`:core:data`)

- [X] T016 [P] [US1] FCM SDK 전용 예외 매핑 지점 작성 — `core/data/src/main/java/team/mino/core/data/push/extension/Task.kt`. `internal suspend fun <T> Task<T>.awaitDomain(): T` — `FirebaseNetworkException` → `MinoDomainException.Network`, 그 외는 원본 그대로 rethrow. 인증 제공자용(`core/data/src/main/java/team/mino/core/data/auth/extension/Task.kt`)을 **재사용하지 않고** 같은 형태로 새로 두는 이유는 [research.md D10](research.md#d10-firebase-messaging-sdk-예외의-도메인-매핑-지점을-새로-연다), 규약은 [error_handling.md §3](../../conventions/error_handling.md)
- [X] T017 [US1] `PushTokenProvider` 인터페이스와 `PushTokenProviderImpl` 작성 — `core/data/src/main/java/team/mino/core/data/push/PushTokenProvider.kt`·`PushTokenProviderImpl.kt`. `suspend fun currentToken(): String`이 주입받은 `FirebaseMessaging.token`을 T016의 `awaitDomain()`으로 기다린다. `IdTokenProviderImpl`(`core/data/src/main/java/team/mino/core/data/auth/IdTokenProviderImpl.kt`)과 같은 형태. T016에 의존
- [X] T018 [US1] `FirebaseMessagingModule`·`PushProviderModule` 작성 — `core/data/src/main/java/team/mino/core/data/push/di/FirebaseMessagingModule.kt`(`@Provides FirebaseMessaging.getInstance()`, `FirebaseAuthModule`과 같은 형태)·`PushProviderModule.kt`(`@Binds PushTokenProvider`). 둘 다 `SingletonComponent`·`internal`. T017에 의존
- [X] T019 [P] [US1] `PushTokenRequest` DTO 작성 — `core/data/src/main/java/team/mino/core/data/network/dto/request/PushTokenRequest.kt`. `@Serializable data class PushTokenRequest(val token: String)` ([contracts/push-token-api.md §1](contracts/push-token-api.md#1-엔드포인트))
- [X] T048 [US1] `UserApiService`에 `updatePushToken` 추가 — `core/data/src/main/java/team/mino/core/data/network/service/UserApiService.kt`. `suspend fun updatePushToken(request: PushTokenRequest)`가 `client.put("$USERS_ME_PATH/push-token")`을 호출하고 **응답 본문을 역직렬화하지 않는다**(2xx 여부만, [contracts/push-token-api.md §1](contracts/push-token-api.md#1-엔드포인트)). 지역 catch 없음(비2xx는 `convertDomainException`이 던진다). **새 서비스를 만들지 않는다** — 이 오퍼레이션은 `user` 태그라 그 소유자가 갖는다([ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md), [research.md D5](research.md#d5-토큰-조회등록은-coredata에-기존-인증-제공자-패턴을-그대로-재사용한다) 1.0.1 보정). 기존 `UserApiServiceTest`(`core/data/src/test/java/team/mino/core/data/network/UserApiServiceTest.kt`)가 그대로 통과하는지 확인한다. T019에 의존
- [X] T049 [US1] `UserRemoteDataSource`·`UserRemoteDataSourceImpl`에 `registerPushToken` 추가 — `core/data/src/main/java/team/mino/core/data/datasource/UserRemoteDataSource.kt`·`UserRemoteDataSourceImpl.kt`. `suspend fun registerPushToken(token: String)`이 T048을 `PushTokenRequest(token)`으로 감싼다. 자기가 안 쓰는 기존 함수(`isRegistered`·`getMe`·`register`·`updateMe`)의 계약은 건드리지 않는다(ADR §결과). DI는 기존 `UserDataSourceModule`이 그대로 맡으므로 새 모듈을 두지 않는다. 기존 `UserRemoteDataSourceImplTest`가 그대로 통과하는지 확인한다. T048에 의존
- [X] T023 [US1] `PushRegistrationRepositoryImpl` 구현 — `core/data/src/main/java/team/mino/core/data/repository/PushRegistrationRepositoryImpl.kt`. `UserRemoteDataSource`·`PushTokenProvider`를 주입받아 `registerCurrentToken()` = `runCatchingDomain { userRemote.registerPushToken(provider.currentToken()) }.onDomainFailure { /* 알릴 곳 없음 */ }` — `PlaceRepositoryImpl.recordAccess`(`core/data/src/main/java/team/mino/core/data/repository/PlaceRepositoryImpl.kt`)와 같은 형태([contracts/push-token-api.md §2](contracts/push-token-api.md#2-데이터-흐름), FR-004). 매퍼·로컬 저장·재시도 큐를 두지 않는다. T014·T017·T049에 의존
- [X] T024 [US1] `PushRegistrationRepositoryModule` 작성 — `core/data/src/main/java/team/mino/core/data/repository/di/PushRegistrationRepositoryModule.kt` (`@Binds`). T023에 의존

### 사용자 스토리 1 구현 — 호출 지점 둘

- [X] T025 [US1] `SplashViewModel`에 세션 확보 직후 토큰 등록 호출 배선 — `feature/splash/src/main/java/team/mino/feature/splash/main/vm/SplashViewModel.kt`. `RegisterPushTokenUseCase`를 주입받아 `awaitEntry()` 안에서 `ensureAnonymousSession()`이 성공한 직후 `launchSafely { registerPushToken() }`로 fire-and-forget 한다(진입 판정을 기다리게 하지 않는다 — SC-003과 무관하지만 스플래시 지연을 만들지 않기 위해). **앱 시작마다 한 번**이므로 재시도 루프가 돌아도 두 번 부르지 않도록 가드한다(D5, spec §4 가정). 세션이 없으면 호출되지 않는다(EC-003). 실패는 Repository가 삼키므로 이 자리에서 `runCatchingDomain`을 다시 두지 않는다(UX-002). T015에 의존
- [X] T026 [US1] `SplashViewModelTest` 생성자 갱신 — `feature/splash/src/test/java/team/mino/feature/splash/main/vm/SplashViewModelTest.kt`. 테스트 파일 안에 `private class FakePushRegistrationRepository : PushRegistrationRepository`(호출 횟수만 기록)를 두고 `RegisterPushTokenUseCase(fake)`를 넘긴다. 기존 케이스가 그대로 통과하는지 확인한다. 새 케이스는 plan의 테스트 범위 밖이라 더하지 않는다. T025에 의존
- [X] T027 [US1] `MinoFirebaseMessagingService.onNewToken` 구현 — `core/notification/src/main/java/team/mino/core/notification/MinoFirebaseMessagingService.kt`. `RegisterPushTokenUseCase`를 주입받아 호출한다. **인자로 온 토큰 값은 쓰지 않고 다시 조회한다**([research.md D5](research.md#d5-토큰-조회등록은-coredata에-기존-인증-제공자-패턴을-그대로-재사용한다)). 콜백이 non-suspend이므로 코루틴으로 띄운다 — 실패는 Repository가 삼켜 CEH 경로가 없다. T013·T015에 의존

### 사용자 스토리 1 검증

- [ ] T028 [US1] [quickstart.md §1](quickstart.md#1-토큰-등록-확인-fr-001fr-005-ts-001ts-005) 실기기 검증 — 절차 1~4(TS-001·TS-003), 실패 주입(TS-004·EC-004), 토큰 폐기 요청이 나가지 않음(TS-005 — Logcat에 `DELETE`/폐기 요청이 없음). TS-002(갱신)는 `onNewToken`을 임의로 일으킬 수 없으므로 코드 리뷰로 T027을 확인하는 것으로 갈음한다. 결과를 이 줄 아래에 기록한다

**체크포인트**: 서버가 이 설치의 토큰을 보유한다(SC-001). 이것만으로 서버가 알림을 보낼 수 있다.

---

## Phase 4: 사용자 스토리 2 - 앱 밖에서 알림을 받아 열기 (FR-006~FR-010·FR-013·FR-014)

**목표**: data-only FCM 메시지를 단일 채널 시스템 알림으로 띄우고, 탭하면 `MainActivity`(`singleTask`)가 받아 유형별 도착지(장소 상세·방 상세·알림 탭)에 도달한다 — 앱이 살아 있으면 `onNewIntent`로 스플래시 없이, 프로세스가 죽어 있으면 `SplashActivity`로 우회해 시작 경로를 거친 뒤(D13~D15).

**독립 테스트**: [quickstart.md §2](quickstart.md#2-알림-표시탭-진입-확인-fr-006fr-014-유저-플로우-24)의 표에서 "백그라운드 수신"·"종료 상태 수신"·"장소 알림 탭"·"공동방 알림 탭"·"저장 오류 알림 탭"·"종료 상태에서 탭"·"탭한 알림 소거"·"권한 없음"·"채널 단일 확인"·"모르는 유형" 행과 "콜드 스타트 백스택 확인"(TS-006~TS-014, EC-008). 서버 또는 테스트 발송 수단으로 실제 data 메시지를 보내야 한다.

**⚠️ 소그룹 4-A는 나머지 모두의 전제다.** 4-B·4-C·4-D·4-E는 4-A 산출물만 있으면 서로 병렬이다. plan 1.1.0에서 4-A가 셋에서 다섯(T051·T052 추가)으로 늘었고, 알림의 대상이 `SplashActivity`에서 `MainActivity`로 바뀌었다(D13~D15).

### 4-A. 전환 계약 (`:core:navigation`) — 4-B~4-E 전부가 쓴다

- [X] T029 [P] [US2] `ExtraTag.kt`에 딥링크 extra 키 2개와 값 상수 3개 추가 — `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/ExtraTag.kt`. 이름과 문자열은 [contracts/push-deeplink-contract.md §1](contracts/push-deeplink-contract.md#1-intent-extra-키-corenavigation--activitylauncherextratagkt에-추가). 대상 feature 접두어를 붙이지 않는 이유가 그 절에 있으므로 파일 주석에 다시 풀어쓰지 않고 계약을 지목한다
- [X] T030 [P] [US2] `SplashDeepLinkIntentFactory` 인터페이스 생성 — `core/navigation/src/main/java/team/mino/core/navigation/deeplink/SplashDeepLinkIntentFactory.kt`. `fun create(context: Context): Intent` 하나([계약 §2](contracts/push-deeplink-contract.md#2-intent-팩토리-둘-corenavigation--deeplink)). plan 1.1.0부터 소비자는 `:core:notification`이 아니라 `:feature:main`의 콜드 우회(T038)다
- [X] T051 [P] [US2] `MainDeepLinkIntentFactory` 인터페이스 생성 — `core/navigation/src/main/java/team/mino/core/navigation/deeplink/MainDeepLinkIntentFactory.kt`. `fun create(context: Context): Intent` 하나, `SplashDeepLinkIntentFactory`와 같은 형태([계약 §2](contracts/push-deeplink-contract.md#2-intent-팩토리-둘-corenavigation--deeplink), [research.md D13](research.md#d13-알림-탭은-mainactivity를-겨냥하고-게이트-통과-여부는-탭-시점에-수신자가-판정한다)). `:core:notification`이 `PendingIntent`를 만들 때 쓴다
- [X] T052 [P] [US2] `MainEntryGate` 생성 — `core/navigation/src/main/java/team/mino/core/navigation/entry/MainEntryGate.kt`. `@Singleton`, `isPassed`·`markPassed()`만 갖는다. "스플래시가 Main 진입을 확정했다"는 사실 하나만 담고 온보딩·프로필 판정을 넣지 않는다([계약 §3](contracts/push-deeplink-contract.md#3-mainentrygate-corenavigation--entry), D13, [ADR 2026-08-29](../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md))
- [X] T031 [P] [US2] `RoomDetailRequestHolder` 생성 — `core/navigation/src/main/java/team/mino/core/navigation/entry/RoomDetailRequestHolder.kt`. `@ActivityRetainedScoped`, `pending: StateFlow<String?>`, `request(roomId)`·`consume()`. `PlaceDetailRequestHolder`(같은 디렉터리)를 본뜨되 **`origin`을 싣지 않는다**([research.md D8](research.md#d8-방-상세의-탭-간-진입은-placedetailrequestholder를-그대로-본떠-새로-만든다)). 배치 기준은 [core/navigation README §2.3](../../../core/navigation/README.md)

### 4-B. 딥링크 Intent 공급·전달·이어받기 (`:feature:splash`)

- [X] T032 [US2] `SplashDeepLinkIntentFactoryImpl`과 `SplashDeepLinkModule` 작성 — `feature/splash/src/main/java/team/mino/feature/splash/di/SplashDeepLinkIntentFactoryImpl.kt`·`SplashDeepLinkModule.kt`(`di/` 디렉터리 신설). `Intent(context, SplashActivity::class.java)`에 **`FLAG_ACTIVITY_CLEAR_TOP`**을 건다 — 진행 중인 스플래시가 있으면 위의 우회용 `MainActivity`를 정리하고 그 스플래시가 `onNewIntent`로 받는다([계약 §2 표](contracts/push-deeplink-contract.md#2-intent-팩토리-둘-corenavigation--deeplink), [D16](research.md#d16-스플래시가-진행-중일-때의-알림-탭은-그-스플래시가-이어받는다)). `NEW_TASK`는 걸지 않는다. 모듈은 **`SingletonComponent`**다. T030에 의존
- [X] T055 [P] [US2] `SplashActivity`에 `android:launchMode="singleTop"` 선언 — `feature/splash/src/main/AndroidManifest.xml`. 우회 Intent의 `CLEAR_TOP`과 짝을 이뤄 진행 중인 스플래시가 재생성 대신 `onNewIntent`를 받게 한다([D16](research.md#d16-스플래시가-진행-중일-때의-알림-탭은-그-스플래시가-이어받는다)). 런처 재탭 경로에는 영향이 없다(D16 영향 범위)
- [X] T033 [US2] `SplashActivity`에 딥링크 extra 전달·게이트 통과 표시·`onNewIntent` 갱신 배선 — `feature/splash/src/main/java/team/mino/feature/splash/SplashActivity.kt`. (1) `EXTRA_PUSH_DESTINATION_TYPE`·`EXTRA_PUSH_DESTINATION_ID`를 **필드로** 보관하고 `onCreate`에서 읽는다. (2) `onNavigateToMain`에서 `mainEntryGate.markPassed()` 직후 `mainLauncher.launch(this, withFinish = true) { putExtra(...) }`로 그대로 싣는다. **판단하지 않는다.** (3) `onNewIntent`에서 `setIntent` 후 같은 두 extra를 다시 읽어 필드를 갱신한다 — `SplashViewModel`은 건드리지 않는다(진행 중인 세션 확보·최소 노출 유지, [D16](research.md#d16-스플래시가-진행-중일-때의-알림-탭은-그-스플래시가-이어받는다)). 온보딩 갈래(`onNavigateToOnboarding`)는 extra를 버리고 게이트도 켜지 않는다([계약 §3](contracts/push-deeplink-contract.md#3-mainentrygate-corenavigation--entry)). T029·T052에 의존

### 4-C. 알림 표시 (`:core:notification`)

- [X] T034 [P] [US2] `PushNotificationChannel` 작성 — `core/notification/src/main/java/team/mino/core/notification/PushNotificationChannel.kt`. 채널 ID 상수 하나와 `ensureCreated(context)` — `NotificationManagerCompat.createNotificationChannel`로 **알림을 만들기 직전 멱등 생성**한다([research.md D3](research.md#d3-알림-채널은-지연-생성한다)). 채널은 하나뿐이다(FR-008·UX-006). 채널 표시 이름은 `core/notification/src/main/res/values/strings.xml`의 문자열 리소스로 둔다
- [X] T035 [US2] `PushNotificationBuilder` 작성 — `core/notification/src/main/java/team/mino/core/notification/PushNotificationBuilder.kt`. `PushMessage` + `PendingIntent` → `Notification`. `title`·`body`를 그대로 싣고(FR-007), `imageUrl`이 있으면 `BigPictureStyle`([contracts/push-payload-contract.md §3](contracts/push-payload-contract.md#3-알림-표시-규칙과의-연결)), `setAutoCancel(true)`(UX-004), `setSmallIcon`은 런처 아이콘 임시 재사용([research.md D11](research.md#d11-알림-아이콘은-기존-런처-아이콘을-임시로-재사용한다)) — 이 모듈은 `:app`의 `R`을 볼 수 없으므로 `context.applicationInfo.icon`으로 런타임에 얻는다([미결 사항](#미결-사항) 3). T034에 의존(채널 ID)
- [X] T036 [US2] `MinoFirebaseMessagingService.onMessageReceived` 구현 — `core/notification/src/main/java/team/mino/core/notification/MinoFirebaseMessagingService.kt`. 순서: `remoteMessage.data`만 읽는다(D1) → `ParsePushMessageUseCase` → `type == null`이면 조용히 반환(EC-008, [payload 계약 §4](contracts/push-payload-contract.md#4-type이-이-표에-없을-때ec-008)) → `ResolvePushDestinationUseCase` → `MainDeepLinkIntentFactory.create(this)`에 extra 인코딩([딥링크 계약 §1·§2 호출](contracts/push-deeplink-contract.md#2-intent-팩토리-둘-corenavigation--deeplink), `NotificationTab`은 ID 생략) → `PendingIntent.getActivity(FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)`, `requestCode`는 `targetId ?: type`의 해시 → T034 채널 보장 → T035로 빌드 → `notify(id, ...)`. **알림 ID도 건마다 달라야 한다**(EC-011·UX-003 — 덮어쓰지 않는다). **앱 상태(foreground/background)를 판정하는 코드를 두지 않는다**(FR-011, US3의 근거) — 상태 판정은 탭 시점에 `MainActivity`가 한다(D13). 대기·지연을 넣지 않는다(SC-003). T009·T011·T013·T029·T051·T034·T035에 의존

### 4-D. 도착지 소비 — 탭 셸 (`:feature:main`)

- [X] T053 [P] [US2] `MainDeepLinkIntentFactoryImpl`과 `MainDeepLinkModule` 작성 — `feature/main/src/main/java/team/mino/feature/main/di/MainDeepLinkIntentFactoryImpl.kt`·`MainDeepLinkModule.kt`. `Intent(context, MainActivity::class.java)`에 `FLAG_ACTIVITY_NEW_TASK`만 건다(`CLEAR_TOP` 없음 — [research.md D14](research.md#d14-mainactivity는-singletask이고-딥링크-intent는-new_task만-건다)). `SingletonComponent`, 기존 `MainLauncherImpl`·`MainNavigationModule`과 같은 디렉터리([계약 §2 표](contracts/push-deeplink-contract.md#2-intent-팩토리-둘-corenavigation--deeplink)). T051에 의존
- [X] T054 [P] [US2] `MainActivity`에 `android:launchMode="singleTask"` 선언 — `feature/main/src/main/AndroidManifest.xml`. `onNewIntent`가 오려면 필수다(D14). 영향 범위 셋(런처 경로·`RoomForm` 결과 요청·위에 뜬 Activity 정리)은 D14가 검토했다
- [X] T037 [P] [US2] `MainShell`·`MainNavHost`에 `startTab`·`pendingTab` 파라미터 추가 — `feature/main/src/main/java/team/mino/feature/main/MainShell.kt`·`MainNavHost.kt`. `startTab: MainTab = MainTab.HOME`은 `startDestination = startTab.route`(콜드, [D9](research.md#d9-알림-탭-목록-도착지는-maintabnotification을-그대로-쓴다)). `pendingTab: MainTab?`·`onPendingTabConsumed`는 `MainShell`의 `LaunchedEffect(pendingTab)`에서 `navController.navigateToTab(it)` 후 비운다(웜, [D15](research.md#d15-웜-경로의-탭-전환은-명령형이고-대기-중인-도착지-탭을-mainactivity가-상태로-든다), [계약 §5](contracts/push-deeplink-contract.md#5-mainshellmainnavhost--시작-탭-파라미터와-대기-탭-소비)). 기존 호출부는 기본값으로 그대로 컴파일된다
- [X] T038 [US2] `MainActivity`에 콜드 우회·웜 소비 배선 — `feature/main/src/main/java/team/mino/feature/main/MainActivity.kt`. (1) `RoomDetailRequestHolder`·`MainEntryGate`·`SplashDeepLinkIntentFactory`를 추가 주입. (2) `onCreate`에서 `super.onCreate` 직후·**`setContent` 전에** `intent.getStringExtra(EXTRA_PUSH_DESTINATION_TYPE) != null && !mainEntryGate.isPassed`면 TYPE·ID 두 extra만 실어 `SplashActivity`를 열고 `finish()`·`return`(콜드 우회, D13 — 범위는 푸시 extra가 있을 때만. 플래그는 팩토리가 건다, T032). (3) 아니면 `resolvePendingPushDestination()`으로 `startTab`을 정해 `MainShell`에 넘긴다. (4) `onNewIntent`에서 `setIntent` 후 같은 함수를 불러 `pendingTab` 상태를 세운다(웜, D15). 소비 함수는 `PLACE` → `placeDetailRequestHolder.request(id, NOTIFICATION)` + `SAVED`, `ROOM` → `roomDetailRequestHolder.request(id)` + `SAVED`, 그 외·`id == null` → `NOTIFICATION`, extra 없음 → `null`. 읽은 뒤 `intent.removeExtra`로 소비 표시([계약 §4](contracts/push-deeplink-contract.md#4-mainactivity--extra를-소비해-요청-홀더도착지-탭을-정한다)). T029·T030·T031·T052·T037에 의존

### 4-E. 도착지 소비 — 방 상세 (`:feature:room`)

- [X] T039 [US2] `RoomListViewModel`에 `observeRoomDetailRequests()` 추가 — `feature/room/src/main/java/team/mino/feature/room/main/vm/RoomListViewModel.kt`. `RoomDetailRequestHolder`를 주입받아 `init`에서 `observePlaceDetailRequests()` 옆에 한 번만 연다. `pending.filterNotNull().collect { consume(); updateState { copy(selectedRoomId = it) } }` — **방 조회를 다시 하지 않는다**([계약 §6](contracts/push-deeplink-contract.md#6-roomdetailrequestholder-소비-featureroom--roomlistviewmodel)). 방이 목록에 없거나 접근 불가인 경우는 이 feature 범위 밖(EC-010). T031에 의존
- [X] T040 [US2] `RoomListViewModelTest` 생성자 갱신 — `feature/room/src/test/java/team/mino/feature/room/main/vm/RoomListViewModelTest.kt`. `RoomDetailRequestHolder()`를 생성해 넘긴다(무인자 생성자). 기존 케이스가 그대로 통과하는지 확인한다. 새 케이스는 plan의 테스트 범위 밖이라 더하지 않는다. T039에 의존

### 사용자 스토리 2 검증

- [ ] T041 [US2] [quickstart.md §2](quickstart.md#2-알림-표시탭-진입-확인-fr-006fr-014-유저-플로우-24) 실기기 검증 — 먼저 [§0](quickstart.md#0-선행-조건)대로 알림 권한을 수동 허용한다. 발송은 [§2.0](quickstart.md#20-발송-수단)의 실사용 시나리오(인스타그램 공유·초대 링크)를 1차 수단으로 쓰고, 합성 발송이 필요한 행(대표 알림·모르는 유형)은 서비스 계정 키가 없으면 미검증으로 기록한다. §2.1 표의 TS-006·TS-007·TS-008·TS-009·TS-010·TS-011·TS-012·TS-013·TS-014·EC-008 행, **"실행 중 탭"(스플래시 없이 상태 보존, D13·D15)·"스플래시 진행 중 탭"(스플래시 한 번·세션 확보 한 번, D16)·"폼을 열어 둔 채 탭"(D14) 행**, 그리고 "백스택 확인"(콜드 우회의 첫 `MainActivity`가 스택에 남지 않음). 특히 TS-008로 [contracts/push-payload-contract.md](contracts/push-payload-contract.md)가 전제한 `pinId` 필드가 실제 서버 payload에 실려 오는지 재확인한다. 결과를 이 줄 아래에 기록한다

**체크포인트**: 알림을 눌러 세 도착지에 각각 도달한다(SC-004·SC-005). US1과 합쳐 이 feature의 MVP다.

---

## Phase 5: 사용자 스토리 3 - 앱을 보고 있는 동안 알림이 도착한다 (FR-011·UX-001)

**목표**: 앱을 쓰는 중에도 앱 밖과 똑같이 시스템 알림이 뜨고, 보고 있던 화면은 그대로다.

**독립 테스트**: [quickstart.md §2](quickstart.md#2-알림-표시탭-진입-확인-fr-006fr-014-유저-플로우-24) "사용 중 수신" 행(TS-015·TS-016)과 EC-013·EC-014.

**코드 작업이 없다.** [research.md D1](research.md#d1-fcm-메시지는-data-only-페이로드를-전제한다)(data-only라 `onMessageReceived`가 상태와 무관하게 항상 불린다)과 T036의 "앱 상태를 판정하지 않는다"가 이 스토리를 성립시킨다. 별도 foreground 분기를 만드는 것이 오히려 FR-011 위반이다.

- [X] T042 [US3] 사용 중 수신 실기기 검증 — 임의 화면(장소 상세·지도·폼 입력 중)을 보는 중에 data 메시지를 보내 알림이 뜨고 화면 전환·입력 중단이 없음을 확인한다(TS-015·TS-016·SC-008). 알림 탭을 보고 있을 때 목록이 자동 갱신되지 않아도 정상이다(EC-013 — [SCR-007] 소관). TS-017(알림함에 남는다)은 알림 탭 화면(#160)이 없어 검증 불가이므로 그 사실을 기록한다. 결과를 이 줄 아래에 기록한다
  - **결과(2026-09-04, 실기기)**: TS-015·TS-016·SC-008 통과 — 앱을 보고 있는 중에 알림이 떴고 보고 있던 화면은 전환·입력 중단 없이 그대로였다(FR-011·UX-001).
  - **발송 수단**: 중복저장 알림(`PIN_DUPLICATED`) — [quickstart.md §2.0](quickstart.md#20-발송-수단)의 실사용 경로(인스타그램 재공유)로 **실제 서버가 보낸 메시지**다. 합성 발송이 아니다.
  - **함께 확정된 것**: 서버가 `data`에 `type`을 싣고 그 문자열이 `PushMessageType`의 멤버와 일치한다([contracts/push-payload-contract.md §1](contracts/push-payload-contract.md#1-타입별-data-필드)). `type`이 파싱되지 않았다면 알림 자체가 뜨지 않는다(EC-008).
  - **아직 미확정**: 서버가 `notification` 블록을 함께 싣는지는 이 검증으로 알 수 없다 — 앱이 떠 있을 때는 블록이 있어도 `onMessageReceived`가 불리기 때문이다. 백그라운드 동작을 가르는 전제이므로 T041이 판정한다([research.md D1](research.md#d1-fcm-메시지는-data-only-페이로드를-전제한다)). `pinId` 유무도 탭해 봐야 확정된다(T041 TS-008).
  - **TS-017 미검증**: 알림이 알림함에 남는지는 알림 탭 화면(#160)이 아직 없어 확인할 수 없다.
  - **EC-013 해당 없음**: 같은 이유로 알림 탭 목록의 자동 갱신 여부를 볼 대상이 없다([SCR-007] 소관).

**체크포인트**: 세 스토리가 동작한다.

---

## Phase 6: 사용자 스토리 4 - 위치 기반 대표 알림을 받아 알림 탭으로 들어오기 (FR-012)

**목표**: `NEARBY_PLACE_SUMMARY` 한 건이 알림 영역에 뜨고, 탭하면 알림 탭 목록으로 간다.

**독립 테스트**: [quickstart.md §2](quickstart.md#2-알림-표시탭-진입-확인-fr-006fr-014-유저-플로우-24) "대표 알림" 행(TS-018·TS-019).

**코드 작업이 없다.** 유형 멤버(T005)와 `NotificationTab` 라우팅(T010·T011)이 Phase 2에 있고, 알림 탭 진입은 T037·T038이 한다. 몇 건을 묶을지는 서버가 정한다(EC-016, spec §3.2).

- [ ] T043 [US4] 대표 알림 실기기 검증 — [quickstart.md §2.0](quickstart.md#20-발송-수단)의 합성 발송으로 `NEARBY_PLACE_SUMMARY` 예시를 보내(키가 없으면 미검증으로 기록) 알림 1건이 뜨고(TS-018·SC-007) 탭하면 알림 탭 placeholder가 보이는지(TS-019) 확인한다. 개별 리마인드가 함께 오면 각각 표시되는지(EC-016)도 본다. 결과를 이 줄 아래에 기록한다

**체크포인트**: 네 스토리가 모두 동작한다.

---

## Phase 7: 마무리 및 공통 관심사

**목적**: plan이 범위 가드 때문에 미뤄 둔 SSOT 문서 갱신과 결정 기록, 신규 모듈 README, 품질 게이트.

- [X] T044 [P] `docs/architecture/modularization.md`에 `:core:notification` 항목 추가 — 「모듈 구성」의 `:core:analytics` 다음에 같은 형식으로("FCM — `FirebaseMessagingService`·알림 채널·딥링크 `PendingIntent` 조립. → Android Library"), 「의존성 흐름 › 그래프」에 `:core:domain`·`:core:navigation`으로 향하는 간선을 더한다. plan 헌법 게이트 원칙 I이 유예한 항목([plan.md §헌법 준수 확인 게이트](plan.md), [research.md D4 주의](research.md#d4-fcm-sdk-통합은-새-모듈-corenotification에-둔다))
- [X] T050 [P] `core/notification/README.md` 작성과 `CLAUDE.md` 표 행 추가 — 기존 core 모듈 README(예: `core/analytics/README.md`)와 같은 6절 구조를 따르되 짧게 둔다. 핵심은 §확장 규칙이다: 새 알림 유형은 `PushMessageType`(`:core:domain`)·[data-model.md §3](data-model.md#3-pushdestination) 라우팅 표·[payload 계약](contracts/push-payload-contract.md)을 고치고 이 모듈은 건드리지 않는다 / 아이콘 교체는 `PushNotificationBuilder` 한 곳 / 채널은 하나를 유지한다(FR-008). 같은 작업 안에서 `CLAUDE.md` 표에 "`core:notification` 모듈 사용·확장" 행을 넣는다(헌법 원칙 I). 규칙 본문을 다시 풀어쓰지 않고 spec·계약을 지목한다
- [X] T045 [P] `docs/conventions/error_handling.md` §3 매핑 정책 표에 "FCM Messaging SDK" 원천 행 추가 — 매핑 지점은 `core:data`의 `push/extension/Task.kt`(`awaitDomain`), 예외 표에는 `FirebaseNetworkException` → `Network` 한 줄. [research.md D10 후속 문서화](research.md#d10-firebase-messaging-sdk-예외의-도메인-매핑-지점을-새로-연다)
- [X] T046 [P] `:core:notification` 신설 ADR 작성 — `docs/adr/2026-09-03-core-notification-module.md`(파일명 날짜는 작성 시점으로). "SDK 하나를 감싸는 전용 core 모듈" 선례를 따르되 토큰 등록은 `:core:data`에 두는 경계 판단([research.md D4·D5](research.md), [plan.md §복잡도 추적](plan.md))을 기록한다. 형식은 [docs/adr/README.md](../../adr/README.md), 작성은 `adr-writer` 스킬을 쓴다. plan 헌법 게이트 원칙 III이 승격 대상으로 지목한 항목
- [ ] T047 품질 게이트 실행 — `./gradlew :core:domain:test :core:data:test :feature:splash:test :feature:profile:test :feature:room:test :app:assembleQaDebug lintDebug`. `:feature:profile`·`:feature:splash`는 T048·T049가 넓힌 `UserApiService`·`UserRemoteDataSource`의 회귀 대상이라 포함한다([ADR 2026-08-28 §결과](../../adr/2026-08-28-api-service-owned-per-server-tag.md)). `lintDebug`가 JBR 크래시로 멈추면 `-Dorg.gradle.jvmargs=-XX:-TieredCompilation`으로 재실행한다. Compose Lint 위반은 [compose-lint.md](../../conventions/compose-lint.md)대로 처리한다. T001~T046·T050에 의존

---

## 미결 사항

최초 작성 시 4건을 남겼고 2026-09-03 같은 날 사용자 결정으로 전부 닫았다. 기록으로 남겨 둔다 — 현재 열린 항목은 없다.

1. ~~**`PushApiService`·`PushRemoteDataSource` 신설이 [ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md)과 충돌한다.**~~ **해소 (plan 1.0.1, 2026-09-03).** `PUT /api/v1/users/me/push-token`은 OpenAPI `user` 태그·`uH_updatePushToken`이라 ADR대로 `UserApiService`·`UserRemoteDataSource`를 넓히는 것으로 확정했다. 미착수였던 T020~T022를 지우고 T048·T049로 대체했으며, 회귀 범위는 T047이 든다.
2. ~~**`POST_NOTIFICATIONS` `<uses-permission>` 선언의 소유자가 정해지지 않았다.**~~ **해소 (2026-09-03).** `firebase-messaging` AAR이 그 선언을 이미 갖고 있어 매니페스트 병합으로 앱에 들어온다. 이 feature는 선언·요청 어느 쪽도 하지 않고, 런타임 허용은 #152, 실기기 검증은 수동 허용(`adb shell pm grant`)을 전제한다 — [research.md D12](research.md#d12-post_notifications-선언은-fcm-sdk의-매니페스트-병합에-맡긴다), [quickstart.md §0](quickstart.md#0-선행-조건). T012·T041 문구를 맞췄다.
3. ~~**D11의 런처 아이콘 참조 방법.**~~ **해소 (2026-09-03).** `context.applicationInfo.icon` 런타임 조회로 확정하고 [research.md D11](research.md#d11-알림-아이콘은-기존-런처-아이콘을-임시로-재사용한다)에 참조 방법을 보강했다. T035는 이미 그대로 적혀 있다.
4. ~~**`core/notification/README.md`.**~~ **해소 (2026-09-03).** 다른 core 모듈과 같은 구조의 짧은 README와 `CLAUDE.md` 표 행을 T050으로 추가했다. plan 1.0.1 §프로젝트 구조에도 줄을 더했다.

### plan이 완료 보고에서 제안한 spec 개정 (이 문서의 작업이 아님)

- **FR-013 PATCH** — 장소 대상 알림의 도착지 키를 `pinId`로 쓰는 [research.md D2](research.md#d2-장소-대상-알림의-도착지-방은-payload의-pinid로-정한다) 결정을 spec에 반영. 요구사항 경계는 그대로다.
- **§4 가정 갱신** — "서버의 토큰 등록 API는 아직 존재하지 않는다"는 2026-09-03 조회로 해소됐다([contracts/push-token-api.md §3](contracts/push-token-api.md#3-서버팀-협의가-필요-없는-이유)).

---

## 요구사항 커버리지

| 요구사항 | 작업 |
|---|---|
| FR-001 세션 확보 후 토큰 확보 | T014·T015·T017·T025 |
| FR-002 서버 등록 | T019·T048·T049·T023·T024 |
| FR-003 갱신 토큰 재등록 | T027 |
| FR-004 실패 비노출·비차단 | T016·T023·T025 |
| FR-005 권한과 무관하게 등록 | T025(권한 조회 없음)·T028 TS-003 |
| FR-006 시스템 알림 표시 | T012·T013·T034~T036 |
| FR-007 문구 그대로 | T035 |
| FR-008 단일 채널 | T034 |
| FR-009 유형별 도착지 | T007·T010·T011·T036·T038·T039·T051·T053 |
| FR-010 종료 상태 → 시작 경로 경유 | T030·T032·T033·T052·T038(콜드 우회, `setContent` 전)·T055(스플래시 진행 중이면 이어받음) |
| FR-011 사용 중 동일 표시·화면 비전환 | T036·T042 |
| FR-012 대표 알림 → 알림 탭 | T005·T011·T038·T043 |
| FR-013 유형+식별자 하나로 판정, 불가 시 알림 탭 | T006·T009·T011·T038 (spec 개정 제안 D2) |
| FR-014 권한 요청·대체 표시 없음 | T036(요청 코드 없음)·T041 TS-013 |
| FR-015 폐기 요청 없음 | T048(PUT만)·T028 TS-005 |
| UX-001~UX-006 | T042 · T025 · T036 · T035 · T041(UX-005는 도착지 화면 규칙, 코드 없음) · T034 |
| EC-001~EC-005 | T023(삼킴)·T025(세션 전제)·T028 |
| EC-006~EC-012 | T041 · T036(EC-008·EC-011) · T010(EC-009) · T039(EC-010 범위 밖) |
| EC-013~EC-016 | T042 · T043 |
| SC-001~SC-008 | T028(SC-001·006) · T041(SC-004·005 — 실행 중 탭은 스플래시 없이, T037·T054) · T036(SC-003) · T043(SC-007) · T042(SC-008) · SC-002는 T027 리뷰 |

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **셋업 (Phase 1)**: 의존성 없음. T001·T002·T004는 병렬, T003은 T002 뒤.
- **기반 작업 (Phase 2)**: Phase 1 완료에 의존. 2-A(도메인)는 실제로는 Phase 1과 무관하게 시작할 수 있다(`:core:domain`은 신규 모듈을 모른다). 2-B는 T003·T004 뒤.
- **US1 (Phase 3)**: 2-B의 T013만 필요하다(T027). 도메인·데이터 작업(T014~T019·T048·T049·T023·T024)은 Phase 2와 무관하게 시작할 수 있다.
- **US2 (Phase 4)**: 2-A 전체(T005~T011)와 2-B(T012·T013)에 의존. 4-A(T029·T030·T031·T051·T052)가 4-B~4-E의 전제.
- **US3·US4 (Phase 5·6)**: 코드 작업이 없고 US2 완료에 의존한다.
- **마무리 (Phase 7)**: T044~T046·T050은 Phase 1이 끝나면 언제든 가능(문서), T047은 전부 완료 뒤.

### 사용자 스토리 간 의존성

- **US1**: 다른 스토리에 의존하지 않는다. `MinoFirebaseMessagingService.kt`를 US2와 공유하므로 T027과 T036은 순차다.
- **US2**: US1에 의존하지 않는다 — 토큰 등록 없이도 Firebase 콘솔로는 알림 도달 검증이 안 되지만(data-only 제약, quickstart §2), 서버 발송 수단이 있으면 독립 검증된다.
- **US3·US4**: US2에 의존한다(같은 코드 경로의 검증).

### 각 사용자 스토리 내부

- 테스트(T008·T010)는 구현(T009·T011) 전에 작성하고 실패를 확인한다.
- US1: 도메인 계약(T014·T015) → 데이터(T016~T019·T048·T049·T023·T024) → 호출 지점(T025~T027) → 검증(T028). 데이터 안에서는 매핑 지점(T016) → Provider(T017·T018) ∥ DTO·API·DataSource(T019 → T048 → T049) → Repository(T023·T024).
- US2: 계약(4-A) → 공급자·소비자(4-B~4-E 병렬) → 검증(T041).

### 병렬 처리 기회

- Phase 1: T001 ∥ T002 ∥ T004
- Phase 2: T005 ∥ T006 ∥ T007 ∥ T012, 그리고 2-A 전체 ∥ 2-B 전체
- US1: T014 ∥ T016 ∥ T019 (서로 다른 모듈·파일)
- US2: T029 ∥ T030 ∥ T031 ∥ T051 ∥ T052 ∥ T055 → 이후 T032 ∥ T053 ∥ T054 ∥ T034 ∥ T037, T033 ∥ T035 ∥ T039 → T036(T051·T034·T035 뒤) · T038(T052·T037 뒤)
- Phase 7: T044 ∥ T045 ∥ T046 ∥ T050

---

## 병렬 실행 예시: 사용자 스토리 2

```bash
# 4-A 전환 계약 다섯을 함께 (서로 다른 파일):
Task: "ExtraTag.kt에 EXTRA_PUSH_DESTINATION_* 키·값 상수 추가"                      # T029
Task: "deeplink/SplashDeepLinkIntentFactory.kt 인터페이스 생성"                       # T030
Task: "entry/RoomDetailRequestHolder.kt 생성"                                        # T031
Task: "deeplink/MainDeepLinkIntentFactory.kt 인터페이스 생성"                         # T051
Task: "entry/MainEntryGate.kt 생성"                                                  # T052

# 4-A가 끝나면 공급자·소비자를 함께 (모듈이 다르다):
Task: "feature/splash/di/SplashDeepLinkIntentFactoryImpl.kt + SplashDeepLinkModule.kt"   # T032
Task: "feature/main/di/MainDeepLinkIntentFactoryImpl.kt + MainDeepLinkModule.kt"         # T053
Task: "feature/main/AndroidManifest.xml launchMode=singleTask"                           # T054
Task: "feature/splash/AndroidManifest.xml launchMode=singleTop"                          # T055
Task: "core/notification/PushNotificationChannel.kt"                                     # T034
Task: "feature/main/MainShell.kt·MainNavHost.kt startTab·pendingTab 파라미터"            # T037
Task: "feature/room/main/vm/RoomListViewModel.kt observeRoomDetailRequests()"            # T039
```

## 병렬 실행 예시: 사용자 스토리 1

```bash
# 모듈이 다른 셋을 함께:
Task: "core/domain/repository/PushRegistrationRepository.kt"          # T014
Task: "core/data/push/extension/Task.kt awaitDomain()"                # T016
Task: "core/data/network/dto/request/PushTokenRequest.kt"             # T019
```

---

## 구현 전략

### MVP 우선 (US1 + US2)

1. Phase 1 → Phase 2(2-A 테스트 통과, 2-B 골격).
2. Phase 3(US1) 완료 → T028로 서버가 토큰을 보유함을 확인. **이 시점부터 서버가 실제 알림을 보낼 수 있다.**
3. Phase 4(US2) 완료 → T041로 세 도착지 확인. 여기까지가 MVP다.
4. Phase 5·6은 검증만 남는다 → Phase 7.

### 점진적 전달

1. 셋업 → 기반(도메인 테스트가 먼저 green이 된다)
2. US1 → 서버 로그 검증 → 커밋
3. US2 → 실기기 검증 → 커밋
4. US3·US4 검증 기록 → 문서·ADR·품질 게이트

### 팀 병렬 전략

개발자가 둘이면 US1(데이터 레이어)과 US2(네비게이션·알림 표시)를 나눈다. 두 사람이 만나는 파일은 `MinoFirebaseMessagingService.kt`(T027·T036) 하나뿐이므로, 골격(T013)을 먼저 합의하고 콜백 하나씩을 각자 얹는다.

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 동일 파일 충돌, 독립성을 깨뜨리는 스토리 간 의존성
- 커밋 단위는 [`docs/conventions/commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다
- `firebase-messaging`은 Google Play 서비스가 있는 기기에서만 토큰을 발급한다 — 순정 에뮬레이터에서는 T028 이후의 검증이 성립하지 않는다([quickstart.md §0](quickstart.md#0-선행-조건))
