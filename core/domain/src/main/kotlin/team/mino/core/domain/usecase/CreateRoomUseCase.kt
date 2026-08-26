package team.mino.core.domain.usecase

import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.repository.RoomRepository
import javax.inject.Inject

/**
 * 방을 만든다. 색을 고르지 않은 초안의 색을 [RoomColor.GRAY]로 확정한 뒤 저장을 요청한다(FR-006·TS-007).
 *
 * 회색 기본값은 도메인 규칙이라 ViewModel·Mapper가 아니라 여기 있다. 확정은 `createRoom`을 **부르기 전에** 끝나므로
 * Repository에는 `color`가 `null`인 초안이 도달하지 않는다.
 *
 * 생성 실패는 잡지 않는다. `MinoDomainException`의 소비는 ViewModel의 `runCatchingDomain`이 한다.
 */
class CreateRoomUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(draft: RoomDraft): Room =
        roomRepository.createRoom(draft.copy(color = draft.color ?: RoomColor.GRAY))
}
