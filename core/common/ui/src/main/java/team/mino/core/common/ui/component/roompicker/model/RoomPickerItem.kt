package team.mino.core.common.ui.component.roompicker.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor

/**
 * 방 카드 한 장이 그리는 것.
 *
 * 저장하려는 장소가 그 방에 이미 있는지는 담지 않는다. 시트를 그리는 시점에는 어떤 장소인지조차 정해지지 않아
 * 판정이 성립하지 않는다(spec FR-016·FR-017). 멤버 아바타도 FR-006이 제외한다.
 *
 * 도메인 모델(`RoomSummary`)에서 이 형태로 옮기는 변환은 이 타입이 갖지 않는다 — 문구 포맷과 팔레트 대응이
 * feature마다 다른 도메인 자원(문자열 리소스·`RoomColor`)에 걸쳐 있어 그 소유는 각 feature다
 * (`docs/specs/shared-link-receiver/data-model.md` §5.2).
 *
 * @property description 설명이 없는 방은 `null`이다. 카드는 이 값의 유무로 메모 줄을 접는다.
 * @property placeCountLabel 이미 포맷된 문구다. 카드는 개수를 세지 않고 받은 문자열을 그대로 그린다.
 * @property color 썸네일이 없을 때 폴백이 쓰는 팔레트 값. 회색 방은 `null`이다.
 */
@Immutable
data class RoomPickerItem(
    val id: String,
    val name: String,
    val description: String?,
    val placeCountLabel: String,
    val thumbnailImageUrls: ImmutableList<String>,
    val color: MinoRoomColor?,
)
