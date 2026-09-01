@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.main.model

import team.mino.core.domain.model.RoomMember
import team.mino.core.domain.model.RoomMemberSummary
import kotlin.time.ExperimentalTime

/**
 * `GET /rooms/{roomId}/members`는 "최근 저장자가 먼저"로 정렬해 내려준다(PRD). 아바타 스택은 반대로
 * 최근 저장자가 **마지막(우측)**에 와야 하므로([RoomMemberSummary.visibleAvatars] KDoc) 자른 뒤 뒤집는다.
 *
 * 방 리스트(`RoomListViewModel`)·방 상세(`RoomDetailViewModel`) 둘 다 같은 응답을 같은 규칙으로
 * 요약해야 해서 이 모듈 공용 자리에 둔다.
 */
internal fun List<RoomMember>.toMemberSummary(): RoomMemberSummary =
    if (size <= MAX_VISIBLE_AVATAR_COUNT) {
        RoomMemberSummary(visibleAvatars = map { it.avatar }.reversed(), overflowCount = 0)
    } else {
        RoomMemberSummary(
            visibleAvatars = take(OVERFLOW_VISIBLE_AVATAR_COUNT).map { it.avatar }.reversed(),
            overflowCount = size - OVERFLOW_VISIBLE_AVATAR_COUNT,
        )
    }

private const val MAX_VISIBLE_AVATAR_COUNT = 4
private const val OVERFLOW_VISIBLE_AVATAR_COUNT = 3
