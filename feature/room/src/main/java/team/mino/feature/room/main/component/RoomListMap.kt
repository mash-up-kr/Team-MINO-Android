package team.mino.feature.room.main.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.common.ui.component.RoomMapPin
import team.mino.core.map.MinoMap
import team.mino.core.map.rememberMinoCameraState
import team.mino.core.map.toLatLng
import team.mino.feature.room.main.model.MapPinUiModel

/** [RoomListMap] 기본 줌 레벨. Figma 대조 노드가 지도 줌 값을 변수로 노출하지 않아 실측 근거가 없다 — 화면 전체가 보이는 통상 줌 값을 그대로 쓴다. */
private const val DEFAULT_ZOOM = 15f

/** [RoomMapPin] 마커 아이콘 크기. Figma `Pin` 컴포넌트(`node-id=17055-23397`)의 기본 상자(42×48) 비율을 따른다. */
private val PinIconWidth = 32.dp
private val PinIconHeight = 37.dp

/**
 * 방 리스트 탭의 지도. `:core:map`의 [MinoMap]을 래핑해 [mapPins]를 마커로 얹는다.
 *
 * 내가 속한 모든 방(개인 방 + 공동방)에 저장된 장소를 실좌표(`Place.location`, `GET /api/v1/pins`)에
 * 얹는다(PRD 「자신이 저장한 모든 장소를 지도뷰로 볼 수 있다」) — 각 핀의 색은 [RoomListViewModel]이
 * 이미 정해 [MapPinUiModel.color]에 실어 보낸다(개인 방은 내 프로필 색, 공동방은 방 대표 색).
 */
@Composable
internal fun RoomListMap(
    mapCenter: GeoPoint?,
    mapCenterRequestId: Int,
    mapPins: ImmutableList<MapPinUiModel>,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberMinoCameraState(
        center = mapCenter ?: DefaultMapCenter,
        zoom = DEFAULT_ZOOM,
    )

    // rememberMinoCameraState는 최초 컴포지션 시점의 center만 반영한다 — 권한 허용·현재 위치
    // 버튼 클릭으로 mapCenter가 바뀔 때마다 카메라를 그 위치로 옮기려면 별도로 반응해야 한다.
    // mapCenter "값"이 아니라 mapCenterRequestId로 키를 잡는다 — GeoPoint는 데이터 클래스라 사용자가
    // 지도를 옮긴 뒤 같은 위치로 되돌리는 버튼을 다시 누르면 값 자체는 이전과 같아서, mapCenter만
    // 키로 쓰면 LaunchedEffect가 재실행되지 않아 카메라가 안 움직인다(실기기에서 재현된 버그).
    LaunchedEffect(mapCenterRequestId) {
        mapCenter?.let { center ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(center.toLatLng(), DEFAULT_ZOOM))
        }
    }

    MinoMap(
        cameraPositionState = cameraPositionState,
        modifier = modifier.fillMaxSize(),
    ) {
        mapPins.forEach { pin -> PlacePin(pin) }
    }
}

/**
 * 저장된 장소 하나의 마커 — 실좌표에 그 방의 색으로 핀을 얹고, 장소 상세가 열린 핀만 강조 외형으로 그린다.
 *
 * [MarkerComposable]은 content를 비트맵으로 한 번 구워 두고 keys가 달라질 때만 다시 굽는다 — 핀 그림을
 * 고르는 두 값을 키로 넘기지 않으면 [MapPinUiModel.selected]가 뒤집혀도 마커가 이전 그림 그대로 남는다.
 */
@Composable
@GoogleMapComposable
private fun PlacePin(pin: MapPinUiModel) {
    MarkerComposable(
        pin.color to pin.selected,
        state = rememberUpdatedMarkerState(position = pin.place.location.toLatLng()),
    ) {
        RoomMapPin(
            color = pin.color,
            selected = pin.selected,
            modifier = Modifier.size(width = PinIconWidth, height = PinIconHeight),
        )
    }
}

/**
 * [EC-002] 기본 디폴트 좌표 — 강남역(PRD [SYS-004] Flow A "현재 강남역으로 임시 지정, 추후 변경될 수 있음").
 * 위치 권한 미허용 시 [RoomListMap]·[RoomListViewModel][team.mino.feature.room.main.vm.RoomListViewModel]이
 * 함께 참조한다 — 다른 패키지가 참조해야 해서 `internal`(모듈 전체 가시성)로 둔다. 방 상세는 이제 이 지도를
 * 그대로 공유해 쓰므로 별도로 참조하지 않는다(`RoomListRoute`/`RoomDetailRoute` KDoc 참고).
 */
internal val DefaultMapCenter = GeoPoint(latitude = 37.4979, longitude = 127.0276)
