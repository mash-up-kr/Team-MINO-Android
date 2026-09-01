package team.mino.core.domain.usecase

import javax.inject.Inject

/**
 * 닉네임이 저장 가능한 값인지 판정한다(FR-002).
 *
 * 앞뒤 공백을 제거한 값이 완성형 한글(`가`–`힣`)과 영문 대소문자만으로 2자 이상이면 유효하다.
 * 자모 단독(`ㄱ`·`ㅏ`)·숫자·특수문자·이모지는 무효이며, 내부 공백도 무효다.
 *
 * **길이 상한을 판정하지 않는다.** 상한이 없어서가 아니라 이 판정의 몫이 아니다 —
 * 15자는 오류가 아니라 입력 차단이라 `ProfileViewModel`이 지키고(FR-014), 이 함수에 도달하는 값은 이미 상한 이하다.
 *
 * 화면의 실시간 판정과 저장 경로가 이 판정 하나를 공유한다.
 */
class ValidateNicknameUseCase @Inject constructor() {
    operator fun invoke(rawNickname: String): Boolean = rawNickname.trim().matches(NICKNAME_PATTERN)

    private companion object {
        val NICKNAME_PATTERN = Regex("[가-힣a-zA-Z]{2,}")
    }
}
