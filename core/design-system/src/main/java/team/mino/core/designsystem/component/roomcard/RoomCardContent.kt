package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import team.mino.core.designsystem.component.roomcard.token.RoomCardTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 방 카드 두 종류가 공유하는 본문 — 커버 이미지 + 제목·메모·장소 개수.
 *
 * @param memo null이면 Figma `Show memo=off`. 메모 줄의 높이는 그대로 비워둔다.
 * @param content 장소 개수 줄의 오른쪽 끝에 붙는 요소(아바타 그룹).
 */
@Composable
internal fun RoomCardContent(
    title: String,
    placeCountLabel: String,
    coverImageUrl: String?,
    memo: String?,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(RoomCardTokens.CoverSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomCardCover(imageUrl = coverImageUrl)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MinoRoomCardDefaults.titleFont,
                color = MinoRoomCardDefaults.titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(RoomCardTokens.TitleMemoSpacing))

            // 메모가 없어도 시안은 장소 개수 줄의 위치를 유지하므로, 빈 문자열로 한 줄을 남겨둔다.
            Text(
                text = memo.orEmpty(),
                style = MinoRoomCardDefaults.memoFont,
                color = MinoRoomCardDefaults.memoColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(RoomCardTokens.MemoPlaceCountSpacing))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = placeCountLabel,
                    style = MinoRoomCardDefaults.placeCountFont,
                    color = MinoRoomCardDefaults.placeCountColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                content()
            }
        }
    }
}

@Composable
private fun RoomCardCover(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    MinoAsyncImage(
        imageUrl = imageUrl,
        fallback = rememberVectorPainter(MinoIcons.Image),
        fallbackTint = MinoRoomCardDefaults.coverPlaceholderTint,
        modifier = modifier
            .size(RoomCardTokens.CoverSize)
            .surface(
                shape = MinoRoomCardDefaults.coverShape,
                containerColor = MinoRoomCardDefaults.coverBackgroundColor,
            ),
    )
}
