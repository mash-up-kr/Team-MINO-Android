package team.mino.core.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings

/**
 * 프로젝트 표준 지도 컴포저블. maps-compose의 [GoogleMap]을 감싼 단일 진입점으로,
 * feature가 GoogleMap을 직접 다루지 않고 일관된 방식으로 지도를 그리도록 한다.
 *
 * 마커·폴리곤 등 지도 위 오버레이는 [content] 슬롯에서 maps-compose 컴포저블로 구성한다.
 *
 * @param contentPadding 지도 위에 다른 UI가 덮고 있어 **보이지 않는 가장자리**. 카메라의 타깃은 이 패딩을
 *  뺀 영역의 중앙에 놓이므로, 바텀시트가 지도의 아래쪽을 가리는 화면은 그 높이를 여기에 실어야 마커가
 *  「사용자가 보는 지도」의 중앙에 온다. 구글 로고·저작권 표기도 이 패딩을 피해 올라온다.
 */
@Composable
fun MinoMap(
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier,
    uiSettings: MapUiSettings = MapUiSettings(),
    properties: MapProperties = MapProperties(),
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable @GoogleMapComposable () -> Unit = {},
) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = properties,
        contentPadding = contentPadding,
        content = content,
    )
}
