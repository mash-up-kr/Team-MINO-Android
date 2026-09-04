# 데이터 모델: 푸시 알림 (Push Notification)

**plan 버전**: 1.0.0 기준

이 문서는 `core:domain`에 새로 추가되는 도메인 모델만 다룬다. 서버 응답 스키마(DTO)는 [`contracts/push-token-api.md`](contracts/push-token-api.md)·[`contracts/push-payload-contract.md`](contracts/push-payload-contract.md)가 소유한다.

---

## 1. `PushMessageType`

```kotlin
// core:domain/model/PushMessageType.kt
enum class PushMessageType {
    PIN_DUPLICATED,       // 중복 저장 — spec 유저 플로우 2, [SCR-007] FR-004 6종 중 하나
    TOP_COMMENTED_PLACE,  // 코멘트 기반 리마인드
    NEARBY_PLACE,         // 위치 기반 리마인드 — 반경 안 저장 장소가 1개일 때
    NEARBY_PLACE_SUMMARY, // 위치 기반 대표 알림 — 반경 안 저장 장소가 여럿일 때(FR-012)
    ROOM_MEMBER_JOINED,   // 공동방 참가 ① — 기존 멤버가 받는 알림
    ROOM_JOINED_SELF,     // 공동방 참가 ② — 참가 당사자가 받는 알림
    SAVE_FAILED,          // 저장 오류
}
```

payload의 `type` 문자열이 이 열거에 없으면(EC-008) `PushMessage.type`은 `null`이다 — 별도 `UNKNOWN` 멤버를 두지 않는다. `when`이 `null` 분기를 강제하게 해 새 서버 타입 추가 시 컴파일 경고 없이 조용히 새 열거값으로 오인되는 일을 막는다.

---

## 2. `PushMessage`

```kotlin
// core:domain/model/PushMessage.kt
data class PushMessage(
    val type: PushMessageType?,
    val title: String,
    val body: String,
    val imageUrl: String?,
    val targetId: String?,
)
```

| 필드 | 채우는 값 | 비고 |
|---|---|---|
| `type` | payload `type` → [`PushMessageType`](#1-pushmessagetype) 매핑. 모르는 문자열이면 `null` | EC-008 |
| `title`·`body` | payload 그대로 | 서버가 완성해 보낸다(spec §4 가정) — 앱은 조립하지 않는다 |
| `imageUrl` | payload `imageUrl`. 없으면 `null` | `SAVE_FAILED`·`NEARBY_PLACE_SUMMARY`·`ROOM_*`는 항상 `null`([contracts/push-payload-contract.md](contracts/push-payload-contract.md) §1) |
| `targetId` | 장소 대상 알림은 `pinId`([research.md D2](research.md#d2-장소-대상-알림의-도착지-방은-payload의-pinid로-정한다)), 공동방 대상 알림은 `roomId`. 그 외 `null` | 실제 서버 필드명이 유형마다 다르므로(`pinId`/`roomId`) 파싱이 도메인 모델의 단일 필드로 흡수한다 |

`placeId`는 이 모델에 없다 — 도착지 판정에 쓰지 않으므로 도메인으로 올리지 않는다(D2). 파싱 시점에 버린다.

**파싱 책임**: `ParsePushMessageUseCase(data: Map<String, String>): PushMessage`(`core:domain/usecase`)가 `RemoteMessage.data`(Android SDK 타입을 이미 벗겨낸 순수 `Map<String, String>`)를 받아 위 규칙대로 변환한다. 필수 필드(`title`·`body`)가 없으면 예외가 아니라 빈 문자열로 채운다 — 파싱 실패로 알림을 통째로 버리는 대신 EC-008과 같은 "무시" 갈래는 `type == null`로만 표현한다.

---

## 3. `PushDestination`

```kotlin
// core:domain/model/PushDestination.kt
sealed interface PushDestination {
    data class PlaceDetail(val pinId: String) : PushDestination
    data class RoomDetail(val roomId: String) : PushDestination
    data object NotificationTab : PushDestination
}
```

`ResolvePushDestinationUseCase(message: PushMessage): PushDestination`(`core:domain/usecase`)가 FR-009·FR-012·FR-013의 라우팅 표를 그대로 구현한다.

| `PushMessage.type` | `targetId` 있음 | `targetId` 없음/해석 불가 | 관련 FR |
|---|---|---|---|
| `PIN_DUPLICATED` | `PlaceDetail(targetId)` | `NotificationTab`(EC-009) | FR-009, FR-013 |
| `TOP_COMMENTED_PLACE` | `PlaceDetail(targetId)` | `NotificationTab` | FR-009, FR-013 |
| `NEARBY_PLACE` | `PlaceDetail(targetId)` | `NotificationTab` | FR-009, FR-013 |
| `ROOM_MEMBER_JOINED` | `RoomDetail(targetId)` | `NotificationTab` | FR-009, FR-013 |
| `ROOM_JOINED_SELF` | `RoomDetail(targetId)` | `NotificationTab` | FR-009, FR-013 |
| `SAVE_FAILED` | — (`targetId` 없음이 정상) | `NotificationTab` | FR-009 |
| `NEARBY_PLACE_SUMMARY` | — (`targetId` 없음이 정상) | `NotificationTab` | FR-012 |
| `null`(모르는 유형) | — | 알림 자체를 표시하지 않는다(EC-008) — `ResolvePushDestinationUseCase`를 호출하지 않는다 | FR-013 |

---

## 4. `PushRegistrationRepository` (계약만 — 구현은 `core:data`)

```kotlin
// core:domain/repository/PushRegistrationRepository.kt
interface PushRegistrationRepository {
    /**
     * 현재 등록 토큰을 조회해 서버에 등록한다. 실패는 삼키고 재시도하지 않는다 — FR-004,
     * `PlaceRepository.recordAccess`와 같은 형태. `CancellationException`만 그대로 전파한다.
     */
    suspend fun registerCurrentToken()
}
```

서버 API 계약은 [`contracts/push-token-api.md`](contracts/push-token-api.md), 조회·등록을 묶는 구현 구조는 [research.md D5](research.md#d5-토큰-조회등록은-coredata에-기존-인증-제공자-패턴을-그대로-재사용한다)가 소유한다.
