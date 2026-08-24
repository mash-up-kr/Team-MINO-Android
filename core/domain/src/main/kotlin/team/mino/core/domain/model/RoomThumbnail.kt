package team.mino.core.domain.model

/**
 * 방 썸네일. PRD 「방 썸네일」정의: 장소 0개는 색상+캐릭터, 1개 이상은 최대 4장 콜라주.
 */
sealed interface RoomThumbnail {
    /** 장소 0개. [color]가 null이면 회색(미선택)으로 표시한다. */
    data class ColorAndCharacter(val color: String?) : RoomThumbnail

    /** 장소 N개. [imageUrls]는 1..4개만 유효(그 이상은 상위에서 자른다). */
    data class Collage(val imageUrls: List<String>) : RoomThumbnail
}
