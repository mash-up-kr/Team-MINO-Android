package team.mino.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import team.mino.core.data.datasource.PlaceRemoteDataSource
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.domain.model.Place
import team.mino.core.domain.repository.RoomPlacesRepository
import javax.inject.Inject

/**
 * DTO가 밖으로 나가지 않는 경계다. [PlaceRemoteDataSource]가 반환하는 `PinResponse`는 여기서 [Place]로
 * 전부 변환된다(`core/data/README.md` §6).
 *
 * 예외를 잡지 않는다 — `sharePlaces`의 `409 DUPLICATE_PIN_IN_ROOM`을 포함한 실패는 이미
 * `HttpClient`의 `convertDomainException`이 `MinoDomainException`으로 매핑해 던지고, 이 클래스는 그대로
 * 위로 전파한다(`docs/conventions/error_handling.md`).
 */
internal class RoomPlacesRepositoryImpl @Inject constructor(
    private val remoteDataSource: PlaceRemoteDataSource,
) : RoomPlacesRepository {
    override fun observePlaces(roomId: String): Flow<List<Place>> =
        flow {
            emit(remoteDataSource.getPins(roomId).map { it.toDomain() })
        }

    override suspend fun sharePlaces(
        pinId: String,
        targetRoomIds: List<String>,
    ) = remoteDataSource.duplicatePin(pinId, targetRoomIds)

    /**
     * no-op — 대응하는 서버 엔드포인트가 아직 없다(`docs/specs/room-detail/research.md` D14,
     * `docs/specs/room-detail/contracts/place-repository.md` "DTO 갭 대응"). `PlaceRepository` 인터페이스와
     * 도메인 모델은 이 갭 때문에 바꾸지 않고, 백엔드가 엔드포인트를 확정하면 이 구현만 교체한다.
     */
    override suspend fun deletePlace(
        roomId: String,
        pinId: String,
    ) = Unit
}
