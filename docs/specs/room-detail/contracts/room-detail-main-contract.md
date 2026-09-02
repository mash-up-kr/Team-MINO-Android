# 계약: `RoomDetailMain` 화면

`:feature:room/detail/` — 방 상세의 유일한 Route. room-list의 `RoomListMain`과 같은 `:feature:room` 모듈 안에 있으며 그 화면의 `roomGraph()`가 등록한다([room-list/plan.md](../../room-list/plan.md), [research.md D1](../research.md)). `Peek`/`Half`/`Full` 3단계는 별도 Route가 아니라 이 화면의 상태다([research.md D5](../research.md), room-list D2와 동일 패턴).

`RoomDetailMain`은 `:core:navigation`의 `ImmersiveRoute` 마커 인터페이스를 구현한다([research.md D3](../research.md)) — `:feature:main`의 `MainShell`이 이 마커로 바텀 네비게이션 노출 여부를 판정한다.

## Route

```kotlin
// :feature:room — RoomNavigation.kt (기존 파일에 병합)
@Serializable
internal data class RoomDetailMain(val roomId: String) : Route, ImmersiveRoute
```

- `roomId`는 primitive라 `typeMap` 불필요(`feature-navigation.md` 2장).

## UiState

```kotlin
data class RoomDetailUiState(
    val room: Room? = null,                                    // 헤더(제목·설명·장소 수·멤버 아바타, FR-001) — room-list의 Room 재사용
    val sheetLevel: BottomSheetLevel = BottomSheetLevel.HALF,   // 진입 기본값(FR-001, spec.md §4 — room-list와 우연히 같은 값)
    val places: ImmutableList<Place> = persistentListOf(),
    val sortOption: MapMarkerSortOption = MapMarkerSortOption.GGUK_PICK,  // FR-005 표시 순서 1번째 항목을 기본값으로 확정(plan 2.0.1) — PRD가 정렬 드롭다운 펼침 순서 1번째로 "꾹 Pick"을 명시
    val categoryFilter: PlaceCategoryFilter = PlaceCategoryFilter.ALL,
    val viewType: PlaceViewType = PlaceViewType.LIST,           // FR-007 "좌측 아이콘=리스트형"이 먼저 언급된 순서를 기본값으로 확정(plan 2.0.1)
    val isOwner: Boolean = false,                                // FR-012 방 편집 노출 여부(EC-006), FR-013 나가기 문구 분기(EC-005/개인방)
    val isPersonalRoom: Boolean = false,                         // EC-002·EC-005 — 나가기 메뉴 비노출 판정
    val showMoreMenu: Boolean = false,                           // 더보기[⋮] 메뉴 표출 여부(FR-013)
    val placeToShare: Place? = null,                             // SYS-003 방 선택 시트(FR-009). null이면 닫힘 — 여는 순간 대상이 정해지므로 열림 플래그를 따로 두지 않는다
    val shareRooms: ImmutableList<RoomShareItem> = persistentListOf(),   // 공유 후보. GetRoomPickerRoomsUseCase(place.placeId) 결과이며 alreadySaved가 EC-004의 체크+비활성을 정한다
    val shareSelectedRoomIds: ImmutableSet<String> = persistentSetOf(),  // 새로 고른 방. 이미 담긴 방은 들어오지 않는다
    val isSharing: Boolean = false,                              // 복제 요청 중. CTA를 잠가 같은 방에 두 번 가지 않게 한다
    val showInviteSheet: Boolean = false,                        // SYS-006 초대 시트(FR-011)
    val inviteCode: String? = null,                              // RoomRepository.createInvitation() 결과(research.md D16). null이면 미발급/로딩 중
    val roomMembers: ImmutableList<RoomMember> = persistentListOf(),  // RoomRepository.getMembers() — 초대 참여자 목록 + 위임 대상 선택 공용(research.md D16)
    val placeToDelete: Place? = null,                            // FR-010 삭제 확인 모달 대상. null이면 모달 비표출
    val leaveDialogState: LeaveDialogState = LeaveDialogState.None,  // SYS-007 나가기/위임 모달 상태(research.md D12·D15)
) : UiState

enum class LeaveDialogState { None, ConfirmMember, ConfirmOwnerSingle, DelegateOwner }
```

- `places`는 `sortOption`·`categoryFilter`로 이미 정렬·필터링된 최종 목록이다(room-list의 `groupRooms` 정렬 패턴과 동일하게 화면이 가공, [room-list/contracts/room-repository.md](../../room-list/contracts/room-repository.md) "정렬·필터는 클라이언트 쪽" 참고).
- `LeaveDialogState`는 PRD [SYS-007] Flow A(일반 멤버)/Flow B(방장 — 1인 공동방은 위임 없이 즉시 삭제, N인 공동방은 위임 모달)를 표현한다.
- **[SYS-003] 방 선택 시트는 [SCR-006] 장소 상세와 한 벌을 쓴다.** 시트 자체(`RoomShareItem` 포함)와 그 규칙은 [place-detail-main-contract.md §3.4](../../place-detail/contracts/place-detail-main-contract.md)가 소유하고, 이 화면은 목록을 만들어 넘기고 콜백을 받는 쪽이다. 목록 출처가 `RoomRepository.observeMyRooms()`에서 `GetRoomPickerRoomsUseCase(place.placeId)`로 바뀐 것도 그 시트가 요구하는 `hasPlace` 때문이며(EC-004), 복제는 `PlaceRepository.duplicatePin(place.id, roomIds)`이 보낸다.
- 필드 근거·타입 상세는 [data-model.md](../data-model.md) 참조 — 이 계약과 data-model.md가 어긋나면 data-model.md를 갱신한다(중복 정의 금지).

## Intent

```kotlin
sealed interface RoomDetailIntent : Intent {
    data object OnScreenEntered : RoomDetailIntent          // 진입 — room·place 구독 시작

    data object OnSheetDraggedUp : RoomDetailIntent
    data object OnSheetDraggedDown : RoomDetailIntent

    data class OnSortSelected(val option: MapMarkerSortOption) : RoomDetailIntent
    data class OnCategoryFilterSelected(val category: PlaceCategoryFilter) : RoomDetailIntent
    data class OnViewTypeSelected(val viewType: PlaceViewType) : RoomDetailIntent

    data object OnCloseClick : RoomDetailIntent              // [X] — FR-004
    data object OnPlaceClick : RoomDetailIntent               // 장소 카드/마커 선택 — FR-001 유저 플로우 1-4 (파라미터는 [TBD], SCR-006 계약 미정)

    data class OnPlaceMoreClick(val place: Place) : RoomDetailIntent    // 장소 카드 더보기[...] — FR-008
    data class OnShareToOtherRoomClick(val place: Place) : RoomDetailIntent  // FR-009 → placeToShare
    data class OnPlaceDeleteClick(val place: Place) : RoomDetailIntent      // FR-010 → placeToDelete
    data object OnPlaceDeleteConfirm : RoomDetailIntent
    data object OnPlaceDeleteCancel : RoomDetailIntent
    data class OnRoomSelectToggle(val roomId: String) : RoomDetailIntent  // FR-009 방 카드 탭
    data object OnRoomSelectConfirm : RoomDetailIntent       // FR-009 [공유하기] — 고른 방은 상태가 든다
    data object OnRoomSelectDismiss : RoomDetailIntent

    data object OnMoreMenuClick : RoomDetailIntent            // 화면 더보기[⋮] — FR-013
    data object OnMoreMenuDismiss : RoomDetailIntent
    data object OnInviteClick : RoomDetailIntent               // [친구 +] — FR-011 → showInviteSheet, RoomRepository.getMembers·createInvitation 호출(research.md D16)
    data object OnInviteSheetDismiss : RoomDetailIntent
    data object OnEditRoomClick : RoomDetailIntent             // 더보기 → [방 편집] — FR-012
    data object OnLeaveClick : RoomDetailIntent                 // 더보기 → [나가기] — FR-013, RoomRepository.leaveRoom 호출(research.md D15)
    data object OnLeaveConfirm : RoomDetailIntent
    data object OnLeaveCancel : RoomDetailIntent
    data class OnOwnerDelegateSelected(val memberId: String) : RoomDetailIntent   // SYS-007 Flow B — roomMembers 중 선택
    data object OnOwnerDelegateConfirm : RoomDetailIntent                          // RoomRepository.transferOwner 이어서 leaveRoom(research.md D15)
}
```

## SideEffect

```kotlin
sealed interface RoomDetailSideEffect : SideEffect {
    data object NavigateBack : RoomDetailSideEffect                       // Route가 popBackStackIfResumed(entry) 호출 — FR-004, research.md D2
    data class NavigateToPlaceDetail(val placeId: String) : RoomDetailSideEffect  // [SCR-006] 전환 — 계약은 [TBD](장소 상세 spec 부재)
    data object NavigateToRoomForm : RoomDetailSideEffect                  // RoomFormLauncher 편집 모드 호출 — FR-012, research.md D9([TBD] extra 키)
    data object ShowShareCompleteToast : RoomDetailSideEffect              // FR-009 "공유가 완료되었습니다." 3초 토스트, UX-002
    data object ShowEditCompleteSnackbar : RoomDetailSideEffect            // FR-012 "방 편집이 완료되었어요"
    data object NavigateToRoomList : RoomDetailSideEffect                  // SYS-007 나가기 완료 → popBackStackIfResumed(entry)로 SCR-004 복귀, research.md D12
}
```

- 장소 삭제([FR-010])는 목록에서 바로 사라지는 것으로 충분해(SC-003) 별도 SideEffect 없이 `UiState.places` 갱신만으로 표현한다.

## 분기 규칙 — 시트 드래그 전이 (FR-002)

room-list의 [분기 규칙](../../room-list/contracts/room-list-main-contract.md)과 같은 3단 전이를 쓴다. 단, `Full` 승격 시 숨기는 대상은 room-list의 "바텀 네비게이션·현재 위치 버튼"이 아니라 방 상세 자체의 UI(더보기 위치가 `Peek`→상단, 그 외→하단으로 바뀌는 것 — FR-003)이며, 바텀 네비게이션은 `Full` 여부와 무관하게 `ImmersiveRoute`로 이미 상시 숨겨져 있다(D3).

| 현재 `sheetLevel` | `OnSheetDraggedUp` | `OnSheetDraggedDown` |
|---|---|---|
| `PEEK` | → `HALF` | (무시, 최하단) |
| `HALF` | → `FULL` | → `PEEK`(88dp) |
| `FULL` | (무시, 최상단) | → 직전 `HALF`(256dp)로 복귀 |

## 분기 규칙 — 더보기[⋮] 위치 (FR-003)

| `sheetLevel` | 위치 |
|---|---|
| `PEEK` | 화면 상단 |
| `HALF`/`FULL` | 화면 하단 |

## 분기 규칙 — 더보기 메뉴 항목 (FR-012·FR-013, EC-005·EC-006)

| `isOwner` | `isPersonalRoom` | 노출 항목 |
|---|---|---|
| `true` | `false` | 방 편집, 나가기 |
| `false` | `false` | 나가기 |
| `true`/`false` | `true`(개인방) | (항목 없음 — 메뉴 자체를 비노출하거나 빈 상태로 둔다. 개인방은 방장 개념이 없어 `isOwner`도 의미가 없다) |

## 분기 규칙 — 나가기 플로우 (SYS-007, research.md D12·D15·[failures/2026-08-30-leave-flow-member-count.md](../../../failures/2026-08-30-leave-flow-member-count.md))

| `isOwner` | 방 멤버 수 | `leaveDialogState` 전이 | 서버 호출 |
|---|---|---|---|
| `false` | - | `None` → `ConfirmMember` → (확인) `NavigateToRoomList` | `RoomRepository.leaveRoom(roomId)` → `200` |
| `true` | 1인(본인만) | `None` → `ConfirmOwnerSingle` → (확인) 즉시 삭제 → `NavigateToRoomList` | `leaveRoom(roomId)` → `200`(서버가 방을 자동 삭제) |
| `true` | N인(2명 이상) | `None` → **곧장** `DelegateOwner` → 멤버 선택(`roomMembers`) → `OnOwnerDelegateConfirm` | `transferOwner(roomId, nextOwnerId)` → `200` → 이어서 `leaveRoom(roomId)` → `200` |

- **클라이언트가 멤버 수를 미리 본다.** [RoomDetailUiState.room]의 `memberSummary`(화면 진입 시 `GET /rooms/{roomId}/members`로 이미 채워짐)로 `OnLeaveClick` 시점에 곧장 올바른 모달을 고른다 — `leaveRoom` 호출의 `409`를 기다려 뒤늦게 `DelegateOwner`로 바꾸던 이전 방식은 그사이 "혼자라 방이 삭제돼요" 문구가 멤버 2명 이상인 방에도 잘못 보이는 결함으로 실기기에서 확인됐다([failures/2026-08-30-leave-flow-member-count.md](../../../failures/2026-08-30-leave-flow-member-count.md), `research.md` D15의 "클라이언트는 방 멤버 수를 사전에 세지 않는다" 판단을 뒤집음).
- `onLeaveConfirm`의 `409 OWNER_TRANSFER_REQUIRED` 처리는 방어선으로 남긴다 — 판단 이후 경합(다른 멤버 탈퇴 등)이 생겨도 서버가 최종 판정한다.
- 위 표의 서버 계약은 [contracts/place-repository.md](./place-repository.md) "`RoomRepository` 확장" 절 참조.

## 재조회

- `room`은 `RoomRepository.getRoom(roomId)`(room-list가 이미 정의, [room-list/contracts/room-repository.md](../../room-list/contracts/room-repository.md))로 단건 조회한다 — 목록 화면이 아니라 이 spec이 "캐시 미스 등 필요할 때" 케이스에 해당한다.
- `places`는 `PlaceRepository.observePlaces(roomId)` `Flow` 구독으로 항상 최신 유지된다(다른 화면에서 장소가 추가·삭제돼도 재구독 없이 반영).
- `roomMembers`는 `OnInviteClick`·`OnLeaveClick`(N인 방장 경로)에서 `RoomRepository.getMembers(roomId)`로 조회한다 — 1회성 요청이라 화면 진입 시 미리 구독하지 않는다([contracts/place-repository.md](./place-repository.md) "`RoomRepository` 확장").
- `inviteCode`는 `OnInviteClick`에서 `RoomRepository.createInvitation(roomId)`로 발급한다(이미 발급했으면 서버가 같은 code를 재반환).

## Figma

[004 방 상세 annotation](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2400-270425&m=dev) · [헤더/장소 수 인디케이터](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2542-125341&m=dev) · [정렬 드롭다운](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2542-125333&m=dev) · [카테고리 칩](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=3510-125317&m=dev) · [다른 방에 공유](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=3225-91770&m=dev) · [친구 초대 참여자 목록](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2542-125613&m=dev) — 정렬 드롭다운·카테고리 칩·더보기 메뉴는 구현 단계에서 `MinoMenu`·`MinoChip`을 조립해 그린다(대조 절차는 `figma-design-fidelity.md`, 재사용 근거는 [research.md D13](../research.md)).
