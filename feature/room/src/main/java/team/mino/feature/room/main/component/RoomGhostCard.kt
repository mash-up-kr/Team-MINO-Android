package team.mino.feature.room.main.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * [RoomGhostCard] 치수 토큰. `spec.md` 유저 플로우 4는 이 카드에 별도 Figma 노드를 달지 않아
 * (FR-009, [contracts/room-list-main-contract.md] Figma 절 참고) 인접한 [MinoRoomCard]와 톤을
 * 맞추는 최소 실측값만 둔다.
 */
private object RoomGhostCardTokens {
    val Height = 80.dp
    val Shape: Shape = RoundedCornerShape(14.dp)
    val BorderWidth = 1.dp
    val ContentSpacing = 8.dp
    val IconSize = 20.dp
}

/**
 * 공동방 0개 사용자에게 첫 공동방 생성을 유도하는 Ghost Card(FR-009, [research.md D9]).
 * 방 카드 목록 자리에 대신 놓이는 카드형 CTA로, `[+] 공동방 만들기` 문구를 보여준다.
 *
 * 재노출 여부는 이 컴포저블이 아니라 호출부의 `showGhostCard`(=`groupRooms.isEmpty()` 파생값)가
 * 결정한다.
 *
 * @param onClick Ghost Card 클릭(FR-009) — `NavigateToRoomForm` 재사용.
 */
@Composable
internal fun RoomGhostCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RoomGhostCardTokens.Height)
            .clip(RoomGhostCardTokens.Shape)
            .border(
                border = BorderStroke(RoomGhostCardTokens.BorderWidth, MinoAndroidTheme.colors.lineSolidNeutral),
                shape = RoomGhostCardTokens.Shape,
            ).rippleSingleClickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(RoomGhostCardTokens.ContentSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(RoomGhostCardTokens.IconSize),
            imageVector = MinoIcons.Plus,
            contentDescription = null,
            tint = MinoAndroidTheme.colors.labelAlternative,
        )
        Text(
            text = "공동방 만들기",
            color = MinoAndroidTheme.colors.labelAlternative,
            style = MinoAndroidTheme.typography.body1NormalMedium,
        )
    }
}
