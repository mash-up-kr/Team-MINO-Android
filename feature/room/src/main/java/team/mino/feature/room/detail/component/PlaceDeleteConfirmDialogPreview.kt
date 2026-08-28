package team.mino.feature.room.detail.component

import androidx.compose.runtime.Composable
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/** [PlaceDeleteConfirmDialog] 프리뷰 — 장소 삭제 확인 모달. */
@UiModePreviews
@Composable
private fun PlaceDeleteConfirmDialogPreview() {
    MinoAndroidAppTheme {
        PlaceDeleteConfirmDialog(onConfirm = {}, onCancel = {})
    }
}
