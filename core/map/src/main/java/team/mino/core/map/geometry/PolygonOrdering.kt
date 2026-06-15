package team.mino.core.map.geometry

import team.mino.core.common.kotlin.geo.GeoPoint
import kotlin.math.atan2

/**
 * 임의 순서의 좌표들을 무게중심 기준 각도(반시계 방향)로 정렬해 둘레 순서로 재배열한다.
 *
 * Polygon은 넘긴 점 순서대로 변을 잇기 때문에, 정렬하지 않으면 변이 교차해 나비넥타이 형태로
 * 그려진다. 무게중심에서 각 점을 바라본 각도로 정렬하면 (볼록 다각형 기준) 교차 없는 단순
 * 다각형 둘레 순서가 된다.
 */
fun List<GeoPoint>.sortedIntoPolygonOrder(): List<GeoPoint> {
    if (size < 3) return this

    val centerLatitude = sumOf { it.latitude } / size
    val centerLongitude = sumOf { it.longitude } / size

    return sortedBy { atan2(it.latitude - centerLatitude, it.longitude - centerLongitude) }
}
