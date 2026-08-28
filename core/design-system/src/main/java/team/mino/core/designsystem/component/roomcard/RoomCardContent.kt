package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import team.mino.core.designsystem.component.roomcard.token.RoomCardTokens

/**
 * 방 카드 두 종류가 공유하는 본문 — 썸네일 + 제목·메모·장소 개수.
 *
 * 텍스트 블록은 높이가 고정이고 제목 묶음과 장소 개수 줄이 위아래 끝에 붙는다. 메모가 없어도
 * 장소 개수 줄의 위치가 유지되는 것은 그 때문이다.
 *
 * @param memo null이면 Figma `Show memo=off`. 메모 줄의 높이는 그대로 비워둔다.
 * @param thumbnail 카드 왼쪽 썸네일. 크기와 모서리는 채워 넣는 쪽이 정한다.
 * @param placeCountTrailing 장소 개수 줄의 오른쪽 끝에 붙는 요소(아바타 그룹).
 */
@Composable
internal fun RoomCardContent(
    title: String,
    placeCountLabel: String,
    memo: String?,
    thumbnail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    placeCountTrailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(RoomCardTokens.ItemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        thumbnail()

        Column(
            modifier = Modifier
                .weight(1f)
                .height(RoomCardTokens.ContentHeight),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(RoomCardTokens.TitleMemoSpacing)) {
                Text(
                    text = title,
                    style = MinoRoomCardDefaults.titleFont,
                    color = MinoRoomCardDefaults.titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // 메모가 없어도 시안은 장소 개수 줄의 위치를 유지하므로, 빈 문자열로 한 줄을 남겨둔다.
                Text(
                    text = memo.orEmpty(),
                    style = MinoRoomCardDefaults.memoFont,
                    color = MinoRoomCardDefaults.memoColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(RoomCardTokens.PlaceCountTrailingSpacing),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = placeCountLabel,
                    style = MinoRoomCardDefaults.placeCountFont,
                    color = MinoRoomCardDefaults.placeCountColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                placeCountTrailing()
            }
        }
    }
}
