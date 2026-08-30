package team.mino.core.data.repository

import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.data.datasource.DeckRemoteDataSource
import team.mino.core.data.datasource.PinRemoteDataSource
import team.mino.core.data.datasource.RoomRemoteDataSource
import team.mino.core.data.network.dto.request.PinDuplicateRequest
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.domain.model.Deck
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.repository.HomeDeckRepository
import javax.inject.Inject

/**
 * 홈 덱 계약(`docs/specs/home-deck-exploration/contracts/home-ui.md` §4.2)의 구현.
 *
 * 출처가 함수마다 갈리지만 전부 실서버다 — 덱은 [DeckRemoteDataSource], 방 목록은 [RoomRemoteDataSource],
 * 두 확인 이벤트는 [PinRemoteDataSource]가 맡는다.
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
    private val pinRemoteDataSource: PinRemoteDataSource,
) : HomeDeckRepository {
    override suspend fun getRoomSummaries(): List<RoomSummary> = roomRemoteDataSource.listRooms().map { it.toDomain() }

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

    /**
     * `POST /api/v1/pins/{pinId}/accesses` 하나로 끝난다 — 계약 §3.2. 출처 구분자가 없어 홈에서 부르든
     * 다른 화면에서 부르든 같은 요청이다.
     *
     * 실패를 삼키지 않는다. 결과를 기다리지 않고 화면을 전환하는 것은 호출자의 판정이므로(R-012) 여기서
     * 미리 성공으로 만들어 두면 그 판정을 호출자에게서 뺏는다.
     */
    override suspend fun recordPlaceOpened(pinId: String) = pinRemoteDataSource.recordAccess(pinId)

    /**
     * `POST /api/v1/pins/{pinId}/duplicate` — 계약 §3.3. 계약의 본문은 방 배열이고 도메인은 방 하나를
     * 받으므로 여기서 한 칸짜리 배열로 감싼다. 여러 방 저장은 spec §3.2가 비목표로 둔 범위다.
     *
     * 대상 방에 같은 장소가 있을 때의 `409`를 잡지 않는다 — 저장되지 않은 것이므로 `MinoDomainException`이
     * 그대로 올라가 스낵바가 돼야 한다(FR-005, `docs/conventions/error_handling.md` §5).
     */
    override suspend fun savePinToRoom(
        pinId: String,
        roomId: String,
    ) = pinRemoteDataSource.duplicatePin(
        pinId = pinId,
        request = PinDuplicateRequest(roomIds = listOf(roomId)),
    )
}
