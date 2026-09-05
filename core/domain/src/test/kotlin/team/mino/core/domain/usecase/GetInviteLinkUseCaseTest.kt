package team.mino.core.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import team.mino.core.domain.invite.InviteLinkBuilder
import team.mino.core.domain.model.InvitationPreview
import team.mino.core.domain.repository.RoomInvitationRepository
import java.io.IOException

/**
 * 발급받은 코드가 링크로 조립되어 나오는 경로와, 발급 실패가 **뭉개지지 않고** 올라오는 경로를 고정한다.
 *
 * 실패를 `null`이나 빈 문자열로 수렴시키면 화면이 "링크는 확보됐다"고 오판해 빈 링크를 공유·복사한다
 * (EC-008 · `docs/specs/onboarding-flow/contracts/invite-link.md` §3).
 */
class GetInviteLinkUseCaseTest {
    private val roomInvitationRepository = FakeRoomInvitationRepository()
    private val inviteLinkBuilder = FakeInviteLinkBuilder()
    private val getInviteLink = GetInviteLinkUseCase(roomInvitationRepository, inviteLinkBuilder)

    @Test
    fun `발급받은 코드를 조립기에 넘겨 만든 링크를 돌려준다`() =
        runTest {
            roomInvitationRepository.code = "K7Q2MZ"

            val link = getInviteLink(roomId = "room-1")

            assertEquals("room-1", roomInvitationRepository.requestedRoomId)
            assertEquals("발급받은 코드가 그대로 조립기에 들어가야 한다", "K7Q2MZ", inviteLinkBuilder.receivedCode)
            assertEquals("invite-link-of:K7Q2MZ", link)
        }

    /**
     * 실패 타입이 계약의 `MinoDomainException`이 아닌 평범한 [Throwable]인 것은 의도다 —
     * `:core:domain`은 `:core:error-handling`에 의존하지 않고([ResolveSplashEntryUseCaseTest]와 같은 이유),
     * 이 UseCase에는 `catch`가 없어 실패 타입이 결과를 가르지 않는다. 여기서 고정하는 것은
     * "무엇을 던지든 삼켜서 값으로 바꾸지 않는다"는 전파 경로다.
     */
    @Test
    fun `발급 실패는 빈 링크로 뭉개지지 않고 그대로 전파된다`() =
        runTest {
            val failure = IOException("네트워크 실패")
            roomInvitationRepository.failure = failure
            var thrown: Throwable? = null
            var returned: String? = null

            try {
                returned = getInviteLink(roomId = "room-1")
            } catch (e: Throwable) {
                thrown = e
            }

            assertNull("실패가 null이나 빈 문자열로 수렴하면 안 된다", returned)
            assertSame(failure, thrown)
            assertNull("코드를 못 받았으면 조립도 하지 않는다", inviteLinkBuilder.receivedCode)
        }
}

private class FakeRoomInvitationRepository : RoomInvitationRepository {
    var code: String = "K7Q2MZ"
    var failure: Throwable? = null
    var requestedRoomId: String? = null

    override suspend fun issueInviteCode(roomId: String): String {
        requestedRoomId = roomId
        failure?.let { throw it }
        return code
    }

    override suspend fun previewInvitation(inviteCode: String): InvitationPreview = error("not used in this test")

    override suspend fun joinRoom(
        roomId: String,
        inviteCode: String,
    ) = error("not used in this test")
}

/** 호스트·경로를 아는 것은 `:core:data`의 구현이므로, 여기서는 "코드가 조립기를 거쳤다"만 관찰한다. */
private class FakeInviteLinkBuilder : InviteLinkBuilder {
    var receivedCode: String? = null

    override fun build(inviteCode: String): String {
        receivedCode = inviteCode
        return "invite-link-of:$inviteCode"
    }
}
