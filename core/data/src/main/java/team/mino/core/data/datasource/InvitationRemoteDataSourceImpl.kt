package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.InvitationPreviewResponse
import team.mino.core.data.network.dto.response.InvitationResponse
import team.mino.core.data.network.service.InvitationApiService
import javax.inject.Inject

/**
 * [InvitationRemoteDataSource]의 실서버 구현. 계약은
 * `docs/specs/onboarding-flow/contracts/invite-link.md` §2가 소유한다.
 *
 * [InvitationApiService]에 위임만 한다 — 봉투 해제는 서비스가, 도메인 변환은 Repository가 하므로
 * 이 클래스에는 변환도 비즈니스 로직도 없다(`core/data/README.md` §5).
 */
internal class InvitationRemoteDataSourceImpl @Inject constructor(
    private val service: InvitationApiService,
) : InvitationRemoteDataSource {
    override suspend fun issueInvitation(roomId: String): InvitationResponse = service.issueInvitation(roomId)

    override suspend fun previewInvitation(code: String): InvitationPreviewResponse = service.previewInvitation(code)
}
