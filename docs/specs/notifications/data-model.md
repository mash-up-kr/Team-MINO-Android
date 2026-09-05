# 데이터 모델: 알림함 (Notifications)

**대상 스펙**: [spec.md](./spec.md) 7.0.0 · **계획**: [plan.md](./plan.md)

이 문서는 현재 설계의 타입만 담는다. 과거 형태는 남기지 않는다.

---

## 1. 도메인 모델 (`:core:domain`)

### 1.1 `Notification` — 알림 한 건

spec §2.3 「알림」에 대응한다. **읽음 여부 필드가 없다** — FR-016이 그 상태 자체를 두지 않는다.

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | `String` | 서버 UUID. 목록 키 |
| `type` | `NotificationType` | 도착지와 썸네일 갈래를 정한다 |
| `typeLabel` | `String` | 화면에 그대로 그리는 유형 문구. 서버가 완성해 준다([research.md D4](./research.md)) |
| `targetName` | `String` | 대상 이름(장소명 또는 공동방 이름). 저장 오류는 `잠시 후 다시 시도해주세요` |
| `thumbnailUrl` | `String?` | 장소 대상 3종의 썸네일. 그 밖의 유형에서는 쓰지 않는다([research.md D5](./research.md)) |
| `target` | `NotificationTarget` | 이동 대상 참조 |
| `createdAt` | `Instant` | 발생 시각. FR-003의 경과 시간 계산 원천 |

`createdAt`을 `Instant`로 든다. 표기 문자열로 미리 바꾸지 않는 것은 계산 시점을 화면이 정하기 때문이다(EC-005, [research.md D12](./research.md)).

### 1.2 `NotificationType` — 유형 6종

FR-004의 6종에 1:1 대응하며 서버 enum과 같은 갈래다. **위치 기반 대표 알림은 이 목록에 없다** — FR-019대로 목록에 실리지 않는다.

| 값 | 서버 enum | 대상 | 썸네일 |
|---|---|---|---|
| `PLACE_DUPLICATED` | `PIN_DUPLICATED` | 핀 | `thumbnailUrl` |
| `SAVE_FAILED` | `SAVE_FAILED` | 없음 | 오류 아이콘 (고정) |
| `NEARBY_PLACE` | `NEARBY_PLACE` | 핀 | `thumbnailUrl` |
| `TOP_COMMENTED_PLACE` | `TOP_COMMENTED_PLACE` | 핀 | `thumbnailUrl` |
| `ROOM_MEMBER_JOINED` | `ROOM_MEMBER_JOINED` | 방 | `thumbnailUrl` |
| `ROOM_JOINED_SELF` | `ROOM_JOINED_SELF` | 방 | `thumbnailUrl` |

**서버에 없는 값이 오면 그 항목을 버린다.** 알 수 없는 유형은 문구도 도착지도 정할 수 없어 행으로 그릴 수 없다. 목록 전체를 실패로 만들지는 않는다 — 남은 항목은 그대로 그린다.

### 1.3 `NotificationTarget` — 이동 대상

spec §2.3 「알림 대상」. 유형이 대상의 종류를 정하므로 sealed로 가른다.

```
sealed interface NotificationTarget
  Pin(pinId: String)       — 장소 대상 3종
  Room(roomId: String)     — 공동방 참가 ①②
  None                     — 저장 오류
```

`Pin`이 드는 것은 **`placeId`가 아니라 `pinId`다.** 서버 `payload`가 둘 다 주지만 "장소 상세는 `pinId`로 연다"고 명시하고([contracts/notification-api.md §1](./contracts/notification-api.md)), 도착지 방이 그 핀으로 정해지기 때문이다(spec FR-022). `placeId`는 모델에 싣지 않는다 — 쓰는 곳이 없는 값을 들면 나중에 어느 쪽이 도착지 키인지 헷갈린다. push-notification의 `PushMessage.targetId`가 같은 이유로 `placeId`를 버린다.

### 1.4 `NotificationPage` — 한 묶음

```
NotificationPage(items: List<Notification>, hasNext: Boolean)
```

`page`·`pageSize`를 담지 않는다. 다음에 무엇을 요청할지는 호출자가 알고 있고, 응답이 알려 줄 필요가 있는 것은 "더 있는가" 하나다(FR-018·EC-018).

### 1.5 `NotificationDestination` — 해석된 도착지

[research.md D8](./research.md)의 UseCase가 돌려준다. **알림의 대상 참조와 다르다** — 이쪽은 실제로 열 화면이 정해진 결과다.

```
sealed interface NotificationDestination
  PlaceDetail(pinId: String)   — 장소 대상 3종([research.md D14])
  RoomDetail(roomId: String)   — 공동방 참가 ①②([research.md D10])
  SaveErrorGuide               — 그래프 내부 전환
```

**`NotificationTarget`과 거의 같은 모양이 됐다.** spec 5.0.0에서 도착지 판정이 순수 매핑으로 줄어 둘의 거리가 좁아졌지만 타입은 나눠 둔다 — 저장 오류가 대상으로는 `None`이고 도착지로는 화면 하나라 대응이 어긋나고, 도메인이 서버에서 받은 것과 화면이 열 것을 같은 타입으로 부르면 어느 쪽 규칙이 바뀔 때 다른 쪽이 함께 흔들린다.

`PlaceDetail`이 `roomId`를 들지 않는다. 4.1.1까지는 FR-020의 나갈 방을 위해 함께 실었으나, `PlaceDetailRequestHolder`가 받는 것은 `pinId`와 `origin`뿐이고 나갈 방은 저장 탭이 그 핀에서 스스로 정한다(place-detail spec FR-009).

**`Unreachable`을 두지 않는다.** spec 7.0.0 UX-006이 대상 소멸 판정을 도착지 화면의 몫으로 옮겼으므로, 알림함이 「갈 수 없음」이라는 도착지를 만들 일이 없다([contracts/notification-repository.md §2](./contracts/notification-repository.md)).

---

## 2. 화면 모델 (`:feature:notifications`)

도메인 모델을 그대로 그리지 않는다. 화면이 필요로 하는 것은 **어느 구간으로 끊을지 정해진 경과 시간**과 **어떤 썸네일을 그릴지 정해진 상태**다.

### 2.1 `NotificationItemUiModel`

| 필드 | 타입 | 유래 |
|---|---|---|
| `id` | `String` | `Notification.id` |
| `typeLabel` | `String` | 그대로 |
| `targetName` | `String` | 그대로 |
| `elapsed` | `ElapsedTime` | `createdAt`을 FR-003의 네 구간 중 하나로 끊은 결과([research.md D12](./research.md)). 아래 |
| `thumbnail` | `NotificationThumbnail` | 아래 |

### 2.1.1 `ElapsedTime` — 경과 시간 네 구간

FR-003의 네 구간을 타입으로 세운다. **문구를 담지 않고 어느 갈래인지와 그 갈래가 쓰는 수만 든다.**

```
sealed interface ElapsedTime
  JustNow                      — 1시간 미만
  HoursAgo(hours)              — 1시간 이상 24시간 미만
  DaysAgo(days)                — 24시간 이상 7일 미만
  AbsoluteDate(month, day)     — 7일 이상. 경과가 아니라 발생한 날짜
```

경계값은 **경계 자체가 다음 구간에 속한다**(판정이 `<`) — 정확히 60분이 지난 알림은 `방금`이 아니라 `1시간 전`이다(SC-005).

**문자열을 화면 모델에 담지 않는 이유**는 둘이다. 문구를 만들려면 문자열 리소스가 필요하고, 그러면 ViewModel이 `Context`를 잡아 UI 문구를 조립하게 된다. 그리고 판정만 순수 함수로 남겨야 SC-005의 경계값 6개를 기기 없이 확인할 수 있다 — `:feature:notifications`에 Robolectric이 없다. 문구는 `NotificationRow`가 이 갈래를 받아 모듈의 `res/values/strings.xml`에서 꺼낸다. 같은 형태의 선례가 `:feature:room`의 `placeCommentTime`이다.

`type`과 `target`을 화면 모델에 남기지 않는다. 화면이 하는 일은 행을 그리고 탭을 알리는 것뿐이고, 도착지 판정은 UseCase가 한다(D8). 탭 Intent는 `id`만 싣는다.

### 2.2 `NotificationThumbnail` — 썸네일 두 갈래

FR-012의 표를 그대로 타입으로 세운다. 렌더링 시점에 `when`이 남김없이 갈리도록 sealed로 둔다.

```
sealed interface NotificationThumbnail
  Image(url: String?)   — 서버가 준 thumbnailUrl. null이면 플레이스홀더
  SaveError             — 고정 오류 아이콘
```

**저장 오류만 유형으로 갈린다.** 나머지 5종은 대상이 장소든 방이든 같은 갈래이고, 무엇을 대표 이미지로 삼을지는 서버가 정한다(spec 6.0.0 FR-012).

`SaveError`를 `Image(null)`로 합치지 않는다. 저장 오류는 **고정 오류 아이콘**을 그리고 나머지의 `null`은 **플레이스홀더**를 그려 서로 다른 그림이며, 서버가 저장 오류에 이미지를 실어 보내더라도 이 유형은 그것을 쓰지 않는다.

> 5.0.0까지는 세 갈래(`Place`·`Room`·`SaveError`)였고 `Room`이 `ImmutableList<String>`과 `MinoRoomColor?`를 함께 들었다. 방 목록에서 찾아 합성하던 값인데, spec 6.0.0이 그 합성을 걷어냈다([research.md D5](./research.md)).

### 2.3 `NotificationUiState`

| 필드 | 타입 | 설명 |
|---|---|---|
| `items` | `ImmutableList<NotificationItemUiModel>` | 지금까지 이어 붙인 전체 |
| `phase` | `NotificationPhase` | `Loading` / `Content` / `Empty` / `Error` |
| `isAppending` | `Boolean` | 다음 묶음을 받는 중 |
| `appendError` | `Boolean` | 목록 끝의 재시도 표시(UX-012·EC-016) |
| `hasNext` | `Boolean` | 더 불러올 것이 있는가(EC-018) |

`phase`를 따로 두는 이유는 UX-001이다 — 조회 중에는 빈 상태 문구를 그리면 안 되므로 `Loading`과 `Empty`가 서로 다른 상태여야 한다. `Error`는 첫 페이지 실패 전용이고(UX-002·EC-001), 추가 로드 실패는 `phase`를 바꾸지 않고 `appendError`만 세운다 — 이미 그린 목록을 지우지 않기 위해서다.

**읽음 여부·권한 상태·배너 노출 필드가 없다.** SC-009가 화면 상태를 목록·빈 상태·오류 셋으로 못 박았고, FR-016·FR-017이 나머지를 금지한다.

### 2.4 `NotificationIntent` · `NotificationSideEffect`

계약은 [contracts/notification-ui.md](./contracts/notification-ui.md)가 소유한다.

---

## 3. 상태 전이

```
                  ┌──────────────── 재시도 ────────────────┐
                  ↓                                        │
  (진입) ──▶ Loading ──▶ Content ──▶ (끝 도달) ──▶ Content(isAppending)
                  │           ▲                              │
                  │           └───────── 성공 ───────────────┤
                  │                                          │
                  ├──▶ Empty                     실패 ──▶ Content(appendError)
                  └──▶ Error
```

- `Loading → Empty`는 첫 응답이 0건이고 `hasNext`가 `false`일 때만이다(UX-001).
- `Content(appendError)`에서 재시도하면 `isAppending`으로 돌아가며, **`items`는 어느 경로에서도 줄어들지 않는다**(UX-012).
- 탭을 떠났다 돌아오는 것은 이 전이를 타지 않는다 — `saveState`/`restoreState`가 상태를 통째로 되살린다(FR-015·TS-011·TS-043).

## 4. 영속 데이터

| 대상 | 저장소 | 소유 |
|---|---|---|
| 장소별 표시 기준 방 | DataStore Preferences | `:core:data`([research.md D6](./research.md)) |

알림 목록은 저장하지 않는다 — spec §4가 "사용자는 온라인 상태에서 알림함을 연다"로 오프라인 캐시를 전제에서 뺐다.
