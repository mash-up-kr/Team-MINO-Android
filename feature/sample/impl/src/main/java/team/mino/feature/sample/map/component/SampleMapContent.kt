package team.mino.feature.sample.map.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberUpdatedMarkerState
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.map.MinoMap
import team.mino.core.map.geometry.sortedIntoPolygonOrder
import team.mino.core.map.rememberMinoCameraState
import team.mino.core.map.toLatLng

private val AreaStrokeColor = Color(0xFF1E88E5)
private val AreaFillColor = Color(0x331E88E5)
private const val AREA_STROKE_WIDTH = 6f

@Composable
internal fun SampleMapContent(
    cameraCenter: GeoPoint,
    zoom: Float,
    areaPoints: List<GeoPoint>,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberMinoCameraState(center = cameraCenter, zoom = zoom)

    // 임의 순서의 좌표를 둘레 순서로 정렬한 뒤 지도 좌표(LatLng)로 변환한다. areaPoints가 바뀔 때만 재계산한다.
    val orderedPoints =
        remember(areaPoints) {
            areaPoints.sortedIntoPolygonOrder().map { it.toLatLng() }
        }

    MinoMap(
        cameraPositionState = cameraPositionState,
        modifier = modifier.fillMaxSize(),
    ) {
        orderedPoints.forEachIndexed { index, point ->
            Marker(
                state = rememberUpdatedMarkerState(position = point),
                title = "지점 ${index + 1}",
            )
        }

        if (orderedPoints.size >= 3) {
            Polygon(
                points = orderedPoints,
                strokeColor = AreaStrokeColor,
                strokeWidth = AREA_STROKE_WIDTH,
                fillColor = AreaFillColor,
            )
        }
    }
}
