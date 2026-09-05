package team.mino.core.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방 멤버 전체 목록 항목. `GET /api/v1/rooms/{roomId}/members` 응답 그대로 매핑한다.
 *
 * [FR-011] 초대 시트의 참여자 전체 목록과 [FR-013] 방장 위임 대상 선택 모달이 함께 소비한다 — 두 화면이
 * 같은 서버 응답을 쓰므로 타입을 하나로 합친다(헌법 원칙 I).
 *
 * [RoomMemberSummary]("방 카드에 그리는 최대 4개 아바타 + overflow count" 축약 표현)와는 다른 타입이다 —
 * 서로 다른 API 응답(카드 목록 vs 멤버 목록)에서 오므로 하나를 다른 하나로 파생시키지 않는다.
 *
 * [avatar]는 서버가 URL이 아니라 색 키로 내려주는 [ProfileAvatar] 12종 중 하나다(`ProfileMapper`의
 * 색 대응표를 공유). 모르는 값·없는 값은 [ProfileAvatar.Default]로 읽는다.
 */
@OptIn(ExperimentalTime::class)
data class RoomMember(
    val userId: String,
    val nickname: String,
    val avatar: ProfileAvatar,
    val isOwner: Boolean,
    val joinedAt: Instant,
)
