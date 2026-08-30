package team.mino.core.data.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 이미 있는 핀을 다른 방에 복제하는 요청 —
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §3.3.
 *
 * [roomIds]는 비어 있을 수 없다(`minItems: 1`). 홈은 시트에서 고른 방 하나만 담지만 계약이 배열이라 타입도
 * 배열이다. 대상 방 중 하나라도 같은 장소를 이미 갖고 있으면 서버가 **전체를 거절**하고 `409`를 돌려준다.
 *
 * [PinCreateRequest]와 합치지 않는다 — 그쪽은 공유받은 `url`에서 핀을 새로 만들고, 이쪽은 이미 있는 핀을
 * 경로의 `pinId`로 가리킨다. 본문이 겹치는 것은 우연이다.
 */
@Serializable
internal data class PinDuplicateRequest(
    val roomIds: List<String>,
)
