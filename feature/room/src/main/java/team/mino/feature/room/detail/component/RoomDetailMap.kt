package team.mino.feature.room.detail.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.Place
import team.mino.core.map.MinoMap
import team.mino.core.map.rememberMinoCameraState
import team.mino.core.map.toLatLng
import team.mino.feature.room.main.component.DefaultMapCenter
import kotlin.math.abs

/** [RoomDetailMap] 기본 줌 레벨. Figma 대조 노드가 지도 줌 값을 변수로 노출하지 않아 실측 근거가 없다 — [RoomListMap]과 동일한 통상 줌 값을 그대로 쓴다. */
private const val DEFAULT_ZOOM = 15f

/** 임시 마커 좌표 산정에 쓰는 최대 오프셋(도 단위, 대략 300m). 실제 장소 좌표가 생기면 이 상수·[mockMarkerCenter]를 통째로 지운다. */
private const val MOCK_MARKER_OFFSET_DEGREES = 0.003

/**
 * 방 상세 화면([SCR-005])의 지도. `:core:map`의 [MinoMap]을 래핑해 그 방이 보유한 [places]만 마커로 얹는다.
 *
 * **알려진 제약**: `core/domain/model/Place.kt`·`docs/specs/room-detail/data-model.md`는 서버 lat/lng를
 * [Place.distanceMeters] 계산에만 쓰고 `Place` 도메인 모델 자체에는 좌표를 노출하지 않는다 — `RoomListMap`이
 * `Room`에 좌표가 없어 방 ID 해시로 좌표를 파생하는 것과 같은 상황이 여기서도 장소 단위로 발생한다. 장소 좌표
 * 계약이 생기기 전까지는 [Place.id] 해시로 파생한 **임시 목데이터 좌표**로 마커를 얹는다 — 장소마다 위치가
 * 안정적으로 고정되어야 화면이 재구성돼도 마커가 튀지 않는다. 실제 장소 좌표 계약이 생기면 [mockMarkerCenter]를
 * 지우고 `Place`가 들고 있는 실좌표로 교체한다.
 *
 * **알려진 제약(핀 디자인)**: US1 대조 노드(Figma `node-id=2400-270425`, "004 방 상세" annotation 페이지)의
 * "4. 장소 핀 마커" 항목은 "클릭 시 장소 상세 바텀 표기 및 디자인 Attention 핀으로 변경(작업 중)"이라는 텍스트
 * 설명뿐이고 실제 핀 이미지·색상 값은 담고 있지 않다. 핀 디자인 커스터마이징은 Figma 대조 후 후속 작업으로 미루고,
 * 지금은 [RoomListMap]과 동일하게 구글맵 표준 [Marker]를 그대로 쓴다.
 */
@Composable
internal fun RoomDetailMap(
    mapCenter: GeoPoint?,
    mapCenterRequestId: Int,
    places: ImmutableList<Place>,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberMinoCameraState(
        center = mapCenter ?: DefaultMapCenter,
        zoom = DEFAULT_ZOOM,
    )

    // rememberMinoCameraState는 최초 컴포지션 시점의 center만 반영한다 — mapCenter가 나중에 바뀌면
    // (예: 위치 권한 허용 후 현재 위치 반영) 카메라를 그 위치로 옮기려면 별도로 반응해야 한다.
    // mapCenter "값"이 아니라 mapCenterRequestId로 키를 잡는다 — GeoPoint는 데이터 클래스라 사용자가
    // GPS 버튼을 다시 눌러도 좌표가 이전과 같으면 값이 바뀌지 않아 LaunchedEffect(mapCenter)로는 카메라가
    // 다시 움직이지 않는다(`RoomListMap`과 같은 버그·같은 조치).
    LaunchedEffect(mapCenterRequestId) {
        mapCenter?.let { center ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(center.toLatLng(), DEFAULT_ZOOM))
        }
    }

    MinoMap(
        cameraPositionState = cameraPositionState,
        modifier = modifier.fillMaxSize(),
    ) {
        places.forEach { place -> PlaceMarker(place) }
    }
}

@Composable
@GoogleMapComposable
private fun PlaceMarker(place: Place) {
    Marker(state = rememberUpdatedMarkerState(position = mockMarkerCenter(place.id).toLatLng()))
}

/** 장소 ID를 시드로 [DefaultMapCenter] 주변에 결정적으로 흩뿌린 임시 좌표. 실제 장소 좌표 도입 시 삭제 대상. */
private fun mockMarkerCenter(placeId: String): GeoPoint {
    val seed = placeId.hashCode()
    val latOffset = (seed % 1000) / 1000.0 * MOCK_MARKER_OFFSET_DEGREES
    val lngOffset = (abs(seed / 1000) % 1000) / 1000.0 * MOCK_MARKER_OFFSET_DEGREES
    return GeoPoint(
        latitude = DefaultMapCenter.latitude + latOffset,
        longitude = DefaultMapCenter.longitude + lngOffset,
    )
}
