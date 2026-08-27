package team.mino.core.domain.repository

import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomSummary

/**
 * 방의 목록·조회·생성·수정 계약.
 *
 * 네 함수 모두 1회성 요청이라 `Flow`를 흘리지 않으며, 실패를 `Result`로 감싸지 않고 `MinoDomainException`으로 던진다.
 * 취소는 그대로 전파한다.
 */
interface RoomRepository {
    /**
     * 참여 중인 방 목록을 가져온다.
     *
     * **정렬 책임을 갖지 않는다** — 받은 순서를 그대로 돌려주고, 개인방을 최상단에 고정하는 판정은
     * `GetRoomPickerRoomsUseCase`가 한다.
     *
     * 실패는 던진다. 빈 목록으로 수렴시키는 것은 화면의 몫이다
     * (`docs/specs/shared-link-receiver/contracts/room-list-api.md` §5).
     */
    suspend fun getRooms(): List<RoomSummary>

    /**
     * 방 하나를 가져온다. 편집 폼의 초기값을 채우는 원천이다.
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
