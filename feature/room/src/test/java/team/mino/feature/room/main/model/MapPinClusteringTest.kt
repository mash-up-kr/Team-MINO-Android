package team.mino.feature.room.main.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class MapPinClusteringTest {
    @Test
    fun `빈 목록은 빈 클러스터 목록이다`() {
        val clusters = emptyList<MapPinUiModel>().toMapPinClusters(zoom = LOW_ZOOM, defaultZoom = DEFAULT_ZOOM)

        assertTrue(clusters.isEmpty())
    }

    @Test
    fun `핀 하나는 크기 1짜리 클러스터 하나다`() {
        val pins = listOf(pin("a", SEOUL, MinoRoomColor.Red))

        val clusters = pins.toMapPinClusters(zoom = LOW_ZOOM, defaultZoom = DEFAULT_ZOOM)

        assertEquals(1, clusters.size)
        assertEquals(1, clusters.single().pins.size)
    }

    @Test
    fun `같은 색 핀이 붙어 있고 줌이 낮으면 하나로 묶인다`() {
        val pins = listOf(
            pin("a", SEOUL, MinoRoomColor.Red),
            pin("b", SEOUL.nudged(), MinoRoomColor.Red),
        )

        val clusters = pins.toMapPinClusters(zoom = LOW_ZOOM, defaultZoom = DEFAULT_ZOOM)

        assertEquals(1, clusters.size)
        assertEquals(2, clusters.single().pins.size)
        assertEquals(MinoRoomColor.Red, clusters.single().color)
    }

    @Test
    fun `방장 색이 다르면 붙어 있어도 묶이지 않는다`() {
        val pins = listOf(
            pin("a", SEOUL, MinoRoomColor.Red),
            pin("b", SEOUL.nudged(), MinoRoomColor.Blue),
        )

        val clusters = pins.toMapPinClusters(zoom = LOW_ZOOM, defaultZoom = DEFAULT_ZOOM)

        assertEquals(2, clusters.size)
        assertTrue(clusters.all { it.pins.size == 1 })
    }

    @Test
    fun `같은 색이어도 충분히 멀면 묶이지 않는다`() {
        val pins = listOf(
            pin("a", SEOUL, MinoRoomColor.Red),
            pin("b", BUSAN, MinoRoomColor.Red),
        )

        val clusters = pins.toMapPinClusters(zoom = LOW_ZOOM, defaultZoom = DEFAULT_ZOOM)

        assertEquals(2, clusters.size)
        assertTrue(clusters.all { it.pins.size == 1 })
    }

    @Test
    fun `줌이 높으면(확대) 같은 자리 핀도 묶이지 않는다`() {
        val pins = listOf(
            pin("a", SEOUL, MinoRoomColor.Red),
            pin("b", SEOUL.nudged(), MinoRoomColor.Red),
        )

        val clusters = pins.toMapPinClusters(zoom = HIGH_ZOOM, defaultZoom = DEFAULT_ZOOM)

        assertEquals(2, clusters.size)
    }

    @Test
    fun `기본 줌에서는 핀이 겹쳐 있어도 클러스터링하지 않는다`() {
        val pins = listOf(
            pin("a", SEOUL, MinoRoomColor.Red),
            pin("b", SEOUL, MinoRoomColor.Red),
        )

        val clusters = pins.toMapPinClusters(zoom = DEFAULT_ZOOM, defaultZoom = DEFAULT_ZOOM)

        assertEquals(2, clusters.size)
        assertTrue(clusters.all { it.pins.size == 1 })
    }

    @Test
    fun `기본 줌에서 살짝만 축소해도 아직 클러스터링하지 않는다`() {
        val pins = listOf(
            pin("a", SEOUL, MinoRoomColor.Red),
            pin("b", SEOUL, MinoRoomColor.Red),
        )

        val clusters = pins.toMapPinClusters(zoom = DEFAULT_ZOOM - 1f, defaultZoom = DEFAULT_ZOOM)

        assertEquals(2, clusters.size)
    }

    private fun pin(
        id: String,
        location: GeoPoint,
        color: MinoRoomColor?,
    ): MapPinUiModel =
        MapPinUiModel(
            place = Place(
                id = id,
                placeId = "place-$id",
                name = id,
                address = "",
                category = PlaceCategoryFilter.ALL,
                thumbnailUrls = emptyList(),
                savedAt = Clock.System.now(),
                commentCount = 0,
                isGgukPick = false,
                distanceMeters = null,
                location = location,
            ),
            color = color,
        )

    /**
     * 약 22m 옮긴 좌표 — 저줌(넓은 클러스터 반경)에서는 겹친 걸로 묶이고, 고줌(좁은 반경)에서는
     * 갈라질 만큼의 거리다(두 값 다 [LOW_ZOOM]·[HIGH_ZOOM]에서의 반경을 계산해 고른 값).
     */
    private fun GeoPoint.nudged(): GeoPoint = copy(latitude = latitude + 0.0002)

    private companion object {
        val SEOUL = GeoPoint(latitude = 37.5665, longitude = 126.9780)
        val BUSAN = GeoPoint(latitude = 35.1796, longitude = 129.0756)

        /** [RoomListMap]의 기본 배율(`DEFAULT_ZOOM`)과 같은 값 — 이 줌에서는 클러스터링하지 않는다. */
        const val DEFAULT_ZOOM = 15f

        /** 기본 줌보다 충분히 축소(클러스터가 형성돼야 하는 줌). */
        const val LOW_ZOOM = 10f

        /** 지도 확대(클러스터가 안 생겨야 하는 줌). */
        const val HIGH_ZOOM = 20f
    }
}
