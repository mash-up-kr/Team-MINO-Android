package team.mino.core.domain.repository

import kotlinx.coroutines.flow.Flow
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomDraft

/**
 * 방의 조회·생성·수정 계약.
 *
 * room-list(목록 관찰)와 group-room-form(생성·편집)이 각자 만들었다가 develop 병합 과정에서
 * 합쳐졌다. [observeMyRooms]만 `Flow`를 흘리고 나머지 셋은 1회성 요청이다 — 실패를 `Result`로
 * 감싸지 않고 `MinoDomainException`으로 던지며, 취소는 그대로 전파한다.
 */
interface RoomRepository {
    /** 내가 속한 모든 방(개인방 + 공동방)을 실시간 관찰. 개인방은 항상 포함된다. */
    fun observeMyRooms(): Flow<List<Room>>

    /**
     * 방 하나를 가져온다. 편집 폼의 초기값을 채우는 원천이다.
     *
     * room-list는 이 함수를 쓰지 않는다(`observeMyRooms`로 충분) — [team.mino.core.data.repository.RoomRepositoryImpl]
     * 참고.
     */
    suspend fun getRoom(roomId: String): Room

    /**
     * 방을 만들고 만들어진 방을 돌려준다. 반환값은 서버가 부여한 식별자와 소유자를 담는다.
     *
     * [draft]의 `color`가 `null`인 채로 이 함수에 도달하지 않는다 — 미선택을 [team.mino.core.domain.model.RoomColor.GRAY]로
     * 확정하는 것은 `CreateRoomUseCase`의 책임이다.
     */
    suspend fun createRoom(draft: RoomDraft): Room

    /**
     * 방의 내용을 [draft]로 바꾸고 수정된 방을 돌려준다.
     */
    suspend fun updateRoom(
        roomId: String,
        draft: RoomDraft,
    ): Room
}
