package team.mino.feature.room.detail.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 나가기 확인 모달([SYS-007] Flow A — 일반 멤버, `LeaveDialogState.ConfirmMember`).
 *
 * [PlaceDeleteConfirmDialog]("이 장소를 삭제할까요?")와 같은 카드·버튼 배치를 [RoomConfirmDialog]·
 * [RoomConfirmDialogCard]로 재사용한다 — 이 모달에 대응하는 Figma 노드가 브리프에 없어, 저장소가 이미
 * 확정한 카드 스타일을 그대로 따른다.
 *
 * @param onConfirm [나가기] 클릭 — `RoomRepository.leaveRoom(roomId)` 호출로 이어진다.
 * @param onCancel [취소] 클릭 · 딤 바깥 탭이 모두 이 하나로 올라온다. 사용자가 고르지 않은 결과를
 *   만들지 않으려면 셋의 처리가 같아야 하므로 콜백을 나누지 않는다.
 */
@Composable
internal fun RoomLeaveConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoomConfirmDialog(onDismiss = onCancel, modifier = modifier) {
        RoomConfirmDialogCard(
            title = "이 방에서 나갈까요?",
            description = "나가면 이 방을 더 이상 볼 수 없어요\n방의 장소와 댓글은 그대로 남아요",
            cancelText = "취소",
            onCancel = onCancel,
            confirmText = "나가기",
            onConfirm = onConfirm,
        )
    }
}
