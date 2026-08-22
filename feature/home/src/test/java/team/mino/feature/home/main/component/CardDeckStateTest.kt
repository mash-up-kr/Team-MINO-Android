package team.mino.feature.home.main.component

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.domain.model.PlaceCard
import team.mino.core.domain.model.PlaceCategoryLabel

private val AlwaysSaveable = object : SaverScope {
    override fun canBeSaved(value: Any) = true
}

private fun <T : Any> Saver<T, Any>.saveForTest(value: T): Any =
    with(AlwaysSaveable) { save(value)!! }

/** data-model.md §3.1 상태 전이표와 계약 C-07·C-10·C-12를 확인한다. */
class CardDeckStateTest {

    private fun card(pinId: String) = PlaceCard(
        pinId = pinId,
        placeName = "장소 $pinId",
        address = "주소 $pinId",
        imageUrls = emptyList(),
        label = PlaceCategoryLabel.WORTH_VISITING,
    )

    private fun deckOf(vararg pinIds: String) = CardDeckState().apply {
        setCards(pinIds.map(::card))
    }

    @Test
    fun `카드를 넘기면 다음 카드가 최상단이 되고 되돌릴 수 있다`() {
        val state = deckOf("a", "b")

        assertEquals("a", state.confirmCurrent()?.pinId)

        assertEquals("b", state.currentCard?.pinId)
        assertEquals(1, state.remainingCount)
        assertTrue(state.canUndo)

        state.undo()

        assertEquals("a", state.currentCard?.pinId)
        assertEquals(2, state.remainingCount)
        assertFalse(state.canUndo)
    }

    @Test
    fun `되돌릴 카드가 없거나 애니메이션 중이면 무동작이다`() {
        val state = deckOf("a", "b")

        state.undo()
        assertEquals("a", state.currentCard?.pinId)

        state.isAnimating = true
        assertNull(state.confirmCurrent())
        assertEquals("a", state.currentCard?.pinId)
    }

    @Test
    fun `장소 가리기는 현재 덱에서만 빼고 다음 카드를 올린다`() {
        val state = deckOf("a", "b")

        state.hidePlace("a")

        assertEquals("b", state.currentCard?.pinId)
        assertEquals(1, state.remainingCount)

        // 새 목록에 다시 들어오면 정상 노출된다 (C-12)
        state.setCards(listOf(card("a"), card("c")))
        assertEquals("a", state.currentCard?.pinId)
    }

    @Test
    fun `덱은 중복 pinId를 앞의 것만 남기고 최대 10장이다`() {
        val state = CardDeckState()

        state.setCards((1..12).map { card("p$it") } + card("p1"))

        assertEquals(10, state.remainingCount)
        assertEquals("p1", state.currentCard?.pinId)
    }

    @Test
    fun `같은 목록 재주입은 진행을 유지하고 다른 목록은 초기화한다`() {
        val state = deckOf("a", "b")
        state.confirmCurrent()

        state.setCards(listOf(card("a"), card("b")))
        assertEquals("b", state.currentCard?.pinId)

        state.setCards(listOf(card("x"), card("y")))
        assertEquals("x", state.currentCard?.pinId)
        assertFalse(state.canUndo)
    }

    @Test
    fun `복원하면 카드 본문 없이도 진행 위치가 살아난다`() {
        val state = deckOf("a", "b", "c")
        state.confirmCurrent()
        state.hidePlace("b")

        val saver: Saver<CardDeckState, Any> = CardDeckState.Saver
        val saved = saver.saveForTest(state)
        val restored = saver.restore(saved)!!
        restored.setCards(listOf(card("a"), card("b"), card("c")))

        assertEquals("c", restored.currentCard?.pinId)
        assertTrue(restored.canUndo)
    }

    @Test
    fun `마지막 1장까지 가리면 덱이 비고 뒷장도 남지 않는다`() {
        val state = deckOf("a", "b")

        assertEquals("b", state.cardAtDepth(1)?.pinId)

        state.hidePlace("a")
        state.hidePlace("b")

        assertNull(state.currentCard)
        // 잔여 0장도 `장소 더 보기` 노출 조건(2장 이하)을 만족한다 (C-17)
        assertEquals(0, state.remainingCount)
        assertNull(state.cardAtDepth(1))
    }
}
