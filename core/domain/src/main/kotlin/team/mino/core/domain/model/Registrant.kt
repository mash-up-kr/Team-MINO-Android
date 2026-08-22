package team.mino.core.domain.model

/**
 * 장소 등록자. 카드에는 아바타 이미지만 쓰므로 닉네임은 두지 않는다.
 */
data class Registrant(
    val userId: String,
    val avatarUrl: String? = null,
)
