package team.mino.core.domain.usecase

import team.mino.core.domain.model.RoomNameValidation
import javax.inject.Inject

/**
 * 방 이름이 저장 가능한 값인지 판정한다(FR-004).
 *
 * 앞뒤 공백을 제거한 값으로 판정하며, 허용 문자는 한글(완성형·자모)·영문·숫자·공백이다.
 * 자모 단독(`ㄱ`·`ㅏ`)은 IME 조합 중에 반드시 지나가는 상태라 오류로 보지 않는다(EC-025).
 *
 * **길이를 판정하지 않는다.** 상한(15자)은 입력 차단이라 이 함수에 도달하는 값은 이미 상한 이하다.
 * 글자 단위 입력마다 동기로 불리므로 `suspend`가 아니다.
 */
class ValidateRoomNameUseCase @Inject constructor() {
    operator fun invoke(rawName: String): RoomNameValidation {
        val name = rawName.trim()
        return when {
            name.isEmpty() -> RoomNameValidation.Blank
            name.matches(ROOM_NAME_PATTERN) -> RoomNameValidation.Valid
            else -> RoomNameValidation.InvalidCharacter
        }
    }

    private companion object {
        /** `ㄱ`–`ㅣ`은 Hangul Compatibility Jamo의 자음과 모음을 함께 덮는다. */
        val ROOM_NAME_PATTERN = Regex("[가-힣ㄱ-ㅣa-zA-Z0-9 ]+")
    }
}
