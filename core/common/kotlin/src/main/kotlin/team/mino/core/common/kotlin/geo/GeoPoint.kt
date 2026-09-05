package team.mino.core.common.kotlin.geo

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 위경도 좌표를 표현하는 프레임워크 무관 값 객체.
 *
 * 지도(maps-compose의 LatLng) 등 특정 SDK 타입에 의존하지 않으므로 도메인·UI 어디서든 쓸 수 있다.
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

/** 지구 반지름(m) — [distanceMetersTo] 하버사인 계산에 쓴다. */
private const val EARTH_RADIUS_METERS = 6_371_000.0

/**
 * [other]까지의 대권거리(하버사인 공식, 미터). 지도 "거리순" 정렬·"N km 반경" 필터처럼 서버가 모르는
 * 사용자 위치 기준 계산은 전부 클라이언트 몫이라 여기 둔다.
 */
fun GeoPoint.distanceMetersTo(other: GeoPoint): Double {
    val lat1 = Math.toRadians(latitude)
    val lat2 = Math.toRadians(other.latitude)
    val deltaLat = Math.toRadians(other.latitude - latitude)
    val deltaLng = Math.toRadians(other.longitude - longitude)

    val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
        cos(lat1) * cos(lat2) * sin(deltaLng / 2) * sin(deltaLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_METERS * c
}
