package team.mino.feature.sharereceiver.picker.model

import android.content.res.Resources
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.common.ui.component.roompicker.model.RoomPickerItem
import team.mino.core.domain.model.RoomSummary
import team.mino.feature.sharereceiver.R

/**
 * 방 하나를 카드가 그릴 수 있는 형태로 옮긴다.
 *
 * 문구 포맷과 팔레트 대응은 도메인이 알지 않는 UI의 결정이라 이 변환이 소유한다
 * (`docs/specs/shared-link-receiver/data-model.md` §5.2).
 */
internal fun RoomSummary.toRoomPickerItem(resources: Resources): RoomPickerItem =
    RoomPickerItem(
        id = id,
        name = name,
        description = description.ifEmpty { null },
        placeCountLabel = resources.getString(R.string.sharereceiver_room_place_count, placeCount),
        thumbnailImageUrls = thumbnailImageUrls.toImmutableList(),
        color = color.chip,
    )
