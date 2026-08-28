package team.mino.core.domain.model

/**
 * 홈 덱의 카드 한 장.
 *
 * 홈이 그리는 데 필요한 필드만 담는다. 서버 계약에만 있거나 다른 화면이 쓰는 필드(좌표·카테고리·생성 시각 등)는
 * 두지 않는다 — `core/domain/README.md` §5.
 *
 * **저장 경과일을 담지 않는다.** 경과일은 서버가 계산해 순위에 반영하는 값이지 카드가 나르는 값이 아니다 —
 * spec §2.3(「장소 카드」).
 *
 * [imageUrls]는 대표 이미지 그리드가 그릴 수 있는 만큼만 담는다. 이미지가 없는 장소는 빈 목록을 갖고,
 * 서버가 내려준 `null`은 Mapper가 흡수한다.
 *
 * [registrant]는 nullable이 아니다. 서버 `createdBy`의 `null`은 Mapper가 흡수한다.
 */
data class PlaceCard(
    val pinId: String,
    val placeName: String,
    val address: String,
    val imageUrls: List<String>,
    val label: PlaceLabel,
    val registrant: Registrant,
)

/**
 * 카드 헤더에 아바타로 보이는 등록자.
 *
 * [avatarId]는 아바타 목록의 한 항목을 가리키는 식별자이며 [Profile.avatarId]와 같은 값 체계를 쓴다.
 * 아바타를 고르지 않은 사용자가 있으므로 nullable이고, 그 경우의 대체 표시는 feature가 정한다.
 */
data class Registrant(
    val userId: String,
    val nickname: String,
    val avatarId: Int?,
)
