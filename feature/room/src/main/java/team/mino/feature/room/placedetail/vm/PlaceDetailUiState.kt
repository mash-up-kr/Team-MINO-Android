package team.mino.feature.room.placedetail.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import team.mino.core.common.android.architecture.UiState
import team.mino.core.domain.model.PlaceDetail
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.room.placedetail.model.PlaceCommentUiModel
import team.mino.feature.room.placedetail.model.PlaceHeaderMode
import team.mino.feature.room.placedetail.model.PlaceSheetLevel
import team.mino.feature.room.placedetail.model.RoomPickerItem
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 장소 상세 화면의 상태.
 *
 * 화면이 그리려고 계산하는 값(제출 버튼 활성·[원문보기] 활성)은 필드가 아니라 아래의 파생 프로퍼티다. 필드로 두면
 * 근거가 되는 값이 바뀔 때 함께 갱신하는 것을 빠뜨려 두 출처가 갈린다.
 *
 * **200자 상한을 여기서 강제하지 않는다.** 입력 컴포저블이 201자째를 받지 않는 것으로 막으므로 이 타입에 도달한
 * [commentDraft]는 이미 상한 안이다(spec EC-011).
 *
 * @property place `null`이 로딩 중이다. 헤더·캐러셀·액션 행이 아직 그려지지 않는 구간이며, 화면 상태를 sealed로
 *  가르지 않고 필드로 두는 근거는 `docs/adr/2026-07-25-uistate-isloading-over-sealed-status.md`.
 * @property loadError 주 데이터(핀 상세·최신 코멘트) 조회의 실패. 채워지면 화면 전체가 재시도 가능한
 *  오류로 바뀐다. 문구가 아니라 리프를 담는 것도, 이 실패만 여기로 오고 사용자 액션의 일회성 실패는
 *  방출자로 나가는 것도 `docs/conventions/error_handling.md` §5의 규정이다. [savedRooms] 조회의 실패는
 *  여기 오지 않는다 — 그 갈래는 `PlaceDetailViewModel`이 소유한다.
 * @property headerMode [sheetLevel]에서 파생시키지 않는다. 두 값을 가르는 근거가 서로 달라서이며 그 이유는
 *  [PlaceHeaderMode]가 소유한다.
 * @property carouselPage 외부 지도·원문 링크로 나갔다 돌아와도 보고 있던 장이 유지되어야 한다(spec UX-009).
 * @property comments 오래된 것이 위다. 이전 페이지를 받으면 목록 앞에 붙는다(spec FR-010 · 역방향 페이징).
 * @property commentsObservedAt [comments]의 경과 시간을 판정할 기준 시각. 화면은 이 값과 각 코멘트의 `createdAt`
 *  두 개만으로 표기를 만든다 — 컴포지션 안에서 「지금」을 직접 읽지 않는다
 *  (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §6.1). [comments]가 다시 만들어질 때만
 *  갱신되므로, 목록을 둔 채 시간만 흐르는 동안에는 표기가 움직이지 않는다(spec EC-028).
 *  기본값은 판정에 쓰이지 않는다 — 초기 [comments]가 비어 있어 견줄 코멘트가 없고, 목록이 처음 채워지는 순간
 *  덮인다.
 * @property commentPage 마지막으로 받아 든 코멘트 페이지. 0이 최신이고 위로 갈수록 커진다.
 * @property savedRooms 방 목록과 「이 장소가 그 방에 이미 있는지」를 함께 담는다. [place]가 도착한 뒤 한 번
 *  조회한 결과이며, 그 한 번이 공유 시트의 이미 저장된 방 표시와 [저장된 방] 버튼·시트를 함께 먹인다
 *  (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §3.1). [shareSheet] 안이 아니라
 *  여기 있는 것은 시트가 닫혀 있는 동안에도 남아 있어야 하기 때문이다 — 그쪽은 `null`이 곧 닫힘이다.
 * @property shareSheet `null`이 닫힘이다. 열림 여부 플래그를 따로 두지 않아 목록 없이 열린 시트가 생기지 않는다.
 * @property savedRoomsSheet `null`이 닫힘이다. [shareSheet]와 같은 규칙이며, 두 시트를 하나의 필드로 합치지
 *  않는 것은 담는 것도 닫히는 경로도 서로 다르기 때문이다.
 */
@OptIn(ExperimentalTime::class)
@Immutable
internal data class PlaceDetailUiState(
    val pinId: String,
    val place: PlaceDetail? = null,
    val loadError: MinoDomainException? = null,
    val sheetLevel: PlaceSheetLevel = PlaceSheetLevel.HALF,
    val headerMode: PlaceHeaderMode = PlaceHeaderMode.EXPANDED,
    val carouselPage: Int = 0,
    val comments: ImmutableList<PlaceCommentUiModel> = persistentListOf(),
    val commentsObservedAt: Instant = Instant.DISTANT_PAST,
    val commentPage: Int = 0,
    val hasOlderComments: Boolean = false,
    val isLoadingOlderComments: Boolean = false,
    val commentDraft: String = "",
    val isSubmittingComment: Boolean = false,
    val savedRooms: ImmutableList<RoomPickerItem> = persistentListOf(),
    val shareSheet: ShareSheetUiState? = null,
    val savedRoomsSheet: SavedRoomsSheetUiState? = null,
) : UiState {
    /** 공백만 있는 입력은 보낼 것이 없다. 전송 중에도 같은 코멘트가 두 번 올라가지 않게 잠근다(spec EC-012). */
    val isSubmitEnabled: Boolean
        get() = commentDraft.isNotBlank() && !isSubmittingComment

    /** 원문 링크가 없는 장소에서는 [원문보기]가 열 곳이 없다(spec EC-017). */
    val isSourceEnabled: Boolean
        get() = place?.sourceUrl != null

    /**
     * 이 장소가 두 방 이상에 저장돼 있을 때만 [저장된 방] 버튼을 그린다(spec FR-023 · TS-040 · TS-041).
     *
     * **둘째 방부터가 조건인 것은 지금 보고 있는 방도 이 수에 들어 있기 때문이다.** 하나뿐이면 그 하나가 곧
     * 지금 보고 있는 방이라 옮겨 갈 곳이 없고, 그 사실을 버튼이 없다는 것이 그대로 알린다(spec EC-024 · UX-011).
     * 비활성으로 자리만 남기지 않는 것은, 왜 못 누르는지 설명할 문구도 토스트도 두지 않는 자리이기 때문이다.
     *
     * [savedRooms]가 아직 비어 있는 구간에서도 이 판정이 성립한다 — 조회 전에는 `false`라 버튼이 없다가
     * 결과가 도착하면서 나타난다.
     */
    val isSavedRoomsVisible: Boolean
        get() = savedRooms.count { it.hasPlace } >= 2
}

/**
 * [저장된 방] 시트의 상태.
 *
 * `null`이 닫힘이다. 공유 시트와 같은 규칙으로 열림 플래그를 따로 두지 않아 목록 없이 열린 시트가 생기지 않는다.
 *
 * @property rooms 이 장소가 저장된 방들에서 **지금 보고 있는 방을 뺀** 나머지. 지금 보고 있는 방을 선택 상태로
 *  남겨 두지 않고 빼는 것은 spec FR-024 · TS-042 · EC-026의 규정이며, 그래야 눌러도 아무 일이 없는 카드가
 *  시트에 생기지 않는다(spec UX-012). 목록을 만들 때 이미 빠진 채로 오므로
 *  (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §3.3) 그리는 쪽이 다시 거르지 않으며,
 *  거른 기준인 「지금 보고 있는 핀」도 여기까지 따라오지 않는다.
 */
@Immutable
internal data class SavedRoomsSheetUiState(
    val rooms: ImmutableList<RoomPickerItem>,
)

/**
 * [다른방에 공유] 시트의 상태.
 *
 * 선택은 [selectedRoomIds] 한 곳에만 있다. 카드가 자기 선택 여부를 들지 않으므로 목록이 다시 그려지거나 시트가
 * 스크롤돼도 선택이 흩어지지 않는다.
 *
 * @property rooms `hasPlace`가 `true`인 방은 체크된 채 비활성이라 [selectedRoomIds]에 들어오지 않는다
 *  (spec FR-018 · FR-022).
 * @property isSubmitting 공유 요청이 도는 동안 CTA를 잠가 같은 방에 두 번 보내지지 않게 한다.
 */
@Immutable
internal data class ShareSheetUiState(
    val rooms: ImmutableList<RoomPickerItem> = persistentListOf(),
    val selectedRoomIds: ImmutableSet<String> = persistentSetOf(),
    val isSubmitting: Boolean = false,
) {
    /** 하나라도 고른 뒤에야 보낼 곳이 정해진다. */
    val isShareEnabled: Boolean
        get() = selectedRoomIds.isNotEmpty() && !isSubmitting
}
