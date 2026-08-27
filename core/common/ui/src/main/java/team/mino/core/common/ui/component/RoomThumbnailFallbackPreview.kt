package team.mino.core.common.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * 색을 고른 방과 회색 방의 폴백 프리뷰.
 *
 * 크기와 모서리는 [RoomThumbnailFallback]이 갖지 않으므로 프리뷰가 자리표시 값을 준다 — 디자인
 * 근거가 있는 값이 아니라 폴백이 그려지는 것만 보이면 된다.
 */
@UiModePreviews
@Composable
private fun RoomThumbnailFallbackPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        RoomThumbnailFallback(
            color = MinoRoomColor.Cyan,
            modifier = modifier.then(PreviewThumbnailModifier),
        )
    }
}

@UiModePreviews
@Composable
private fun RoomThumbnailFallbackGrayPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        RoomThumbnailFallback(
            color = null,
            modifier = modifier.then(PreviewThumbnailModifier),
        )
    }
}

private val PreviewThumbnailModifier = Modifier
    .size(80.dp)
    .clip(RoundedCornerShape(14.dp))
