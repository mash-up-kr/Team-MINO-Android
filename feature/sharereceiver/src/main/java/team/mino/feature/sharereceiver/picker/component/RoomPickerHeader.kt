package team.mino.feature.sharereceiver.picker.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.sharereceiver.R

/**
 * 시트 맨 위에 고정으로 놓이는 제목·안내 두 줄. 목록이 스크롤해도 움직이지 않고, 방이 하나도 없는
 * 상태에서도 그대로 보인다.
 *
 * **문구를 받지 않는다.** 두 줄 모두 상황과 무관하게 같은 문장이라 바깥에서 정할 여지가 없다.
 *
 * 시트 손잡이는 여기 없다 — [RoomPickerSheet]가 갖는다.
 */
@Composable
internal fun RoomPickerHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .padding(
                start = HorizontalPadding,
                end = HorizontalPadding,
                bottom = BottomPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(TextSpacing, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.sharereceiver_header_title),
            style = MinoAndroidTheme.typography.heading2Bold,
            color = MinoAndroidTheme.colors.labelNormal,
        )
        Text(
            text = stringResource(R.string.sharereceiver_header_subtitle),
            style = MinoAndroidTheme.typography.label1NormalRegular,
            color = MinoAndroidTheme.colors.labelNeutral,
        )
    }
}

private val HeaderHeight = 64.dp

private val HorizontalPadding = 20.dp

private val BottomPadding = 12.dp

private val TextSpacing = 4.dp

@UiModePreviews
@Composable
private fun RoomPickerHeaderPreview() {
    MinoAndroidAppTheme {
        RoomPickerHeader()
    }
}
