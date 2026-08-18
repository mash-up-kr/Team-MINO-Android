# 계약: 크로스 feature 전환 (`:core:navigation`)

**대상 plan**: [../plan.md](../plan.md) · 근거: [../research.md](../research.md) D5·D6

> ⚠️ **미구현 의존성**: 아래 두 계약의 구현체는 이 spec 범위 밖 모듈(`:feature:roomdetail`·`:feature:roomform`, 둘 다 현재 미존재)이 갖는다. `:feature:room`은 인터페이스만 주입받아 호출하며, 실제 화면 전환이 동작하려면 그 모듈들의 구현이 먼저(또는 함께) 필요하다. `/mino-task`가 작업 순서를 정할 때 반영해야 한다.

## `RoomDetailLauncher` — [FR-006] 방 카드 선택 → [SCR-005] 방 상세

```kotlin
// :core:navigation — activity/launcher/RoomDetailLauncher.kt
interface RoomDetailLauncher : ActivityLauncher

// :core:navigation — activity/launcher/ExtraTag.kt
const val EXTRA_ROOM_DETAIL_ROOM_ID = "room_detail_room_id"
```

**호출 (`:feature:room`)**

```kotlin
roomDetailLauncher.launch(activity) { putExtra(EXTRA_ROOM_DETAIL_ROOM_ID, room.id) }
```

- `feature-navigation.md` 1장의 표준 패턴을 그대로 따른다(결과 필요 없음 — fire-and-forget).
- `EC-007`(방 상세 `[X]` 복귀 시 시트 상태 유지)은 이 계약의 책임이 아니다 — room-detail이 나갈 때 자신의 시트 상태를 어떤 형태로든 되돌려 보내야 room-list가 이어받을 수 있는데, 그 반환 경로(Activity result? 공유 상태?)는 **아직 미확정**이며 room-detail의 plan이 결정할 문제다. 이 spec은 room-list 쪽 수신 인터페이스(`RoomListMain` 시작 Route의 `sheetLevelOverride`)만 [data-model.md](../data-model.md) §2에 정의해 뒀다.

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

- 결과가 필요한 이유: [FR-007]이 "생성 완료 시 [SCR-005] 방 상세로 직행"을 요구하므로, room-list는 생성된 `roomId`를 받아 곧바로 `RoomDetailLauncher`를 다시 호출해야 한다. 결과 계약(extra 키)은 `:feature:roomform` 쪽 plan이 확정한다 — 여기서는 room-list가 "결과를 받는 호출자"라는 사실만 못박는다.
