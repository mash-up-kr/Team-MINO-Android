package team.mino.core.data.repository.mapper

import team.mino.core.data.network.dto.response.InvitationPreviewResponse
import team.mino.core.domain.model.InvitationPreview

/** 지금 필요한 건 참여 API 호출에 쓸 방 id뿐이라 나머지 필드는 옮기지 않는다. */
internal fun InvitationPreviewResponse.toDomain(): InvitationPreview =
    InvitationPreview(
        roomId = room.id,
    )
