package team.mino.core.domain.repository

import kotlinx.coroutines.flow.Flow
import team.mino.core.domain.model.Room

interface RoomRepository {
    /** 내가 속한 모든 방(개인방 + 공동방)을 실시간 관찰. 개인방은 항상 포함된다. */
    fun observeMyRooms(): Flow<List<Room>>

    /** 단건 조회 — 방 상세 진입 시 캐시 미스 등 필요할 때만. 목록 화면은 observeMyRooms로 충분. */
    suspend fun getRoom(roomId: String): Room
}
