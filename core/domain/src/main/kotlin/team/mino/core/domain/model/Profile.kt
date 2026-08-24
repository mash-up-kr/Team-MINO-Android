package team.mino.core.domain.model

/**
 * 다른 사람에게 보이는 사용자의 프로필. 하나의 익명 세션(앱 설치)에 하나만 존재하며, 없는 상태는 `null`로 표현한다.
 *
 * [nickname]은 앞뒤 공백이 제거된 값만 담는다. 값의 유효성(한글 음절·영문 알파벳, 2자 이상)은 생성자가 강제하지 않는다 —
 * 판정은 `ValidateNicknameUseCase`, 정규화는 `SaveProfileUseCase`가 소유한다.
 *
 * [avatarId]는 아바타 목록의 한 항목을 가리키는 식별자이며 서버 계약(`Avatar { id: integer }`)의 타입을 그대로 따른다.
 * 식별자와 그림의 대응은 이 모델이 알지 않는다.
 */
data class Profile(
    val nickname: String,
    val avatarId: Int,
)
