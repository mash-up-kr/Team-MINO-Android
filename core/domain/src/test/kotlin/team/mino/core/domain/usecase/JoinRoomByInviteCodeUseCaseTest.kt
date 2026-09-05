package team.mino.core.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import team.mino.core.domain.model.InvitationPreview
import team.mino.core.domain.repository.RoomInvitationRepository
import java.io.IOException

/**
 * 미리보기 → 참여를 순서대로 조합하고, 들어간 방의 id를 돌려주는 경로를 고정한다.
 *
 * 실패는 `null`로 뭉개지 않고 그대로 전파된다(이 저장소의 다른 UseCase와 같은 관례,
 * [GetInviteLinkUseCaseTest] 참고).
 */
class JoinRoomByInviteCodeUseCaseTest {
    private val roomInvitationRepository = FakeJoinRoomInvitationRepository()
    private val joinRoomByInviteCode = JoinRoomByInviteCodeUseCase(roomInvitationRepository)

    @Test
    fun `미리보기의 방 id로 참여하고 그 방 id를 돌려준다`() =
        runTest {
            roomInvitationRepository.preview = InvitationPreview(roomId = "room-9")

            val roomId = joinRoomByInviteCode(inviteCode = "AB12CD")

            assertEquals("AB12CD", roomInvitationRepository.previewRequestedCode)
            assertEquals("room-9", roomInvitationRepository.joinRequestedRoomId)
            assertEquals("AB12CD", roomInvitationRepository.joinRequestedCode)
            assertEquals("room-9", roomId)
        }

    @Test
    fun `미리보기 실패는 뭉개지지 않고 그대로 전파되며 참여를 호출하지 않는다`() =
        runTest {
            val failure = IOException("네트워크 실패")
            roomInvitationRepository.previewFailure = failure
            var thrown: Throwable? = null

            try {
                joinRoomByInviteCode(inviteCode = "AB12CD")
            } catch (e: Throwable) {
                thrown = e
            }

            assertSame(failure, thrown)
            assertNull("미리보기가 실패했으면 참여도 호출되지 않아야 한다", roomInvitationRepository.joinRequestedRoomId)
        }

    @Test
    fun `참여 실패는 뭉개지지 않고 그대로 전파된다`() =
        runTest {
            roomInvitationRepository.preview = InvitationPreview(roomId = "room-9")
            val failure = IOException("네트워크 실패")
            roomInvitationRepository.joinFailure = failure
            var thrown: Throwable? = null

            try {
                joinRoomByInviteCode(inviteCode = "AB12CD")
            } catch (e: Throwable) {
                thrown = e
            }

            assertSame(failure, thrown)
        }
}

private class FakeJoinRoomInvitationRepository : RoomInvitationRepository {
    var preview: InvitationPreview = InvitationPreview(roomId = "room-1")
    var previewFailure: Throwable? = null
    var previewRequestedCode: String? = null

    var joinFailure: Throwable? = null
    var joinRequestedRoomId: String? = null
    var joinRequestedCode: String? = null

    override suspend fun issueInviteCode(roomId: String): String = error("not used in this test")

    override suspend fun previewInvitation(inviteCode: String): InvitationPreview {
        previewRequestedCode = inviteCode
        previewFailure?.let { throw it }
        return preview
    }

    override suspend fun joinRoom(
        roomId: String,
        inviteCode: String,
    ) {
        joinRequestedRoomId = roomId
        joinRequestedCode = inviteCode
        joinFailure?.let { throw it }
    }
}
