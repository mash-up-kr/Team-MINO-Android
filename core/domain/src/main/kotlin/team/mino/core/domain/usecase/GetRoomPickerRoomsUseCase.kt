package team.mino.core.domain.usecase

import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType
import team.mino.core.domain.repository.RoomRepository
import javax.inject.Inject

/**
 * 방 선택 목록을 개인방이 최상단에 오도록 정렬해 돌려준다(FR-005).
 *
 * 정렬 기준은 [RoomType.PERSONAL] 여부 하나뿐이다. 공동방 사이의 순서는 앱이 정하지 않으며 서버가 준 순서를
 * 그대로 유지한다 — PRD와 디자인 어디에도 정렬 기준이 없어 spec §4 가정이 "기본 순서를 그대로 쓴다"로 닫았다
 * (`docs/specs/shared-link-receiver/contracts/room-list-api.md` §4).
 *
 * 실패는 잡지 않는다. [RoomRepository.getRooms]가 던지는 `MinoDomainException`은 그대로 통과하며, 빈 목록으로
 * 수렴시킬지는 화면이 정한다.
 */
class GetRoomPickerRoomsUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
) {
    /**
     * @param placeId 저장 여부를 함께 묻고 싶은 장소의 식별자. [RoomRepository.getRooms]로 그대로 흘려보내며,
     *  주면 각 [RoomSummary]의 `hasPlace`·`matchedPinId`가 채워지고 주지 않으면 둘 다 `null`로 남는다 —
     *  `null`은 "저장돼 있지 않다"가 아니라 "물어보지 않았다"다
     *  (`docs/specs/place-detail/contracts/place-repository.md` §3). 정렬은 이 인자와 무관하게 언제나 한다.
     */
    suspend operator fun invoke(placeId: String? = null): List<RoomSummary> =
        // sortedBy는 안정 정렬이라 같은 키(= 공동방)끼리는 받은 순서가 보존된다.
        roomRepository.getRooms(placeId).sortedBy { it.type != RoomType.PERSONAL }
}
