package team.mino.feature.room.placedetail.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.DocumentText
import team.mino.core.designsystem.foundation.icons.icons.Location
import team.mino.core.designsystem.foundation.icons.icons.Persons
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.room.R

/**
 * 장소에 대해 할 수 있는 일 세 가지를 한 행에 늘어놓는다.
 *
 * **세 버튼은 언제나 한 행이다.** 좁은 화면에서 폭이 모자라도 줄을 바꾸거나 글자를 줄이지 않고 가로로 밀어
 * 꺼낸다(spec FR-006). 마지막 버튼이 오른쪽 경계에서 잘려 보이는 것이 밀 수 있다는 신호이므로, 잘림을 없애려고
 * 버튼을 좁히면 그 신호가 사라진다(spec UX-004).
 *
 * **[장소보기]만 강조 스타일이다.** 나머지 둘은 같은 무게의 보조 행동이라 서로 우열을 두지 않는다.
 *
 * @param isSourceEnabled 원문 링크가 없는 장소에서는 [원문보기]가 열 곳이 없어 비활성이다(spec FR-017 · EC-017).
 */
@Composable
internal fun PlaceActionRow(
    isSourceEnabled: Boolean,
    onPlaceClick: () -> Unit,
    onSourceClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            // 패딩이 스크롤 안쪽에 있어야 끝까지 밀었을 때 마지막 버튼 뒤로도 같은 여백이 남는다.
            .padding(horizontal = RowHorizontalPadding, vertical = RowVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(ButtonSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MinoButton(
            text = stringResource(R.string.placedetail_action_view_place),
            onClick = onPlaceClick,
            size = ButtonSize.Medium,
            style = ButtonStyle.SolidPrimary,
            leadingIcon = {
                // 버튼 글자가 같은 것을 말하므로 아이콘은 장식이다.
                Icon(imageVector = MinoIcons.Location, contentDescription = null)
            },
        )
        MinoButton(
            text = stringResource(R.string.placedetail_action_view_source),
            onClick = onSourceClick,
            enabled = isSourceEnabled,
            size = ButtonSize.Medium,
            style = ButtonStyle.OutlinedAssistive,
            leadingIcon = {
                Icon(imageVector = MinoIcons.DocumentText, contentDescription = null)
            },
        )
        MinoButton(
            text = stringResource(R.string.placedetail_action_share),
            onClick = onShareClick,
            size = ButtonSize.Medium,
            style = ButtonStyle.OutlinedAssistive,
            leadingIcon = {
                Icon(imageVector = MinoIcons.Persons, contentDescription = null)
            },
        )
    }
}

private val RowHorizontalPadding = 20.dp

private val RowVerticalPadding = 12.dp

private val ButtonSpacing = 8.dp

@UiModePreviews
@Composable
private fun PlaceActionRowPreview() {
    MinoAndroidAppTheme {
        PlaceActionRow(
            isSourceEnabled = true,
            onPlaceClick = {},
            onSourceClick = {},
            onShareClick = {},
        )
    }
}

@UiModePreviews
@Composable
private fun PlaceActionRowWithoutSourcePreview() {
    MinoAndroidAppTheme {
        PlaceActionRow(
            isSourceEnabled = false,
            onPlaceClick = {},
            onSourceClick = {},
            onShareClick = {},
        )
    }
}
