package team.mino.core.map

import com.google.android.gms.maps.model.LatLng
import team.mino.core.common.kotlin.geo.GeoPoint

/**
 * 프레임워크 무관 [GeoPoint]를 maps-compose의 [LatLng]로 변환한다.
 *
 * GeoPoint↔LatLng 변환을 이 모듈 경계에서만 수행해, feature는 GeoPoint로만 좌표를 다루게 한다.
 */
fun GeoPoint.toLatLng(): LatLng = LatLng(latitude, longitude)
