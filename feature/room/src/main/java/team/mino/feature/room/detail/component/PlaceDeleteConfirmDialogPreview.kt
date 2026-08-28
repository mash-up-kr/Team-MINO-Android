package team.mino.feature.room.detail.component

import androidx.compose.runtime.Composable
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * [PlaceDeleteConfirmDialog] 프리뷰 — 실제 [PlaceDeleteConfirmDialog] 대신 내부 오버레이를 직접
 * 그린다. Compose 프리뷰는 `Dialog`(별도 창)의 내용을 렌더링하지 않기 때문이다.
 */
@UiModePreviews
@Composable
private fun PlaceDeleteConfirmDialogPreview() {
    MinoAndroidAppTheme {
        PlaceDeleteConfirmDialogOverlay(onConfirm = {}, onCancel = {})
    }
}
