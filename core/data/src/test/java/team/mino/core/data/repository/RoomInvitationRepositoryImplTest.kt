package team.mino.core.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import team.mino.core.data.datasource.InvitationRemoteDataSource
import team.mino.core.data.network.dto.response.InvitationResponse
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

/**
 * DTO가 이 클래스 밖으로 나가지 않는 경계다 — 나가는 것은 [InvitationResponse]가 아니라 코드 문자열이다.
 *
 * 계약은 `docs/specs/onboarding-flow/contracts/invite-link.md` §2가 소유한다.
 */
class RoomInvitationRepositoryImplTest {
    private val remoteDataSource = FakeInvitationRemoteDataSource()
    private val repository = RoomInvitationRepositoryImpl(remoteDataSource)

    @Test
    fun `발급 응답의 코드를 그대로 돌려준다`() =
        runTest {
            remoteDataSource.response = InvitationResponse(code = "K7Q2MZ")

            val code = repository.issueInviteCode(roomId = "room-1")

            assertEquals("room-1", remoteDataSource.requestedRoomId)
            assertEquals("K7Q2MZ", code)
        }

    /** 실패를 `null`이나 빈 문자열로 뭉개지 않는다(계약 §2 · EC-008). 소비는 위쪽의 `runCatchingDomain`이 한다. */
    @Test
    fun `DataSource가 던진 예외를 잡지 않고 전파한다`() =
        runTest {
            val origin = MinoDomainException.Network(cause = IOException("offline"))
            remoteDataSource.error = origin

            val result = runCatching { repository.issueInviteCode(roomId = "room-1") }

            assertSame(origin, result.exceptionOrNull())
        }
}

private class FakeInvitationRemoteDataSource : InvitationRemoteDataSource {
    var response: InvitationResponse = InvitationResponse(code = "K7Q2MZ")
    var error: Throwable? = null
    var requestedRoomId: String? = null

    override suspend fun issueInvitation(roomId: String): InvitationResponse {
        requestedRoomId = roomId
        error?.let { throw it }
        return response
    }
}
