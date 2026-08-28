package team.mino.feature.room.detail.component

import androidx.compose.runtime.Composable
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/** [RoomLeaveConfirmDialog] 프리뷰 — 일반 멤버 나가기 확인 모달. */
@UiModePreviews
@Composable
private fun RoomLeaveConfirmDialogPreview() {
    MinoAndroidAppTheme {
        RoomLeaveConfirmDialog(onConfirm = {}, onCancel = {})
    }
}
