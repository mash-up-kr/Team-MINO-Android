# 리서치: 푸시 알림 (Push Notification)

**plan 버전**: 1.0.0에서 결정, 1.0.1에서 D5 보정·D12 추가, 1.1.0에서 D6·D7·D9 재검토(D13~D15로 대체), 1.2.0에서 D16 추가

각 결정은 이 feature 안에서만 유효한 선택이다. 다른 feature에도 구속력을 갖는 결정은 완료 보고에서 ADR 승격을 제안한다.

---

## D1. FCM 메시지는 data-only 페이로드를 전제한다

**Decision**: 서버가 보내는 FCM 메시지는 `notification` 필드 없이 `data`만 싣는 data 메시지다. 앱은 `RemoteMessage.notification`을 읽지 않고 `RemoteMessage.data`만 파싱한다.

**Rationale**: 사용자가 첨부한 실제 payload 예시(Raw FCM Payload · SDK 추출본)가 `"notification": null`이고 `data` 객체에 `type`·`title`·`body`가 실려 있다. FCM은 `notification` 페이로드가 있으면 앱이 background일 때 시스템이 알림을 자동으로 그려 `onMessageReceived`를 우회한다 — 그러면 FR-011(foreground·background 동일 처리)과 FR-007(유형별 문구를 앱이 직접 구성)이 성립하지 않는다. data-only 메시지여야 `onMessageReceived`가 상태와 무관하게 항상 불려, 앱이 직접 시스템 알림을 그릴 수 있다.

**Alternatives considered**: notification+data 혼합 페이로드 — background 자동 표시에 기댈 수 있지만 FR-011 위반이라 기각.

---

## D2. 장소 대상 알림의 도착지 방은 payload의 `pinId`로 정한다

**Decision**: `PIN_DUPLICATED`·`TOP_COMMENTED_PLACE`·`NEARBY_PLACE` 세 유형은 payload의 `pinId`를 그대로 장소 상세 진입 키로 쓴다. `placeId`는 저장하되 도착지 판정에 쓰지 않는다.

**Rationale**: (사용자 결정, 2026-09-03) 실제 payload는 `placeId`와 `pinId`를 함께 싣는데, 코드베이스에는 이미 탭 간 장소 상세 진입 인프라(`PlaceDetailRequestHolder` + `PlaceRepository.getPlaceDetail(pinId)`, `docs/specs/place-detail/contracts/place-detail-entry.md`)가 있고 `PlaceDetailEntryOrigin.NOTIFICATION`이 이 용도로 이미 예약돼 있다(`core/navigation/entry/PlaceDetailRequestHolder.kt`). `pinId`를 그대로 쓰면 이 인프라를 온전히 재사용해 별도 방 조회를 만들 필요가 없다.

**spec.md와의 괴리**: FR-013은 "장소 식별자 하나만 싣고, 방은 앱이 [SYS-004] 표시 기준 방을 읽어 정한다"고 확정되어 있고, `pinId`는 "장소·방 쌍"이라는 이유로 TBD 답변에서 명시적으로 기각된 후보였다(spec §5). 그런데 [SYS-004] 표시 기준 방("최초로 저장한 방", PRD 5.1.0)을 `placeId`만으로 조회하는 API·UseCase는 코드베이스 어디에도 없다 — 기존 `RoomRepository.getRooms(placeId)`는 "이미 저장돼 있는지"만 묻고 "어느 방을 기본으로 보여줄지"는 응답하지 않는다. 이 설계는 spec의 문언과 다르게 구현되므로, **완료 보고에서 spec.md FR-013의 PATCH 개정(가정 갱신 — 서버 payload가 `pinId`를 함께 보낸다는 사실 반영)을 제안한다.** 사용자 시나리오·완료 조건 관점에서는 두 방식 모두 "그 알림이 가리키는 장소의 장소 상세로 이동한다"는 요구를 충족하므로 요구사항 경계 변경은 아니다.

**Alternatives considered**: `placeId` + 신규 "표시 기준 방 조회" API를 서버에 요청 — 서버 개발 범위가 늘고, 이미 동작하는 `pinId` 경로를 두고 별도 경로를 유지해야 해 기각.

---

## D3. 알림 채널은 지연 생성한다

**Decision**: 단일 채널(FR-008)을 앱 시작 시점이 아니라 `MinoFirebaseMessagingService.onMessageReceived`에서 알림을 만들기 직전 `NotificationManagerCompat.createNotificationChannel`로 멱등 생성한다.

**Rationale**: `createNotificationChannel`은 이미 존재하는 채널에 다시 호출해도 안전하다(Android 표준 계약). 별도의 Application 초기화 훅을 추가하지 않아도 되고, 알림을 한 번도 받지 않은 설치는 채널도 만들지 않는다.

**Alternatives considered**: `MinoApplication.onCreate`에서 미리 생성 — 동작은 같지만 앱 전역 초기화 목록에 푸시 전용 항목이 하나 늘어 근거 없이 결합도만 높인다.

---

## D4. FCM SDK 통합은 새 모듈 `:core:notification`에 둔다

**Decision**: `FirebaseMessagingService` 구현체, 알림 채널·빌더, payload → `PushMessage` 파싱 호출, 딥링크 `PendingIntent` 조립을 새 Android 모듈 `:core:notification`(Hilt, non-Compose)에 둔다.

**Rationale**: `docs/architecture/modularization.md`의 기존 `core:*` 모듈 중 이 책임에 맞는 곳이 없다 — `core:data`는 "Repository 구현·DataSource·DTO 매핑"으로 범위가 명시돼 있어 `NotificationManager`·`PendingIntent` 조립 같은 시스템 UI 통합은 그 경계 밖이고, `core:common:android`는 "공용 유틸리티"로 범위가 넓어 FCM 전용 서비스를 얹으면 범용 모듈이 특정 기능에 종속된다. `core:map`·`core:analytics`가 이미 "SDK 하나를 감싸는 전용 core 모듈"의 선례다 — 같은 형태를 따른다.

**주의**: `docs/architecture/modularization.md`는 원칙 I(SSOT)에 따라 모듈 목록의 단일 출처이지만 `/mino-plan`의 범위 가드가 규약 문서 편집을 금지한다. **완료 보고에서 `:core:notification` 항목을 그 문서에 추가하는 후속 작업을 제안한다.**

**Alternatives considered**: `core:data`에 얹기 — Firebase Auth SDK를 감싼 선례(`AnonymousAuthProviderImpl`)와 겹쳐 보이지만, 그쪽은 순수 데이터 조회·인증이고 이쪽은 `Service` 컴포넌트·알림 UI 조립이라 책임이 다르다. 토큰 등록(D5)만 `core:data`로 가고 알림 표시·딥링크는 분리한 이유다.

---

## D5. 토큰 조회·등록은 `core:data`에 기존 인증 제공자 패턴을 그대로 재사용한다

**Decision**: `FirebaseMessaging.getInstance().token` 조회와 `PUT /api/v1/users/me/push-token` 호출을 묶는 `PushRegistrationRepositoryImpl`을 `core:data`에 둔다. `AnonymousAuthProviderImpl`·`IdTokenProviderImpl`(`core/data/auth/`)이 `Task<T>` → suspend 변환과 SDK 예외 매핑을 한 지점에 모은 것과 같은 구조를, `core/data/push/`에 새로 만든다(`PushTokenProvider`/`Impl`, 전용 `Task.awaitDomain()` 매핑).

**서버 등록의 소유자 (1.0.1 보정)**: `PUT /api/v1/users/me/push-token`은 OpenAPI `user` 태그(`uH_updatePushToken`)다. [ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md)은 `ApiService`·`DataSource`의 단위를 태그로 두고 "그 태그의 소유자가 이미 있으면 그것을 넓힌다"고 정하므로, 새 `PushApiService`·`PushRemoteDataSource`를 만들지 않고 **`UserApiService.updatePushToken(PushTokenRequest)`·`UserRemoteDataSource.registerPushToken(token)`을 더한다.** 1.0.0이 이를 신설로 적은 것은 ADR 대조 누락이었다. 도메인 `PushRegistrationRepository`는 ADR이 명시한 대로 관심사 단위라 그대로 둔다 — 같은 `UserRemoteDataSource`를 `ProfileRepository`·`ProfileRegistrationRepository`·`PushRegistrationRepository` 셋이 쓰는 것이 정상 상태다. 대가는 ADR §결과가 적은 회귀 범위다: `UserApiService`를 쓰는 splash·profile의 테스트가 회귀 대상이 된다.

**등록은 앱 시작마다 한 번, 값과 무관하게 시도한다** — spec §4 가정("값이 이전과 같으면 서버가 같은 값을 다시 받아도 무해하다")과 FR-004(실패를 노출하지 않는다)를 그대로 따라, `registerCurrentToken()`은 실패를 삼키고 `Unit`을 반환한다(`PlaceRepository.recordAccess`와 같은 형태). 별도 재시도 큐·로컬 저장을 두지 않는다.

**호출 지점은 둘, UseCase는 하나**: 앱 시작(`EnsureAnonymousSessionUseCase` 성공 직후, Splash)과 `onNewToken` 콜백(`:core:notification`) 모두 같은 `RegisterPushTokenUseCase`(무인자)를 호출한다. `onNewToken`이 새 토큰 값을 인자로 주더라도 그 값을 쓰지 않고 다시 조회한다 — 호출 지점을 하나로 유지해 "토큰을 어디서 얻어 어디로 보내는지"의 매핑 지점이 갈라지지 않게 한다.

**Rationale**: 기존 코드베이스가 이미 이 패턴(Firebase SDK를 감싼 Provider + Repository, `core:data`가 소유)을 두 번 반복했다(인증 제공자용) — 세 번째 반복은 새 관례를 만드는 대신 그대로 잇는다.

**Alternatives considered**: `onNewToken`이 받은 토큰 값을 바로 등록(재조회 생략) — 네트워크 왕복 하나를 아끼지만 호출 지점이 둘로 갈라지고, 두 경로가 다른 값을 등록할 여지(레이스)가 생겨 기각.

---

## D6. 알림 탭은 항상 `SplashActivity`를 거쳐 진입한다

> **재검토됨(plan 1.1.0)** — [D13](#d13-알림-탭은-mainactivity를-겨냥하고-게이트-통과-여부는-탭-시점에-수신자가-판정한다)이 대체한다. 아래 본문은 1.0.0의 결정과 기각 이력으로 남긴다.

**Decision**: ~~알림의 `PendingIntent`는 앱 실행 상태와 무관하게 항상 `SplashActivity`를 대상으로 한다. `MainActivity`를 직접 열지 않는다.~~

**Rationale**: FR-010이 "앱이 종료된 상태에서 알림을 눌렀을 때, 앱의 기존 시작 경로(세션 확보 포함)를 거친 뒤 도착지로 이동한다"를 요구한다. `EnsureAnonymousSessionUseCase.ensureSession()`은 이미 확보된 세션이 있으면 왕복 없이 즉시 반환하는 멱등 함수라, 앱이 이미 실행 중이어도 Splash를 다시 거치는 비용은 캐시 히트 수준으로 작다. 두 경로(콜드/웜)를 가르면 "종료 상태 판정"을 앱이 직접 해야 하는데, `FirebaseMessagingService`(빌드 시점)는 알림을 누를 때의 앱 상태를 알 수 없다 — `PendingIntent`는 미래 시점에 실행되기 때문이다. 하나의 경로로 통일하면 이 판정 자체가 필요 없어진다.

**대가**: 앱이 이미 켜져 있는 상태에서 알림을 눌러도 `MINIMUM_EXPOSURE`(3초) 스플래시 노출을 다시 거친다. spec SC-004는 "추가 조작 없이 도착한다"만 요구하고 지연 시간을 규정하지 않으므로 요구사항 위반은 아니다.

**Alternatives considered**: `MainActivity`를 직접 열고 필요할 때만(프로세스가 죽어 있을 때만) Splash로 우회 — 판정 근거(프로세스 생존 여부)를 `PendingIntent` 빌드 시점에 결정할 수 없어 기각.

---

## D7. 딥링크 Intent는 `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP`을 쓴다

> **재검토됨(plan 1.1.0)** — [D14](#d14-mainactivity는-singletask이고-딥링크-intent는-new_task만-건다)가 대체한다. 여기서 "별도 과제"로 미뤘던 `launchMode` 조정을 1.1.0이 채택했다.

**Decision**: ~~`SplashDeepLinkIntentFactory`가 만드는 Intent는 두 플래그를 함께 건다.~~

**Rationale**: `Service` Context에서 `startActivity`/`PendingIntent.getActivity`를 쓰려면 `FLAG_ACTIVITY_NEW_TASK`가 필수다. 두 Activity 모두 `launchMode`가 기본값(`standard`)이라(`feature/main`·`feature/splash`의 `AndroidManifest.xml`), 이 플래그만 걸면 앱이 이미 실행 중일 때 기존 태스크 위에 `SplashActivity`·`MainActivity` 인스턴스가 새로 쌓인다. `CLEAR_TOP`을 더하면 기존 태스크의 루트 위에 쌓인 Activity들을 정리하고 그 자리를 재사용해 백스택이 알림을 누를 때마다 불어나지 않는다.

**Alternatives considered**: 두 Activity에 `singleTask`를 선언 — 더 근본적인 해법이지만 이 feature의 범위를 넘는 전역 launchMode 변경이라, 이번 변경은 Intent 플래그로 국한하고 launchMode 조정은 별도 과제로 남긴다.

---

## D8. 방 상세의 탭 간 진입은 `PlaceDetailRequestHolder`를 그대로 본떠 새로 만든다

**Decision**: `core/navigation/entry/RoomDetailRequestHolder.kt`를 신설한다. `ActivityRetainedScoped`, `pending: StateFlow<String?>`(roomId), `request(roomId)`/`consume()`만 갖는다 — `PlaceDetailRequestHolder`와 달리 진입 출처(`origin`)를 싣지 않는다.

**Rationale**: `ROOM_MEMBER_JOINED`·`ROOM_JOINED_SELF` 알림은 `roomId`를 직접 싣고(공동방 대상, FR-013), 방 상세는 저장 탭 안의 로컬 상태(`RoomListUiState.selectedRoomId`)로 열린다는 점이 장소 상세와 완전히 같다. `origin`을 두지 않는 이유는 방 상세의 [나가기] 규칙이 진입 경로에 따라 갈리지 않기 때문이다(장소 상세만 PRD 13.0.0에서 홈 진입 예외가 생겼다 — spec §3.2 "도착지 화면의 내용과 동작은 각자의 스펙이 정의한다"로 이미 이 문서 범위 밖).

**Alternatives considered**: `PlaceDetailRequestHolder`를 제네릭하게 확장해 장소·방을 함께 다루기 — 두 도메인의 진입 규칙(특히 origin 유무)이 달라 제네릭화가 오히려 각 소비자에서 쓰지 않는 필드를 갖게 해 기각.

---

## D9. "알림 탭 목록" 도착지는 `MainTab.NOTIFICATION`을 그대로 쓴다

> **부분 재검토됨(plan 1.1.0)** — `startTab`으로 시작 목적지를 정하는 콜드 경로는 유지한다. 여기서 기각한 명령형 `navigateToTab`은 NavHost가 이미 떠 있는 웜 경로에서는 유일한 방법이라 [D15](#d15-웜-경로의-탭-전환은-명령형이고-대기-중인-도착지-탭을-mainactivity가-상태로-든다)가 그 경로에 한해 채택한다.

**Decision**: 저장 오류·위치 기반 대표 알림·해석 불가 알림(FR-009 낙하 지점)은 `MainNavHost`의 시작 탭을 `MainTab.NOTIFICATION`으로 설정해 진입한다. `MainShell`·`MainNavHost`가 지금 하드코딩한 `startDestination = MainTab.HOME.route`를 `startTab: MainTab = MainTab.HOME` 매개변수로 바꾸고, `MainActivity`가 자신의 Intent extra로 값을 계산해 넘긴다.

**Rationale**: `feature/main/MainDestinations.kt`에 이미 `Notification` Route가 탭 목록에 등록돼 있고(`MainTabPlaceholderScreen`으로 렌더링), [SCR-007] 알림 탭 화면(이슈 #160)이 아직 없어도 탭 자체는 존재한다. 화면이 나중에 채워져도 이 계약(시작 탭 지정)은 그대로 유효하다.

**Alternatives considered**: `MainShell` 컴포지션 이후 `navController.navigateToTab(NOTIFICATION)`을 명령형으로 호출 — 콜드 스타트에서는 `NavHost`가 아직 만들어지지 않아 이 시점에 `navController`가 없다. `startDestination`으로 구조적으로 지정하는 편이 유일하게 콜드·웜 두 경로에서 같은 코드로 동작한다.

---

## D10. Firebase Messaging SDK 예외의 도메인 매핑 지점을 새로 연다

**Decision**: `core/data/push/extension/Task.kt`에 `Task<String>.awaitDomain()`을 새로 만든다. 인증 제공자용(`core/data/auth/extension/Task.kt`)과 파일은 다르지만 형태는 같다 — `FirebaseNetworkException` → `MinoDomainException.Network`, 그 외 매핑 안 된 예외는 원본 그대로.

**Rationale**: `docs/conventions/error_handling.md` §3이 "HttpClient를 거치지 않는 원천이 추가되면 그 원천의 모든 호출이 통과하는 지점을 하나 만들고 매핑을 거기에만 둔다"고 이미 규정한다. 인증 제공자용 매핑(`toDomainExceptionOrSelf`)은 `FirebaseAuthException` 전용이라 재사용하면 Messaging SDK 예외가 그 화이트리스트를 벗어나 전부 버그(CEH)로 샌다.

**후속 문서화**: `docs/conventions/error_handling.md` §3 표에 "FCM Messaging SDK" 행 추가가 필요하다 — `/mino-plan`의 범위 가드가 규약 문서 편집을 금지하므로 완료 보고에서 후속 작업으로 제안한다.

---

## D11. 알림 아이콘은 기존 런처 아이콘을 임시로 재사용한다

**Decision**: `NotificationCompat.Builder.setSmallIcon`에 앱의 기존 런처 아이콘을 우선 쓴다. 상태 표시줄 전용 모노크롬 아이콘(Android 요구사항 — 단색·투명 배경)이 디자인 자산으로 아직 없다.

**참조 방법 (1.0.1 보강)**: 런처 아이콘(`@mipmap/ic_launcher`)은 `:app`의 리소스라 `:core:notification`이 컴파일 시점에 그 `R`을 볼 수 없다(헌법 원칙 II — core는 app을 의존하지 않고, `:app`은 바인딩을 두지 않으므로 Hilt로 리소스 ID를 내려보내는 길도 없다). 그래서 `context.applicationInfo.icon`으로 **런타임에** 리소스 ID를 얻는다 — `PackageManager`가 앱 아이콘을 알고 있으므로 모듈 의존 없이 같은 그림을 쓴다. 코드 한 줄이고 새 에셋이 없다. 전용 아이콘이 생기면 `PushNotificationBuilder`의 이 한 곳만 바꾼다.

**Rationale**: Android는 상태 표시줄 아이콘에 알파 채널만 있는 실루엣을 요구하고, 컬러 런처 아이콘을 그대로 쓰면 OS가 흰 사각형으로 뭉갠다. 이 feature의 spec은 알림에 실을 이미지를 "전제하지 않는다"(§4 가정)고만 적어 전용 아이콘 자산 여부를 정하지 않았다. 기능 검증(토큰 등록·탭 진입)에는 영향이 없어 자산 없이도 구현·검증이 가능하다.

**후속**: 전용 모노크롬 아이콘은 디자인 자산 제작이 필요한 별도 작업이다. `docs/conventions/component-asset-placement.md` 판정에 따라 배치할 모듈을 정한다.

---

## D12. `POST_NOTIFICATIONS` 선언은 FCM SDK의 매니페스트 병합에 맡긴다

**Decision**: 이 feature는 `POST_NOTIFICATIONS`를 어느 매니페스트에도 선언하지 않고, 런타임 요청도 하지 않는다. 선언은 `firebase-messaging` AAR이 자기 매니페스트에 이미 갖고 있어 병합으로 앱에 들어오고, 런타임 허용은 spec §3.2대로 [SCR-008] 마이페이지(#152)가 소유한다.

**Rationale**: `firebase-messaging` AAR의 `AndroidManifest.xml`이 `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>`를 선언한다(2026-09-03, Gradle 캐시의 23.4.0으로 확인 — `unzip -p firebase-messaging-*.aar AndroidManifest.xml | grep uses-permission`. 이 프로젝트의 BOM 34.14.0이 끌어오는 버전은 더 새롭고 같은 선언을 유지한다). 앱이 다시 선언해도 병합 결과는 같으므로 선언 한 줄을 이 feature가 갖는 것은 근거 없는 중복이다. `targetSdk = 36`이라 Android 13+ 기기에서는 런타임 허용이 없으면 알림이 표시되지 않지만, 그것은 FR-014·EC-006이 이미 "권한 없는 설치에는 알림이 뜨지 않는다"로 규정한 정상 동작이다.

**검증 전제**: #152가 없는 동안 실기기 검증은 이슈 #275 본문대로 권한을 수동으로 켠 뒤 진행한다 — 기기 설정 → 앱 → 알림, 또는 `adb shell pm grant <applicationId> android.permission.POST_NOTIFICATIONS`([quickstart.md §0](quickstart.md#0-선행-조건)).

**Alternatives considered**: `core/notification/src/main/AndroidManifest.xml`에 명시 선언 — SDK 버전이 바뀌어 선언이 빠질 가능성에 대한 보험이지만, 그 가능성은 FCM이 알림을 표시하는 SDK인 이상 현실적이지 않고, 지금은 같은 선언이 두 곳에 생겨 어느 쪽이 근거인지 흐려진다. 기각.

---

## D13. 알림 탭은 `MainActivity`를 겨냥하고, 게이트 통과 여부는 탭 시점에 수신자가 판정한다

**Decision** (plan 1.1.0, 사용자 확정 2026-09-04): 알림의 `PendingIntent`는 앱 실행 상태와 무관하게 항상 `MainActivity`를 대상으로 한다. 실행 상태의 판정은 알림을 **만드는 시점**이 아니라 **눌린 시점**에 `MainActivity`가 한다.

- 앱이 살아 있으면(포그라운드·백그라운드) `singleTask`인 기존 인스턴스가 `onNewIntent`로 받아 [계약 §4](contracts/push-deeplink-contract.md#4-mainactivity--extra를-소비해-요청-홀더도착지-탭을-정한다)의 소비 함수를 그대로 부른다. 스플래시도 재생성도 없고 보고 있던 화면 상태가 보존된다.
- 프로세스가 죽어 있으면 `MainActivity.onCreate`가 `super.onCreate` 직후·`setContent` **전에** "푸시 extra가 있고 `MainEntryGate`가 아직 통과 표시가 아니다"를 확인하고, extra를 그대로 실어 `SplashActivity`로 넘긴 뒤 `finish()`한다. 그 시점에 스플래시가 이미 진행 중이면(앱을 막 켠 참) 새로 띄우지 않고 그 스플래시에 extra를 건넨다 — [D16](#d16-스플래시가-진행-중일-때의-알림-탭은-그-스플래시가-이어받는다). 화면·ViewModel·서버 요청은 만들어지지 않는다. 스플래시는 평소대로 세션 확보·진입 판정·최소 노출을 거쳐 `MainLauncher`로 Main을 연다 — FR-010이 요구한 시작 경로를 그대로 지나고, 스플래시 spec UX-002(최소 노출)도 건드리지 않는다.
- `MainEntryGate`(`core/navigation/entry/`)는 "스플래시가 Main 진입을 확정했다"는 사실 하나만 담는 프로세스 스코프 `@Singleton`이다. 스플래시가 `SplashEntry.Main`으로 전환할 때 켜고, 프로세스가 죽으면 저절로 사라진다. 온보딩·프로필 판정을 복제하지 않으므로 [ADR 2026-08-29](../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md)(진입 판정은 온보딩이 소유, 스플래시가 소비)와 충돌하지 않는다.
- 분기 범위는 **푸시 extra가 있을 때만**이다. 프로세스가 죽은 뒤 최근 앱 목록에서 Main이 복원되는 경로는 건드리지 않는다 — 그 경로는 이 feature가 다룰 것이 없다(1.2.2 정정). 익명 세션은 SDK가 디스크에서 복원하고(`AnonymousAuthProviderImpl`, 익명 세션 spec FR-002) `RoomListViewModel`이 첫 요청 전에 `ensureSession()`을 선행하므로 세션은 확보된다. 건너뛰는 것은 그 실행의 푸시 토큰 재등록(스플래시가 하는 일)뿐이며, spec §4 "앱 시작마다 시도"에 따라 다음 정상 실행에서 회복된다(FR-001~003 위반 아님).

**Rationale**: D6가 Splash를 고른 근거("빌드 시점엔 눌릴 때의 앱 상태를 알 수 없다")는 사실이지만, 결론이 유일하지 않았다. 수신자는 자기 프로세스 상태를 오차 없이 알고, `setContent` 전에 우회하면 세션 없이 뜨는 위험이 없다. 웜 경로에서 3초 스플래시와 화면 상태 소실이 사라지는 것이 이득이다. 대조한 외부 사례(다른 프로젝트의 `activityCnt` 기반 분기)는 빌드 시점에 판정해 종료 후 탭에서 틀리고, 딥링크를 영속 저장소 + 500ms 지연으로 소비해 결정적이지 않아 방식은 가져오지 않았다. Android 12의 알림 트램폴린 금지는 Service·Receiver → Activity에만 걸리므로 Activity → Activity 우회는 허용된다.

**Alternatives considered**: (a) D6 유지 — 요구사항 위반은 아니지만 웜 탭마다 3초 대기·상태 소실. (b) Splash를 계속 겨냥하고 `SplashActivity.onCreate`가 같은 플래그로 화면 없이 Main으로 넘김 — 동작은 같으나 스플래시 spec UX-002에 예외 조항(MINOR 개정)이 필요해 기각. (c) 빌드 시점 `activityCnt` 분기 — 위 Rationale대로 기각.

---

## D14. `MainActivity`는 `singleTask`이고, 딥링크 Intent는 `NEW_TASK`만 건다

**Decision** (plan 1.1.0): `feature/main/src/main/AndroidManifest.xml`의 `MainActivity`에 `android:launchMode="singleTask"`를 선언한다. `MainDeepLinkIntentFactory`가 만드는 Intent는 `FLAG_ACTIVITY_NEW_TASK`만 건다 — `Service` Context에서 `PendingIntent.getActivity`를 쓰기 위한 필수 플래그다. D7의 `CLEAR_TOP`은 `singleTask`가 같은 일(태스크 안에서 Main 위에 쌓인 Activity 정리, 인스턴스 재사용)을 구조적으로 하므로 걷어낸다.

**Rationale**: `singleTask`는 인스턴스를 하나로 유지해 `onNewIntent`를 보장한다. `CLEAR_TOP`은 `standard`에서 Main을 **재생성**하므로 웜 경로의 상태 보존이 성립하지 않는다. 전역 `launchMode` 변경이지만 영향은 셋뿐이다 — `MainLauncher`가 Splash·온보딩에서 Main을 여는 경로(변화 없음), Main이 `RoomFormActivity`를 결과 요청으로 여는 경로(`singleTask` 제약은 결과를 **돌려주는 쪽**이 별도 태스크일 때 걸리므로 호출자인 Main에는 영향 없음), 그리고 Main 위에 `RoomForm`·`Profile`이 떠 있을 때 알림을 누르면 그 Activity가 정리되는 것(D7의 `CLEAR_TOP`도 같은 동작이었으므로 새 손실이 아니다 — [quickstart.md §2.1](quickstart.md#21-시나리오)에 검증 행을 둔다).

**Alternatives considered**: `singleTop` — 태스크 최상단일 때만 재사용해, 폼이 떠 있으면 Main이 새로 쌓인다. 기각. D7의 `CLEAR_TOP` 유지 — 위와 같이 재생성이라 기각.

---

## D15. 웜 경로의 탭 전환은 명령형이고, "대기 중인 도착지 탭"을 `MainActivity`가 상태로 든다

**Decision** (plan 1.1.0): 콜드 경로는 D9대로 `startTab`으로 시작 목적지를 정한다. 웜 경로(`onNewIntent`)는 NavHost가 이미 떠 있으므로 `MainActivity`가 `pendingTab: MainTab?`를 Compose 상태로 들고 `MainShell`에 넘기며, `MainShell`이 `LaunchedEffect(pendingTab)`에서 `navController.navigateToTab(...)`을 부른 뒤 `onPendingTabConsumed()`로 비운다. 홀더(`PlaceDetailRequestHolder`·`RoomDetailRequestHolder`)에는 두 경로 모두 `request()`를 먼저 한다.

**Rationale**: D9가 명령형을 기각한 이유는 콜드 스타트에서 `navController`가 없다는 것뿐이었다. 웜에서는 그 전제가 뒤집혀 명령형이 유일하다. 상태 + `LaunchedEffect`로 두면 소비가 컴포지션 생명주기에 묶여, 외부 사례의 500ms 지연 같은 시간 가정이 필요 없다.

**Alternatives considered**: 웜 경로도 Activity를 재생성해 `startTab`으로 통일 — 상태 보존을 버리는 것이라 D13의 목적과 어긋나 기각. 콜드도 `LaunchedEffect`로 통일 — 동작할 가능성은 있으나 D9가 이미 `startDestination`을 구조적 보장으로 택했고 바꿀 근거가 없어 유지.

---

## D16. 스플래시가 진행 중일 때의 알림 탭은 그 스플래시가 이어받는다

**Decision** (plan 1.2.0, 사용자 확정 2026-09-04): `SplashActivity`를 `launchMode="singleTop"`으로 두고, `MainActivity`의 콜드 우회 Intent(`SplashDeepLinkIntentFactory` 산출물)에 `FLAG_ACTIVITY_CLEAR_TOP`을 건다. `SplashActivity.onNewIntent`는 `setIntent` 후 보관 중인 푸시 extra(type·id)를 새 값으로 바꾼다.

**Rationale**: 앱을 막 켜 스플래시가 세션을 확보하는 중에 알림을 누르면 스택이 `[Splash, Main(우회용)]`이 된다. `launchMode`만으로는 Splash가 최상단이 아니라 재사용되지 않아 두 번째 Splash가 뜨고 세션 확보가 두 번 돈다(2차 `/mino-analyze` C1). `CLEAR_TOP`은 태스크 안에 이미 있는 Splash 위의 Activity(우회용 Main)를 정리하고 그 Splash를 최상단으로 올리며, `singleTop`은 그때 Splash를 재생성하지 않고 `onNewIntent`로 전달하게 한다. 진행 중이던 `SplashViewModel`의 세션 확보·최소 노출은 그대로 이어지고, 전환 시점에 최신 extra를 Main에 싣는다. 결과는 스플래시 한 번·세션 확보 한 번·도착지는 알림 쪽. 스플래시가 없을 때는 `CLEAR_TOP`이 아무것도 정리하지 않고 새 Splash가 뜨므로 1.1.0의 콜드 경로와 같다.

**영향 범위**: `feature/splash` 매니페스트 속성 하나와 Activity 콜백 하나가 는다. 사용자에게 보이는 스플래시 동작(최소 노출·재시도·온보딩 분기)은 그대로라 스플래시 spec의 요구사항은 바뀌지 않는다. 런처 아이콘 재탭은 태스크를 앞으로 가져올 뿐 새 Intent를 만들지 않으므로 `singleTop`이 그 경로에 끼어들 일이 없다.

**Alternatives considered**: `SplashActivity`를 `singleTask`로 — 재사용은 되지만 런처 진입 Activity의 태스크 규칙이 바뀌어 영향 범위가 이 feature를 넘는다. 기각. 아무것도 하지 않고 두 스플래시를 허용 — 결과는 맞지만(`singleTask` Main이 `onNewIntent`로 흡수) 스플래시가 겹쳐 보이고 세션 확보 요청이 중복된다. 기각.
