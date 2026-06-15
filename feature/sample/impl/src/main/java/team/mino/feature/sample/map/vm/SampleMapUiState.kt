package team.mino.feature.sample.map.vm

import team.mino.core.common.android.architecture.UiState
import team.mino.core.common.kotlin.geo.GeoPoint

data class SampleMapUiState(
    val cameraCenter: GeoPoint = DEFAULT_CENTER,
    val zoom: Float = DEFAULT_ZOOM,
    val areaPoints: List<GeoPoint> = DEMO_AREA_POINTS,
) : UiState {
    companion object {
        // 기본 좌표: 서울 시청
        val DEFAULT_CENTER = GeoPoint(37.5666, 126.9784)
        const val DEFAULT_ZOOM = 15f

        // 데모용: 서울 시청 인근 임의의 4개 좌표. 둘레 순서가 아닌 의도적으로 섞인 순서로 두어
        // 화면에서 자동 정렬(sortedIntoPolygonOrder)이 동작하는지 확인할 수 있게 한다.
        val DEMO_AREA_POINTS =
            listOf(
                GeoPoint(37.5695, 126.9755), // 북서
                GeoPoint(37.5650, 126.9815), // 남동
                GeoPoint(37.5695, 126.9815), // 북동
                GeoPoint(37.5650, 126.9755), // 남서
            )
    }
}
