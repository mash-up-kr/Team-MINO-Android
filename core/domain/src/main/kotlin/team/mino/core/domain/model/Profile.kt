package team.mino.core.domain.model

/**
 * 다른 사람에게 보이는 사용자의 프로필. 하나의 익명 세션(앱 설치)에 하나만 존재하며, 없는 상태는 `null`로 표현한다.
 *
 * [nickname]은 앞뒤 공백이 제거된 값만 담는다. 값의 유효성(한글 음절·영문 알파벳, 2자 이상)은 생성자가 강제하지 않는다 —
 * 판정은 `ValidateNicknameUseCase`, 정규화는 `SaveProfileUseCase`가 소유한다.
 *
 * [avatar]는 항상 값이 있다. 사용자가 고르지 않은 채로 저장하면 기본 아바타가 채워져 들어온다.
 */
data class Profile(
    val nickname: String,
    val avatar: ProfileAvatar,
)
