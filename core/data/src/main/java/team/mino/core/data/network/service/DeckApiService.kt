package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import team.mino.core.data.network.dto.response.CardFeedResponse
import team.mino.core.data.network.dto.response.CardResponse
import team.mino.core.data.network.dto.response.MinoResponse
import javax.inject.Inject

/**
 * 홈 카드 덱 엔드포인트를 호출하는 서비스.
 *
 * 신원 증명 헤더는 `MinoIdentityProofPlugin`이 싣고, 예외는 `convertDomainException`이
 * `MinoDomainException`으로 바꿔 던지므로 여기서 잡지 않는다.
 */
internal class DeckApiService @Inject constructor(
    private val client: HttpClient,
) {
    /**
     * [roomId] 방의 덱을 조회한다. 계약은
     * `docs/specs/home-deck-exploration/contracts/deck-api.md` §2가 소유한다.
     *
     * [sort]는 도메인 enum이 아니라 서버 문자열(`ggukPick`·`latest`·`nearby`)이다 — 대응을 아는 것은
     * `DeckRemoteDataSourceImpl`이고, 네트워크 층은 질의 파라미터를 그대로 싣기만 한다.
     *
     * 좌표는 `sort=nearby`에만 실린다. `null`이면 Ktor의 [parameter]가 그 항목을 URL에서 빼므로
     * 좌표 유무로 분기하지 않는다 — 좌표 없는 `nearby`를 아예 보내지 않는 판정은 `HomeDeckRepositoryImpl`이
     * 이미 했고(EC-009), 여기까지 온 요청은 계약을 지킨 것이다.
     *
     * 최대 10장 절단과 후보 순위 유지는 서버가 한다. 받은 배열을 자르지도 정렬하지도 않는다(계약 §2.3).
     */
    suspend fun getCards(
        roomId: String,
        sort: String,
        lat: Double? = null,
        lng: Double? = null,
    ): List<CardResponse> =
        client
            .get("api/v1/rooms/$roomId/cards") {
                parameter("sort", sort)
                parameter("lat", lat)
                parameter("lng", lng)
            }.body<MinoResponse<CardFeedResponse>>()
            .data
            .cards
}
