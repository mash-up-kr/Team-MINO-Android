package team.mino.core.domain.model

/**
 * 알림이 가리키는 이동 대상 (`docs/specs/notifications/data-model.md` §1.3).
 * 유형이 대상의 종류를 정하므로 sealed로 가른다.
 *
 * 실제로 열 화면을 나타내는 [NotificationDestination]과는 다른 타입이다 — 이쪽은 서버가 준 참조이고 그쪽은
 * 판정된 결과다. 저장 오류가 대상으로는 [None]이면서 도착지로는 화면 하나라 둘의 대응이 어긋난다.
 */
sealed interface NotificationTarget {
    /**
     * 장소 대상 3종.
     *
     * **[pinId]는 `placeId`가 아니다.** 서버 `payload`가 둘 다 주지만 장소 상세는 핀으로 열고
     * 도착지 방도 그 핀으로 정해진다(spec FR-022 ·
     * `docs/specs/notifications/contracts/notification-api.md` §1). 쓰는 곳이 없는 `placeId`를 함께
     * 실으면 나중에 어느 쪽이 도착지 키인지 헷갈리므로 모델에 싣지 않는다 — [PushMessage.targetId]가
     * 같은 이유로 `placeId`를 버린다.
     */
    data class Pin(val pinId: String) : NotificationTarget

    /** 공동방 참가 2종. */
    data class Room(val roomId: String) : NotificationTarget

    /** 저장 오류. 열 대상이 따로 없다. */
    data object None : NotificationTarget
}
