package team.mino.core.data.datasource

import team.mino.core.data.datasource.mock.DeckMockStore
import team.mino.core.data.network.dto.response.CardResponse
import team.mino.core.domain.model.DeckSort
import javax.inject.Inject

/**
 * `GET /api/v1/rooms/{roomId}/cards`가 배포될 때까지 쓰는 [DeckRemoteDataSource]의 유일한 구현.
 *
 * 시드·지연·10장 절단·좌표 없는 `nearby`의 400은 모두 원천인 [DeckMockStore]가 갖는다. 이 클래스는 출처 호출만
 * 한다 — 실서버 구현이 `ApiService`에 위임만 하는 것과 같은 모양이어야, 전환 때 바뀌는 곳이
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §4가 적은 세 곳으로 유지된다.
 */
internal class DeckMockRemoteDataSourceImpl @Inject constructor(
    private val store: DeckMockStore,
) : DeckRemoteDataSource {
    override suspend fun getCards(
        roomId: String,
        sort: DeckSort,
        lat: Double?,
        lng: Double?,
    ): List<CardResponse> = store.getCards(roomId, sort, lat, lng)
}
