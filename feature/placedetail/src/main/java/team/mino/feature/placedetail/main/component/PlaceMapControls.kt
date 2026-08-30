package team.mino.feature.placedetail.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.placedetail.main.model.PlaceSheetLevel

/**
 * 지도 위에 겹쳐 놓이는 컨트롤 한 행. 오른쪽 끝이 [현재 위치]이고 그 왼쪽이 [저장된 방]이다.
 *
 * **[PlaceSheetLevel.FULL]에서는 둘이 함께 사라진다**(spec §4 가정). 이 판정을 버튼 각각이 아니라 여기서
 * 하는 이유는 둘이 같은 조건으로 함께 숨기 때문이다 — 조건을 버튼마다 두면 한쪽만 남는 상태가 만들어질 수 있다.
 *
 * 세로 위치는 정하지 않는다. 시트가 얼마나 올라와 있는지에 따라 달라지므로 이 행을 지도 위에 얹는 쪽이
 * [modifier]로 정한다.
 *
 * @param sheetLevel 지금 시트가 멈춰 선 단계. 이 행이 보일지 말지를 가르는 유일한 입력이다.
 * @param isSavedRoomsEnabled [저장된 방]이 눌리는지 여부. 이 행이 판정하지 않고 화면 상태에서 그대로 받아 넘긴다.
 */
@Composable
internal fun PlaceMapControls(
    sheetLevel: PlaceSheetLevel,
    isSavedRoomsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    // when 식으로 두어 시트 단계가 늘어나면 여기서도 보임 여부를 정하게 만든다.
    val isVisible = when (sheetLevel) {
        PlaceSheetLevel.HALF -> true
        PlaceSheetLevel.FULL -> false
    }

    if (isVisible) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = MapControlsEndPadding),
            horizontalArrangement = Arrangement.spacedBy(MapControlsSpacing, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SavedRoomsButton(enabled = isSavedRoomsEnabled)
            CurrentLocationButton()
        }
    }
}

private val MapControlsEndPadding = 20.dp
private val MapControlsSpacing = 8.dp

@UiModePreviews
@Composable
private fun PlaceMapControlsPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        PlaceMapControls(
            sheetLevel = PlaceSheetLevel.HALF,
            isSavedRoomsEnabled = false,
            modifier = modifier.padding(vertical = MapControlsPreviewPadding),
        )
    }
}

private val MapControlsPreviewPadding = 16.dp
