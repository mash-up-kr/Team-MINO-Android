@file:OptIn(ExperimentalTime::class)

package team.mino.core.data.repository

import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.data.datasource.DeckRemoteDataSource
import team.mino.core.data.datasource.RoomRemoteDataSource
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.data.repository.mapper.PERSONAL_TYPE_IDENTIFIER
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.domain.model.Deck
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.repository.HomeDeckRepository
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 홈 덱 계약(`docs/specs/home-deck-exploration/contracts/home-ui.md` §4.2)의 구현.
 *
 * 출처가 함수마다 갈리지만 전부 실서버다 — 덱은 [DeckRemoteDataSource], 방 목록은 [RoomRemoteDataSource]가 맡는다.
 * 방 목록은 [RoomRepositoryImpl.getRooms]와 **같은 DataSource·같은 Mapper**를 쓰므로 조회가 두 벌로
 * 갈라지지 않는다. 도메인 계약이 둘로 나뉜 것은
 * 홈이 `pinCount`로 다음 방을 고르는 자기 용도를 §4.2에 명시했기 때문이고, Repository끼리는 의존하지
 * 않는 것이 레이어 규칙이라 재사용은 이 층에서 한다(`core/data/README.md` §6).
 *
 * 예외를 잡지 않는다. 실패는 `MinoDomainException`으로 전파되고 소비는 ViewModel의 몫이다.
 */
internal class HomeDeckRepositoryImpl @Inject constructor(
    private val deckRemoteDataSource: DeckRemoteDataSource,
    private val roomRemoteDataSource: RoomRemoteDataSource,
) : HomeDeckRepository {
    /**
     * 순회 순서를 여기서 확정한다 — 개인방(`type == "personal"`) 먼저, 그다음 방을 만든 지 오래된 순
     * (FR-012, R-014, [계약 §1·§3.1](../../../../../../../docs/specs/home-deck-exploration/contracts/deck-api.md)).
     * `GET /api/v1/rooms` 응답 순서는 계약이 보장하지 않으므로 여기서 매핑 전 원본(DTO)의 `type`·`createdAt`으로
     * 재배치한 뒤에 도메인으로 옮긴다 — 정렬 재료(`createdAt`)가 [RoomSummary]에는 없기 때문이다.
     */
    override suspend fun getRoomSummaries(): List<RoomSummary> =
        roomRemoteDataSource
            .listRooms()
            .sortedWith(
                compareByDescending<RoomSummaryResponse> { it.type == PERSONAL_TYPE_IDENTIFIER }
                    .thenBy { Instant.parse(it.createdAt) },
            ).map { it.toDomain() }

    /**
     * 받은 카드를 **그대로** 담는다. 10장 절단은 서버가 이미 했으므로 여기서 다시 자르지도 정렬하지도 않는다
     * (FR-004, 계약 §2.3).
     */
    override suspend fun getDeck(
        roomId: String,
        sort: DeckSort,
        location: GeoPoint?,
    ): Deck {
        // 좌표 없는 `가까운순`은 요청을 보내지 않고 빈 덱으로 끝낸다 — 위치 권한 거부를 「소진」으로 흡수한다
        // (EC-009, R-013). 계약 §2.1의 400은 이 분기 덕에 정상 경로에서 닿지 않는다.
        if (sort == DeckSort.NEAREST && location == null) {
            return Deck(roomId = roomId, sort = sort, cards = emptyList())
        }
        // 좌표는 `가까운순`에만 실린다(계약 §2.1). 다른 정렬에 딸려 온 좌표는 여기서 떨군다.
        val nearestLocation = location.takeIf { sort == DeckSort.NEAREST }
        val cards =
            deckRemoteDataSource.getCards(
                roomId = roomId,
                sort = sort,
                lat = nearestLocation?.latitude,
                lng = nearestLocation?.longitude,
            )
        return Deck(roomId = roomId, sort = sort, cards = cards.map { it.toDomain() })
    }
}
