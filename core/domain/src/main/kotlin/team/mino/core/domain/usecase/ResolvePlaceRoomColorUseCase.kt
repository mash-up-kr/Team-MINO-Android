package team.mino.core.domain.usecase

import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomSummary
import javax.inject.Inject

/**
 * 핀이 속한 방의 대표 색을 정한다(FR-002).
 *
 * 핀 상세 응답에는 `roomId`만 있고 색이 없다. 색은 방 목록에서 같은 `id`의 방을 찾아 드는 값이며, 그 짝짓기가
 * 이 UseCase의 전부다 — 두 조회 결과를 합치는 규칙이라 화면에 두지 않는다
 * (`core/domain/README.md` §4 · `docs/specs/place-detail/contracts/place-detail-main-contract.md` §5.1).
 *
 * **못 찾으면 `null`이다.** 기본색을 만들지 않는다. spec에 그 색의 근거가 없어 마커를 아예 그리지 않는 것으로
 * 처리한다(`docs/specs/place-detail/research.md` D15). 방이 사라졌거나 목록이 아직 오지 않은 경우가 모두 이
 * `null`로 수렴한다.
 *
 * **조회하지 않는다.** 핀 상세와 방 목록은 서로를 기다리지 않고 병렬로 오며, 한쪽이 실패해도 다른 쪽은 그려져야
 * 한다(D15 · 계약 §5). 두 호출을 이 안으로 넣으면 실패가 한 덩어리로 묶여 방 목록 장애가 장소 상세까지 지운다.
 * 그래서 재료를 받아 판정만 하고, 조회는 [GetRoomPickerRoomsUseCase]와 `PlaceRepository`가 각자 한다.
 *
 * [place]가 `null`이면 아직 짝지을 대상이 없다는 뜻이라 그대로 `null`이다. 어느 쪽이 먼저 도착하든 호출자는 같은
 * 자리에서 이 함수를 다시 부르면 된다.
 */
class ResolvePlaceRoomColorUseCase @Inject constructor() {
    operator fun invoke(
        place: PlaceDetail?,
        rooms: List<RoomSummary>,
    ): RoomColor? = place?.let { detail -> rooms.firstOrNull { it.id == detail.roomId }?.color }
}
