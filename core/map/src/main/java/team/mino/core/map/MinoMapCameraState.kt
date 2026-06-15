package team.mino.core.map

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import team.mino.core.common.kotlin.geo.GeoPoint

/**
 * [center]를 카메라 초기 위치로 하는 [CameraPositionState]를 생성한다.
 *
 * GeoPoint→LatLng 변환과 maps-compose 카메라 초기화 보일러플레이트를 흡수한다.
 */
@Composable
fun rememberMinoCameraState(
    center: GeoPoint,
    zoom: Float,
): CameraPositionState =
    rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center.toLatLng(), zoom)
    }
