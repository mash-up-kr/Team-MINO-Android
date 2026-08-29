package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.CardResponse
import team.mino.core.domain.model.DeckSort

/**
 * 홈 카드 덱의 원격 출처. 시그니처는 `docs/specs/home-deck-exploration/contracts/deck-api.md` §4가 소유한다.
 *
 * `GET /api/v1/rooms/{roomId}/cards`가 배포되면서 mock에서 실구현으로 갈아 끼웠고, 예고한 대로 이 인터페이스는
 * 바뀌지 않고 바인딩 대상만 바뀌었다 — 같은 형태를 [RoomRemoteDataSource]가 먼저 통과했다(research.md R-001).
 *
 * 좌표를 [team.mino.core.common.kotlin.geo.GeoPoint]가 아니라 두 개의 `Double?`로 받는 이유는, 이 자리가
 * 질의 파라미터 `lat`·`lng`를 그대로 옮긴 자리이기 때문이다. 도메인 값과의 대응은 `HomeDeckRepositoryImpl`이 안다.
 */
internal interface DeckRemoteDataSource {
    /**
     * `GET /api/v1/rooms/{roomId}/cards`를 호출한다.
     *
     * 응답은 **서버가 최대 10장으로 잘라 준 것**이며 후보가 적으면 그만큼 짧다. 순서는 후보 순위 그대로이므로
     * 호출자가 다시 자르거나 정렬하지 않는다(계약 §2.3).
     *
     * [sort]가 [DeckSort.NEAREST]인데 [lat]·[lng]가 없으면 서버가 `400`을 준다. 홈은 그 경우 요청 자체를
     * 보내지 않으므로(EC-009, R-013) 이 실패는 호출부의 버그를 드러내는 용도다.
     */
    suspend fun getCards(
        roomId: String,
        sort: DeckSort,
        lat: Double? = null,
        lng: Double? = null,
    ): List<CardResponse>
}
