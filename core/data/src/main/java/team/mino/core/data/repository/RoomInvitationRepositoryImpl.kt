package team.mino.core.data.repository

import team.mino.core.data.datasource.InvitationRemoteDataSource
import team.mino.core.data.datasource.RoomRemoteDataSource
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.domain.model.InvitationPreview
import team.mino.core.domain.repository.RoomInvitationRepository
import javax.inject.Inject

/**
 * DTO가 밖으로 나가지 않는 경계다 — 나가는 것은 응답 DTO가 아니라 코드 문자열·[InvitationPreview]다
 * (`core/data/README.md` §6).
 *
 * [previewInvitation]은 초대(`invitation`) 태그라 [InvitationRemoteDataSource]를 쓰지만, [joinRoom]은
 * `POST /api/v1/rooms/{roomId}/members`로 방(`room`) 태그의 엔드포인트라 [RoomRemoteDataSource]를 쓴다.
 * 이 Repository의 단위는 서버 태그가 아니라 "초대"라는 관심사이므로([RoomInvitationRepository] KDoc),
 * 서로 다른 태그의 DataSource 두 개를 함께 주입받는 것은 의도다.
 *
 * 예외를 잡지 않는다. `Network`·`Http`·`Auth` 어느 쪽이든 `MinoDomainException`으로 그대로 전파되고,
 * 소비는 ViewModel의 `runCatchingDomain`이 한다. `null`이나 빈 문자열로 뭉개지 않는다(계약 §5 · EC-008).
 */
internal class RoomInvitationRepositoryImpl @Inject constructor(
    private val remoteDataSource: InvitationRemoteDataSource,
    private val roomRemoteDataSource: RoomRemoteDataSource,
) : RoomInvitationRepository {
    override suspend fun issueInviteCode(roomId: String): String = remoteDataSource.issueInvitation(roomId).code

    override suspend fun previewInvitation(inviteCode: String): InvitationPreview =
        remoteDataSource.previewInvitation(inviteCode).toDomain()

    override suspend fun joinRoom(
        roomId: String,
        inviteCode: String,
    ) {
        roomRemoteDataSource.joinRoom(roomId, inviteCode)
    }
}
