package team.mino.core.data.repository

import team.mino.core.data.datasource.RoomListRemoteDataSource
import team.mino.core.data.datasource.RoomRemoteDataSource
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.data.repository.mapper.toRequest
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.repository.RoomRepository
import javax.inject.Inject

/**
 * DTO가 밖으로 나가지 않는 경계다. 들어오는 [RoomDraft]는 요청 DTO로, 나가는 응답 DTO는 [Room]·[RoomSummary]로
 * 여기서 전부 변환된다 (`core/data/README.md` §6).
 *
 * 예외를 잡지 않는다. 없는 방의 404를 포함한 실패는 `MinoDomainException`으로 그대로 전파되고,
 * 소비는 ViewModel의 `runCatchingDomain`이 한다.
 *
 * 출처가 함수마다 갈리는 과도기다 — [getRooms]는 [RoomListRemoteDataSource]로 실서버에 붙고, 나머지 셋은
 * [RoomRemoteDataSource]의 mock 바인딩을 문다. 두 DataSource가 언제 하나로 합쳐지는지는
 * `docs/specs/shared-link-receiver/research.md` R-015가 적었다.
 */
internal class RoomRepositoryImpl @Inject constructor(
    private val remoteDataSource: RoomRemoteDataSource,
    private val listRemoteDataSource: RoomListRemoteDataSource,
) : RoomRepository {
    override suspend fun getRooms(): List<RoomSummary> = listRemoteDataSource.listRooms().map { it.toDomain() }

    override suspend fun getRoom(roomId: String): Room = remoteDataSource.getRoom(roomId).toDomain()

    override suspend fun createRoom(draft: RoomDraft): Room = remoteDataSource.createRoom(draft.toRequest()).toDomain()

    override suspend fun updateRoom(
        roomId: String,
        draft: RoomDraft,
    ): Room = remoteDataSource.updateRoom(roomId, draft.toRequest()).toDomain()
}
