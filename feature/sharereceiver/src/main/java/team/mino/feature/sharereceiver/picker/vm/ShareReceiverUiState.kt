package team.mino.feature.sharereceiver.picker.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import team.mino.core.common.android.architecture.UiState
import team.mino.core.common.ui.component.roompicker.model.RoomPickerItem
import team.mino.core.common.ui.component.roompicker.model.SheetStep

/**
 * 공유받은 장소를 어느 방에 저장할지 고르는 시트의 상태.
 *
 * 저장 가능 여부와 빈 목록 여부는 필드가 아니라 아래의 파생 프로퍼티다. 필드로 두면
 * [selectedRoomIds]·[rooms]가 바뀔 때 함께 갱신하는 것을 빠뜨려 두 출처가 갈린다.
 *
 * @property selectedRoomIds 선택은 여기 한 곳에만 있다. 카드가 자기 선택 여부를 들지 않으므로
 *  목록이 다시 그려지거나 시트가 스크롤돼도 선택이 흩어지지 않는다.
 * @property sheetStep 시트가 지금 멈춰 선 단계. 단계가 바뀌어도 [selectedRoomIds]는 그대로다(TS-016).
 */
@Immutable
internal data class ShareReceiverUiState(
    val rooms: ImmutableList<RoomPickerItem> = persistentListOf(),
    val selectedRoomIds: ImmutableSet<String> = persistentSetOf(),
    val sheetStep: SheetStep = SheetStep.PEEK,
) : UiState {
    /** 하나라도 고른 뒤에야 저장할 곳이 정해진다. */
    val isSaveEnabled: Boolean
        get() = selectedRoomIds.isNotEmpty()

    /**
     * 방 카드 자리에 안내를 대신 노출해야 하는 상태.
     *
     * 조회 실패도 방 0개와 같은 화면으로 수렴하므로 오류 슬롯을 따로 두지 않는다.
     */
    val isEmpty: Boolean
        get() = rooms.isEmpty()
}
