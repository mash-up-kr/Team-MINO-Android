package team.mino.feature.room.main.model

import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.common.kotlin.geo.distanceMetersTo
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import kotlin.math.cos
import kotlin.math.pow

/**
 * 지도 위에서 겹치는 핀을 하나로 묶은 결과(PRD Flow C, `docs/prd/business-context.md`
 * "지도 축소 시: 핀이 겹치는 구간을 클러스터로 묶는다").
 *
 * [pins]가 1개면 클러스터링되지 않은 낱개 핀이다 — 호출부([RoomListMap])가 그 경우
 * [team.mino.core.common.ui.component.RoomMapPin]으로, 2개 이상이면
 * [team.mino.core.common.ui.component.RoomMapClusterPin]으로 그린다.
 */
internal data class MapPinCluster(
    val center: GeoPoint,
    val color: MinoRoomColor?,
    val pins: List<MapPinUiModel>,
)

/**
 * [zoom]에서 화면상 [CLUSTER_PIXEL_RADIUS]px 이내로 겹치는 **같은 방(색)** 핀만 하나로 묶는다.
 *
 * **기본 줌([defaultZoom], 방 상세·리스트 진입 시 배율)에서는 절대 클러스터링하지 않는다** — 그
 * 줌보다 [CLUSTERING_ZOOM_OUT_MARGIN] 이상 축소해야 비로소 묶기 시작한다(실기기 확인 — 픽셀 반경만
 * 줄이는 것으로는 기본 배율에서 핀이 붙어 있는 흔한 경우까지 묶여, "지도 축소 시"라는 PRD 문구와
 * 어긋났다). 그 밑에서는 [CLUSTER_PIXEL_RADIUS]px 반경으로 계속 묶는다.
 *
 * **다른 방의 핀은 아무리 가까워도 묶지 않는다**(리드 확인) — 클러스터 원이 방 대표색 팔레트를 그대로
 * 쓰는 Figma 시안(node `2392-128633`~`2392-128643`, `RoomColor` 12색과 1:1 대응)이 성립하려면
 * 클러스터 하나가 단일 색이어야 한다. 그래서 [color]별로 먼저 나눈 뒤 그 안에서만 묶는다.
 *
 * 화면 픽셀 반경을 위경도 거리로 바꾸는 데 Web Mercator 근사식([metersPerPixelAt])을 쓴다 — 완전한
 * 화면 투영이 아니라 "이 위도·줌에서 픽셀 하나가 실제로 몇 미터인지"의 근사치다. 이 앱이 다루는
 * 위도 범위(한국)에서는 오차가 무시할 만하다.
 *
 * 그리디 방식이다 — 핀을 순서대로 훑어 반경 안에 이미 생긴 클러스터가 있으면 합류시키고, 없으면 새
 * 클러스터를 연다. 클러스터 중심은 그 클러스터의 첫 핀 위치로 고정해 핀이 늘어도 흔들리지 않는다.
 * 완전한 최근접 클러스터링(k-d tree 등)은 아니라 경계에 걸친 두 핀이 서로 다른 클러스터로 남을 수
 * 있지만, 방 하나에 저장되는 핀 규모(수십~수백)에서는 체감 차이가 없다.
 */
internal fun List<MapPinUiModel>.toMapPinClusters(
    zoom: Float,
    defaultZoom: Float,
): List<MapPinCluster> {
    if (isEmpty()) return emptyList()
    if (zoom > defaultZoom - CLUSTERING_ZOOM_OUT_MARGIN) {
        return map { MapPinCluster(center = it.place.location, color = it.color, pins = listOf(it)) }
    }

    val metersPerPixel = metersPerPixelAt(zoom, latitude = first().place.location.latitude)
    val radiusMeters = metersPerPixel * CLUSTER_PIXEL_RADIUS

    return groupBy { it.color }
        .flatMap { (color, pinsOfColor) -> pinsOfColor.greedyCluster(color, radiusMeters) }
}

/**
 * [color]는 이 함수 안에서는 고정값이다(호출부가 이미 `groupBy { it.color }`로 나눠 넘긴다) — 그래서
 * 누적 중에는 중심 좌표·핀 목록 쌍만 들고 있다가, 다 묶은 뒤에 마지막에 한 번만 [color]를 붙인다.
 */
private fun List<MapPinUiModel>.greedyCluster(
    color: MinoRoomColor?,
    radiusMeters: Double,
): List<MapPinCluster> {
    val clusters = mutableListOf<Pair<GeoPoint, MutableList<MapPinUiModel>>>()
    for (pin in this) {
        val joined = clusters.firstOrNull { (center, _) -> center.distanceMetersTo(pin.place.location) <= radiusMeters }
        if (joined != null) {
            joined.second += pin
        } else {
            clusters += pin.place.location to mutableListOf(pin)
        }
    }
    return clusters.map { (center, pins) -> MapPinCluster(center = center, color = color, pins = pins) }
}

/** [zoom]·[latitude]에서 화면 1px가 나타내는 실제 거리(m) — Web Mercator 근사(적도 기준 상수). */
private fun metersPerPixelAt(
    zoom: Float,
    latitude: Double,
): Double = EARTH_EQUATOR_METERS_PER_PIXEL_AT_ZOOM_0 * cos(Math.toRadians(latitude)) / 2.0.pow(zoom.toDouble())

private const val EARTH_EQUATOR_METERS_PER_PIXEL_AT_ZOOM_0 = 156_543.03392

/**
 * 이 픽셀 반경 안의 같은 색 핀을 하나의 클러스터로 묶는다. [TBD] 정확한 값은 실기기 대조가 필요하다.
 *
 * 50px → 25px로 줄였는데도 실기기에서 살짝만 축소해도 여전히 너무 쉽게 묶였다 — 그래서 반경을 더
 * 줄이는 대신 [CLUSTERING_ZOOM_OUT_MARGIN]으로 "기본 배율에서는 아예 클러스터링하지 않는다"는
 * 문턱을 따로 뒀다(반경만으로는 기본 배율의 흔한 핀 간격까지 묶여 문제가 근본적으로 안 풀렸다).
 */
private const val CLUSTER_PIXEL_RADIUS = 12.0

/**
 * [defaultZoom]에서 이만큼 더 축소해야(줌 값이 작아져야) 클러스터링이 시작된다. [TBD] 정확한 값은
 * 실기기 대조가 필요하다 — 지도 줌은 한 단계에 보이는 영역이 약 2배씩 늘어나므로, 이 값을 올릴수록
 * "많이 축소해야 묶인다"는 체감이 뚜렷해진다.
 */
private const val CLUSTERING_ZOOM_OUT_MARGIN = 3f
