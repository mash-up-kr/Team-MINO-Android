# 계약: FCM data 페이로드

**대상**: FR-006·FR-007·FR-009·FR-012·FR-013 — 수신한 메시지를 시스템 알림으로 표시하고 도착지를 정한다.

**근거**: 사용자가 `/mino-plan` 실행 중 제공한 서버 설계 문서 캡처(2026-09-03). 배포된 OpenAPI 문서는 FCM data 페이로드 스키마를 신지 않으므로([research.md D1](../research.md#d1-fcm-메시지는-data-only-페이로드를-전제한다) 참고 — FCM 전송 자체가 REST가 아니라 별도 채널), 이 계약의 근거는 OpenAPI가 아니라 위 캡처다. **서버가 실제로 이 형태로 보내는지는 실기기·Firebase 콘솔 테스트 메시지로 재확인이 필요하다** — [quickstart.md](../quickstart.md) §2.

---

## 1. 타입별 `data` 필드

`RemoteMessage.data`(전부 `String` 값)가 담는 필드는 유형마다 다르다. 공통 필드는 `type`·`title`·`body`뿐이다.

| `type` | 식별자 필드 | `imageUrl` | 대응 [`PushMessageType`](../data-model.md#1-pushmessagetype) |
|---|---|---|---|
| `PIN_DUPLICATED` | `placeId`, `pinId` (둘 다 옴) | O | `PIN_DUPLICATED` |
| `TOP_COMMENTED_PLACE` | `placeId`, `pinId` | O | `TOP_COMMENTED_PLACE` |
| `NEARBY_PLACE` | `placeId`, `pinId` | O | `NEARBY_PLACE` |
| `NEARBY_PLACE_SUMMARY` | 없음 | 없음 | `NEARBY_PLACE_SUMMARY` |
| `ROOM_MEMBER_JOINED` | `roomId` | 없음 | `ROOM_MEMBER_JOINED` |
| `ROOM_JOINED_SELF` | `roomId` | 없음 | `ROOM_JOINED_SELF` |
| `SAVE_FAILED` | 없음 | 없음 | `SAVE_FAILED` |

`ParsePushMessageUseCase`가 `PushMessage.targetId`로 흡수하는 규칙:

- 장소 대상 세 유형 → `data["pinId"]` (`placeId`는 읽되 버린다 — [research.md D2](../research.md#d2-장소-대상-알림의-도착지-방은-payload의-pinid로-정한다))
- 공동방 대상 두 유형 → `data["roomId"]`
- 그 외 → `null`

**REST 쪽 알림함 응답도 같은 구성이다.** `GET /api/v1/notifications`의 `payload`가 장소 대상에 `placeId`와 `pinId`를 함께 싣고 "장소 상세는 `pinId`로 연다"고 명시한다(OpenAPI 2026-09-04 조회, [SCR-007] 알림함 5.0.0 FR-022). FCM data는 OpenAPI에 실리지 않아 이 계약의 근거가 캡처인 것은 그대로이나, 같은 서버가 두 채널에서 같은 식별자 구성을 쓴다는 방증이 된다.

---

## 2. 예시 (사용자 제공 캡처 그대로)

```json
// PIN_DUPLICATED — 인스타그램에서 추출한 장소가 이미 저장된 경우
{
  "type": "PIN_DUPLICATED",
  "placeId": "(uuid)",
  "pinId": "(uuid)",
  "title": "패스트리 순간",
  "body": "이미 저장해둔 곳이에요",
  "imageUrl": "https://cdn.example/place.jpg"
}

// SAVE_FAILED — 다른 이유로 인스타그램 장소 저장이 실패한 경우
{
  "type": "SAVE_FAILED",
  "title": "잠시 후 다시 시도해주세요",
  "body": "장소를 저장하지 못했어요."
}

// NEARBY_PLACE_SUMMARY — 주변 장소가 여러 곳일 때 대표 푸시
{
  "type": "NEARBY_PLACE_SUMMARY",
  "title": "근처에 저장한 곳 3개가 있어요",
  "body": "반경 3km"
}

// ROOM_MEMBER_JOINED — 다른 사람이 방에 들어온 경우
{
  "type": "ROOM_MEMBER_JOINED",
  "roomId": "4c1d8e20-7b93-4a6f-9e52-0d3fa8b61c47",
  "title": "성수 맛집 탐방",
  "body": "지연님이 들어왔어요"
}
```

`TOP_COMMENTED_PLACE`·`NEARBY_PLACE`는 `PIN_DUPLICATED`와 필드 구성이 같고 `ROOM_JOINED_SELF`는 `ROOM_MEMBER_JOINED`와 같다(본문만 다름) — 반복해 신지 않는다.

---

## 3. 알림 표시 규칙과의 연결

- `title`·`body`는 [SCR-007] 알림함 FR-004가 정한 6종 문구 그대로 온다(FR-007) — 앱이 조립하지 않고 그대로 `NotificationCompat.Builder`의 제목·본문에 싣는다.
- `imageUrl`이 있으면 `NotificationCompat.BigPictureStyle`로 확장 이미지를 싣는다. 없으면 텍스트만(기본 스타일).
- 채널은 하나(FR-008) — 유형별 분기 없이 모두 같은 채널 ID로 보낸다.

## 4. `type`이 이 표에 없을 때(EC-008)

앱이 모르는 문자열이면 `ParsePushMessageUseCase`가 `PushMessage.type = null`을 반환하고, 호출자(`MinoFirebaseMessagingService`)는 알림을 표시하지 않고 조용히 반환한다 — `ResolvePushDestinationUseCase`를 호출하지 않는다.
