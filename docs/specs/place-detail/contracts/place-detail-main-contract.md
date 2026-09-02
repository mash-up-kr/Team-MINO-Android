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
    val selectedPinId: String? = null,   // 신규
)
```

**세 갈래는 `selectedPinId`가 우선이다.** `selectedPinId != null` → 장소 상세, 아니면 `selectedRoomId != null` → 방 상세, 아니면 리스트. 장소 상세가 열려 있을 때 방 상세 시트는 그려지지 않는다(FR-009가 [나가기] 후에야 그것을 드러내라고 규정한다).

`selectedPinId != null`이면 `selectedRoomId`도 반드시 `null`이 아니다 — §2.3이 둘을 함께 세운다.

### 2.2 인텐트

| 인텐트 | 처리 |
|---|---|
| `OnPlaceSelected(pinId)` | `selectedPinId = pinId`, `mapCenter`를 그 장소로 + `mapCenterRequestId++` |
| `OnClosePlaceDetailClick` | `selectedPinId = null` |
| `OnPlaceDetailRoomSwitched(pinId, roomId)` | `selectedPinId = pinId`, `selectedRoomId = roomId` — [저장된 방] 전환(FR-024) |

`OnPlaceSelected`는 세 곳에서 온다 — 지도 마커, 방 상세의 `NavigateToPlaceDetail(pinId)`, 그리고 §2.3의 탭 간 요청.

### 2.3 탭 간 요청 소비

`PlaceDetailRequestHolder.pending`을 구독한다([place-detail-entry.md §3](./place-detail-entry.md)).

```
pending = pinId
  → getPlaceDetail(pinId)로 roomId를 해석
  → selectedRoomId = roomId; selectedPinId = pinId
  → holder.consume()
```

**방을 먼저 세운다.** 홈·알림에서 들어오면 방 상세가 아직 안 열려 있어, [나가기]가 드러낼 자리가 비어 있다. `roomId`는 핀 상세 응답이 준다.

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

### 2.7 지도 컨트롤 노출

지도 위 컨트롤을 그릴지는 **지금 활성인 시트**의 단계로 판정한다. `RoomListScreen`이 이미 `detailSheetLevel`로 같은 판정을 하고 있고(그 KDoc이 실기기 결함을 기록해 둔 자리), 장소 상세가 세 번째 갈래로 는다 — 장소 상세 `Full`이면 컨트롤을 숨긴다.

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

## 4. `PlaceDetailIntent` 델타

기존 인텐트는 그대로 두고 셋을 더한다.

| 인텐트 | 근거 |
|---|---|
| `OnSavedRoomsClick` | FR-023 — [저장된 방] 시트를 연다. 버튼이 없는 장소에서는 도달하지 않는다 |
| `OnSavedRoomSelected(pinId, roomId)` | FR-024 — 전환 대상 핀. `matchedPinId`가 실린다 |
| `OnSavedRoomsSheetDismiss` | 딤 바깥 탭·아래로 끌기·뒤로가기 |

**기존 KDoc의 "[저장된 방] 버튼의 Intent가 없다"를 지운다.** 그 문장의 근거였던 구현 보류가 해제됐다.

---

## 5. `PlaceDetailSideEffect` 델타

| 이펙트 | 상태 | 비고 |
|---|---|---|
| `Exit` | 유지 | 받는 쪽이 Activity `finish()`가 아니라 `RoomListIntent.OnClosePlaceDetailClick`이 된다 |
| `OpenExternalMap`·`OpenSourceLink` | 유지 | 실행 주체가 `PlaceDetailActivity`에서 `MainActivity`로 바뀐다 |
| `ShowShareCompleted` | 유지 | |
| `SwitchRoom(pinId, roomId)` | **신규** | FR-024 — `RoomListViewModel`로 올려 `selectedPinId`·`selectedRoomId`를 함께 갱신한다 |

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
