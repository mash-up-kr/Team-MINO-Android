@file:OptIn(ExperimentalTime::class)

package team.mino.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomMember
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomThumbnail
import team.mino.core.domain.repository.RoomRepository
import java.io.IOException
import kotlin.time.ExperimentalTime

/**
 * 미선택 색을 [RoomColor.GRAY]로 확정하는 책임이 이 UseCase에 있는지 본다(FR-006·TS-007).
 *
 * 판정 대상은 **Repository에 넘어간 [RoomDraft]**이지 반환된 [Room]이 아니다. `Room.color`는 nullable이 아니라
 * 어떤 구현이든 색을 갖고 돌아오므로, 반환값만 봐서는 UseCase가 확정한 것인지 Repository·Mapper가 채운 것인지
 * 구분되지 않는다. [FakeRoomRepository]가 색과 무관한 고정 [Room]을 돌려주는 것도 같은 이유다.
 */
class CreateRoomUseCaseTest {
    private val roomRepository = FakeRoomRepository()
    private val createRoom = CreateRoomUseCase(roomRepository = roomRepository)

    @Test
    fun `색을 고르지 않으면 회색으로 확정해 Repository에 넘긴다`() =
        runTest {
            createRoom(RoomDraft(name = "민호야 잘하자", description = "설명", color = null))

            assertEquals(1, roomRepository.createCallCount)
            assertEquals(RoomColor.GRAY, roomRepository.createdDraft?.color)
        }

    @Test
    fun `고른 색은 그대로 Repository에 넘긴다`() =
        runTest {
            createRoom(RoomDraft(name = "민호야 잘하자", description = "설명", color = RoomColor.RED))

            assertEquals(RoomColor.RED, roomRepository.createdDraft?.color)
        }

    @Test
    fun `고를 수 있는 색은 어느 것도 회색으로 바뀌지 않는다`() =
        runTest {
            RoomColor.selectable.forEach { selected ->
                createRoom(RoomDraft(name = "민호야 잘하자", description = "설명", color = selected))

                assertEquals(selected, roomRepository.createdDraft?.color)
            }
        }

    @Test
    fun `색 말고 이름과 설명은 손대지 않고 그대로 넘긴다`() =
        runTest {
            createRoom(RoomDraft(name = "민호야 잘하자", description = "", color = null))

            assertEquals(
                RoomDraft(name = "민호야 잘하자", description = "", color = RoomColor.GRAY),
                roomRepository.createdDraft,
            )
        }

    @Test
    fun `Repository가 돌려준 방을 그대로 반환한다`() =
        runTest {
            val created =
                Room(
                    id = "room-1",
                    name = "민호야 잘하자",
                    description = "설명",
                    color = RoomColor.RED,
                    ownerId = "owner-1",
                    isPersonal = false,
                    placeCount = 0,
                    thumbnail = RoomThumbnail.ColorAndCharacter(color = null),
                    memberSummary = RoomMemberSummary(visibleAvatarUrls = emptyList(), overflowCount = 0),
                    lastPlaceSavedAt = null,
                    commentCount = 0,
                )
            roomRepository.createdRoom = created

            val result = createRoom(RoomDraft(name = "민호야 잘하자", description = "설명", color = RoomColor.RED))

            assertSame(created, result)
        }

    @Test
    fun `Repository가 던진 예외를 잡지 않고 그대로 전파한다`() =
        runTest {
            val failure = IOException("생성 실패")
            roomRepository.createFailure = failure
            var thrown: Throwable? = null

            try {
                createRoom(RoomDraft(name = "민호야 잘하자", description = "설명", color = null))
            } catch (e: Throwable) {
                thrown = e
            }

            assertSame(failure, thrown)
        }
}

/**
 * `:core:domain` 테스트용 [RoomRepository] 테스트 더블.
 *
 * [createdDraft]가 이 더블의 존재 이유다 — 미선택 색이 무엇으로 확정돼 도달했는지는 여기서만 보인다.
 * `:feature:roomform`에도 같은 이유로 `createdDraft`를 노출하는 더블이 있으나 그쪽 테스트 소스셋에 갇혀 있어
 * 이 모듈에서 쓸 수 없다.
 *
 * [createdRoom]은 넘어온 초안과 무관한 고정값이다. 반환값을 초안에서 만들어 주면 "UseCase가 확정했는가"라는
 * 질문이 반환값으로도 답해지는 것처럼 보여 판정 지점이 흐려진다.
 *
 * [createFailure]의 타입이 평범한 [Throwable]인 것은 `SaveProfileUseCaseTest`와 같은 이유다 — 계약이 선언한
 * 실패 타입이 아니라 "UseCase가 아무것도 잡지 않는가"라는 전파 경로만 흔든다.
 */
private class FakeRoomRepository : RoomRepository {
    /** [createRoom]에 마지막으로 들어온 초안. 회색 확정 여부를 판정하는 자리다. */
    var createdDraft: RoomDraft? = null
        private set

    /** [createRoom]이 호출된 횟수. */
    var createCallCount: Int = 0
        private set

    /** [createRoom]이 돌려줄 방. 넘어온 초안을 반영하지 않는다. */
    var createdRoom: Room =
        Room(
            id = "room-1",
            name = "민호야 잘하자",
            description = "설명",
            color = RoomColor.GRAY,
            ownerId = "owner-1",
            isPersonal = false,
            placeCount = 0,
            thumbnail = RoomThumbnail.ColorAndCharacter(color = null),
            memberSummary = RoomMemberSummary(visibleAvatarUrls = emptyList(), overflowCount = 0),
            lastPlaceSavedAt = null,
            commentCount = 0,
        )

    /** 값이 있으면 [createRoom]이 생성 대신 이 예외를 던진다. */
    var createFailure: Throwable? = null

    override fun observeMyRooms(): Flow<List<Room>> = flowOf(listOf(createdRoom))

    override suspend fun getRooms(): List<RoomSummary> = error("CreateRoomUseCase는 getRooms를 부르지 않는다.")

    override suspend fun getRoom(roomId: String): Room = error("CreateRoomUseCase는 getRoom을 부르지 않는다.")

    override suspend fun createRoom(draft: RoomDraft): Room {
        createCallCount++
        createdDraft = draft
        createFailure?.let { throw it }
        return createdRoom
    }

    override suspend fun updateRoom(
        roomId: String,
        draft: RoomDraft,
    ): Room = error("CreateRoomUseCase는 updateRoom을 부르지 않는다.")

    override suspend fun getMembers(roomId: String): List<RoomMember> = error("CreateRoomUseCase는 getMembers를 부르지 않는다.")

    override suspend fun createInvitation(roomId: String): String =
        error("CreateRoomUseCase는 createInvitation을 부르지 않는다.")

    override suspend fun leaveRoom(roomId: String): Unit = error("CreateRoomUseCase는 leaveRoom을 부르지 않는다.")

    override suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    ): Unit = error("CreateRoomUseCase는 transferOwner를 부르지 않는다.")
}
