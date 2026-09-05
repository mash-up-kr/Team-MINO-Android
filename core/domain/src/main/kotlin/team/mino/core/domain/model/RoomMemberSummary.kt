package team.mino.core.domain.model

/**
 * 방 멤버 아바타 표시용 요약. PRD 「방 멤버 아바타」정의:
 * 4명 이하는 전부 표시하고 카운터가 없으며, 5명 이상은 아바타 3개 + 카운터로 표시한다.
 *
 * 서버는 멤버 아바타를 이미지 URL이 아니라 `avatar.color` 식별자로 내려준다
 * (`GET /api/v1/rooms/{roomId}/members`) — 그래서 [visibleAvatars]는 [ProfileAvatar]를 든다.
 */
data class RoomMemberSummary(
    /** 최대 4개, 최근 저장자가 마지막(우측). */
    val visibleAvatars: List<ProfileAvatar>,
    /** 5명 이상일 때 "보이지 않는 나머지 인원". 99 초과면 상위에서 "99+"로 표기한다. */
    val overflowCount: Int,
)
