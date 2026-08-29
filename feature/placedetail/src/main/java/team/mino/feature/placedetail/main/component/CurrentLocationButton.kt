package team.mino.feature.placedetail.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.MyLocation
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.placedetail.R

/**
 * 지도 위에 놓이는 [현재 위치] 버튼.
 *
 * **누르는 동작이 없다.** 카메라를 현재 위치로 옮기는 일과 위치 권한 요청은 [SYS-004] 소관이고 그 구현이
 * 이 저장소에 없어, 이번 범위는 렌더링과 배치까지다(`docs/specs/place-detail/research.md` D16).
 * 그래서 클릭 콜백을 미리 열어 두지 않는다 — [SYS-004]가 생기면 그때 디폴트 인자로 더한다.
 *
 * 이 버튼은 Figma 디자인 시스템의 버튼 컴포넌트가 아니라 지도 화면이 직접 조립한 원형 프레임이라
 * `MinoIconButton`이 아닌 이 화면의 컴포저블로 둔다(`MinoIconButton`은 모서리가 둥근 정사각형이다).
 *
 * 배치는 이 컴포저블이 정하지 않는다 — 지도 어디에 놓일지는 [PlaceMapControls]의 몫이다.
 */
@Composable
internal fun CurrentLocationButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(CurrentLocationButtonSize)
            .dropShadow(shape = CircleShape, shadow = MinoAndroidTheme.shadows.normalMedium)
            .background(color = MinoAndroidTheme.colors.backgroundNormalNormal, shape = CircleShape),
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

@UiModePreviews
@Composable
private fun CurrentLocationButtonPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        CurrentLocationButton(modifier = modifier.padding(CurrentLocationPreviewPadding))
    }
}

private val CurrentLocationPreviewPadding = 16.dp
