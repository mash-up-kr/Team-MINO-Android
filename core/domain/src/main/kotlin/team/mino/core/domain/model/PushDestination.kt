package team.mino.core.domain.model

/**
 * 푸시 알림을 탭했을 때 진입할 지점. `ResolvePushDestinationUseCase`의 반환 타입이다.
 *
 * [PushMessage.type]이 `null`인 알림은 표시하지 않으므로 이 값이 아예 만들어지지 않는다.
 */
sealed interface PushDestination {
    /** 장소 상세. [pinId]는 장소·방 쌍을 가리키는 저장 식별자다. */
    data class PlaceDetail(val pinId: String) : PushDestination

    /** 방 상세. */
    data class RoomDetail(val roomId: String) : PushDestination

    /** 알림 탭. 도착지 식별자가 없거나 해석할 수 없을 때의 기본 도착지다. */
    data object NotificationTab : PushDestination
}
