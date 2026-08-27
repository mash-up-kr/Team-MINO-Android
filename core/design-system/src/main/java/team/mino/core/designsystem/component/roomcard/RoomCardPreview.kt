package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

private const val PREVIEW_TITLE = "내 방"
private const val PREVIEW_MEMO = "내가 꾹 저장한 장소"
private const val PREVIEW_PLACE_COUNT_LABEL = "장소 0개"

@UiModePreviews
@Composable
private fun MinoRoomCardWithMemoPreview() {
    RoomCardPreviewContainer {
        MinoRoomCard(
            title = PREVIEW_TITLE,
            placeCountLabel = PREVIEW_PLACE_COUNT_LABEL,
            participantImageUrls = persistentListOf(null),
            memo = PREVIEW_MEMO,
            onClick = {},
        )
    }
}

@UiModePreviews
@Composable
private fun MinoRoomCardWithoutMemoPreview() {
    RoomCardPreviewContainer {
        MinoRoomCard(
            title = PREVIEW_TITLE,
            placeCountLabel = PREVIEW_PLACE_COUNT_LABEL,
            participantImageUrls = persistentListOf(null, null, null),
            onClick = {},
        )
    }
}

@UiModePreviews
@Composable
private fun MinoRoomCheckBoxCardWithMemoPreview() {
    RoomCardPreviewContainer {
        MinoRoomCheckBoxCard(
            title = PREVIEW_TITLE,
            placeCountLabel = PREVIEW_PLACE_COUNT_LABEL,
            checked = true,
            memo = PREVIEW_MEMO,
            onCheckedChange = {},
            onClick = {},
        )
    }
}

@UiModePreviews
@Composable
private fun MinoRoomCheckBoxCardWithoutMemoPreview() {
    RoomCardPreviewContainer {
        MinoRoomCheckBoxCard(
            title = PREVIEW_TITLE,
            placeCountLabel = PREVIEW_PLACE_COUNT_LABEL,
            checked = false,
            onCheckedChange = {},
            onClick = {},
        )
    }
}

@Composable
private fun RoomCardPreviewContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .background(MinoAndroidTheme.colors.backgroundNormalNormal)
                .padding(horizontal = 16.dp),
        ) {
            content()
        }
    }
}
