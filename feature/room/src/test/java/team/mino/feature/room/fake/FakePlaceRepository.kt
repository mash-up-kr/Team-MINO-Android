package team.mino.feature.room.fake

import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.repository.PlaceRepository

/**
 * `:feature:room` 테스트용 [PlaceRepository] 테스트 더블.
 *
 * 지금 이 모듈의 테스트가 쓰는 것은 [getPlaceDetail] 하나다 — 탭 간 장소 상세 요청이 `pinId`로 방을
 * 해석하는 경로(`RoomListViewModel.openRequestedPlaceDetail`)를 위해서다. 채워 두지 않은 핀을 조회하면
 * 조용히 `null`을 돌려주지 않고 던져, 준비를 빠뜨린 테스트가 그 자리에서 드러나게 한다.
 */
internal class FakePlaceRepository : PlaceRepository {
    private var placeDetails: Map<String, PlaceDetail> = emptyMap()

    fun givenPlaceDetail(detail: PlaceDetail) {
        placeDetails = placeDetails + (detail.pinId to detail)
    }

    override suspend fun getPlaceDetail(pinId: String): PlaceDetail =
        placeDetails[pinId] ?: error("FakePlaceRepository에 $pinId 핀 상세가 없다.")

    override suspend fun recordAccess(pinId: String): Unit = error("FakePlaceRepository는 recordAccess를 지원하지 않는다.")

    override suspend fun duplicatePin(
        pinId: String,
        roomIds: List<String>,
    ): Unit = error("FakePlaceRepository는 duplicatePin을 지원하지 않는다.")
}
