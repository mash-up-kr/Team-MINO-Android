package team.mino.core.data.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 핀 공유(복제) 요청 — `POST /api/v1/pins/{pinId}/duplicate`.
 *
 * [roomIds]는 최소 1개 필요하다(minItems 1). 대상 방 중 하나라도 같은 장소가 이미 저장돼 있으면
 * 서버가 `409 DUPLICATE_PIN_IN_ROOM`으로 전체 요청을 거절한다 — 근거: docs/specs/room-detail/contracts/place-repository.md
 */
@Serializable
internal data class PinDuplicateRequest(
    val roomIds: List<String>,
)
