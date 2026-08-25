package team.mino.core.domain.model

/**
 * 방 이름이 저장 가능한 값인지에 대한 판정 결과.
 *
 * 판정은 앞뒤 공백을 제거한 값으로 하며, 판정의 소유자는 `ValidateRoomNameUseCase`다.
 *
 * 길이 초과를 표현하는 값은 없다 — 상한(15자)은 판정이 아니라 입력 차단이라 이 타입에 도달하는 값은 이미 상한 이하다.
 */
sealed interface RoomNameValidation {
    /** 1자 이상이고 허용 문자(한글 완성형·자모, 영문, 숫자, 공백)만으로 이루어졌다. */
    data object Valid : RoomNameValidation

    /** 비었거나 공백뿐이다. 저장을 막되 오류로 표시하지는 않는 상태다. */
    data object Blank : RoomNameValidation

    /** 허용되지 않는 문자를 하나 이상 포함한다(이모지 포함). 저장을 막고 오류로 표시하는 상태다. */
    data object InvalidCharacter : RoomNameValidation
}
