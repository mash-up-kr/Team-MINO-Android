# 데이터 모델: 방 리스트 탭 (Room List Tab)

**대상 spec**: [spec.md](./spec.md) 2.1.0 · **대상 plan**: [plan.md](./plan.md) · 근거: [research.md](./research.md) D3·D7

이 문서는 현재 설계 상태만 담는다(개정 시 대체). 실제 필드 목록은 구현 단계에서 API 계약이 확정되면 좁혀질 수 있다.

---

## 1. `:core:domain` 도메인 모델

### `Room`

```kotlin
data class Room(
    val id: String,
    val name: String,
    val description: String?,
    val color: MinoRoomColor?,          // :core:design-system 팔레트, 미선택 시 null(회색) — ADR 2026-08-14
    val isPersonal: Boolean,            // 개인방("내 장소") 여부
    val placeCount: Int,
    val thumbnail: RoomThumbnail,
    val memberSummary: RoomMemberSummary,
    val lastPlaceSavedAt: Instant?,     // "최근 저장 순" 정렬 근거. 장소 0개면 null
    val commentCount: Int,              // "코멘트 순" 정렬 근거 — 방에 속한 장소들의 코멘트 총합
)
```

- **검증 규칙**: `isPersonal == true`인 `Room`은 최대 1개(개인방은 사용자당 하나, PRD 「개인방」정의). `placeCount == 0`이면 `lastPlaceSavedAt == null`.
- **관계**: `Room` 1 — N `Place`(다른 spec 소유, 이 spec은 `placeCount`·`lastPlaceSavedAt`·`commentCount`로 집계값만 소비하고 `Place` 자체를 모델링하지 않는다 — [spec.md §3.2](./spec.md)).

### `RoomThumbnail` (sealed)

```kotlin
sealed interface RoomThumbnail {
    data class ColorAndCharacter(val color: MinoRoomColor?) : RoomThumbnail   // 장소 0개
    data class Collage(val imageUrls: List<String>) : RoomThumbnail            // 장소 N개, 최대 4장
}
```

- PRD 「방 썸네일」정의 그대로: 장소 0개는 색상+캐릭터, N개는 최대 4장 콜라주.
- `Collage.imageUrls`는 `1..4`개만 유효(그 이상은 상위에서 자른다).

### `RoomMemberSummary`

```kotlin
data class RoomMemberSummary(
    val visibleAvatarUrls: List<String?>,   // 최대 4개, 최근 저장자가 마지막(우측)
    val overflowCount: Int,                  // 5명 이상일 때 "보이지 않는 나머지 인원". 99 초과면 상위에서 "99+"로 표기
)
```

- PRD 「방 멤버 아바타」정의(4명 이하: 전부 표시·카운터 없음 / 5명 이상: 아바타 3개+카운터). `visibleAvatarUrls`가 4개면 카운터 없음(0), 3개면 `overflowCount > 0`.

### `RoomListSortOption` (enum) — [FR-005], `Full` 상태 방 카드 정렬

```kotlin
enum class RoomListSortOption { ALL, RECENTLY_SAVED, MOST_COMMENTED }
```

### `MapMarkerSortOption` (enum) — [FR-011], `Peek`/`Half` 지도 마커 정렬 드롭다운

```kotlin
enum class MapMarkerSortOption { ALL, GGUK_PICK, LATEST, NEARBY, MOST_COMMENTED }
```

- `RoomListSortOption`과 통합하지 않는 이유는 [research.md D7](./research.md).

### `PlaceCategoryFilter` (enum) — [FR-011]

```kotlin
enum class PlaceCategoryFilter { ALL, CAFE, RESTAURANT }
```

- PRD 「카테고리 필터」정의: 3종 고정(동적 생성 아님).

---

## 2. `:feature:room` UI 상태

이 절은 domain이 아니라 화면 상태다. `RoomListUiState`·`Intent`·`SideEffect`·분기 규칙의 공식 계약은 [contracts/room-list-main-contract.md](./contracts/room-list-main-contract.md)가 소유한다 — 아래는 그 계약이 참조하는 보조 enum만 남긴다(중복 정의 금지).

### `BottomSheetLevel` (enum)

```kotlin
enum class BottomSheetLevel { PEEK, HALF, FULL }
```

- **EC-007(방 상세 `[X]` 복귀 시 시트 상태 유지)은 시작 인자가 아니라 NavHost 백스택 보존으로 자연히 해결된다** — 방 상세([SCR-005])가 `:feature:room` 내부 nested Route(`RoomDetailMain`)가 되면서([room-list/research.md D13](./research.md)), `RoomListMain`은 방 상세 진입 중에도 백스택에 그대로 남아 있고 그 화면의 `RoomListViewModel`(및 `sheetLevel`)도 NavHost가 보존한다. 별도 `sheetLevelOverride` 시작 인자나 result 계약이 필요 없다(2026-08-20 plan 1.2.0 설계였던 `sheetLevelOverride: BottomSheetLevel?` 시작 인자는 이 재검토로 폐기됐다). 실제 `RoomListUiState` 필드·Intent·SideEffect·상태 전이 규칙은 [contracts/room-list-main-contract.md](./contracts/room-list-main-contract.md) 참조.

---

## 3. Repository 계약 참조

인터페이스 시그니처는 [contracts/room-repository.md](./contracts/room-repository.md) 참조 — 중복 기술하지 않는다.
