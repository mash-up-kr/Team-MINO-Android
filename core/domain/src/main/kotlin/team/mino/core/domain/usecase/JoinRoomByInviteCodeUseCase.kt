package team.mino.core.domain.usecase

import team.mino.core.domain.repository.RoomInvitationRepository
import javax.inject.Inject

/**
 * 초대 코드로 미리보기 → 참여를 이어 수행하고, 들어간 방의 id를 돌려준다.
 *
 * 두 Repository 호출(미리보기·참여)을 순서대로 조합하는 재사용 행위라 UseCase로 분리한다
 * (`core/domain/README.md` §4). 실패는 잡지 않고 그대로 전파한다 — 값으로 뭉개지 않는다.
 */
class JoinRoomByInviteCodeUseCase @Inject constructor(
    private val roomInvitationRepository: RoomInvitationRepository,
) {
    suspend operator fun invoke(inviteCode: String): String {
        val preview = roomInvitationRepository.previewInvitation(inviteCode)
        roomInvitationRepository.joinRoom(preview.roomId, inviteCode)
        return preview.roomId
    }
}
