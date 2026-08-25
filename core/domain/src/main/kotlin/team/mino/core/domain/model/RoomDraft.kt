package team.mino.core.domain.model

/**
 * 폼이 만들어 저장을 요청하는 방의 입력값. 아직 방이 아니므로 식별자와 소유자가 없다.
 *
 * [name]은 앞뒤 공백이 제거된 값만 담는다. 값의 유효성은 생성자가 강제하지 않으며 판정은 `ValidateRoomNameUseCase`가 소유한다.
 *
 * [description]은 설명이 없으면 빈 문자열이다.
 *
 * [color]의 `null`은 **사용자가 색을 고르지 않았다**는 뜻이다. 여기에 [RoomColor.GRAY]를 넣지 않는다 —
 * 미선택을 [RoomColor.GRAY]로 확정하는 것은 저장 경로의 책임이고, 그 구분이 사라지면 "고르지 않음"과 "회색을 골랐음"을 되돌릴 수 없다.
 */
data class RoomDraft(
    val name: String,
    val description: String,
    val color: RoomColor?,
)
