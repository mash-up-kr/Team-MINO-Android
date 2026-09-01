package team.mino.core.domain.repository

import kotlinx.coroutines.flow.Flow
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomMember
import team.mino.core.domain.model.RoomSummary

/**
 * 방의 목록·조회·생성·수정 계약.
 *
 * room-list(목록 관찰)·group-room-form(생성·편집)·shared-link-receiver(방 선택 시트)가 각자 만들었다가
 * develop 병합 과정에서 합쳐졌다. [observeMyRooms]만 `Flow`를 흘리고 나머지는 1회성 요청이다 — 실패를
 * `Result`로 감싸지 않고 `MinoDomainException`으로 던지며, 취소는 그대로 전파한다.
 */
interface RoomRepository {
    /** 내가 속한 모든 방(개인방 + 공동방)을 실시간 관찰. 개인방은 항상 포함된다. */
    fun observeMyRooms(): Flow<List<Room>>

    /**
     * 참여 중인 방 목록을 가져온다. 방 선택 시트(shared-link-receiver)가 쓰는 얕은 모델이다.
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

    /**
     * 방 멤버 전체 목록. 초대 시트의 참여자 목록과 방장 위임 대상 선택이 함께 소비한다.
     */
    suspend fun getMembers(roomId: String): List<RoomMember>

    /**
     * 내 초대 링크의 코드를 발급한다. 이미 발급했다면 서버가 같은 code를 돌려준다.
     * 완성된 URL이 아니라 code만 오므로, 링크 조립은 호출 측 책임이다.
     */
    suspend fun createInvitation(roomId: String): String

    /**
     * 방에서 나간다. 방장이 다른 멤버가 남은 채로 호출하면 `OWNER_TRANSFER_REQUIRED` 도메인
     * 예외가 던져진다 — 위임을 먼저 요구한다는 뜻이다. 방장이 마지막 1인이면 방이 자동 삭제된다.
     */
    suspend fun leaveRoom(roomId: String)

    /**
     * 방장 권한을 [nextOwnerId]에게 위임한다. 나가기를 완료하려면 성공 후 [leaveRoom]을 이어서
     * 호출해야 한다 — 이 함수만으로는 호출자가 방에 남는다.
     */
    suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    )
}
