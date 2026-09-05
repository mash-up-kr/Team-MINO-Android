package team.mino.core.domain.model

/**
 * 알림함에 실리는 알림의 유형. 도착지와 썸네일 갈래를 정한다
 * (`docs/specs/notifications/data-model.md` §1.2).
 *
 * 서버 enum 문자열과 이 값의 대응은 `:core:data`의 Mapper만 안다 — [PLACE_DUPLICATED]만 서버 이름이
 * `PIN_DUPLICATED`로 다르고 나머지는 같다.
 *
 * 모르는 문자열을 흡수하는 `UNKNOWN` 멤버는 두지 않는다 — 유형을 모르면 문구도 도착지도 정할 수 없어
 * 행으로 그릴 수 없으므로, Mapper가 그 **항목만 버리고** 나머지는 그대로 그린다. 목록 전체를 실패로
 * 만들지 않는다(`docs/specs/notifications/contracts/notification-repository.md` §1).
 *
 * **위치 기반 대표 알림이 이 목록에 없다.** 푸시로만 오고 알림함 목록에는 실리지 않는다(spec FR-019) —
 * 그래서 푸시 쪽 [PushMessageType]과 멤버가 어긋나며, 두 타입을 합치지 않는다.
 */
enum class NotificationType {
    /** 중복 저장. 서버 enum은 `PIN_DUPLICATED`다 */
    PLACE_DUPLICATED,

    /** 저장 오류 */
    SAVE_FAILED,

    /** 위치 기반 리마인드 */
    NEARBY_PLACE,

    /** 코멘트 기반 리마인드 */
    TOP_COMMENTED_PLACE,

    /** 공동방 참가 — 기존 멤버가 받는 알림 */
    ROOM_MEMBER_JOINED,

    /** 공동방 참가 — 참가 당사자가 받는 알림 */
    ROOM_JOINED_SELF,
}
