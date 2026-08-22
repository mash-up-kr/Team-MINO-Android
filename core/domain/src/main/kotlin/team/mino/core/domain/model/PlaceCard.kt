package team.mino.core.domain.model

/**
 * 카드덱 카드 한 장. 덱 안에서 [pinId]는 유일하며, 중복 제거는 덱 구성 시점의 책임이다.
 */
data class PlaceCard(
    val pinId: String,
    val placeName: String,
    val address: String,
    val imageUrls: List<String>,
    val label: PlaceCategoryLabel,
    val registrant: Registrant? = null,
) {
    init {
        require(placeName.isNotBlank()) { "placeName must not be blank" }
        require(address.isNotBlank()) { "address must not be blank" }
    }
}
