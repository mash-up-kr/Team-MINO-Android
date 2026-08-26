package team.mino.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import team.mino.core.data.datasource.RoomRemoteDataSource
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.data.repository.mapper.toRequest
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.repository.RoomRepository
import javax.inject.Inject

/**
 * DTO가 밖으로 나가지 않는 경계다. 들어오는 [RoomDraft]는 요청 DTO로, 나가는 응답 DTO는 [Room]으로
 * 여기서 전부 변환된다 (`core/data/README.md` §6).
 *
 * 예외를 잡지 않는다. 없는 방의 404를 포함한 실패는 `MinoDomainException`으로 그대로 전파되고,
 * 소비는 ViewModel의 `runCatchingDomain`이 한다.
 *
 * [RoomRemoteDataSource]가 [getRoom]·[createRoom]·[updateRoom]에 mock([team.mino.core.data.datasource.mock.RoomMockStore])을,
 * `getRooms`(→ [observeMyRooms])에 실서버(`RoomApiService`)를 쓰는 걸 이 클래스는 모른다 — 그래서
 * 서버 전환 때 바뀌지 않는다(`docs/specs/group-room-form/contracts/room-api-mock.md` §4).
 *
 * [getRoom]을 `getRooms()`에서 파생하지 않고 [RoomRemoteDataSource.getRoom]에 직접 위임하는 이유:
 * mock으로 생성·수정한 방은 실서버 목록(`getRooms`)에 나타나지 않으므로, 방금 만든/고친 방을 이 함수로
 * 다시 읽으려면 같은 mock 저장소를 가리켜야 한다. room-list는 이 함수를 쓰지 않아(`observeMyRooms`로
 * 충분) 실서버 목록과의 불일치가 지금 당장 드러나지 않는다 — 실서버가 생성·수정까지 갖추면 이 구분은
 * 자연히 없어진다.
 */
internal class RoomRepositoryImpl @Inject constructor(
    private val remoteDataSource: RoomRemoteDataSource,
) : RoomRepository {
    override fun observeMyRooms(): Flow<List<Room>> =
        flow {
            emit(remoteDataSource.getRooms().map { it.toDomain() })
        }

    override suspend fun getRoom(roomId: String): Room = remoteDataSource.getRoom(roomId).toDomain()

    override suspend fun createRoom(draft: RoomDraft): Room = remoteDataSource.createRoom(draft.toRequest()).toDomain()

    override suspend fun updateRoom(
        roomId: String,
        draft: RoomDraft,
    ): Room = remoteDataSource.updateRoom(roomId, draft.toRequest()).toDomain()
}
