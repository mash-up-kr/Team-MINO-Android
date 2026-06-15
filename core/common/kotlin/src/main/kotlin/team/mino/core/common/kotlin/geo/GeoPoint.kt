package team.mino.core.common.kotlin.geo

/**
 * 위경도 좌표를 표현하는 프레임워크 무관 값 객체.
 *
 * 지도(maps-compose의 LatLng) 등 특정 SDK 타입에 의존하지 않으므로 도메인·UI 어디서든 쓸 수 있다.
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)
