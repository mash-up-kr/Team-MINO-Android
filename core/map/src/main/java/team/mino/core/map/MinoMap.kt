package team.mino.core.map

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
 */
@Composable
fun MinoMap(
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier,
    uiSettings: MapUiSettings = MapUiSettings(),
    properties: MapProperties = MapProperties(),
    content: @Composable @GoogleMapComposable () -> Unit = {},
) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = properties,
        content = content,
    )
}
