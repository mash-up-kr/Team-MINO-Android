package team.mino.core.data.repository

import team.mino.core.data.datasource.InvitationRemoteDataSource
import team.mino.core.domain.repository.RoomInvitationRepository
import javax.inject.Inject

/**
 * DTO가 밖으로 나가지 않는 경계다 — 나가는 것은 응답 DTO가 아니라 코드 문자열이다
 * (`core/data/README.md` §6).
 *
 * 필드 하나를 그대로 꺼내는 것이라 변환 규칙이 없어 Mapper를 두지 않는다
 * (`docs/specs/onboarding-flow/contracts/invite-link.md` §2).
 *
 * 예외를 잡지 않는다. `Network`·`Http`·`Auth` 어느 쪽이든 `MinoDomainException`으로 그대로 전파되고,
 * 소비는 ViewModel의 `runCatchingDomain`이 한다. `null`이나 빈 문자열로 뭉개지 않는다(계약 §5 · EC-008).
 */
internal class RoomInvitationRepositoryImpl @Inject constructor(
    private val remoteDataSource: InvitationRemoteDataSource,
) : RoomInvitationRepository {
    override suspend fun issueInviteCode(roomId: String): String = remoteDataSource.issueInvitation(roomId).code
}
