package team.mino.core.domain.model

/**
 * 푸시 알림의 유형. payload의 `type` 문자열과 이 값의 대응은 파싱 UseCase만 안다.
 *
 * 모르는 문자열을 흡수하는 `UNKNOWN` 멤버는 두지 않는다 — 그 경우는 [PushMessage.type]이 `null`이다.
 * `when`이 `null` 분기를 강제하게 해, 서버가 새 유형을 더했을 때 기존 열거값으로 조용히 오인되는 일을 막는다.
 */
enum class PushMessageType {
    /** 중복 저장 */
    PIN_DUPLICATED,

    /** 코멘트 기반 리마인드 */
    TOP_COMMENTED_PLACE,

    /** 위치 기반 리마인드 — 반경 안 저장 장소가 1개일 때 */
    NEARBY_PLACE,

    /** 위치 기반 대표 알림 — 반경 안 저장 장소가 여럿일 때 */
    NEARBY_PLACE_SUMMARY,

    /** 공동방 참가 — 기존 멤버가 받는 알림 */
    ROOM_MEMBER_JOINED,

    /** 공동방 참가 — 참가 당사자가 받는 알림 */
    ROOM_JOINED_SELF,

    /** 저장 오류 */
    SAVE_FAILED,
}
