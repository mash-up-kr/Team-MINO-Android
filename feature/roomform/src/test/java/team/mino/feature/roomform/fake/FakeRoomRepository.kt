@file:OptIn(ExperimentalTime::class)

package team.mino.feature.roomform.fake

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomMember
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomThumbnail
import team.mino.core.domain.repository.RoomRepository
import team.mino.core.errorhandling.MinoDomainException
import kotlin.time.ExperimentalTime

/**
 * `:feature:roomform` 테스트용 [RoomRepository] 테스트 더블.
 *
 * 세 함수 각각에 대해 **성공 값 · 실패 · 응답을 붙잡아 두는 관문 · 호출 횟수**를 테스트가 지정하고 읽는다.
 * 관문과 호출 횟수가 함께 있어야 "제출 중 재클릭이 요청을 늘리지 않는다"(EC-008·SC-005)를 판정할 수 있다 —
 * 관문으로 첫 요청을 붙잡아 둔 채 두 번째를 보내고, 카운트가 그대로인지 본다.
 *
 * 실패 타입이 [MinoDomainException]인 것은 편의가 아니라 계약이다. 조회 실패는 `UiState.loadError`가
 * 이 타입만 담고, 제출 실패는 ViewModel의 `runCatchingDomain`이 이 타입만 잡는다 — 다른 예외를 주입하면
 * 판정하려던 통로가 아니라 CEH로 빠진다.
 *
 * `:core:data`의 실서버 DataSource에는 실패 주입 스위치가 없다(research.md R-002). 실패 경로의 검증은
 * 프로덕션 분기가 아니라 이 더블의 몫이다.
 */
internal class FakeRoomRepository : RoomRepository {
    /** [createRoom]·[updateRoom]이 돌려주는 [Room]의 소유자. */
    var ownerId: String = "owner-id"

    /** [createRoom]이 성공했을 때 서버가 부여한 셈 치는 식별자. `Finish(Created(roomId))`의 기대값이다. */
    var newRoomId: String = "new-room-id"

    private var storedRoom: Room? = null

    /** [getRoom]이 성공할 때 돌려줄 방을 채워 둔다. */
    fun givenRoom(room: Room) {
        storedRoom = room
    }

    /** [getRoom]이 호출된 횟수. 재시도가 실제로 다시 조회하는지 보는 데 쓴다. */
    var getCallCount: Int = 0
        private set

    /** [getRoom]에 마지막으로 들어온 `roomId`. 조회가 없었으면 `null`이다. */
    var requestedRoomId: String? = null
        private set

    /** 값이 있으면 [getRoom]이 조회 결과 대신 이 예외를 던진다 — 편집 진입 조회 실패(EC-014). */
    var getFailure: MinoDomainException? = null

    /** 값이 있으면 [getRoom]이 이것이 완료될 때까지 멈춘다 — 로딩 중 상태를 붙잡아 둔다. */
    var getGate: CompletableDeferred<Unit>? = null

    /** [createRoom]이 호출된 횟수. */
    var createCallCount: Int = 0
        private set

    /** [createRoom]에 마지막으로 들어온 초안. 미선택 색이 무엇으로 확정돼 도달했는지도 여기서 본다. */
    var createdDraft: RoomDraft? = null
        private set

    /** 값이 있으면 [createRoom]이 생성 대신 이 예외를 던진다 — 생성 요청 실패(EC-009). */
    var createFailure: MinoDomainException? = null

    /** 값이 있으면 [createRoom]이 이것이 완료될 때까지 멈춘다 — 제출 중(`isSubmitting`) 상태를 붙잡아 둔다. */
    var createGate: CompletableDeferred<Unit>? = null

    /** [updateRoom]이 호출된 횟수. */
    var updateCallCount: Int = 0
        private set

    /** [updateRoom]에 마지막으로 들어온 `roomId`. 편집이 없었으면 `null`이다. */
    var updatedRoomId: String? = null
        private set

    /** [updateRoom]에 마지막으로 들어온 초안. */
    var updatedDraft: RoomDraft? = null
        private set

    /** 값이 있으면 [updateRoom]이 수정 대신 이 예외를 던진다 — 편집 요청 실패(EC-014). */
    var updateFailure: MinoDomainException? = null

    /** 값이 있으면 [updateRoom]이 이것이 완료될 때까지 멈춘다. */
    var updateGate: CompletableDeferred<Unit>? = null

    override fun observeMyRooms(): Flow<List<Room>> = flowOf(listOfNotNull(storedRoom))

    /**
     * 이 모듈의 테스트는 방 목록을 쓰지 않는다. 조용히 빈 목록을 돌려주면 "방이 없다"와 구분되지 않으므로,
     * 호출되면 그 자리에서 드러나도록 둔다.
     */
    override suspend fun getRooms(placeId: String?): List<RoomSummary> =
        error("FakeRoomRepository는 목록 조회를 지원하지 않는다. 필요해지면 응답을 지정하는 자리를 먼저 만든다.")

    override suspend fun getRoom(roomId: String): Room {
        getCallCount++
        requestedRoomId = roomId
        getGate?.await()
        getFailure?.let { throw it }
        return checkNotNull(storedRoom) {
            "givenRoom()으로 조회 결과를 채우거나 getFailure를 지정한 뒤에 getRoom을 부른다."
        }
    }

    override suspend fun createRoom(draft: RoomDraft): Room {
        createCallCount++
        createdDraft = draft
        createGate?.await()
        createFailure?.let { throw it }
        return draft.toRoom(id = newRoomId)
    }

    override suspend fun updateRoom(
        roomId: String,
        draft: RoomDraft,
    ): Room {
        updateCallCount++
        updatedRoomId = roomId
        updatedDraft = draft
        updateGate?.await()
        updateFailure?.let { throw it }
        return draft.toRoom(id = roomId)
    }

    /**
     * 저장된 방은 색이 이미 확정된 상태이므로 [Room.color]가 nullable이 아니다. 여기서 `null`을
     * [RoomColor.GRAY]로 채우는 것은 반환값을 만들기 위한 것일 뿐, 확정 규칙의 판정 대상은
     * 이 결과가 아니라 [createdDraft]에 담긴 색이다.
     */
    override suspend fun getMembers(roomId: String): List<RoomMember> =
        error("FakeRoomRepository(:feature:roomform)는 getMembers를 지원하지 않는다.")

    override suspend fun createInvitation(roomId: String): String =
        error("FakeRoomRepository(:feature:roomform)는 createInvitation을 지원하지 않는다.")

    override suspend fun leaveRoom(roomId: String): Unit =
        error("FakeRoomRepository(:feature:roomform)는 leaveRoom을 지원하지 않는다.")

    override suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    ): Unit = error("FakeRoomRepository(:feature:roomform)는 transferOwner를 지원하지 않는다.")

    private fun RoomDraft.toRoom(id: String): Room =
        Room(
            id = id,
            name = name,
            description = description,
            color = color ?: RoomColor.GRAY,
            ownerId = ownerId,
            isPersonal = false,
            placeCount = 0,
            thumbnail = RoomThumbnail.ColorAndCharacter(color = null),
            memberSummary = RoomMemberSummary(visibleAvatars = emptyList(), overflowCount = 0),
            lastPlaceSavedAt = null,
            commentCount = 0,
        )
}
