package team.mino.core.domain.model

/**
 * 초대 코드를 참여 전에 미리 들여다본 결과.
 *
 * 지금은 참여 API 호출에 쓸 [roomId]만 필요해 이 필드 하나만 둔다. 초대장 화면이 방 이름·멤버 등을
 * 더 보여줘야 하면 그때 필드를 늘린다.
 */
data class InvitationPreview(
    val roomId: String,
)
