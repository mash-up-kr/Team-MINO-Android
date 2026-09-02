package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import team.mino.core.designsystem.component.roomcard.token.RoomCardTokens
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * 트레일링 슬롯만 다른 방 카드들이 공유하는 바깥 줄 — 클릭 영역·세로 여백·본문 배치.
 *
 * 카드 전체가 하나의 클릭 영역이고 [trailing]은 그 안에 얹힌다. 트레일링이 자기 클릭 영역을 갖는지는
 * 그 슬롯을 채우는 쪽이 정한다.
 *
 * @param enabled false면 카드 본문의 탭을 막는다. **본문의 색은 바뀌지 않는다** — 어느 부분이
 *   비활성으로 보이는지는 [trailing]을 채우는 쪽이 정한다.
 * @param trailing 본문 오른쪽 끝에 붙는 요소(체크박스·꺽쇠). 이 자리가 카드 종류를 가른다.
 */
@Composable
internal fun RoomCardRow(
    title: String,
    placeCountLabel: String,
    memo: String?,
    onClick: () -> Unit,
    thumbnail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(enabled = enabled, onClick = onClick)
            .padding(vertical = RoomCardTokens.VerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(RoomCardTokens.ItemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomCardContent(
            title = title,
            placeCountLabel = placeCountLabel,
            memo = memo,
            modifier = Modifier.weight(1f),
            thumbnail = thumbnail,
        )

        trailing()
    }
}

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
