package team.mino.feature.placedetail

import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route

/**
 * 장소 상세 화면의 시작 라우트이자 이 feature의 유일한 라우트.
 *
 * `PlaceDetailActivity`가 `intent.getStringExtra(EXTRA_PLACE_DETAIL_PIN_ID)`로 읽은 값을 실어 만들고,
 * `PlaceDetailViewModel`이 `savedStateHandle.toRoute<PlaceDetailMain>()`으로 복원한다.
 *
 * @param pinId 열려는 장소 핀의 ID.
 */
@Serializable
internal data class PlaceDetailMain(val pinId: String) : Route
