package team.mino.feature.placedetail.main.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberUpdatedMarkerState
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColorChipDefaults
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.PinFill
import team.mino.core.domain.model.RoomColor
import team.mino.core.map.MinoMap
import team.mino.core.map.rememberMinoCameraState
import team.mino.core.map.toLatLng
import team.mino.feature.placedetail.main.model.palette

/**
 * 시트 뒤에 깔리는 지도. 카메라를 이 장소에 맞추고 그 자리에 선택 핀 하나를 세운다(spec FR-002).
 *
 * **핀은 [roomColor]가 정해진 뒤에야 그려진다.** 방 대표 색은 핀 상세가 아니라 방 목록에서 오므로 두 조회가
 * 모두 끝나야 값이 선다. 그 전에 임시로 그릴 색은 spec에 근거가 없어 만들지 않고, 색이 설 때까지 핀을 띄우지
 * 않는 것으로 대신한다(`docs/specs/place-detail/research.md` D15).
 *
 * **핀의 외형·치수를 이 화면이 정하지 않는다.** 마커를 실제로 그리는 색상·외형 판별은
 * [SYS-004] 소관이라(spec §3.2) 여기서는 방 색을 입힌 표준 핀 글리프를 그 자리에 세우는 데서 끊는다.
 *
 * **지도 컨트롤을 데리고 있지 않는다.** 그 행은 시트보다 위에 그려져야 시트 그림자에 덮이지 않는데, 여기에
 * 두면 시트보다 아래 레이어가 된다. 그래서 지도와 시트를 함께 놓는 화면이 시트 다음 순서로 얹는다.
 *
 * @param location 카메라 중심이자 핀이 설 자리.
 * @param roomColor 지금 보고 있는 방의 대표 색. `null`인 동안에는 핀을 그리지 않는다.
 */
@Composable
internal fun PlaceDetailMap(
    location: GeoPoint,
    roomColor: RoomColor?,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberMinoCameraState(center = location, zoom = PLACE_ZOOM)
    val markerColor = roomColor?.palette

    MinoMap(
        cameraPositionState = cameraPositionState,
        modifier = modifier.fillMaxSize(),
    ) {
        if (markerColor != null) {
            SelectedPlaceMarker(location = location, color = markerColor)
        }
    }
}

/**
 * 선택 핀 하나.
 *
 * 지도 마커는 컴포저블을 그린 그대로가 아니라 한 장의 비트맵으로 구워 얹히므로, 다시 구울 계기를 [color]로
 * 준다 — 방 색이 늦게 도착해도 그 색으로 다시 구워진다.
 *
 * 크기를 지정하지 않아 글리프가 자기 기본 크기로 선다. 핀의 치수는 [SYS-004]가 정할 값이라(spec §3.2)
 * 여기서 지어내지 않는다.
 */
@Composable
@GoogleMapComposable
private fun SelectedPlaceMarker(
    location: GeoPoint,
    color: MinoRoomColor,
    modifier: Modifier = Modifier,
) {
    val tint = MinoRoomColorChipDefaults.colors(color).selectedContainerColor

    MarkerComposable(
        tint,
        state = rememberUpdatedMarkerState(position = location.toLatLng()),
    ) {
        Icon(
            imageVector = MinoIcons.PinFill,
            contentDescription = null,
            tint = tint,
            modifier = modifier,
        )
    }
}

/**
 * 카메라 배율. 지도 SDK의 값이라 대응하는 디자인 값이 없다 — 장소 하나와 그 주변 골목이 함께 보이는 단계다.
 */
private const val PLACE_ZOOM = 16f
