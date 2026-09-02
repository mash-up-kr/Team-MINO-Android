# 계약: 장소 상세 화면 상태 (PlaceDetail Main Contract)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [plan.md](../plan.md)

편입 구조([research.md D17](../research.md)) 기준. 상태가 두 ViewModel에 나뉘므로 **§1이 그 경계를 먼저 정한다.**

---

## 1. 소유권 경계

| 값 | 소유자 | 왜 |
|---|---|---|
| `selectedPinId` | `RoomListViewModel` | 시트 세 갈래를 가르는 값. `selectedRoomId`와 한자리에 있어야 판정이 한 곳이다 |
| `selectedRoomId` | `RoomListViewModel` | 기존 |
| `returnsToHomeOnClose` | `RoomListViewModel` | [나가기] 목적지를 가르는 값(FR-009). 갈래를 실제로 실행하는 곳과 같은 자리에 둔다 |
| `mapPins`(선택 표시 포함) | `RoomListViewModel` | 지도를 그리는 주체 |
| `mapCenter`·`mapCenterRequestId` | `RoomListViewModel` | 카메라를 실제로 움직이는 주체([research.md D25](../research.md)) |
| 핀 상세·코멘트·시트 단계·헤더 밀도·공유 시트·[저장된 방] 시트 | `PlaceDetailViewModel` | `pinId` 하나의 화면 상태 |

**`PlaceDetailUiState.roomColor`를 없앤다.** 선택 핀의 색은 `RoomListViewModel`이 이미 `MapPinUiModel.color`로 들고 있다 — `Place.id`가 곧 `pinId`이므로(`Place` KDoc) 선택 여부는 `pin.place.id == selectedPinId` 한 비교다. 색이 늦게 도착해 마커를 못 그리던 구간([research.md D15](../research.md))이 구조적으로 사라진다.

---

## 2. `RoomListViewModel` 델타

### 2.1 상태

```kotlin
data class RoomListUiState(
    // ... 기존 그대로
    val selectedRoomId: String? = null,
    val selectedPinId: String? = null,        // 신규
    val returnsToHomeOnClose: Boolean = false, // 신규 — FR-009의 홈 예외
)
```

**`returnsToHomeOnClose`는 [나가기]가 갈릴지를 미리 굳혀 둔 값이다.** 진입 출처는 탭 전환이 끝나면 어디에도 남지 않으므로([place-detail-entry.md §3.1](./place-detail-entry.md)), 여는 순간 판정해 `Boolean` 하나로 들고 있는다. `PlaceDetailEntryOrigin`을 그대로 상태에 두지 않는 이유는 이 화면이 출처로 하는 일이 이 분기 하나뿐이라, 출처를 남겨 두면 읽는 쪽마다 조건을 다시 세우게 되기 때문이다.

**세 갈래는 `selectedPinId`가 우선이다.** `selectedPinId != null` → 장소 상세, 아니면 `selectedRoomId != null` → 방 상세, 아니면 리스트. 장소 상세가 열려 있을 때 방 상세 시트는 그려지지 않는다(FR-009가 [나가기] 후에야 그것을 드러내라고 규정한다).

`selectedPinId != null`이면 `selectedRoomId`도 반드시 `null`이 아니다 — §2.3이 둘을 함께 세운다.

### 2.2 인텐트

| 인텐트 | 처리 |
|---|---|
| `OnPlaceSelected(pinId)` | `selectedPinId = pinId`, `mapCenter`를 그 장소로 + `mapCenterRequestId++` |
| `OnClosePlaceDetailClick` | `returnsToHomeOnClose`면 `selectedPinId`·`selectedRoomId`를 함께 `null`로 두고 `NavigateToHome` 발행, 아니면 `selectedPinId = null`. 어느 쪽이든 `returnsToHomeOnClose = false`로 되돌린다 |
| `OnPlaceDetailRoomSwitched(pinId, roomId)` | `selectedPinId = pinId`, `selectedRoomId = roomId`, **`returnsToHomeOnClose = false`** — [저장된 방] 전환(FR-024·FR-025) |

`OnPlaceSelected`는 `returnsToHomeOnClose`를 **`false`로 세운다** — 탭 안 진입은 홈 예외의 대상이 아니고(FR-009), 홈에서 열어 둔 상세가 남아 있는 채로 마커를 눌러 다른 장소로 옮겨간 경우에 플래그가 따라붙는 것을 막는다.

`OnPlaceSelected`는 **두 곳**에서 온다 — 지도 마커와 방 상세의 `NavigateToPlaceDetail(pinId)`다. **§2.3의 탭 간 요청은 이 인텐트로 오지 않는다** — 방을 함께 세워야 해서 그쪽이 따로 받는다. 다만 **카메라 이동은 §2.3도 똑같이 한다**(spec FR-002는 진입점 넷 전부에 걸린다).

### 2.3 탭 간 요청 소비

`PlaceDetailRequestHolder.pending`을 구독한다([place-detail-entry.md §3](./place-detail-entry.md)).

```
pending = pinId
  → holder.consume()                                  ← 결과와 무관하게 먼저 비운다
  → getPlaceDetail(pinId)로 roomId·location을 해석
  → selectedRoomId = roomId; selectedPinId = pinId
       mapCenter = location; mapCenterRequestId++
       returnsToHomeOnClose = (origin == HOME)         ← 한 번에 세운다
```

**방을 함께 세운다.** 알림에서 들어오면 방 상세가 아직 안 열려 있어, [나가기]가 드러낼 자리가 비어 있다. `roomId`는 핀 상세 응답이 준다. 홈 진입은 §4의 갈래를 타 방 상세를 드러내지 않지만, **방은 똑같이 세운다** — 마커 양식(§2.4)과 코멘트 목록이 「지금 보고 있는 방」을 따르고(FR-027), 사용자가 [저장된 방]으로 방을 바꾸면 그 자리에서 기본 갈래로 넘어가기 때문이다.

**출처는 여는 순간에만 쓴다.** `origin == HOME`을 `returnsToHomeOnClose`로 굳혀 두고 요청 자체는 비운다 — 조회에 실패해 아무것도 열지 않았다면 이 플래그도 서지 않는다.

**카메라도 여기서 옮긴다.** spec FR-002의 카메라 이동은 진입점 넷 전부에 걸리므로 탭 간 진입도 예외가 아니다. 좌표는 **핀 상세 응답의 `location`**이 준다 — `OnPlaceSelected`처럼 `placesByRoomId`에서 찾지 않는다. 홈에서 콜드 진입하면 그 목록이 아직 비어 있어 좌표를 못 찾고, 못 찾으면 카메라가 그 자리에 머물러 선택 핀이 화면 밖에 남는다(확인된 현상). 셋을 한 번에 세우는 이유는 [§2.2의 `OnPlaceSelected`](#22-인텐트)와 같다 — 나눠 내보내면 장소는 골라졌는데 카메라는 옛 자리인 중간 상태가 한 프레임 드러난다.

이 조회가 실패하면 요청을 소비만 하고 아무것도 열지 않는다 — 열 화면이 없는 채로 빈 상세를 띄우지 않는다.

### 2.4 지도 마커 선택 표시

```kotlin
MapPinUiModel(place, color, selected = place.id == selectedPinId)
```

`RoomListMap.PlacePin`이 `selected = false`로 박아 둔 값을 이 값으로 바꾼다. `:core:common:ui`의 `RoomMapPin(color, selected)`이 이미 두 외형을 갖고 있어 새 컴포넌트가 없다(FR-002, TS-002).

### 2.5 바텀 네비게이션

`RoomListRoute`의 기존 `DisposableEffect` 판정식에 조건을 더한다 — 새 `DisposableEffect`를 만들지 않는다([research.md D19](../research.md)).

```kotlin
bottomNavVisibility.value =
    selectedPinId == null && !isDetailMode && !state.isNudgeSheetVisible
```

### 2.6 시스템 뒤로가기

```kotlin
BackHandler(enabled = selectedPinId != null || selectedRoomId != null) {
    if (selectedPinId != null) OnClosePlaceDetailClick else OnCloseRoomDetailClick
}
```

**여기는 그대로 둔다.** 홈 복귀 갈래(FR-009)는 `OnClosePlaceDetailClick`을 처리하는 ViewModel 안에서 갈리므로, [나가기] 버튼과 뒤로가기가 같은 인텐트로 모이는 이상 두 조작이 다른 자리로 갈 수 없다([place-detail-entry.md §4.3](./place-detail-entry.md)).

### 2.6.1 `RoomListSideEffect` 델타

```kotlin
sealed interface RoomListSideEffect : SideEffect {
    // ... 기존 그대로
    data object NavigateToHome : RoomListSideEffect   // 신규
}
```

`OnClosePlaceDetailClick`이 홈 복귀 갈래를 탈 때만 발행한다. `RoomListRoute`가 받아 `roomGraph`의 콜백으로 셸에 올리고, 탭을 옮기는 것은 셸이 한다 — `:feature:room`은 `MainTab`을 모른다([place-detail-entry.md §4.2](./place-detail-entry.md)).

**상태가 아니라 SideEffect인 이유**: 탭 전환은 한 번 일어나고 끝나는 사건이라, 상태로 두면 구성 변경 때 다시 소비돼 사용자가 저장 탭으로 돌아올 때마다 홈으로 튕긴다. 기존 `NavigateToRoomForm`과 같은 성격이다.

### 2.7 지도 컨트롤 노출

지도 위 컨트롤을 그릴지는 **지금 활성인 시트**의 단계로 판정한다. `RoomListScreen`이 이미 `detailSheetLevel`로 같은 판정을 하고 있고(그 KDoc이 실기기 결함을 기록해 둔 자리), 장소 상세가 세 번째 갈래로 는다 — 장소 상세 `Full`이면 컨트롤을 숨긴다.

### 2.8 자동 카메라 이동은 장소 상세가 열려 있는 동안 멈춘다

`OnScreenEntered`·`OnLocationPermissionResult`가 하는 `mapCenter`·`mapCenterRequestId` 갱신은 **`selectedPinId != null`이면 건너뛴다**(spec EC-030).

방 리스트 계약은 탭 진입 시점에 위치를 해석해 카메라를 현재 위치로 옮기라고 규정한다([room-list-main-contract.md 「분기 규칙 — 위치 권한 요청」](../../room-list/contracts/room-list-main-contract.md)). 탭 간 진입(§2.3)은 탭 전환과 장소 상세 열기가 같은 순간이라 그 이동과 §2.3의 이동이 겹치고, **나중에 끝난 쪽이 이긴다** — 위치 해석이 캐시로 즉시 끝나면 §2.3이 이기지만, 활성 측위로 넘어가면 그쪽이 늦게 도착해 선택 핀에 맞춘 카메라를 덮는다. 그래서 순서에 기대지 않고 한쪽을 끈다.

**사용자가 직접 누른 [현재 위치](`OnCurrentLocationClick`)는 막지 않는다.** 장소 상세 위의 그 버튼도 같은 인텐트로 오므로(§7·[research.md D25](../research.md)) 함께 막으면 버튼이 죽는다.

---

## 3. `PlaceDetailUiState`

기존 정의에서 **`roomColor` 삭제**, **`savedRooms`·`commentsObservedAt` 추가**, `isSavedRoomsVisible`의 근거 교체.

```kotlin
@Immutable
internal data class PlaceDetailUiState(
    val pinId: String,
    val place: PlaceDetail? = null,
    val loadError: MinoDomainException? = null,
    val sheetLevel: PlaceSheetLevel = PlaceSheetLevel.HALF,
    val headerMode: PlaceHeaderMode = PlaceHeaderMode.EXPANDED,
    val carouselPage: Int = 0,
    val comments: ImmutableList<PlaceCommentUiModel> = persistentListOf(),
    val commentsObservedAt: Instant = Instant.DISTANT_PAST,   // 신규 — §6.1
    val commentPage: Int = 0,
    val hasOlderComments: Boolean = false,
    val isLoadingOlderComments: Boolean = false,
    val commentDraft: String = "",
    val isSubmittingComment: Boolean = false,
    val savedRooms: ImmutableList<RoomPickerItem> = persistentListOf(),
    val shareSheet: ShareSheetUiState? = null,
    val savedRoomsSheet: SavedRoomsSheetUiState? = null,
) : UiState
```

**`commentsObservedAt`의 기본값은 판정에 쓰이지 않는다.** 초기 상태의 `comments`가 비어 있어 이 값과 견줄 코멘트가 없고, 목록이 처음 채워지는 순간 `clock.now()`로 덮인다(§6.1). `Instant.DISTANT_PAST`는 `RoomListViewModel`이 정렬 폴백으로 쓰는 것과 같은 관용이다.

### 3.1 `savedRooms` — 한 번 조회로 세 곳을 먹인다

`getRooms(placeId = place.placeId)` 한 번이 방마다 `hasPlace`·`matchedPinId`·`color`를 함께 준다. 그 결과가 세 군데에 쓰인다.

| 쓰임 | 근거 |
|---|---|
| [다른방에 공유] 시트의 이미 저장된 방 표시 | FR-018·FR-022 |
| [저장된 방] 버튼의 노출 판정 | FR-023 |
| [저장된 방] 시트의 목록과 전환 대상 `matchedPinId` | FR-024 |

**`place`가 도착한 뒤에 부른다** — 질의 키가 `placeId`인데 그 값이 핀 상세 응답에서 오기 때문이다.

### 3.2 파생 프로퍼티

```kotlin
val isSubmitEnabled: Boolean get() = commentDraft.isNotBlank() && !isSubmittingComment
val isSourceEnabled: Boolean get() = place?.sourceUrl != null

/** FR-023 — 이 장소가 두 방 이상에 저장돼 있을 때만 버튼이 그려진다(TS-040·TS-041). */
val isSavedRoomsVisible: Boolean
    get() = savedRooms.count { it.hasPlace == true } >= 2
```

**`false` 고정을 벗는다.** 서버가 `matchedPinId`를 내려주면서 전환 대상을 특정할 수 있게 됐다([research.md D20](../research.md)). **이름이 `isSavedRoomsEnabled`가 아니라 `isSavedRoomsVisible`인 것은 spec 5.0.0이 이 판정을 활성/비활성이 아니라 노출/미노출로 정했기 때문이다**(FR-023).

### 3.3 `SavedRoomsSheetUiState`

```kotlin
@Immutable
internal data class SavedRoomsSheetUiState(
    val rooms: ImmutableList<RoomPickerItem>,   // hasPlace == true 이고 지금 보고 있는 방을 뺀 나머지
    val currentPinId: String,                    // 지금 보고 있는 핀 — rooms에서 그 방을 빼는 기준
)
```

`null`이 닫힘이다. 공유 시트와 같은 규칙으로, 열림 플래그를 따로 두지 않아 목록 없이 열린 시트가 생기지 않는다.

**지금 보고 있는 방은 목록에 없다.** 선택 상태로 표시하는 것이 아니라 **빼는** 것이다 — FR-024가 "지금 보고 있는 방을 제외한 나머지"로 규정하고, TS-042(A방 기준으로 보면 시트에 B·C만 보이고 A방 카드는 없다)·EC-026·UX-012가 같은 것을 요구한다. 그래야 시트에 눌러도 아무 일이 없는 카드가 생기지 않는다.

**치수는 이 스펙이 소유한다** — [SYS-003] 방 선택 시트와 값이 다르므로 `RoomShareSheet`를 따라가지 않는다.

| 항목 | 값 | 근거 |
|---|---|---|
| 시트 높이 | **442dp 고정** (하단 safe area 60dp 포함) | FR-024·TS-048 |
| 내부 스크롤 영역 | **312dp 고정** — 방이 늘어도 시트 높이는 그대로고 목록만 스크롤된다 | FR-024·TS-048 |
| 체크박스·확정 CTA | **없다.** 카드를 누르는 것이 곧 확정이다 | FR-024 |
| 카드 구성 | 썸네일 · 방 이름 · 장소 N개 · 우측 이동 표시(>) | FR-024 |

---

### 3.4 `ShareSheetUiState`와 [SYS-003] 방 선택 시트

```kotlin
@Immutable
internal data class ShareSheetUiState(
    val rooms: ImmutableList<RoomPickerItem>,   // savedRooms 그대로 — 거르지 않는다
    val selectedRoomIds: ImmutableSet<String>,  // 이미 저장된 방은 들어오지 않는다
    val isSubmitting: Boolean = false,
) {
    val isShareEnabled: Boolean get() = selectedRoomIds.isNotEmpty() && !isSubmitting
}
```

`null`이 닫힘이다. 목록은 [§3.1](#31-savedrooms--한-번-조회로-세-곳을-먹인다)의 `savedRooms`를 그대로 쓰므로 시트를 여는 순간 조회가 다시 돌지 않는다.

#### 3.4.1 이미 저장된 방은 두 곳에서 막는다

`hasPlace == true`인 방은 **체크된 채 비활성**이다(spec FR-018·FR-022·EC-019·TS-034·TS-058). 그 규칙을 카드와 ViewModel **양쪽이** 지킨다.

| 자리 | 하는 일 |
|---|---|
| `RoomShareSheet` | 그 카드의 탭과 체크박스를 함께 잠근다(`enabled = !hasPlace`) |
| `PlaceDetailViewModel.toggleShareRoom` | 그 방의 토글 요청을 무시한다 |

**둘 중 하나로 줄이지 않는다.** 표시만 흐리고 입력을 열어 두면 이미 저장된 방이 `selectedRoomIds`에 들어가 `duplicatePin`에 실리고, 서버가 중복을 `409`로 거절해([place-api.md §3](./place-api.md)) 사용자에게는 이유를 알 수 없는 실패만 남는다. ViewModel 쪽 방어는 그 실패를 서버까지 보내지 않고 여기서 끊는다 — UI가 막는다는 전제를 서버 응답으로 확인하지 않는다.

#### 3.4.2 시각 표현

| 상태 | 표현 | 근거 |
|---|---|---|
| 미체크 | 테두리만 있는 빈 상자 | `MinoCheckbox` 기본 |
| 체크됨 | `Primary/Normal` 채움 + `Static/White` 체크 | 〃 |
| 체크됨 + 비활성 | **같은 모습에 체크박스만 43% 불투명도** | Figma `2862-175313`의 `Checkbox` 노드 `opacity 43%` |

**흐려지는 것은 체크박스뿐이다.** 같은 카드의 썸네일·방 이름·`장소 N개`는 온전한 밝기다(Figma 같은 노드 — 불투명도가 체크박스에만 걸려 있다).

비활성용 **색**을 새로 두지 않는다. Figma가 체크 상태와 같은 토큰을 쓰고 불투명도만 낮추므로, `MinoCheckboxColors`에 `enabled` 축의 슬롯이 늘지 않고 `MinoCheckbox`가 `enabled = false`에서 자신을 흐리게 그린다.

#### 3.4.3 치수

Figma가 시트를 세 프레임으로 그려 두었다(spec 유저 플로우 6 「Figma」).

| 단계 | 전체 높이 | 목록 영역 | Figma |
|---|---|---|---|
| `Peek` (진입 기본값) | 500dp | 240dp | `2392-128669` |
| `Full` — 방 4개 이하 | 676dp | 416dp | `2542-10516` |
| `Full` — 방 5개 이상 | 708dp | 448dp | `2392-128693` |

**세 값의 차이는 목록 영역 하나다.** 위아래 고정 영역은 어느 단계에서나 같다 — 헤더 146dp(손잡이 30 + 장소 행 60 + [새 방 만들기] 행 56) · 구분선 띠 12dp · 액션 영역 102dp, 합해서 **260dp**. 그래서 시트 높이는 `목록 영역 + 260dp` 하나로 나오고, 단계가 바뀌어도 [공유하기]가 화면 밖으로 밀리지 않는다.

**`Full` 높이가 방 개수로 갈리는 것은 스크롤 여지를 알리기 위해서다** — 방이 다섯 이상이면 32dp를 더 얹어 5번째 카드가 일부만 보이게 하고, 넷 이하면 카드가 딱 맞아 잘릴 것이 없다.

#### 3.4.4 단계 전환과 닫기

| 조작 | 결과 |
|---|---|
| 손잡이를 위로 끌기 | `Peek` → `Full` |
| 손잡이를 아래로 끌기 | `Full` → `Peek`, `Peek`에서는 **닫힘** |
| 딤 영역 탭 · 시스템 뒤로가기 | 닫힘 |

**끄는 것을 받는 자리는 손잡이 하나다.** 시트 본문 전체가 받으면 목록을 세로로 훑는 손짓과 단계를 바꾸는 손짓이 같은 자리에서 갈린다.

**닫는 판단은 시트가 하지 않는다.** `Peek`에서 아래로 끌렸다는 사실만 `onDismissRequest`로 올리고, 치우는 것은 상태를 든 쪽이다(spec EC-021).

#### 3.4.5 소유 위치

`:feature:room` 모듈 루트의 `component/`다. **방 상세와 장소 상세 두 화면이 같은 시트를 부르므로** 어느 한 화면의 `component/`에 둘 수 없고, 사용처가 한 feature 안에 머무르므로 `:core:common:ui`로도 올리지 않는다([feature-module.md 「모듈 루트 `component/`」](../../../architecture/feature-module.md), [component-asset-placement.md §1.2](../../../conventions/component-asset-placement.md)).

**시트는 두 화면의 도메인을 모른다.** 카드 한 장이 그리는 값을 `RoomShareItem`으로 받고, 무엇을 공유하는지(핀이냐 장소냐)도 어떤 API로 보내는지도 알지 않는다 — 두 진입점이 서로 다른 것을 보내기 때문이다(§3.4.6).

#### 3.4.6 두 진입점

| | 방 상세 | 장소 상세 |
|---|---|---|
| 여는 곳 | 장소 카드 [⋮] → [다른 방에 공유] | 액션 행 [다른방에 공유] |
| 목록 출처 | `GetRoomPickerRoomsUseCase(place.placeId)` | 〃 (`place.placeId`) |
| 보내는 것 | `PlaceRepository.duplicatePin(place.id, roomIds)` | `duplicatePin(pinId, roomIds)` |

**목록 출처가 같아졌다.** 방 상세는 `RoomRepository.observeMyRooms()`로 `Room` 목록을 받아 「이미 저장된 방」을 언제나 빈 집합으로 두고 있었다 — 그 자리의 KDoc이 근거로 든 「도메인에 판정할 필드가 없다」는 `getRooms(placeId)`가 `hasPlace`를 내려주면서 사실이 아니게 됐다(room-detail spec EC-004가 요구하던 것이다).

**보내는 곳도 하나가 됐다.** `RoomPlacesRepository.sharePlaces`는 `PlaceRepository.duplicatePin`과 같은 엔드포인트(`POST /pins/{pinId}/duplicate`)를 가리키는 중복이었고, 그 인터페이스의 KDoc이 「room-detail이 `PlaceRepository` 쪽으로 갈아타면 지워질 수 있다」고 예고해 둔 것을 이 개정이 실행했다.

#### 3.4.7 완료 토스트

| 항목 | 값 | 근거 |
|---|---|---|
| 문구 | `공유가 완료됐습니다.` | Figma `2542-125820` 실측 |
| 노출 시간 | **3초** | Figma 주석 3번(섹션 `3225-88512`) |
| 위치·모양 | `MinoSnackbar` 기본(체크 아이콘) | 〃 |

**시간을 세는 주체는 Route다.** `SnackbarDuration.Short`가 4초라 그 값을 쓰지 못하고, 3초 뒤 스스로 거두는 방식으로 표현한다 — 방 상세의 `ShowShareCompleteToast`가 이미 같은 패턴이다.

**문구는 진입점에 따라 갈리지 않는다.** 방 상세와 장소 상세가 같은 시트를 부르므로 같은 문자열 리소스를 쓴다.

---

## 4. `PlaceDetailIntent` 델타

기존 인텐트는 그대로 두고 셋을 더한다.

| 인텐트 | 근거 |
|---|---|
| `OnSavedRoomsClick` | FR-023 — [저장된 방] 시트를 연다. 버튼이 없는 장소에서는 도달하지 않는다 |
| `OnSavedRoomSelected(pinId, roomId)` | FR-024 — 전환 대상 핀. `matchedPinId`가 실린다 |
| `OnSavedRoomsSheetDismiss` | 딤 바깥 탭·아래로 끌기·뒤로가기 |
| `OnShareCreateRoomClick` | FR-022 · EC-020 — 공유 시트의 [새 방 만들기]. 전환 결정만 올리고 실제 호출은 Route가 한다 |
| `OnShareRoomFormResult(createdRoomId)` | EC-020 — 새 방을 만들고 돌아왔다. `null`이면 만들지 않고 나온 것이라 아무 일도 하지 않는다 |

**기존 KDoc의 "[저장된 방] 버튼의 Intent가 없다"를 지운다.** 그 문장의 근거였던 구현 보류가 해제됐다.

**돌아온 자리에서 방 목록을 다시 조회한다.** 방 생성 화면이 돌려주는 것은 방 id 하나뿐이라(`RoomFormLauncher` 결과 계약) 이름·썸네일·장소 개수를 알 방법이 없고, 그 값 없이 카드를 세우면 빈 줄이 보인다. 시트는 닫지 않으므로 사용자가 보는 것은 목록이 한 번 갱신되는 것뿐이다.

**선택은 지키고 새 방을 더한다.** 만들러 가기 전에 고른 방은 그대로 두고 새 방을 **선택된 상태로** 얹는다 — EC-020이 「이 새 방이 유일한 선택지가 되어 [공유하기]가 활성으로 바뀐다」로 규정한 결과가 그것이며, 돌아와서 한 번 더 눌러야 한다면 그 규정이 성립하지 않는다.

**조회가 실패하면 목록을 갈아 끼우지 않는다.** 알림만 남기고 만들러 가기 전의 목록을 그대로 둔다 — 새 방만 안 보일 뿐 고르던 것을 잃지 않는다.

---

## 5. `PlaceDetailSideEffect` 델타

| 이펙트 | 상태 | 비고 |
|---|---|---|
| `Exit` | 유지 | 받는 쪽이 Activity `finish()`가 아니라 `RoomListIntent.OnClosePlaceDetailClick`이 된다 |
| `OpenExternalMap`·`OpenSourceLink` | 유지 | 실행 주체가 `PlaceDetailActivity`에서 `MainActivity`로 바뀐다 |
| `ShowShareCompleted` | 유지 | |
| `SwitchRoom(pinId, roomId)` | **신규** | FR-024 — `RoomListViewModel`로 올려 `selectedPinId`·`selectedRoomId`를 함께 갱신한다 |
| `OpenCreateRoomForm` | **신규** | FR-022 · EC-020 — 공동방 생성 화면을 연다. 방 상세의 `NavigateToCreateRoomForm`과 같은 이유로 SideEffect다(Activity 전환은 Route가 한다) |

`SwitchRoom`이 SideEffect인 이유: 바꿔야 할 상태가 **다른 ViewModel의 것**이라 `PlaceDetailViewModel`이 직접 쓸 수 없다. `RoomDetailSideEffect.NavigateBack`이 같은 이유로 SideEffect인 것과 같다.

---

## 6. 코멘트 작성 시각 (FR-028)

`PlaceCommentUiModel`이 표기 문자열이 아니라 **시각 원본**을 들고, 표기는 컴포지션 시점에 만든다.

```kotlin
@Immutable
internal data class PlaceCommentUiModel(
    val id: String,
    val content: String,
    val author: ...,
    val canDelete: Boolean,
    val createdAt: Instant,   // 신규 — kotlin.time.Instant. 표기 계산의 원천
)
```

표기 문자열은 상태에 담지 않는다. 컴포지션 시점에 순수 함수로 환산한다.

| 경과 | 표기 |
|---|---|
| 1시간 미만 (음수 포함) | `방금` |
| 1시간 ~ 24시간 | `N시간 전` |
| 24시간 ~ 7일 | `N일 전` |
| 7일 이상 | `NNNN년 NN월 NN일` |

- **음수를 `방금`으로 흡수한다**(EC-029) — 기기 시각이 서버보다 앞설 때 `-1시간 전`이 새어 나가지 않게 하는 하한이다.
- **실시간 갱신하지 않는다**(EC-028) — 목록을 다시 그릴 때 갱신된다. 그래서 상태가 아니라 순수 함수다.
- 문자열 리소스는 feature가 소유한다([research.md D22](../research.md)).

### 6.1 기준 시각은 주입한 `Clock`에서 와서 상태에 실린다

경과를 재려면 `createdAt` 말고 **「지금」**이 필요하다. 그 값을 컴포저블이 직접 읽지 않고 ViewModel이 상태로 올린다([research.md D26](../research.md)).

```kotlin
@HiltViewModel
internal class PlaceDetailViewModel @AssistedInject constructor(
    @Assisted private val pinId: String,
    private val clock: Clock,          // 신규 — kotlin.time.Clock
    …
)
```

```kotlin
internal data class PlaceDetailUiState(
    …
    val comments: ImmutableList<PlaceCommentUiModel>,
    val commentsObservedAt: Instant,   // 신규 — 이 목록을 판정한 기준 시각
)
```

- **판정 함수의 입력은 둘이다** — `(createdAt, commentsObservedAt)`. 컴포저블 안에서 `Clock.System.now()`를 부르지 않는다. 부르면 값이 컴포지션마다 달라져 EC-028이 지켜지는지 확인할 수단이 없다.
- **`commentsObservedAt`은 코멘트 목록 상태를 다시 만들 때마다 갱신한다** — 최초 조회, 이전 페이지 추가 로드, 등록·삭제 후 반영. 등록 직후 `방금`이 뜨는 것(TS-054)이 이 갱신으로 성립하고, 목록을 둔 채 시간만 흐르는 동안에는 갱신되지 않아 EC-028이 성립한다.
- **바인딩은 `:feature:room`의 `di/`가 `ViewModelComponent`에 설치한다.** 요구하는 곳이 이 모듈의 ViewModel 하나뿐이라 앱 전역 그래프에 올리지 않는다 — `ShareReceiverResourcesModule`이 같은 판단을 KDoc에 적어 둔 선례다.
- **`@OptIn(ExperimentalTime::class)`이 필요하다.** Kotlin 2.2.10에서 `kotlin.time.Instant`·`kotlin.time.Clock`이 모두 실험적이고 전역 opt-in 설정이 없다 — 이 타입에 닿는 파일마다 붙인다.

---

## 7. 화면 구성

`PlaceDetailScreen`이 `BoxScope` 확장이 된다 — `RoomDetailScreen`과 같은 형태다.

```kotlin
@Composable
internal fun BoxScope.PlaceDetailScreen(
    state: PlaceDetailUiState,
    commentState: TextFieldState,
    onIntent: (PlaceDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
)
```

**지도를 그리지 않는다.** 호출부(`RoomListScreen`)가 이미 그린 `RoomListMap` 위에 컨트롤·시트·오버레이만 얹는다. `PlaceDetailMap`은 삭제된다([research.md D25](../research.md)).

**`Full`의 윗변 계산은 그대로다.** 시트가 자기가 놓인 자리의 높이를 `Full` 높이로 쓰고 상태바만큼 빼는 규칙은 편입과 무관하게 유지된다. 다만 그 값을 넘겨주던 `PlaceDetailShell`이 사라지므로, 인셋 처리는 `RoomListScreen`이 이미 하는 `mapBleed` 계산과 한 자리에서 만난다.

---

## 8. 삭제되는 것

| 대상 | 사유 |
|---|---|
| `PlaceDetailUiState.roomColor` | §1 — `MapPinUiModel.color`가 이미 든다 |
| `isSavedRoomsEnabled`(이름 포함)의 `false` 고정 | §3.2 — 보류 해제, `isSavedRoomsVisible`로 대체 |
| `PlaceDetail.label` 관련 헤더 표현 | FR-005 재정의([research.md D21](../research.md)) |
| `PlaceDetailMap`·`CurrentLocationButton` | 지도·컨트롤 단일화 |
