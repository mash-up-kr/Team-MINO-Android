package team.mino.feature.sample.main.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.LocationFill
import team.mino.core.designsystem.foundation.icons.icons.Thumbnail
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun MinoHeaderRoomShowMemoOnPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        MinoHeaderRoom(
            modifier = modifier.fillMaxWidth(),
            title = "Title",
            memo = "memo",
            resourceCountText = "999+개",
            resourceIcon = MinoIcons.LocationFill,
            onThumbnailClick = {},
            thumbnailIcon = MinoIcons.Thumbnail,
            thumbnailContentDescription = "썸네일 모아보기",
        )
    }
}

@UiModePreviews
@Composable
private fun MinoHeaderRoomShowMemoOffPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        MinoHeaderRoom(
            modifier = modifier.fillMaxWidth(),
            title = "Title",
            resourceCountText = "999+개",
            resourceIcon = MinoIcons.LocationFill,
            onThumbnailClick = {},
            thumbnailIcon = MinoIcons.Thumbnail,
            thumbnailContentDescription = "썸네일 모아보기",
        )
    }
}
