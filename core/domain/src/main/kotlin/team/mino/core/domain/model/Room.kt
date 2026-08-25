package team.mino.core.domain.model

/**
 * 여러 사람이 함께 쓰는 방.
 *
 * 방 생성·편집 폼이 쓰는 필드만 담는다. 서버 계약에만 존재하거나 다른 화면이 쓰는 필드(초대 코드·생성 시각·집계 수 등)는
 * 두지 않는다 — `core/domain/README.md` §5. 필요해지는 feature가 생길 때 더한다.
 *
 * [description]은 nullable이 아니다. 설명이 없는 방은 빈 문자열을 갖고, 서버가 내려준 `null`은 Mapper가 흡수한다.
 *
 * [color]도 nullable이 아니다. 사용자가 색을 고르지 않았더라도 저장된 방은 이미 [RoomColor.GRAY]로 확정된 상태다.
 */
data class Room(
    val id: String,
    val name: String,
    val description: String,
    val color: RoomColor,
    val ownerId: String,
)
