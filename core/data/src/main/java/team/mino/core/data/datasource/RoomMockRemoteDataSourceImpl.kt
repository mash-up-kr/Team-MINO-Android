package team.mino.core.data.datasource

import team.mino.core.data.datasource.mock.RoomMockStore
import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomResponse
import javax.inject.Inject

/**
 * 서버가 없는 동안 쓰는 [RoomRemoteDataSource]의 유일한 구현.
 *
 * 지연·식별자 생성·없는 방의 404는 모두 원천인 [RoomMockStore]가 갖는다. 이 클래스는 출처 호출만 한다 —
 * 실서버 구현이 `RoomApiService`에 위임만 하는 것과 같은 모양이어야, 전환 때 바뀌는 곳이
 * `docs/specs/group-room-form/contracts/room-api-mock.md` §4가 적은 세 곳으로 유지된다.
 */
internal class RoomMockRemoteDataSourceImpl @Inject constructor(
    private val store: RoomMockStore,
) : RoomRemoteDataSource {
    override suspend fun getRoom(roomId: String): RoomResponse = store.getRoom(roomId)

    override suspend fun createRoom(request: RoomRequest): RoomResponse = store.createRoom(request)

    override suspend fun updateRoom(
        roomId: String,
        request: RoomRequest,
    ): RoomResponse = store.updateRoom(roomId, request)
}
