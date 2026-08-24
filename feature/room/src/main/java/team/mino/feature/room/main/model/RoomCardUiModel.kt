package team.mino.feature.room.main.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomThumbnail

/**
 * `Room` 도메인 모델을 `:core:design-system`의 `MinoRoomCard`(stateless)가 받는 원시 파라미터로
 * 매핑한 결과. `MinoRoomCard`는 `Room`을 모르므로(ADR 2026-08-18 「결정」) 이 매핑을 `:feature:room`이
 * 소유한다(FR-004, T020).
 */
data class RoomCardUiModel(
    val title: String,
    val placeCountLabel: String,
    val participantImageUrls: ImmutableList<String?>,
    val coverImageUrl: String?,
    val memo: String?,
)

/**
 * [Room.thumbnail]은 sealed(`ColorAndCharacter`/`Collage`)인데 `MinoRoomCard.coverImageUrl`은 단일
 * URL만 받는다 — 두 타입이 1:1로 대응하지 않는다.
 *
 * - `RoomThumbnail.Collage`(장소 1개 이상): 첫 번째 이미지를 대표 커버로 근사한다. `MinoRoomCard`
 *   자체가 콜라주(최대 4장) 레이아웃을 갖지 않고 단일 커버만 그리므로, 나머지 이미지는 이 카드에서
 *   쓰이지 않는다.
 * - `RoomThumbnail.ColorAndCharacter`(장소 0개, 대표 색상+캐릭터): `MinoRoomCard`가 색상+캐릭터
 *   조합을 표현할 파라미터 자체가 없어 `coverImageUrl = null`로 근사한다 — `MinoRoomCard` 문서상
 *   `coverImageUrl`이 null이면 placeholder 글리프를 그리므로, "장소 0개" 의미가 자연스럽게 유지된다.
 *   단 대표 색상 정보는 이 근사에서 손실된다.
 */
fun Room.toRoomCardParams(): RoomCardUiModel =
    RoomCardUiModel(
        title = name,
        placeCountLabel = "장소 ${placeCount}개",
        participantImageUrls = memberSummary.visibleAvatarUrls.toImmutableList(),
        coverImageUrl = (thumbnail as? RoomThumbnail.Collage)?.imageUrls?.firstOrNull(),
        memo = description,
    )
