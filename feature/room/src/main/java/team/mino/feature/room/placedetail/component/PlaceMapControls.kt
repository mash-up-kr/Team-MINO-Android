package team.mino.feature.room.placedetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.component.button.MinoButtonDefaults
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Folder
import team.mino.core.designsystem.foundation.icons.icons.MyLocation
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.room.R
import team.mino.feature.room.placedetail.model.PlaceSheetLevel

/**
 * 지도 위에 겹쳐 놓이는 컨트롤 한 행. 오른쪽 끝이 [현재 위치]이고 그 왼쪽이 [저장된 방]이다.
 *
 * **[PlaceSheetLevel.FULL]에서는 둘이 함께 사라진다**(spec UX-013). 이 판정을 버튼 각각이 아니라 여기서
 * 하는 이유는 둘이 같은 조건으로 함께 숨기 때문이다 — 조건을 버튼마다 두면 한쪽만 남는 상태가 만들어질 수 있다.
 *
 * 세로 위치는 정하지 않는다. 시트가 얼마나 올라와 있는지에 따라 달라지므로 이 행을 지도 위에 얹는 쪽이
 * [modifier]로 정한다.
 *
 * @param sheetLevel 지금 시트가 멈춰 선 단계. 이 행이 보일지 말지를 가르는 유일한 입력이다.
 * @param isSavedRoomsEnabled [저장된 방]이 눌리는지 여부. 이 행이 판정하지 않고 화면 상태에서 그대로 받아 넘긴다.
 * @param onSavedRoomsClick [저장된 방] 클릭. 비활성일 때는 버튼이 클릭을 받지 않아 도달하지 않는다(spec FR-023).
 * @param onCurrentLocationClick [현재 위치] 클릭. 이 화면은 지도를 소유하지 않으므로 장소 상세의 인텐트가
 *   아니라 지도를 실제로 그리는 쪽(`RoomListViewModel`)의 인텐트로 이어져야 카메라가 움직인다
 *   (`RoomDetailScreen`이 같은 이유로 같은 배선을 한다, `docs/specs/place-detail/research.md` D25).
 */
@Composable
internal fun PlaceMapControls(
    sheetLevel: PlaceSheetLevel,
    isSavedRoomsEnabled: Boolean,
    onSavedRoomsClick: () -> Unit,
    onCurrentLocationClick: () -> Unit,
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
            SavedRoomsButton(enabled = isSavedRoomsEnabled, onClick = onSavedRoomsClick)
            CurrentLocationButton(onClick = onCurrentLocationClick)
        }
    }
}

/**
 * 지도 위에 놓이는 [저장된 방] 버튼.
 *
 * **활성 여부를 여기서 판정하지 않는다.** 그 근거는 `PlaceDetailUiState.isSavedRoomsEnabled` 하나뿐이므로
 * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §3.2) 이 컴포저블은 결과만 받는다 —
 * 같은 규칙을 화면과 버튼 두 곳에 두면 어느 쪽이 진짜인지 갈린다.
 *
 * **활성인지 아닌지가 곧 「이 장소가 여러 방에 있다」는 안내다**(spec UX-011). 그래서 중복 저장을 따로 알리는
 * 뱃지·문구를 옆에 두지 않는다.
 *
 * 비활성 외형은 [MinoButton]이 `enabled`로 이미 갖고 있어 여기서 색을 다시 정하지 않는다.
 *
 * 배치는 이 컴포저블이 정하지 않는다 — 지도 어디에 놓일지는 [PlaceMapControls]의 몫이다.
 */
@Composable
private fun SavedRoomsButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 그림자와 배경을 버튼 바깥에서 얹으므로 버튼과 같은 모서리가 필요하다. 값을 다시 적지 않고 받아 온다.
    val buttonShape = MinoButtonDefaults.shape(ButtonSize.Medium)

    MinoButton(
        text = stringResource(R.string.placedetail_saved_rooms),
        onClick = onClick,
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

/**
 * 지도 위에 놓이는 [현재 위치] 버튼.
 *
 * **카메라를 옮기는 것은 이 화면이 아니다.** 지도는 호출부가 소유하므로 클릭을 그대로 위로 올린다 — 장소 상세가
 * 자기 카메라 상태를 들면 그 상태를 읽는 화면이 없어 버튼이 눌려도 지도가 안 움직인다(실기기 확인된 결함,
 * `RoomDetailScreen` KDoc·`docs/specs/place-detail/research.md` D25).
 *
 * 디자인 시스템의 버튼 컴포넌트가 아니라 지도 화면이 직접 조립한 원형 프레임이라 `MinoIconButton`을 쓰지 않는다
 * (그쪽은 모서리가 둥근 정사각형이다).
 *
 * 배치는 이 컴포저블이 정하지 않는다 — 지도 어디에 놓일지는 [PlaceMapControls]의 몫이다.
 */
@Composable
private fun CurrentLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(CurrentLocationButtonSize)
            .dropShadow(shape = CircleShape, shadow = MinoAndroidTheme.shadows.normalMedium)
            .background(color = MinoAndroidTheme.colors.backgroundNormalNormal, shape = CircleShape)
            .rippleSingleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = MinoIcons.MyLocation,
            contentDescription = stringResource(R.string.placedetail_current_location),
            modifier = Modifier.size(CurrentLocationIconSize),
            tint = MinoAndroidTheme.colors.labelAlternative,
        )
    }
}

private val CurrentLocationButtonSize = 40.dp
private val CurrentLocationIconSize = 20.dp

private val MapControlsEndPadding = 20.dp
private val MapControlsSpacing = 8.dp

@UiModePreviews
@Composable
private fun PlaceMapControlsPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        PlaceMapControls(
            sheetLevel = PlaceSheetLevel.HALF,
            isSavedRoomsEnabled = true,
            onSavedRoomsClick = {},
            onCurrentLocationClick = {},
            modifier = modifier.padding(vertical = MapControlsPreviewPadding),
        )
    }
}

/** 비활성 [저장된 방]. 이 장소가 한 방에만 있을 때의 외형이다(spec TS-041). */
@UiModePreviews
@Composable
private fun PlaceMapControlsSavedRoomsDisabledPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        PlaceMapControls(
            sheetLevel = PlaceSheetLevel.HALF,
            isSavedRoomsEnabled = false,
            onSavedRoomsClick = {},
            onCurrentLocationClick = {},
            modifier = modifier.padding(vertical = MapControlsPreviewPadding),
        )
    }
}

private val MapControlsPreviewPadding = 16.dp
