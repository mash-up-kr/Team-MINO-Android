package team.mino.core.data.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 이미 있는 핀을 다른 방에 복제(공유)하는 요청 — `POST /api/v1/pins/{pinId}/duplicate`.
 *
 * [roomIds]는 최소 1개 필요하다(`minItems: 1`). 대상 방 중 하나라도 같은 장소가 이미 저장돼 있으면
 * 서버가 `409 DUPLICATE_PIN_IN_ROOM`으로 전체 요청을 거절한다.
 *
 * **호출부는 `PinApiService` 하나다.** room-detail이 `RoomPlacesRepository.sharePlaces`로,
 * place-detail이 `PlaceRepository.duplicatePin`으로 같은 엔드포인트를 각자 부르던 두 갈래는 두 화면이
 * [SYS-003] 시트를 한 벌로 합치면서 뒤쪽으로 모였다 — 근거:
 * `docs/specs/place-detail/contracts/place-detail-main-contract.md` §3.4.6,
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §3.3.
 *
 * [PinCreateRequest]와 합치지 않는다 — 그쪽은 공유받은 `url`에서 핀을 새로 만들고, 이쪽은 이미 있는 핀을
 * 경로의 `pinId`로 가리킨다. 본문이 겹치는 것은 우연이다.
 */
@Serializable
internal data class PinDuplicateRequest(
    val roomIds: List<String>,
)
