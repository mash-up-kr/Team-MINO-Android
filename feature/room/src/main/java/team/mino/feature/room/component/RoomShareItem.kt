package team.mino.feature.room.component

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomSummary

/**
 * [RoomShareSheet]의 방 카드 한 장이 그리는 값.
 *
 * **두 화면이 같은 시트를 부르므로 어느 화면의 모델도 쓰지 않는다.** 방 상세는 `RoomSummary`에서,
 * 장소 상세는 자기 `RoomPickerItem`에서 이 타입으로 옮겨 담는다 — 시트가 한쪽의 모델을 받으면 다른 쪽이
 * 남의 화면 패키지를 참조하게 되고, 그 모델에 시트가 쓰지 않는 필드(`matchedPinId` 같은)가 딸려 온다.
 *
 * **문구를 미리 조립하지 않는다.** 개수 문구·색 팔레트 대응은 그리는 쪽이 정하므로 원자값만 나른다.
 *
 * @property description 설명이 없는 방은 `null`이다. 카드는 이 값의 유무로 메모 줄을 접는다.
 * @property thumbnailImageUrls 없으면 빈 목록이다. 카드는 이때 방 대표 색 폴백을 그린다.
 * @property color 방의 대표 색. 팔레트 값이 아니라 도메인 값이며, 팔레트와의 대응은 양쪽을 모두 아는
 *   컴포저블이 소유한다(`docs/adr/2026-08-14-room-color-palette-in-design-system.md`).
 * @property alreadySaved 공유하려는 장소가 그 방에 이미 담겨 있는지. `true`면 카드가 체크된 채 비활성이라
 *   다시 고를 수 없다(place-detail spec FR-018 · FR-022 · EC-019, room-detail spec EC-004). 모든 방이
 *   `true`인 상태가 EC-019를 그대로 표현한다.
 */
@Immutable
internal data class RoomShareItem(
    val id: String,
    val name: String,
    val description: String?,
    val placeCount: Int,
    val thumbnailImageUrls: ImmutableList<String>,
    val color: RoomColor,
    val alreadySaved: Boolean,
)

/**
 * 방 목록 조회 결과를 시트가 그대로 그릴 수 있는 모양으로 옮긴다.
 *
 * **변환이 이 파일에 모여 있다.** 두 화면이 같은 시트를 부르므로 각자 옮겨 담으면 [RoomShareItem]에 필드가
 * 늘 때 두 ViewModel을 함께 고쳐야 하고, 실제로 그렇게 두었을 때 두 매퍼가 이미 같은 규칙(`hasPlace`의
 * `null` 접기, 빈 설명 접기)을 따로 적고 있었다.
 *
 * **`hasPlace`의 `null`을 `false`로 접는다.** 도메인에서 `null`은 「저장돼 있지 않다」가 아니라 「물어보지
 * 않았다」이지만([RoomSummary.hasPlace]), 이 목록은 언제나 `placeId`를 실어 물은 결과라 그 값이 나올 자리가
 * 없다. 설명이 없는 방은 빈 문자열로 오지만 카드는 `null`로 접는다 — 두 표현의 경계가 여기다.
 */
internal fun List<RoomSummary>.toRoomShareItems(): ImmutableList<RoomShareItem> =
    map {
        RoomShareItem(
            id = it.id,
            name = it.name,
            description = it.description.takeIf(String::isNotBlank),
            placeCount = it.placeCount,
            thumbnailImageUrls = it.thumbnailImageUrls.toImmutableList(),
            color = it.color,
            alreadySaved = it.hasPlace == true,
        )
    }.toImmutableList()
