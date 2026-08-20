# 계약: RoomListMain 화면

`:feature:room/main/` — 방 리스트 탭의 시작 화면(유일한 Route). 탭 feature이므로 `XShell`·`XNavHost`가 없고 `:feature:main`의 셸 안에서 그려진다(`feature-module.md` 1장). `Peek`/`Half`/`Full` 3단계는 별도 Route가 아니라 이 화면의 상태다([research.md D2](../research.md)).

## UiState

```kotlin
data class RoomListUiState(
    val sheetLevel: BottomSheetLevel = BottomSheetLevel.HALF,   // 진입 기본값(FR-001), EC-007 예외는 시작 인자로 override
    val personalRoom: Room? = null,
    val groupRooms: ImmutableList<Room> = persistentListOf(),
    val roomListSort: RoomListSortOption = RoomListSortOption.ALL,
    val mapMarkerSort: MapMarkerSortOption = MapMarkerSortOption.ALL,
    val categoryFilter: PlaceCategoryFilter = PlaceCategoryFilter.ALL,
    val showNudge: Boolean = false,          // groupRooms.isEmpty() 파생값(FR-008)
    val showGhostCard: Boolean = false,      // groupRooms.isEmpty() 파생값(FR-009)
    val mapCenter: GeoPoint? = null,         // 위치 권한 결과에 따른 카메라 중심(SYS-004 Flow A), 현재 위치 버튼 클릭 시에도 갱신
) : UiState
```

- `showNudge`·`showGhostCard`는 `groupRooms`의 파생값이라 독립적으로 set하지 않는다(단일 진실 공급원 유지, [data-model.md §2](../data-model.md)).
- 필드 근거·타입 상세는 [data-model.md](../data-model.md) 참조 — 이 계약과 data-model.md가 어긋나면 data-model.md를 갱신한다(중복 정의 금지).

## Intent

```kotlin
sealed interface RoomListIntent : Intent {
    data object OnScreenEntered : RoomListIntent   // 탭 진입 — 위치 권한 상태 재조회(D8), sheetLevel은 시작 인자로 이미 결정됨

    data object OnSheetDraggedUp : RoomListIntent
    data object OnSheetDraggedDown : RoomListIntent

    data class OnMapSortSelected(val option: MapMarkerSortOption) : RoomListIntent
    data class OnCategoryFilterSelected(val category: PlaceCategoryFilter) : RoomListIntent
    data class OnRoomListSortSelected(val option: RoomListSortOption) : RoomListIntent

    data class OnRoomCardClick(val roomId: String) : RoomListIntent

    data object OnAddRoomClick : RoomListIntent        // 시트 우상단 [+] (FR-007)
    data object OnGhostCardClick : RoomListIntent      // Ghost Card (FR-009)
    data object OnNudgeCreateClick : RoomListIntent    // Nudge [공동방 만들기] (FR-008)
    data object OnNudgeDismissClick : RoomListIntent   // Nudge [나중에 만들래요] (FR-008)
    data class OnRoomFormResult(val createdRoomId: String?) : RoomListIntent   // RoomFormLauncher 결과 콜백

    data object OnCurrentLocationClick : RoomListIntent   // 현재 위치 버튼([research.md D10](../research.md))
    data class OnLocationPermissionResult(val granted: Boolean) : RoomListIntent
}
```

## SideEffect

```kotlin
sealed interface RoomListSideEffect : SideEffect {
    data object RequestLocationPermission : RoomListSideEffect   // Route가 launcher.launch([FINE, COARSE]) 호출 (D8)
    data class NavigateToRoomDetail(val roomId: String) : RoomListSideEffect   // RoomDetailLauncher 호출 (D5)
    data object NavigateToRoomForm : RoomListSideEffect            // RoomFormLauncher 호출 (D6), 결과는 OnRoomFormResult로 수신
}
```

- 현재 위치로 카메라 이동은 SideEffect가 아니라 `UiState.mapCenter` 갱신으로 모델링한다 — `MinoMap`이 선언적으로 `mapCenter`를 구독하므로 별도 일회성 이벤트가 필요 없다.

## 분기 규칙 — 시트 드래그 전이 (D2, UX-002)

| 현재 `sheetLevel` | `OnSheetDraggedUp` | `OnSheetDraggedDown` |
|---|---|---|
| `PEEK` | → `HALF` | (무시, 최하단) |
| `HALF` | → `FULL`(바텀 네비게이션·현재 위치 버튼 숨김, UX-002) | → `PEEK`(88dp) |
| `FULL` | (무시, 최상단) | → 직전 `HALF` 고정 높이로 복귀(공동방 수 기준, FR-002) |

## 분기 규칙 — 진입 시 초기 `sheetLevel` (FR-001, EC-007)

| 시작 인자 `sheetLevelOverride` | 결정된 `sheetLevel` |
|---|---|
| `null`(일반 진입) | `HALF`, 높이는 공동방 수로 결정(FR-002) |
| `HALF`/`FULL`([SCR-005] 방 상세 `[X]` 복귀) | 전달받은 값 그대로 — 규칙 소유는 `room-detail` spec FR-004([spec.md §3.2](../spec.md)) |

## 분기 규칙 — 위치 권한 요청 (D8, FR-001, EC-002)

| `OnScreenEntered` 시점 OS 권한 상태 | 동작 |
|---|---|
| 이미 허용됨([SCR-003]·[SCR-008]에서 허용 이력) | 팝업 없이 즉시 실제 위치로 `mapCenter` 설정 |
| 미허용 | `SideEffect.RequestLocationPermission` → 결과는 `OnLocationPermissionResult`로 수신 |
| `OnLocationPermissionResult(granted = false)` | 기본 디폴트 좌표로 `mapCenter` 설정(EC-002) |
| `OnLocationPermissionResult(granted = true)` | 실제 위치로 `mapCenter` 설정 |

## 분기 규칙 — Nudge·Ghost Card 노출 (FR-008~FR-010, D9)

| `groupRooms.isEmpty()` | `showNudge` | `showGhostCard` |
|---|---|---|
| `true`(매 진입·재조회마다 재계산 — 닫힘을 기억하는 상태 없음, [research.md D9](../research.md)) | `true` | `true` |
| `false` | `false` | `false` |

`OnNudgeDismissClick`은 `showNudge`만 로컬로 `false`로 접어 시트를 닫되, `groupRooms`는 바꾸지 않는다 — 그래서 다음 `OnScreenEntered`(탭 재진입)에서 `groupRooms.isEmpty()`가 여전히 `true`면 `showNudge`가 다시 `true`로 재계산된다(TS-014).

## 재조회

- `personalRoom`·`groupRooms`는 `RoomRepository.observeMyRooms()`([contracts/room-repository.md](./room-repository.md)) `Flow` 구독으로 항상 최신 유지된다 — `Intent`로 명시적 재조회를 트리거하지 않는다(다른 화면에서 방 정보가 바뀌어도 재구독 없이 반영, [spec.md 유저 플로우 3](../spec.md)).
- 위치 권한 상태는 `Flow`가 아니라 `OnScreenEntered`마다 `ContextCompat.checkSelfPermission`로 그 순간 값을 직접 조회한다(D8) — 상태 저장·캐싱 없음.

## Figma

[003-1 방 리스트_개인방만 존재](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2370-114745&m=dev) · [003-2 방리스트_개인방 + 공동방](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2392-123138&m=dev) · [003-1-1 peek](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2661-157242&m=dev) · [003-2-1 peek](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2661-158108&m=dev) · [003-1-2 half(Default)](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2661-157338&m=dev) · [003-2-2 half_공동방 1개(Default)](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2661-158144&m=dev) · [003-2-2-1 half_공동방 N개(Default)](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2661-159182&m=dev) · [003-1-3 full_개인방만 존재](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2661-157259&m=dev) · [003-2-3 full](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2661-158125&m=dev) — 정렬 드롭다운·칩은 구현 단계에서 `MinoMenu`·`MinoChip`을 조립해 그린다(대조 절차는 `figma-design-fidelity.md`, 재사용 근거는 [research.md D11](../research.md)).
