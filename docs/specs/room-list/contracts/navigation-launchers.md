# 계약: 크로스 feature 전환 (`:core:navigation`)

**대상 plan**: [../plan.md](../plan.md) · 근거: [../research.md](../research.md) D6

> ⚠️ **미구현 의존성**: 아래 계약의 구현체는 이 spec 범위 밖 모듈(`:feature:roomform`, 현재 미존재)이 갖는다. `:feature:room`은 인터페이스만 주입받아 호출하며, 실제 화면 전환이 동작하려면 그 모듈의 구현이 먼저(또는 함께) 필요하다. `/mino-task`가 작업 순서를 정할 때 반영해야 한다.
>
> [FR-006] 방 카드 선택 → [SCR-005] 방 상세 전환은 더 이상 이 계약에 속하지 않는다 — 방 상세가 `:feature:room` 내부 nested Route(`RoomDetailMain`)로 재설계되며 `RoomDetailLauncher`·`EXTRA_ROOM_DETAIL_ROOM_ID`는 폐기됐다([research.md D13](../research.md)). 전환 방식은 [room-detail/plan.md](../../room-detail/plan.md)가 정의한다.

## `RoomFormLauncher` — [FR-007]·[FR-008]·[FR-009] 공동방 생성 폼 호출

```kotlin
// :core:navigation — activity/launcher/RoomFormLauncher.kt
interface RoomFormLauncher : ActivityLauncher
```

**호출 (`:feature:room`)**

```kotlin
@Inject lateinit var roomFormLauncher: RoomFormLauncher
// ...
roomFormLauncher.launch(activity, resultLauncher = createRoomResultLauncher)
```

- 결과가 필요한 이유: [FR-007]이 "생성 완료 시 [SCR-005] 방 상세로 직행"을 요구하므로, room-list는 생성된 `roomId`를 받아 곧바로 `navController.navigate(RoomDetailMain(roomId))`를 호출해야 한다(방 상세가 `:feature:room` 내부 nested Route가 되며 크로스 feature Launcher가 아니라 feature 내부 전환으로 바뀌었다 — [research.md D13](../research.md)). 결과 계약(extra 키)은 `:feature:roomform` 쪽 plan이 확정한다 — 여기서는 room-list가 "결과를 받는 호출자"라는 사실만 못박는다.
