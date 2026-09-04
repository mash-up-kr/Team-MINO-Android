# core:notification

MinoAndroid의 **푸시 알림 표시 모듈**. Firebase Cloud Messaging의 수신 방향 — `FirebaseMessagingService`와 시스템 알림(채널·빌더·탭 시 `PendingIntent`) 조립 — 만 갖는다.

> 모듈 책임·경계·의존 방향(레이어 그래프)은 [`docs/architecture/modularization.md`](../../docs/architecture/modularization.md)를 단일 출처로 한다. "왜 `:core:data`가 아니라 전용 모듈인가"는 [ADR 2026-09-04](../../docs/adr/2026-09-04-core-notification-module.md)가 소유한다. 이 문서는 이 모듈의 **구조·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | FCM data 메시지를 받아 도메인 UseCase로 파싱·도착지 판정을 맡기고, 그 결과를 시스템 알림 한 건으로 그린다. 토큰 갱신 콜백은 도메인 UseCase 호출로 넘긴다. |
| **빌드 타입** | Android Library + Hilt, non-Compose (`mino.android.library`, `mino.android.hilt`) |

> [!IMPORTANT]
> **이 모듈은 알림 표시 전용이다.** 토큰 조회·서버 등록은 `:core:data`의 `push/`가 소유하고, 이 모듈은 `:core:data`를 의존하지 않는다. 이유는 [ADR 2026-09-04 §근거](../../docs/adr/2026-09-04-core-notification-module.md#근거).

> [!NOTE]
> `POST_NOTIFICATIONS`는 이 모듈 매니페스트에 선언하지 않는다(FCM SDK 병합, [research.md D12](../../docs/specs/push-notification/research.md)). 권한 요청·대체 표시도 하지 않으며 권한이 없으면 알림을 띄우지 않을 뿐이다([spec FR-014](../../docs/specs/push-notification/spec.md)). Firebase 인프라(google-services 플러그인·구성 파일·BOM)는 앱 모듈 책임이다.

---

## 2. 진입점과 수신 흐름

feature가 호출하는 공개 API는 없다. 진입점은 매니페스트에 등록된 서비스 하나이고, 나머지는 `internal`이다.

| 구성 요소 | 역할 |
|---|---|
| `MinoFirebaseMessagingService` | FCM 콜백 진입점. `onNewToken`은 `RegisterPushTokenUseCase`를 부르고, `onMessageReceived`는 아래 흐름으로 알림을 띄운다. |
| `PushNotificationChannel` | 모든 유형이 공유하는 **단일** 채널. 앱 시작이 아니라 알림 직전에 멱등 생성한다([research.md D3](../../docs/specs/push-notification/research.md)). |
| `PushNotificationBuilder` | `PushMessage` + `PendingIntent` → `Notification`. 문구는 서버가 보낸 그대로, `imageUrl`이 있으면 확장 이미지, 실패해도 알림은 뜬다. |

수신 흐름은 `data` 페이로드 → `ParsePushMessageUseCase` → 모르는 유형이면 폐기 → `ResolvePushDestinationUseCase` → `MainDeepLinkIntentFactory`(`:core:navigation`)의 Intent에 도착지 extra 인코딩 → `PendingIntent` → 채널 보장 → 빌더 → `notify`다. 페이로드 형식은 [`contracts/push-payload-contract.md`](../../docs/specs/push-notification/contracts/push-payload-contract.md), extra 키와 Intent 팩토리는 [`contracts/push-deeplink-contract.md`](../../docs/specs/push-notification/contracts/push-deeplink-contract.md) §1·§2가 계약이다.

---

## 3. 디렉토리 구조

```
team/mino/core/notification/
├── MinoFirebaseMessagingService.kt   # FCM 콜백 진입점 — 토큰 갱신 위임·수신 → 알림 조립
├── PushNotificationChannel.kt        # 단일 채널 지연 생성(internal)
└── PushNotificationBuilder.kt        # Notification 조립 — 문구·이미지·아이콘(internal)
src/main/AndroidManifest.xml          # <service> 등록만 — 권한 선언 없음
src/main/res/values/strings.xml       # 채널 표시 이름
```

---

## 4. 확장 규칙 — 어디를 고칠지 결정

| 하려는 것 | 고치는 곳 |
|---|---|
| **새 알림 유형 추가** | `PushMessageType`(`:core:domain`) · [data-model.md §3](../../docs/specs/push-notification/data-model.md) 라우팅 표 · [payload 계약](../../docs/specs/push-notification/contracts/push-payload-contract.md). **이 모듈은 건드리지 않는다** — 파싱·도착지 판정이 도메인에 있으므로 유형이 늘어도 표시 코드는 같다. |
| **알림 아이콘 교체** | `PushNotificationBuilder` 한 곳. 현재는 런처 아이콘 임시 재사용([research.md D11](../../docs/specs/push-notification/research.md)). |
| **채널** | 하나를 유지한다(FR-008). 유형별 채널·중요도 분기를 만들지 않는다. |
| **새 도착지 종류** | `PushDestination`(`:core:domain`)과 deeplink 계약 §1 extra 키를 먼저 넓히고, 서비스의 extra 인코딩을 그에 맞춘다. |
| **알림 문구 가공** | 하지 않는다(FR-007). 문구는 서버가 완성한다. |

- 새 코드가 **`NotificationManager`·`PendingIntent` 등 시스템 알림 API를 만지면** 이 모듈에, **페이로드 해석·도착지 판정이면** `:core:domain`에, **토큰·서버 통신이면** `:core:data`에 둔다.
- 알림을 눌렀을 때의 **수신 측**(스플래시·`MainActivity`가 extra를 소비하는 쪽)은 이 모듈 밖이다 — deeplink 계약 §3 이후와 [research.md D13](../../docs/specs/push-notification/research.md).

---

## 5. 의존성 추가 가이드

이 모듈을 의존하는 곳은 **`:app`뿐**이다. 매니페스트 병합으로 서비스가 등록되므로 feature나 다른 core 모듈이 의존할 이유가 없다. 알림을 띄우고 싶은 feature가 있어도 여기를 부르지 않는다 — 푸시는 서버가 보낸다.

```kotlin
dependencies {
    implementation(project(":core:notification"))
}
```

이 모듈이 끌어오는 것: `:core:domain`, `:core:navigation`, `firebase-messaging`(BOM 버전), `androidx-core-ktx`, `kotlinx-coroutines-android`.

---

## 6. 컨벤션

| 항목 | 규칙 |
|---|---|
| **data-only** | `notification` 페이로드는 읽지 않는다. 서버는 data-only로 보낸다([research.md D1](../../docs/specs/push-notification/research.md)). |
| **콜백 안에서 끝낸다** | FCM 콜백은 반환 즉시 서비스가 멈추므로 코루틴을 띄워 두지 않고 `runBlocking`으로 마친다. 이미지 다운로드에는 짧은 타임아웃을 건다. |
| **모르는 유형** | 조용히 버린다. 오류 표시·기본 알림을 두지 않는다(payload 계약 §4). |
| **앱 상태 무관** | 포그라운드 여부를 보지 않는다(FR-011). 탭 시점 판정은 수신 측이 한다. |
| **가시성** | 서비스 외에는 `internal`. SDK 타입(`RemoteMessage`)은 서비스 밖으로 새지 않는다. |
| **토큰** | `onNewToken`의 인자를 쓰지 않고 UseCase가 다시 조회한다([research.md D5](../../docs/specs/push-notification/research.md)). |
