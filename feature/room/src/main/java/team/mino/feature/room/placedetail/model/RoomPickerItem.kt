package team.mino.feature.room.placedetail.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.domain.model.RoomColor

/**
 * 방 카드 한 장이 그리는 것. [다른방에 공유] 시트와 [저장된 방] 시트가 같은 타입을 나눠 쓴다 — 한 번의 방 목록
 * 조회가 두 시트를 먹이므로(`docs/specs/place-detail/contracts/place-detail-main-contract.md` §3.1) 카드가
 * 드는 값도 하나다.
 *
 * `:feature:sharereceiver`의 같은 이름 타입과 **별개다.** 겉모습이 비슷해도 CTA 문구·높이 단계·이미 저장된 방 처리가
 * 모두 달라 공용화하지 않는다(`docs/specs/place-detail/research.md` D13). 두 번째 소비자가 같은 규칙을 요구할 때
 * 다시 판단할 일이다.
 *
 * **문구를 미리 조립하지 않는다.** 개수 문구·색 팔레트 대응은 그리는 쪽이 정하므로 이 타입은 원자값만 나른다.
 *
 * @property description 설명이 없는 방은 `null`이다. 카드는 이 값의 유무로 메모 줄을 접는다.
 * @property thumbnailImageUrls 없으면 빈 목록이다. 카드는 이때 색 폴백을 그린다.
 * @property color 방의 대표 색. 팔레트 값이 아니라 도메인 값이며, 팔레트와의 대응은 양쪽을 모두 아는 컴포저블이
 *   소유한다(`docs/adr/2026-08-14-room-color-palette-in-design-system.md`).
 * @property hasPlace 이 장소가 그 방에 이미 저장되어 있는지. `true`면 [다른방에 공유] 시트의 카드는 체크된 채
 *   비활성이라 다시 고를 수 없다(spec FR-018 · FR-022). 모든 방이 `true`인 상태가 EC-019를 그대로 표현한다.
 *   [저장된 방] 버튼의 활성 판정도 이 값을 센다(spec FR-023).
 * @property matchedPinId 이 장소가 그 방에 저장되어 있을 때 대응하는 핀의 식별자. [저장된 방] 시트가 방을
 *   바꿀 때 실어 보내는 값이며, `roomId`가 아니라 핀의 id인 것은 코멘트가 (장소, 방) 쌍인 핀에 매달려 있기
 *   때문이다(spec FR-024 · FR-025). [hasPlace]가 `false`인 방에서는 `null`이다 —
 *   `team.mino.core.domain.model.RoomSummary.matchedPinId`가 이미 그렇게 좁혀 준다. 기본값이 `null`인 것도
 *   같은 이유다 — 저장 여부를 묻지 않고 세운 카드에는 전환 대상이 없다.
 */
@Immutable
internal data class RoomPickerItem(
    val id: String,
    val name: String,
    val description: String?,
    val placeCount: Int,
    val thumbnailImageUrls: ImmutableList<String>,
    val color: RoomColor,
    val hasPlace: Boolean,
    val matchedPinId: String? = null,
)
