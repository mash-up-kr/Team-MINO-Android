package team.mino.feature.room.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.feature.room.R

/**
 * [RoomNudgeSheet] 치수 토큰. Figma `2661-157272`(방 리스트 화면 `2661-157259` 안의 넛지 카드) 대조.
 */
private object RoomNudgeSheetTokens {
    val HorizontalPadding = 20.dp
    val VerticalPadding = 32.dp
    val IllustrationTitleSpacing = 24.dp
    val TitleSubtitleSpacing = 8.dp
    val SubtitleButtonSpacing = 24.dp

    // Figma 일러스트(#2661:157273) 실측 크기.
    val IllustrationWidth = 200.dp
    val IllustrationHeight = 148.89.dp
}

/**
 * 공동방 0개 사용자에게 첫 공동방 생성을 유도하는 Nudge(FR-008, [research.md D9]).
 *
 * Figma `2661-157272`는 일러스트 + 문구 + 버튼 1개(`공동방 만들기`)만 있고 별도 닫기 버튼이 없다.
 * 그래서 버튼은 `:core:design-system`의 [MinoButton]을 `SolidPrimary`·`Medium`으로 하나만 쓴다
 * (Figma `Button/Button` 인스턴스와 크기·패딩·타이포가 일치).
 *
 * 재노출 여부는 이 컴포저블이 아니라 호출부의 `showNudge`(=`groupRooms.isEmpty()` 파생값)가
 * 결정한다. 닫기 버튼이 Figma에 없으므로 별도 dismiss 콜백은 두지 않는다.
 *
 * @param onCreateClick [공동방 만들기] 클릭(FR-008) — `NavigateToRoomForm` 재사용.
 */
@Composable
internal fun RoomNudgeSheet(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                horizontal = RoomNudgeSheetTokens.HorizontalPadding,
                vertical = RoomNudgeSheetTokens.VerticalPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        // Figma 2661:157272 — Nudge 프레임 자체가 justifyContent: center. 시트 남은 높이를 채우는
        // 자리라 세로 가운데 정렬해야 그 안에서 콘텐츠가 위로 쏠리지 않는다.
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.room_nudge_illustration),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(
                width = RoomNudgeSheetTokens.IllustrationWidth,
                height = RoomNudgeSheetTokens.IllustrationHeight,
            ),
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = RoomNudgeSheetTokens.IllustrationTitleSpacing),
            text = "공동방을 생성해보세요!",
            color = MinoAndroidTheme.colors.primaryNormal,
            style = MinoAndroidTheme.typography.title3Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = RoomNudgeSheetTokens.TitleSubtitleSpacing),
            text = "\"저번에 말한 거기가 어디였지?\"\n더 이상 묻지 마세요.",
            color = MinoAndroidTheme.colors.labelAlternative,
            style = MinoAndroidTheme.typography.label1NormalRegular,
            textAlign = TextAlign.Center,
        )
        MinoButton(
            modifier = Modifier.padding(top = RoomNudgeSheetTokens.SubtitleButtonSpacing),
            text = "공동방 만들기",
            onClick = onCreateClick,
            size = ButtonSize.Medium,
            style = ButtonStyle.SolidPrimary,
            leadingIcon = { Icon(imageVector = MinoIcons.Plus, contentDescription = null) },
        )
    }
}
