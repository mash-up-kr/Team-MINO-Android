package team.mino.feature.placedetail.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.component.button.MinoButtonDefaults
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Folder
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.placedetail.R

/**
 * 지도 위에 놓이는 [저장된 방] 버튼.
 *
 * **활성 여부를 여기서 판정하지 않는다.** 그 근거는 `PlaceDetailUiState.isSavedRoomsEnabled` 하나뿐이므로
 * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §6) 이 컴포저블은 결과만 받는다 —
 * 같은 규칙을 화면과 버튼 두 곳에 두면 어느 쪽이 진짜인지 갈린다.
 *
 * 클릭 콜백은 받지 않는다. 비활성 버튼은 클릭 자체를 받지 않아 눌러도 아무 일이 일어나지 않고, 열 곳이
 * 정해지면 그때 디폴트 인자로 더한다.
 *
 * 배치는 이 컴포저블이 정하지 않는다 — 지도 어디에 놓일지는 [PlaceMapControls]의 몫이다.
 *
 * @param enabled 눌리는지 여부. 지금은 언제나 `false`로 들어온다 — 저장된 방 전환이 이번 범위 밖이라
 *   버튼은 자리만 지킨다(`docs/specs/place-detail/research.md` D10).
 */
@Composable
internal fun SavedRoomsButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    // 그림자와 배경을 버튼 바깥에서 얹으므로 버튼과 같은 모서리가 필요하다. 값을 다시 적지 않고 받아 온다.
    val buttonShape = MinoButtonDefaults.shape(ButtonSize.Medium)

    MinoButton(
        text = stringResource(R.string.placedetail_saved_rooms),
        onClick = {},
        modifier = modifier
            .dropShadow(shape = buttonShape, shadow = MinoAndroidTheme.shadows.normalMedium)
            .background(color = MinoAndroidTheme.colors.backgroundNormalNormal, shape = buttonShape),
        enabled = enabled,
        size = ButtonSize.Medium,
        style = ButtonStyle.OutlinedAssistive,
        leadingIcon = {
            // 버튼 글자가 같은 것을 말하므로 아이콘은 장식이다.
            Icon(imageVector = MinoIcons.Folder, contentDescription = null)
        },
    )
}

@UiModePreviews
@Composable
private fun SavedRoomsButtonPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        SavedRoomsButton(enabled = false, modifier = modifier.padding(SavedRoomsPreviewPadding))
    }
}

private val SavedRoomsPreviewPadding = 16.dp
