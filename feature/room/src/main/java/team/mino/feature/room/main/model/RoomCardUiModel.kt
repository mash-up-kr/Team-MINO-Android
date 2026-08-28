package team.mino.feature.room.main.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomThumbnail

/**
 * `Room` 도메인 모델을 `:core:design-system`의 `MinoRoomCard`(stateless)가 받는 원시 파라미터로
 * 매핑한 결과. `MinoRoomCard`는 `Room`을 모르므로(ADR 2026-08-18 「결정」) 이 매핑을 `:feature:room`이
 * 소유한다(FR-004, T020).
 *
 * [thumbnail]은 [Room.thumbnail]을 그대로 들고 있다 — `MinoRoomCard.thumbnail`이 컴포저블 슬롯으로
 * 바뀌면서(콜라주·색상+캐릭터 모두 호출부가 그림), 예전처럼 [RoomThumbnail.Collage]의 첫 장만 근사하거나
 * [RoomThumbnail.ColorAndCharacter]의 색 정보를 버릴 필요가 없어졌다 — 실제 렌더는 호출부([RoomListBottomSheet])가
 * `MinoRoomThumbnail`·`RoomThumbnailFallback`으로 슬롯을 채운다.
 */
data class RoomCardUiModel(
    val title: String,
    val placeCountLabel: String,
    val participantImageUrls: ImmutableList<String?>,
    val thumbnail: RoomThumbnail,
    val memo: String?,
)

fun Room.toRoomCardParams(): RoomCardUiModel =
    RoomCardUiModel(
        title = name,
        placeCountLabel = "장소 ${placeCount}개",
        participantImageUrls = memberSummary.visibleAvatarUrls.toImmutableList(),
        thumbnail = thumbnail,
        memo = description.ifEmpty { null },
    )
