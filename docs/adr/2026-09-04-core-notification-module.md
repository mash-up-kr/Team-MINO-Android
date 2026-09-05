# FCM 수신·알림 표시는 전용 모듈 `:core:notification`이 갖고, 토큰 등록은 `:core:data`에 남긴다

- **상태**: Accepted
- **작성일**: 2026-09-04
- **작성자**: Jaesung Lee

## 컨텍스트

푸시 알림([spec](../specs/push-notification/spec.md), 이슈 #275)은 Firebase Cloud Messaging SDK 하나를 두 방향으로 쓴다.

- **수신 방향** — `FirebaseMessagingService`를 상속한 Android `Service` 컴포넌트를 매니페스트에 등록하고, 받은 data 페이로드로 시스템 알림(`NotificationManager`·채널·`PendingIntent`)을 직접 그린다.
- **등록 방향** — `FirebaseMessaging.token`으로 이 설치의 등록 토큰을 조회해 `PUT /api/v1/users/me/push-token`으로 서버에 보낸다.

이 둘을 어디에 둘지 정해야 했다. [`docs/architecture/modularization.md`](../architecture/modularization.md)의 기존 `core:*` 모듈 중 수신 방향에 맞는 자리가 없었다 — `:core:data`는 "Repository 구현·DataSource·DTO 매핑"으로 범위가 명시돼 있고, `:core:common:android`는 범용 유틸리티다. 반면 등록 방향은 이미 두 번 반복된 형태 그대로다: `:core:data`의 `auth/`가 Firebase Auth SDK를 `internal` 제공자 + `Task<T>` → suspend 변환 + 예외 매핑 지점으로 감싼 것([익명 인증 ADR](2026-08-22-firebase-anonymous-auth-session.md)·[매핑 지점 ADR](2026-08-22-domain-exception-mapping-per-source.md)).

같은 SDK를 쓰니 한 모듈에 모으고 싶어지는 자리이고, 실제로 익명 인증 ADR은 `:core:auth` 신설을 "모듈을 하나 늘릴 만한 산출물 규모가 아니다"라며 기각한 바 있다. 그 판단과 이번 신설이 어긋나지 않는 이유를 남겨야 다음 SDK가 들어올 때 "전용 모듈이냐 `:core:data`냐"를 다시 논의하지 않는다.

한 가지 맥락이 더 있다. 이 spec보다 먼저 마이페이지(`d7d0ca92`, 2026-08-23)가 같은 관심사의 스택을 이미 만들어 두고 있었다 — 도메인 `PushNotificationRepository.syncPushToken()`, `:core:data`의 `device/PushTokenProvider`(`suspendCancellableCoroutine`으로 `Task`를 직접 감싸고 예외를 매핑하지 않음), `PushNotificationRepositoryImpl`. 알림 권한이 처음 허용되는 시점에 토큰을 올리는 용도였다. 이 spec의 `PushRegistrationRepository`·`core/data/push/`와 계약이 둘이 되는 상황이었다.

## 결정

**FCM 수신·알림 표시는 신설 모듈 `:core:notification`이 소유하고, 토큰 조회·서버 등록은 `:core:data`가 소유한다.** 경계는 "SDK가 같은가"가 아니라 **책임의 종류**로 긋는다.

- `:core:notification`(Android Library, Hilt, non-Compose)은 `MinoFirebaseMessagingService`·알림 채널·알림 빌더·딥링크 `PendingIntent` 조립을 갖는다. 의존은 `:core:domain`·`:core:navigation`·`firebase-messaging`·`androidx.core`·coroutines뿐이다. **`:core:data`를 의존하지 않는다** — 토큰 갱신 콜백(`onNewToken`)은 `:core:domain`의 `RegisterPushTokenUseCase`를 호출할 뿐, 토큰이 어디서 나와 어디로 가는지 모른다.
- `:core:data`는 `push/` 패키지에 `PushTokenProvider`(`internal`)·`FirebaseMessaging` 제공 모듈·Messaging SDK 전용 매핑 지점(`push/extension/Task.kt`)을 두고, `PushRegistrationRepositoryImpl`이 토큰 조회와 `UserRemoteDataSource.putPushToken`을 묶는다. 서버 등록 엔드포인트는 OpenAPI `user` 태그라 [태그 단위 ADR](2026-08-28-api-service-owned-per-server-tag.md)대로 `UserApiService`·`UserRemoteDataSource`를 넓혔고 전용 DataSource를 만들지 않았다.
- 등록의 진입점은 `RegisterPushTokenUseCase` 하나다. 앱 시작(Splash)·토큰 갱신 콜백(`:core:notification`)·알림 권한 허용(마이페이지) 세 호출자가 모두 이 무인자 UseCase를 부르고, 토큰 값을 인자로 넘기지 않는다.
- **같은 관심사의 도메인 계약을 둘 두지 않는다** (사용자 결정, 2026-09-04). 마이페이지가 먼저 만든 `PushNotificationRepository.syncPushToken()`·`core/data/device/PushTokenProvider`·`PushNotificationRepositoryImpl`과 그 DI 모듈은 삭제하고, 마이페이지는 `RegisterPushTokenUseCase`로 갈아탔다. 살아남는 쪽은 이 spec의 스택이다.

## 근거

**전용 모듈을 만든 기준은 SDK가 아니라 컴포넌트 종류다.** `:core:map`·`:core:analytics`가 "SDK 하나를 감싸는 전용 core 모듈"의 선례이지만, 그 둘이 분리된 이유는 Compose 의존이다. `:core:notification`이 분리되는 이유는 다르다 — 매니페스트에 등록되는 `Service` 컴포넌트와 시스템 UI(`NotificationManager`) 조립은 데이터 레이어의 책임이 아니다. `:core:data`에 얹으면 "Repository·DataSource·매핑"이라는 범위 선언이 거짓이 되고, `:core:common:android`에 얹으면 범용 모듈이 FCM에 종속된다. 익명 인증 ADR이 `:core:auth`를 기각한 논리("산출물이 Repository 구현·원천 접근자·Ktor 플러그인이라 데이터 레이어 안에서 끝난다")는 그대로 유효하고, 이번엔 그 논리를 **통과하지 못하는** 산출물이 있어서 모듈이 생긴 것이다. 그래서 같은 SDK의 등록 방향은 그 논리에 따라 `:core:data`에 남는다.

**토큰 등록을 `:core:data`에 남긴 것은 세 번째 반복이기 때문이다.** `AnonymousAuthProviderImpl`·`IdTokenProviderImpl`이 세운 형태(`internal` 제공자, SDK 인스턴스는 DI 모듈이 제공, 원천마다 매핑 지점 하나)를 `push/`가 그대로 잇는다. 이 형태를 `:core:notification`으로 옮기면 그 모듈이 `UserRemoteDataSource`를 알아야 해 `:core:data`를 의존하게 되고, 도메인 Repository 구현이 데이터 레이어 밖에 생겨 [DI 바인딩 소유 ADR](2026-08-02-di-binding-ownership.md)의 "구현을 소유한 모듈이 바인딩을 갖는다"가 두 모듈에 걸치게 된다.

**`:core:notification`이 `:core:data`를 의존하지 않는 것이 이 경계의 시험대다.** 서비스가 `RegisterPushTokenUseCase`만 호출하면 되므로 두 모듈은 `:core:domain`을 사이에 두고 만난다. `:app`이 둘 다 조립한다. 어느 날 `:core:notification`이 `:core:data`를 끌어와야 한다면 그것은 "알림 표시" 밖의 책임이 스며들었다는 신호다.

**계약을 하나로 합친 이유는 [태그 단위 ADR](2026-08-28-api-service-owned-per-server-tag.md)이 겪은 것과 같은 갈라짐을 도메인에서 막기 위해서다.** 두 Repository는 같은 `UserRemoteDataSource.putPushToken`을 불렀지만 성질이 달랐다 — 옛 것은 예외를 밖으로 올리고 매핑 지점이 없었으며(Messaging 예외가 전부 CEH로 샌다), 새 것은 FR-004대로 실패를 삼키고 `push/extension/Task.kt`에서 매핑한다. 호출자마다 다른 실패 정책을 가진 채 둘을 두면 "권한 허용 시점에는 에러 토스트가 뜨고 앱 시작 시점에는 안 뜨는" 식으로 갈라진다. 마이페이지의 용도(권한 허용 직후 등록)는 새 UseCase 호출 한 줄로 충족되므로 남길 이유가 없었다. 옛 스택이 develop에 머지돼 있었으나 아직 그 계약을 소비하는 화면 동작이 검증 전이었고, 되돌리는 대가가 삭제 5파일·호출부 한 줄이라 이 spec 안에서 정리했다.

## 결과

- **`:core:notification`은 알림 표시 전용이다.** 새 푸시 유형이 생기면 파싱·도착지 판정은 `:core:domain`(`ParsePushMessageUseCase`·`ResolvePushDestinationUseCase`)에, 알림 문구·채널·Intent 조립은 이 모듈에 둔다. 서버 통신·로컬 저장은 여기에 들어오지 않는다.
- **FCM 관련 `:core:data` 코드는 `push/` 하나로 모인다.** `device/`에 있던 옛 제공자는 삭제됐고, 토큰을 다루는 코드가 다시 필요하면 `push/PushTokenProvider`를 넓힌다. Messaging SDK 예외 매핑은 `push/extension/Task.kt` 한 곳이며, `auth/extension/Task.kt`와 파일을 합치지 않는다(화이트리스트가 다르다 — [매핑 지점 ADR](2026-08-22-domain-exception-mapping-per-source.md)).
- **토큰을 등록하려는 새 호출자는 `RegisterPushTokenUseCase`를 부른다.** 토큰 값을 얻어 직접 보내는 경로를 만들지 않는다. 실패는 Repository가 삼키므로 호출자가 `runCatchingDomain`으로 감쌀 것은 취소 전파뿐이다.
- **다음 SDK가 들어올 때의 판정 기준.** 산출물이 Repository 구현·원천 접근자 안에서 끝나면 `:core:data`(익명 인증과 같음). Compose 컴포저블이 공개 표면이면 전용 Compose 모듈(`:core:map`·`:core:analytics`와 같음). 매니페스트 컴포넌트·시스템 UI 조립이 있으면 전용 non-Compose 모듈(이 ADR). 한 SDK가 둘 이상에 걸치면 이번처럼 책임별로 가른다.
- **`docs/architecture/modularization.md`에 `:core:notification` 항목과 의존 그래프가 추가됐다.** 모듈 목록의 SSOT는 그 문서이고, 이 ADR은 "왜 그 자리인가"만 소유한다.
- 마이페이지의 알림 권한 허용 흐름은 `MyPageViewModel`이 `RegisterPushTokenUseCase`를 호출하는 것으로 유지된다. `PushNotificationRepository`라는 이름은 코드에서 사라졌으므로 옛 문서·이슈 본문에서 그 이름을 만나면 `PushRegistrationRepository`로 읽는다.

## 고려한 대안

**FCM 전부를 `:core:data`에 둔다.** 익명 인증이 `:core:auth`를 기각한 것과 같은 논리로 모듈을 늘리지 않는 선택이다. 그러나 `FirebaseMessagingService`는 매니페스트에 등록되는 `Service`이고 알림 빌더는 `NotificationManager`·`PendingIntent`를 조립하는 시스템 UI 코드라, "Repository 구현·DataSource·DTO 매핑"이라는 `:core:data`의 범위 선언을 어긴다. 선언을 넓혀 맞추면 그 모듈이 무엇인지 말할 수 없게 된다. 기각.

**FCM 전부를 `:core:notification`에 둔다(토큰 등록 포함).** SDK 하나가 모듈 하나에 대응해 찾기 쉽다. 그러나 `PushRegistrationRepositoryImpl`이 `UserRemoteDataSource`를 필요로 하므로 `:core:notification → :core:data` 의존이 생기고, 도메인 Repository 구현과 그 Hilt 바인딩이 데이터 레이어 밖에 놓인다. 이미 두 번 반복된 "Firebase SDK 원천 접근자는 `:core:data`의 `internal`"이라는 형태도 깨진다. 기각.

**`:core:common:android`에 얹는다.** 새 모듈이 없고 Android SDK 의존이 이미 있다. 그러나 범용 유틸리티 모듈이 `firebase-messaging`을 의존하게 되어 그 모듈을 쓰는 모든 소비자가 FCM을 끌어오고, "공용"의 의미가 흐려진다. 기각.

**옛 `PushNotificationRepository`를 남기고 새 `PushRegistrationRepository`를 그 위에 얹거나 나란히 둔다.** 마이페이지 코드를 건드리지 않아 회귀 범위가 작다. 그러나 같은 엔드포인트를 두 도메인 계약이 서로 다른 실패 정책으로 부르게 되고, 매핑 지점이 없는 옛 경로는 Messaging 예외를 전부 버그로 흘린다. 서버가 계약을 바꾸는 날 한쪽만 고쳐도 컴파일은 통과한다 — [태그 단위 ADR](2026-08-28-api-service-owned-per-server-tag.md)이 데이터 레이어에서 막은 갈라짐을 도메인에서 허용하는 셈이다. 기각.

**`onNewToken`이 받은 토큰 값을 인자로 넘겨 바로 등록한다.** 재조회 왕복 하나를 아낀다. 그러나 등록 경로가 "받은 값"과 "조회한 값" 둘로 갈라져 두 경로가 다른 값을 올릴 여지가 생기고, `:core:notification`이 토큰 문자열을 아는 순간 그 모듈의 책임이 표시 밖으로 번진다. 기각([research.md D5](../specs/push-notification/research.md)).
