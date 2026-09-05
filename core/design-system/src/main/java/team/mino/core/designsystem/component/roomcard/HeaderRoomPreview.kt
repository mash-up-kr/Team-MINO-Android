package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun MinoHeaderRoomShowMemoOnPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        MinoHeaderRoom(
            modifier = modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.backgroundNormalNormal),
            title = "Title",
            memo = "memo",
            resourceCountText = "999+개",
            onThumbnailClick = {},
        )
    }
}

@UiModePreviews
@Composable
private fun MinoHeaderRoomShowMemoOffPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        MinoHeaderRoom(
            modifier = modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.backgroundNormalNormal),
            title = "Title",
            resourceCountText = "999+개",
            onThumbnailClick = {},
        )
    }
}
