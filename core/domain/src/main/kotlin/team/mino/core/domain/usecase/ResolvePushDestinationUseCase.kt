package team.mino.core.domain.usecase

import team.mino.core.domain.model.PushDestination
import team.mino.core.domain.model.PushMessage
import team.mino.core.domain.model.PushMessageType
import javax.inject.Inject

/**
 * 파싱된 [PushMessage]에서 알림 탭 시 열 [PushDestination]을 정한다. 식별자가 없거나 비어 있으면 알림 탭이 기본 도착지다.
 *
 * `type == null`인 알림은 표시되지 않아 호출자가 이 함수에 도달시키지 않는다. 그 분기는 `when`의 완전성을 위해서만
 * 알림 탭으로 둔다.
 */
class ResolvePushDestinationUseCase @Inject constructor() {
    operator fun invoke(message: PushMessage): PushDestination {
        val targetId = message.targetId?.takeIf { it.isNotEmpty() }
        return when (message.type) {
            PushMessageType.PIN_DUPLICATED,
            PushMessageType.TOP_COMMENTED_PLACE,
            PushMessageType.NEARBY_PLACE,
            -> targetId?.let(PushDestination::PlaceDetail) ?: PushDestination.NotificationTab

            PushMessageType.ROOM_MEMBER_JOINED,
            PushMessageType.ROOM_JOINED_SELF,
            -> targetId?.let(PushDestination::RoomDetail) ?: PushDestination.NotificationTab

            PushMessageType.NEARBY_PLACE_SUMMARY,
            PushMessageType.SAVE_FAILED,
            null,
            -> PushDestination.NotificationTab
        }
    }
}
