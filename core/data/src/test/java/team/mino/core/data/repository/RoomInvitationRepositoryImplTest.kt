package team.mino.core.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import team.mino.core.data.datasource.FakeRoomRemoteDataSource
import team.mino.core.data.datasource.InvitationRemoteDataSource
import team.mino.core.data.network.dto.response.InvitationPreviewResponse
import team.mino.core.data.network.dto.response.InvitationResponse
import team.mino.core.domain.model.InvitationPreview
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

/**
 * DTO가 이 클래스 밖으로 나가지 않는 경계다 — 나가는 것은 [InvitationResponse]가 아니라 코드 문자열·
 * [InvitationPreview]다.
 *
 * 계약은 `docs/specs/onboarding-flow/contracts/invite-link.md` §2가 소유한다.
 */
class RoomInvitationRepositoryImplTest {
    private val remoteDataSource = FakeInvitationRemoteDataSource()
    private val roomRemoteDataSource = FakeRoomRemoteDataSource()
    private val repository = RoomInvitationRepositoryImpl(remoteDataSource, roomRemoteDataSource)

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

    @Test
    fun `미리보기 응답의 방 id를 InvitationPreview로 돌려준다`() =
        runTest {
            remoteDataSource.previewResponse =
                InvitationPreviewResponse(room = InvitationPreviewResponse.RoomIdOnly(id = "room-9"))

            val preview = repository.previewInvitation(inviteCode = "AB12CD")

            assertEquals("AB12CD", remoteDataSource.requestedPreviewCode)
            assertEquals(InvitationPreview(roomId = "room-9"), preview)
        }

    @Test
    fun `미리보기 실패는 잡지 않고 전파한다`() =
        runTest {
            val origin = MinoDomainException.Http(code = 404, cause = IOException("not found"))
            remoteDataSource.previewError = origin

            val result = runCatching { repository.previewInvitation(inviteCode = "AB12CD") }

            assertSame(origin, result.exceptionOrNull())
        }

    @Test
    fun `참여는 room 태그 DataSource로 roomId와 inviteCode를 그대로 넘긴다`() =
        runTest {
            repository.joinRoom(roomId = "room-9", inviteCode = "AB12CD")

            assertEquals("room-9", roomRemoteDataSource.lastJoinRoomId)
            assertEquals("AB12CD", roomRemoteDataSource.lastJoinInviteCode)
        }

    @Test
    fun `참여 실패는 잡지 않고 전파한다`() =
        runTest {
            val origin = MinoDomainException.Network(cause = IOException("offline"))
            roomRemoteDataSource.joinRoomError = origin

            val result = runCatching { repository.joinRoom(roomId = "room-9", inviteCode = "AB12CD") }

            assertSame(origin, result.exceptionOrNull())
        }
}

private class FakeInvitationRemoteDataSource : InvitationRemoteDataSource {
    var response: InvitationResponse = InvitationResponse(code = "K7Q2MZ")
    var error: Throwable? = null
    var requestedRoomId: String? = null

    var previewResponse: InvitationPreviewResponse =
        InvitationPreviewResponse(room = InvitationPreviewResponse.RoomIdOnly(id = "room-1"))
    var previewError: Throwable? = null
    var requestedPreviewCode: String? = null

    override suspend fun issueInvitation(roomId: String): InvitationResponse {
        requestedRoomId = roomId
        error?.let { throw it }
        return response
    }

    override suspend fun previewInvitation(code: String): InvitationPreviewResponse {
        requestedPreviewCode = code
        previewError?.let { throw it }
        return previewResponse
    }
}
