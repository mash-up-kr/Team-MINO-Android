package team.mino.core.domain.usecase

import javax.inject.Inject

/**
 * 닉네임이 저장 가능한 값인지 판정한다(FR-002).
 *
 * 앞뒤 공백을 제거한 값이 한글 음절(`가`–`힣`)과 영문 알파벳만으로 2자 이상이면 유효하다.
 * 숫자·특수문자·이모지·중간 공백·한글 낱자(`ㄱ`)는 무효이며, 길이 상한은 두지 않는다.
 *
 * 화면의 실시간 판정과 저장 경로가 이 판정 하나를 공유한다.
 */
class ValidateNicknameUseCase @Inject constructor() {
    operator fun invoke(rawNickname: String): Boolean = rawNickname.trim().matches(NICKNAME_PATTERN)

    private companion object {
        val NICKNAME_PATTERN = Regex("[가-힣a-zA-Z]{2,}")
    }
}
