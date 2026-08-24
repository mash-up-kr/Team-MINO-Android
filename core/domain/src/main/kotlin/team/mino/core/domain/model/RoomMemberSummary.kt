package team.mino.core.domain.model

/**
 * 방 멤버 아바타 표시용 요약. PRD 「방 멤버 아바타」정의:
 * 4명 이하는 전부 표시하고 카운터가 없으며, 5명 이상은 아바타 3개 + 카운터로 표시한다.
 */
data class RoomMemberSummary(
    /** 최대 4개, 최근 저장자가 마지막(우측). */
    val visibleAvatarUrls: List<String?>,
    /** 5명 이상일 때 "보이지 않는 나머지 인원". 99 초과면 상위에서 "99+"로 표기한다. */
    val overflowCount: Int,
)
