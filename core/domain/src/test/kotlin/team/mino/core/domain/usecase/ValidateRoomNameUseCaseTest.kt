package team.mino.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.domain.model.RoomNameValidation

class ValidateRoomNameUseCaseTest {
    private val validateRoomName = ValidateRoomNameUseCase()

    @Test
    fun `빈 문자열은 Blank다`() {
        assertEquals(RoomNameValidation.Blank, validateRoomName(""))
    }

    @Test
    fun `공백만 입력하면 Blank다`() {
        assertEquals(RoomNameValidation.Blank, validateRoomName("   "))
    }

    @Test
    fun `한글과 공백으로 이루어진 이름은 Valid다`() {
        assertEquals(RoomNameValidation.Valid, validateRoomName("민호야 잘하자"))
    }

    @Test
    fun `허용되지 않는 기호가 섞이면 InvalidCharacter다`() {
        assertEquals(RoomNameValidation.InvalidCharacter, validateRoomName("민호야 잘하자^^"))
    }

    @Test
    fun `이모지가 섞이면 InvalidCharacter다`() {
        assertEquals(RoomNameValidation.InvalidCharacter, validateRoomName("팀🎉"))
    }

    @Test
    fun `앞뒤 공백을 제거한 값으로 판정한다`() {
        assertEquals(RoomNameValidation.Valid, validateRoomName(" 야호 "))
    }

    @Test
    fun `자모 단독으로만 이루어진 이름은 Valid다`() {
        assertEquals(RoomNameValidation.Valid, validateRoomName("ㄱㄱㄱ"))
    }

    @Test
    fun `완성형과 자모가 섞인 이름은 Valid다`() {
        assertEquals(RoomNameValidation.Valid, validateRoomName("민호ㅇ"))
    }

    @Test
    fun `모음 자모도 한글로 보아 Valid다`() {
        assertEquals(RoomNameValidation.Valid, validateRoomName("ㅏ"))
    }

    @Test
    fun `숫자만 입력해도 Valid다`() {
        assertEquals(RoomNameValidation.Valid, validateRoomName("12345"))
    }

    @Test
    fun `영문만 입력해도 Valid다`() {
        assertEquals(RoomNameValidation.Valid, validateRoomName("MinoTeam"))
    }

    @Test
    fun `한 글자도 Valid다`() {
        assertEquals(RoomNameValidation.Valid, validateRoomName("민"))
    }

    @Test
    fun `공백 포함 15자도 Valid다`() {
        val fifteenChars = "민호야 잘하자 우리 함께가자"
        assertEquals(15, fifteenChars.length)
        assertEquals(RoomNameValidation.Valid, validateRoomName(fifteenChars))
    }

    @Test
    fun `길이를 판정하지 않으므로 15자를 넘어도 Valid다`() {
        assertEquals(RoomNameValidation.Valid, validateRoomName("가".repeat(16)))
    }
}
