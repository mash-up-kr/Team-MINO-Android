package team.mino.core.domain.model

/**
 * 방 목록의 한 항목.
 *
 * 방 생성·편집 폼이 쓰는 [Room]과 별개의 타입이다. 목록은 [type]·[placeCount]·[thumbnailImageUrls]를 필요로 하지만
 * 폼이 쓰는 필드 대부분은 쓰지 않으므로, 두 화면의 합집합을 한 타입에 담으면 어느 쪽에서든 절반이 의미 없는 값이 된다 —
 * `docs/specs/shared-link-receiver/research.md` R-009.
 *
 * [description]은 nullable이 아니다. 설명이 없는 방은 빈 문자열을 갖고, 서버가 내려준 `null`은 Mapper가 흡수한다 —
 * [Room]과 같은 규칙이다.
 *
 * [placeCount]는 그 방에 저장된 장소의 수일 뿐 **지금 저장하려는 장소가 이미 있는지를 뜻하지 않는다.**
 *
 * [thumbnailImageUrls]는 콜라주가 그릴 수 있는 만큼만 담는다. 서버가 더 많이 내려주더라도 앞의 것만 남기는 판정은
 * Mapper가 하고, 썸네일이 없는 방은 빈 목록을 갖는다.
 */
data class RoomSummary(
    val id: String,
    val name: String,
    val description: String,
    val type: RoomType,
    val color: RoomColor,
    val placeCount: Int,
    val thumbnailImageUrls: List<String>,
)
