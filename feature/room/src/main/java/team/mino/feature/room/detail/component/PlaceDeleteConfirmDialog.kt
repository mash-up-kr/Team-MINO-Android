package team.mino.feature.room.detail.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 장소 삭제 확인 모달(UX-001, FR-010, Figma `004-1-3-1_장소 삭제 클릭시` 딤 처리, node `3222-87768`).
 * `Card_Location` 목록 위로 딤이 깔리고 카드 한 장이 화면 중앙에 뜬다.
 *
 * [RoomLeaveConfirmDialog]·[RoomOwnerLeaveDialog]가 같은 딤·카드·버튼 배치를 [RoomConfirmDialog]·
 * [RoomConfirmDialogCard]로 재사용하므로, 이 모달이 표준으로 삼는 원본이지만 구현은 그 공용 뼈대를
 * 그대로 쓴다 — 카드 치수(`Figma node 3222-87796`·`3222-87800`)는 [RoomConfirmDialogCard]의 토큰과 같다.
 *
 * @param onConfirm [삭제] 클릭(FR-010 — 해당 방에서만 장소 제거).
 * @param onCancel [취소] 클릭 · 딤 바깥 탭이 모두 이 하나로 올라온다. 사용자가 고르지 않은 결과를
 *   만들지 않으려면 셋의 처리가 같아야 하므로 콜백을 나누지 않는다.
 */
@Composable
internal fun PlaceDeleteConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoomConfirmDialog(onDismiss = onCancel, modifier = modifier) {
        RoomConfirmDialogCard(
            title = "이 장소를 삭제할까요?",
            description = "장소에 등록된 사진과 댓글이 모두 삭제되며,\n다시 되돌릴 수 없어요.",
            cancelText = "취소",
            onCancel = onCancel,
            confirmText = "삭제",
            onConfirm = onConfirm,
        )
    }
}
