package team.mino.feature.room.main.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.Room
import team.mino.core.map.MinoMap
import team.mino.core.map.rememberMinoCameraState
import team.mino.core.map.toLatLng
import kotlin.math.abs

/** [RoomListMap] 기본 줌 레벨. Figma 대조 노드가 지도 줌 값을 변수로 노출하지 않아 실측 근거가 없다 — 화면 전체가 보이는 통상 줌 값을 그대로 쓴다. */
private const val DEFAULT_ZOOM = 15f

/** 임시 마커 좌표 산정에 쓰는 최대 오프셋(도 단위, 대략 300m). 실제 장소 좌표가 생기면 이 상수·[mockMarkerCenter]를 통째로 지운다. */
private const val MOCK_MARKER_OFFSET_DEGREES = 0.003

/**
 * 방 리스트 탭의 지도. `:core:map`의 [MinoMap]을 래핑해 [personalRoom]·[groupRooms]가 보유한 장소를 마커로 얹는다.
 *
 * **알려진 제약**: `core/domain/model/Room.kt`·[docs/specs/room-list/data-model.md]는 `Place`를 별도 spec 소유로 두고
 * 집계값(`placeCount` 등)만 노출한다 — 장소별 좌표(`GeoPoint`) 계약이 아직 없다(T029 완료 보고, 리드 확인 완료).
 * 백엔드가 장소 좌표를 내려주기 전까지 방 ID 해시로 파생한 **임시 목데이터 좌표**로 마커를 얹는다 — 방마다 위치가
 * 안정적으로 고정되어야 화면이 재구성돼도 마커가 튀지 않는다. 실제 장소 좌표 계약이 생기면 [mockMarkerCenter]를
 * 지우고 `Room`(또는 `Place`)이 들고 있는 실좌표로 교체한다.
 */
@Composable
internal fun RoomListMap(
    mapCenter: GeoPoint?,
    personalRoom: Room?,
    groupRooms: ImmutableList<Room>,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberMinoCameraState(
        center = mapCenter ?: DefaultMapCenter,
        zoom = DEFAULT_ZOOM,
    )

    MinoMap(
        cameraPositionState = cameraPositionState,
        modifier = modifier.fillMaxSize(),
    ) {
        personalRoom?.let { RoomMarker(it) }
        groupRooms.forEach { room -> RoomMarker(room) }
    }
}

@Composable
@GoogleMapComposable
private fun RoomMarker(room: Room) {
    if (room.placeCount > 0) {
        Marker(state = rememberUpdatedMarkerState(position = mockMarkerCenter(room.id).toLatLng()))
    }
}

/** 방 ID를 시드로 [DefaultMapCenter] 주변에 결정적으로 흩뿌린 임시 좌표. 실제 장소 좌표 도입 시 삭제 대상. */
private fun mockMarkerCenter(roomId: String): GeoPoint {
    val seed = roomId.hashCode()
    val latOffset = (seed % 1000) / 1000.0 * MOCK_MARKER_OFFSET_DEGREES
    val lngOffset = (abs(seed / 1000) % 1000) / 1000.0 * MOCK_MARKER_OFFSET_DEGREES
    return GeoPoint(
        latitude = DefaultMapCenter.latitude + latOffset,
        longitude = DefaultMapCenter.longitude + lngOffset,
    )
}

/** [EC-002] 기본 디폴트 좌표 — 강남역(PRD [SYS-004] Flow A "현재 강남역으로 임시 지정, 추후 변경될 수 있음"). 위치 권한 미허용 시 [RoomListMap]·ViewModel이 함께 참조한다. */
internal val DefaultMapCenter = GeoPoint(latitude = 37.4979, longitude = 127.0276)
