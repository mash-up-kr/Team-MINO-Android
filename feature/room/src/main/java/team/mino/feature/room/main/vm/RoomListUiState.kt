package team.mino.feature.room.main.vm

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.common.android.architecture.UiState
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomListSortOption
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.model.MapPinUiModel

/**
 * 방 리스트 탭의 유일한 Route(RoomListMain) 상태.
 *
 * 계약: docs/specs/room-list/contracts/room-list-main-contract.md
 */
data class RoomListUiState(
    val sheetLevel: BottomSheetLevel = BottomSheetLevel.HALF,
    val personalRoom: Room? = null,
    /**
     * 내가 속한 모든 방(개인 방 + 공동방)에 저장된 장소 전체 — 지도에 얹을 핀과 그 색을 함께 든다.
     * 방 목록이 바뀌거나 방마다의 장소 조회가 끝날 때마다 [RoomListViewModel]이 다시 계산한다.
     */
    val mapPins: ImmutableList<MapPinUiModel> = persistentListOf(),
    val groupRooms: ImmutableList<Room> = persistentListOf(),
    val roomListSort: RoomListSortOption = RoomListSortOption.ALL,
    val mapMarkerSort: MapMarkerSortOption = MapMarkerSortOption.ALL,
    val categoryFilter: PlaceCategoryFilter = PlaceCategoryFilter.ALL,
    val showNudge: Boolean = false,
    val showGhostCard: Boolean = false,
    /**
     * [FR-008][SYS-009] 자동 팝업 Nudge 바텀시트([RoomNudgeAutoSheet])의 닫힘 여부 — [showNudge]와
     * 별개다. `showNudge`는 "공동방 0개"만 뜻하고([RoomNudgeSheet]의 `Full` 인라인 카드가 계속
     * 참조한다), 이 값은 그중에서도 팝업이 지금 억제 중인지를 뜻한다. 탭에 재진입할 때마다
     * `RoomListViewModel.isNudgeSuppressionActive`가 `RoomPreferencesRepository`에 저장된 마지막
     * 닫힘 시각을 조회해 다시 계산한다 — [나중에 만들래요] 클릭 후 2주 동안은 `true`, 그 전엔 `false`다
     * (PRD 11.1.0). 세션 로컬 상태가 아니라 기기에 영속 저장된다.
     */
    val nudgeSheetDismissed: Boolean = false,
    val mapCenter: GeoPoint? = null,
    /**
     * `mapCenter`가 갱신될 때마다 1씩 증가하는 값. 값이 아니라 "이동 요청이 있었다는 사실" 자체를
     * 신호로 써야 해서 둔다 — `GeoPoint`는 데이터 클래스라 좌표가 이전과 같으면(예: 사용자가 지도를
     * 수동으로 옮긴 뒤 같은 위치로 되돌리는 현재 위치 버튼을 다시 누른 경우) `mapCenter` 값 자체는
     * 안 바뀌어서, 이 값이 없으면 `LaunchedEffect(mapCenter)`가 재실행되지 않아 카메라가 움직이지
     * 않는다.
     */
    val mapCenterRequestId: Int = 0,
    /**
     * 방 상세로 "전환"된 방 id. `null`이면 리스트, 값이 있으면 그 방의 상세를 같은 목적지 안에서
     * 그린다(`RoomNavigation.kt` KDoc 참고) — Navigation 목적지 전환이 아니라 이 로컬 상태로
     * 표현해야 지도(`RoomListMap`)가 계속 같은 컴포지션에 남아 카메라가 리셋되지 않는다.
     */
    val selectedRoomId: String? = null,
    /**
     * 장소 상세가 열려 있는 핀 id(`Place.id`가 곧 핀 id다) — `null`이면 안 열려 있다.
     *
     * **시트 세 갈래에서 이 값이 [selectedRoomId]보다 우선한다**: 값이 있으면 장소 상세, 없고
     * `selectedRoomId`가 있으면 방 상세, 둘 다 없으면 리스트다(FR-009가 [나가기] 후에야 방 상세를
     * 드러내라고 규정하므로 장소 상세가 열린 동안 방 상세 시트는 그려지지 않는다).
     *
     * 이 값이 `null`이 아니면 [selectedRoomId]도 `null`이 아니다 — 장소 상세를 여는 쪽이 둘을 함께
     * 세운다. 그래야 [나가기]가 `selectedPinId = null` 한 줄로 끝나고, 드러날 방 상세가 이미 그
     * 자리에 있다(`docs/specs/place-detail/contracts/place-detail-entry.md` §4).
     */
    val selectedPinId: String? = null,
    /**
     * [FR-009] 장소 상세 [나가기]가 방 상세가 아니라 **홈 탭으로** 되돌려야 하는지.
     *
     * [SCR-003] 홈 카드로 열었고 그 뒤 [저장된 방](FR-025)으로 방을 바꾼 적이 **없을** 때만 `true`다.
     * 방을 바꾸면 그 자리에서 내려가고(TS-057), 원래 방으로 되돌려도 다시 올라오지 않는다(EC-032) —
     * 판정하는 것은 "지금 어느 방을 보고 있는가"가 아니라 **"방을 바꾼 적이 있는가"**다.
     *
     * **진입 출처를 그대로 들고 있지 않고 여는 순간 `Boolean`으로 굳힌다.** 이 화면이 출처로 하는 일이
     * 이 분기 하나뿐이라, 출처를 남겨 두면 읽는 쪽마다 같은 조건을 다시 세우게 된다. 출처 자체는 탭
     * 전환이 끝나면 어디에도 남지 않으므로 요청이 실어 온다
     * (`docs/specs/place-detail/contracts/place-detail-entry.md` §3.1).
     */
    val returnsToHomeOnClose: Boolean = false,
) : UiState {
    /**
     * [FR-008] 자동 팝업 [RoomNudgeAutoSheet]를 실제로 그려야 하는지 — [RoomListScreen]과
     * [RoomListRoute] 둘 다 이 값을 봐야 한다(Route는 바텀 네비게이션을 숨기려고, Screen은 시트
     * 자체를 그리려고). 한쪽만 고치고 다른 쪽을 안 고치는 사고를 막으려고 한 곳에서만 계산한다.
     * `Full`에서는 인라인 넛지 카드([RoomNudgeSheet])만 있고 이 팝업 자체가 없으므로 제외한다.
     *
     * **`selectedRoomId != null`(방 상세를 보는 중)이면 항상 `false`다.** 호출부가 각자 `isDetailMode`를
     * 따로 AND하는 방식에 기대지 않는다 — 공동방 생성 직후 `OnRoomFormResult`가 `selectedRoomId`를
     * 동기적으로 채우는 시점과 `ON_RESUME`의 `OnScreenEntered`가 비동기로 `groupRooms`를 새로고침하는
     * 시점 사이에 경합이 있어, `groupRooms`가 아직 새 방을 반영하기 전(`showNudge`가 stale하게
     * `true`)이면서 `nudgeSheetDismissed`가 막 `false`로 리셋된 찰나에 이 값이 잘못 `true`로 튈 수
     * 있었다(실기기 확인 — 그 스파이크가 `RoomListRoute`의 바텀 네비게이션 토글을 오발화시켜 그 아래
     * `GoogleMap`이 하얗게 남는 문제로 이어짐). 이 조건 자체에 `selectedRoomId == null`을 넣어야
     * 호출부의 타이밍과 무관하게 항상 안전하다.
     */
    val isNudgeSheetVisible: Boolean
        get() = selectedRoomId == null && showNudge && !nudgeSheetDismissed && sheetLevel != BottomSheetLevel.FULL

    /**
     * 셸의 바텀 네비게이션을 숨겨야 하는지 — [RoomListRoute]가 이 값 하나만 보고 판정한다(FR-003,
     * TS-005 — #290 QA로 "이 화면 자신의 시트가 Full일 때"가 빠져 있던 걸 발견). 방 상세 진입
     * (`selectedRoomId != null`)·자동 팝업 딤(`isNudgeSheetVisible`)·이 화면 자신의 3단 시트가 `Full`로
     * 확장된 상태, 셋 다 화면 전체를 덮어 바텀 네비게이션이 보이면 안 되는 "몰입 모드"라는 같은 이유를
     * 공유한다 — 호출부가 매번 세 조건을 나열하지 않도록 한 곳에서만 계산한다.
     */
    val shouldHideBottomNav: Boolean
        get() = selectedRoomId != null || isNudgeSheetVisible || sheetLevel == BottomSheetLevel.FULL
}
