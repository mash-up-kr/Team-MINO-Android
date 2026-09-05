package team.mino.core.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateNicknameUseCaseTest {
    private val validateNickname = ValidateNicknameUseCase()

    @Test
    fun `한 글자 닉네임은 무효다`() {
        assertFalse(validateNickname("민"))
    }

    @Test
    fun `허용되지 않는 문자가 섞이면 무효다`() {
        assertFalse(validateNickname("abc1"))
    }

    @Test
    fun `앞뒤 공백을 제외한 값으로 판정한다`() {
        assertTrue(validateNickname("  민호  "))
    }

    @Test
    fun `공백만 입력하면 무효다`() {
        assertFalse(validateNickname("   "))
    }

    /**
     * 상한이 없다는 뜻이 아니라, 상한이 이 판정의 몫이 아님을 고정한다.
     * 15자 상한은 오류가 아닌 입력 차단이라 `ProfileViewModel`이 지킨다(FR-014).
     */
    @Test
    fun `한글 30자는 유효하다`() {
        assertTrue(validateNickname("민호".repeat(15)))
    }

    @Test
    fun `한글 낱자는 무효다`() {
        assertFalse(validateNickname("ㄱㄱ"))
    }

    @Test
    fun `중간에 공백이 있으면 무효다`() {
        assertFalse(validateNickname("민 호"))
    }
}
