package team.mino.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import team.mino.core.data.datasource.RoomRemoteDataSource
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.data.repository.mapper.toRequest
import team.mino.core.data.repository.mapper.toRoomListDomain
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomMember
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
 * [observeMyRooms]와 [getRooms]는 같은 [RoomRemoteDataSource.listRooms] 응답을 서로 다른 도메인
 * 모델로 읽는다 — room-list는 풍부한 [Room](`RoomMapper.toRoomListDomain`)을, 방 선택 시트는 얕은
 * [RoomSummary](`RoomSummaryMapper.toDomain`)를 쓴다.
 */
internal class RoomRepositoryImpl @Inject constructor(
    private val remoteDataSource: RoomRemoteDataSource,
) : RoomRepository {
    override fun observeMyRooms(): Flow<List<Room>> =
        flow {
            emit(remoteDataSource.listRooms().map { it.toRoomListDomain() })
        }

    override suspend fun getRooms(): List<RoomSummary> = remoteDataSource.listRooms().map { it.toDomain() }

    override suspend fun getRoom(roomId: String): Room = remoteDataSource.getRoom(roomId).toDomain()

    override suspend fun createRoom(draft: RoomDraft): Room = remoteDataSource.createRoom(draft.toRequest()).toDomain()

    override suspend fun updateRoom(
        roomId: String,
        draft: RoomDraft,
    ): Room = remoteDataSource.updateRoom(roomId, draft.toRequest()).toDomain()

    override suspend fun getMembers(roomId: String): List<RoomMember> =
        remoteDataSource.getMembers(roomId).map {
            it.toDomain()
        }

    override suspend fun createInvitation(roomId: String): String = remoteDataSource.createInvitation(roomId).code

    override suspend fun leaveRoom(roomId: String) {
        remoteDataSource.leaveRoom(roomId)
    }

    override suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    ) {
        remoteDataSource.transferOwner(roomId, nextOwnerId)
    }
}
