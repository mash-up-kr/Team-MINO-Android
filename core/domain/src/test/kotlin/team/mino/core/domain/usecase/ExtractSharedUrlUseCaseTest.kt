package team.mino.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 공유받은 텍스트에서 저장 대상 URL 하나를 뽑는 규칙을 본다
 * (FR-002 · TS-007 · EC-002 · EC-003 · `contracts/share-intent.md` §2.1).
 *
 * 판정하는 것은 **입력 문자열 → 반환값**뿐이다. 추출을 정규식으로 하는지 토큰 분리로 하는지는 이 테스트가 정하지
 * 않는다. 스킴·구두점 처리처럼 계약 표가 다루지 않는 입력도 넣지 않는다 — 넣는 순간 명세에 없는 구현 방식을
 * 테스트가 못박게 된다.
 */
class ExtractSharedUrlUseCaseTest {
    private val extractSharedUrl = ExtractSharedUrlUseCase()

    @Test
    fun `문구와 섞인 URL 하나는 문구를 빼고 URL만 돌려준다`() {
        assertEquals("https://a.com/x", extractSharedUrl("여기 맛집 https://a.com/x 추천"))
    }

    @Test
    fun `URL만 담긴 텍스트는 그대로 돌려준다`() {
        assertEquals("https://a.com/x", extractSharedUrl("https://a.com/x"))
    }

    @Test
    fun `문구가 여러 줄이어도 URL만 돌려준다`() {
        val sharedText =
            """
            오늘 다녀온 곳
            진짜 맛있었다
            https://a.com/x
            """.trimIndent()

        assertEquals("https://a.com/x", extractSharedUrl(sharedText))
    }

    @Test
    fun `URL이 여러 개면 가장 앞의 하나만 돌려준다`() {
        assertEquals("https://a.com/x", extractSharedUrl("https://a.com/x 그리고 https://b.com/y"))
    }

    @Test
    fun `앞선 URL이 문구 뒤에 있어도 등장 순서로 첫 번째를 고른다`() {
        assertEquals("https://a.com/x", extractSharedUrl("여기 https://a.com/x 랑 저기 https://b.com/y 둘 다 좋다"))
    }

    @Test
    fun `줄이 갈려도 먼저 등장한 줄의 URL을 고른다`() {
        val sharedText =
            """
            https://a.com/x
            https://b.com/y
            """.trimIndent()

        assertEquals("https://a.com/x", extractSharedUrl(sharedText))
    }

    @Test
    fun `URL이 하나도 없으면 null이다`() {
        assertNull(extractSharedUrl("오늘 점심 맛있었다"))
    }

    @Test
    fun `빈 문자열이면 null이다`() {
        assertNull(extractSharedUrl(""))
    }

    @Test
    fun `공백뿐이면 null이다`() {
        assertNull(extractSharedUrl("   "))
    }
}
