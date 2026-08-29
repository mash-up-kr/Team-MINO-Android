# 계약: 장소 상세 화면 (PlaceDetailMain)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [../plan.md](../plan.md)

`:feature:placedetail`의 유일한 화면. MVI 타입은 `:core:common:android`의 `architecture` 패키지를 따르고, Route↔Screen 연결은 [feature-module.md 4장](../../../architecture/feature-module.md)을 따른다.

시그니처는 이 문서가 소유한다. 함수 본문은 구현 단계의 몫이다.

---

## 1. Route

```kotlin
// PlaceDetailDestinations.kt
@Serializable
internal data class PlaceDetailMain(val pinId: String) : Route
```

`PlaceDetailActivity`가 `intent.getStringExtra(EXTRA_PLACE_DETAIL_PIN_ID)`로 읽어 시작 Route에 넘긴다([place-detail-launcher.md](./place-detail-launcher.md)). ViewModel은 `savedStateHandle.toRoute<PlaceDetailMain>()`으로 복원한다.

## 2. UiState

```kotlin
internal data class PlaceDetailUiState(
    val pinId: String,
    val place: PlaceDetail?,          // null = 로딩 중
    val roomColor: RoomColor?,        // 핀 상세엔 없어 방 목록에서 채운다 (§5)
    val sheetLevel: PlaceSheetLevel,  // HALF | FULL
    val headerMode: PlaceHeaderMode,  // EXPANDED | COLLAPSED
    val carouselPage: Int,
    val comments: List<PlaceCommentUiModel>,
    val commentPage: Int,
    val hasOlderComments: Boolean,
    val isLoadingOlderComments: Boolean,
    val commentDraft: String,
    val isSubmittingComment: Boolean,
    val shareSheet: ShareSheetUiState?,   // null = 닫힘
) {
    val isSubmitEnabled: Boolean get() = commentDraft.isNotBlank() && !isSubmittingComment
    val isSourceEnabled: Boolean get() = place?.sourceUrl != null
    val isSavedRoomsEnabled: Boolean get() = false   // FR-023 — 이번 범위 보류 (§6)
}
```

| 필드 | 근거 |
|---|---|
| `place == null` | 진입 직후 로딩. 헤더·캐러셀·액션 행이 아직 그려지지 않는 구간 |
| `sheetLevel` | FR-001. `Peek`이 없다 |
| `headerMode` | FR-008. `sheetLevel`에서 파생시키지 않는다 — 스크롤 위치가 결정한다([research.md D5](../research.md)) |
| `carouselPage` | FR-007. 외부 앱 복귀 시 유지되어야 한다(UX-009) |
| `commentDraft` | FR-012. 카운터 `N/200`은 이 값의 길이로 그린다 |
| `isSubmitEnabled` | FR-013·EC-012 — 공백만 있으면 비활성 |
| `isSourceEnabled` | FR-017·EC-017 — 원문 링크가 없으면 [원문보기] 비활성 |
| `isSavedRoomsEnabled` | FR-023. **`false` 고정**이며 그 이유는 §6 |

**200자 상한을 UiState가 강제하지 않는다.** 입력 컴포저블이 `onValueChange`에서 201자째를 받지 않는 것으로 막는다(EC-011 — 카운터를 `200/200`으로 고정).

### 2.1 `PlaceCommentUiModel`

```kotlin
internal data class PlaceCommentUiModel(
    val id: String,
    val content: String,
    val nickname: String,
    val avatarColor: RoomColor?,
    val canDelete: Boolean,
)
```

도메인 `PlaceComment`를 그대로 쓰지 않는 이유는 없다 — 필드가 1:1이다. 그럼에도 UiModel을 두는 것은 **아바타 표현이 서버 협의 중**이라([place-api.md §5](./place-api.md)) 정본이 정해지면 이 경계에서만 바뀌게 하기 위해서다.

### 2.2 `ShareSheetUiState`

```kotlin
internal data class ShareSheetUiState(
    val rooms: List<RoomPickerItem>,   // hasPlace == true 는 체크·비활성
    val selectedRoomIds: Set<String>,
    val isSubmitting: Boolean,
) {
    val isShareEnabled: Boolean get() = selectedRoomIds.isNotEmpty() && !isSubmitting
}
```

시트의 시각 표현·높이(676dp)·카드 구성은 [spec.md §3.2](../spec.md)가 [SYS-003] 소관으로 위임했으므로 **`[TBD]`다**([research.md D13](../research.md)). 이 계약이 확정하는 것은 선택 상태와 CTA 활성 조건까지다.

## 3. Intent

```kotlin
internal sealed interface PlaceDetailIntent {
    data class OnSheetLevelChange(val level: PlaceSheetLevel) : PlaceDetailIntent
    data class OnScrollOffsetChange(val isAtTop: Boolean) : PlaceDetailIntent   // FR-008
    data object OnExitClick : PlaceDetailIntent                                  // FR-009
    data class OnCarouselPageChange(val page: Int) : PlaceDetailIntent           // FR-007

    data object OnOpenMapClick : PlaceDetailIntent                               // FR-016
    data object OnOpenSourceClick : PlaceDetailIntent                            // FR-017

    data class OnCommentDraftChange(val value: String) : PlaceDetailIntent       // FR-012
    data object OnSubmitCommentClick : PlaceDetailIntent                         // FR-014
    data class OnDeleteCommentClick(val commentId: String) : PlaceDetailIntent   // FR-015
    data object OnLoadOlderComments : PlaceDetailIntent                          // D11 역방향 페이징

    data object OnShareClick : PlaceDetailIntent                                 // FR-018
    data class OnShareRoomToggle(val roomId: String) : PlaceDetailIntent
    data object OnShareConfirmClick : PlaceDetailIntent
    data object OnShareSheetDismiss : PlaceDetailIntent
}
```

**`OnSavedRoomsClick`을 두지 않는다.** [저장된 방] 버튼이 항상 비활성이라 눌리는 일이 없다(§6).

**시트를 아래로 드래그해 닫는 것(EC-003)은 `OnExitClick`과 같은 Intent로 흘린다.** [나가기]와 동일 처리라는 것이 spec의 규정이므로 분기를 만들지 않는다.

## 4. SideEffect

```kotlin
internal sealed interface PlaceDetailSideEffect {
    data object Exit : PlaceDetailSideEffect                                   // FR-009 — Activity finish()
    data class OpenExternalMap(val mapUrl: String?, val query: String) : PlaceDetailSideEffect
    data class OpenSourceLink(val url: String) : PlaceDetailSideEffect
    data object ShowShareCompleted : PlaceDetailSideEffect                     // FR-018 토스트
}
```

| SideEffect | 소비처 | 비고 |
|---|---|---|
| `Exit` | `PlaceDetailActivity` | `finish()`만 한다. 방 상세 목적지 배선은 `[TBD]`([research.md D2](../research.md)) |
| `OpenExternalMap` | `PlaceDetailActivity` | 외부 지도 앱 → 없으면 브라우저(FR-016·TS-029). 앱 선택 정책은 [spec.md §3.2](../spec.md)가 비목표로 뒀으므로 `[TBD]`이며 `/mino-task`가 정한다 |
| `OpenSourceLink` | `PlaceDetailActivity` | FR-017 |
| `ShowShareCompleted` | 화면 | `공유가 완료되었습니다.` — `LocalSnackbarHostState` 사용 |

**에러 SideEffect를 두지 않는다.** `MinoDomainException`은 `CollectDomainError`가 공통 스낵바로 처리한다([error_handling.md](../../../conventions/error_handling.md), [research.md D14](../research.md)).

## 5. 진입 시 로딩 순서

```text
1. recordAccess(pinId)        — 결과를 기다리지 않고 던진다. 실패해도 화면에 영향 없음 (FR-026)
2. getPlaceDetail(pinId)      — place 채움
3. getComments(pinId, 0)      — 코멘트 첫 페이지
4. GetRoomPickerRoomsUseCase()  — roomColor 확보 + 공유 시트 목록 (§5.1)
```

2·3·4는 병렬로 띄운다. 1은 다른 셋의 성패와 무관하다.

### 5.1 `roomColor`를 방 목록에서 찾는 이유

핀 상세 응답에 방의 대표 색상이 없다([place-api.md §1](./place-api.md)). 마커 색상(FR-002)을 그리려면 방 목록에서 `id == place.roomId`인 방의 `color`를 찾아야 한다. 이 조회는 [다른방에 공유] 시트를 위해 어차피 필요하므로 요청이 늘지 않는다.

**UI 라운드에서는 인자 없는 `getRooms()`를 쓴다** — `GetRoomPickerRoomsUseCase`가 그것을 호출한다. 이미 구현돼 동작하는 경로라 `:core:data` 변경이 없고, `RoomSummary.color`가 `roomColor`를, 목록 자체가 공유 시트를 채운다. Phase 10에서 `getRooms(placeId)`로 넓혀 `hasPlace`를 받기 시작하면 이 한 자리만 바뀐다([research.md D15](../research.md)).

**마커는 핀 상세와 방 목록이 모두 도착한 뒤에 그린다.** `roomColor`가 아직 `null`인 동안에는 마커를 띄우지 않는다 — spec에 근거가 없는 기본색을 만들어 쓰지 않기 위해서다. 두 조회가 병렬이고 그동안 시트도 로딩 상태이므로 체감 지연이 생기지 않는다. 방 목록 조회가 실패하면 마커 없이 시트만 그리고, 오류는 공통 경로로 흘린다.

## 6. 이번 범위에서 빠지는 것

[research.md D10](../research.md)에 따라 **유저 플로우 7(저장된 방 전환)을 구현하지 않는다.**

| spec 항목 | 이번 처리 |
|---|---|
| FR-023 [저장된 방] 버튼 | 화면에 두되 `isSavedRoomsEnabled = false`로 **항상 비활성**. 단일 방 장소에서 비활성이라는 규칙(FR-023)은 지키고, 중복 저장 장소에서도 비활성인 것이 spec과 어긋난다 |
| FR-024 「저장된 방 시트」 | 만들지 않는다 |
| FR-025 방 전환 갱신 | 없다 |
| FR-026 방 전환 시 재기록 | 진입 시 1회만 기록한다 |
| TS-042~TS-049 · EC-024~EC-027 | 이번 구현의 검증 대상이 아니다 |

서버가 `pinId`를 내려주면([place-api.md §5](./place-api.md) 협의 1번) `Intent`에 `OnSavedRoomsClick`·`OnSavedRoomSelect(pinId)`를 더하고, 선택 시 **같은 Route를 새 `pinId`로 다시 여는 것**으로 FR-025의 전면 초기화가 자연히 성립한다([research.md D4](../research.md)).

## 7. Route ↔ Screen

```kotlin
@Composable
internal fun PlaceDetailRoute(
    onExit: () -> Unit,
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
    viewModel: PlaceDetailViewModel = hiltViewModel(),
)

@Composable
internal fun PlaceDetailScreen(
    state: PlaceDetailUiState,
    onIntent: (PlaceDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
)
```

`PlaceDetailScreen`은 stateless다 — 외부 전환은 Route가 콜백으로 올려보내고 Activity가 실행한다([feature-navigation.md 1장](../../../architecture/feature-navigation.md)). 콜백을 수동으로 `remember`하지 않는다([ADR](../../../adr/2026-08-01-compose-lambda-memoization.md)).
