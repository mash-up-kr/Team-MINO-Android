package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.CardResponse
import team.mino.core.data.network.service.DeckApiService
import team.mino.core.domain.model.DeckSort
import javax.inject.Inject

internal class DeckRemoteDataSourceImpl @Inject constructor(
    private val service: DeckApiService,
) : DeckRemoteDataSource {
    override suspend fun getCards(
        roomId: String,
        sort: DeckSort,
        lat: Double?,
        lng: Double?,
    ): List<CardResponse> =
        service.getCards(
            roomId = roomId,
            sort = sort.toQueryValue(),
            lat = lat,
            lng = lng,
        )
}

/**
 * 정렬의 서버 표현 — `docs/specs/home-deck-exploration/contracts/deck-api.md` §2.1.
 *
 * 도메인은 이 문자열을 모르고(`core/domain/README.md` §5) 네트워크 층은 도메인 enum을 모르므로, 둘을 잇는
 * 이 자리가 대응을 아는 유일한 곳이다.
 *
 * `else`를 두지 않아 정렬이 늘면 컴파일이 멈춘다. 임의의 폴백으로 요청이 조용히 다른 덱을 받아 오는 것보다
 * 그 편이 낫다.
 */
private fun DeckSort.toQueryValue(): String =
    when (this) {
        DeckSort.GGUK_PICK -> "ggukPick"
        DeckSort.LATEST -> "latest"
        DeckSort.NEAREST -> "nearby"
    }
