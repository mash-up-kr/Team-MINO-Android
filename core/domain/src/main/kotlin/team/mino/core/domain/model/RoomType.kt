package team.mino.core.domain.model

/**
 * 방의 갈래. 방 목록에서 개인방을 최상단에 고정할지 판정하는 값이다.
 *
 * 서버 문자열과 이 값의 대응은 Mapper만 안다 — `core/domain/README.md` §5. 알 수 없는 문자열은 [GROUP]으로 흡수한다.
 * 개인방은 사용자당 하나뿐이라 서버가 새 갈래를 더하더라도 최상단 고정 대상이 될 수 없기 때문이다.
 */
enum class RoomType {
    PERSONAL,
    GROUP,
}
