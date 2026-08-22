package team.mino.feature.home.main.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import team.mino.core.domain.model.PlaceCard

/**
 * 카드덱의 진행 상태 홀더. 화면 상태(`UiState`)가 아니라 컴포넌트가 보유하는 UI 상태이며,
 * 호출자가 관찰할 수 있도록 호이스팅된다.
 *
 * 외부에 여는 상태 변경은 [hidePlace] 하나다. 카드 전환·되돌리기는 제스처로만 일어나므로
 * 덱 컴포저블만 쓰는 `internal` 함수로 둔다.
 */
@Stable
internal class CardDeckState(
    initialCurrentIndex: Int = 0,
    initialHiddenPinIds: Set<String> = emptySet(),
    private var restoredUndonePinId: String? = null,
) {
    /** [setCards]로 구성된 덱 원본. 중복 [PlaceCard.pinId]가 제거되고 최대 [MAX_DECK_SIZE]장이다. */
    private var deck by mutableStateOf<List<PlaceCard>>(emptyList())

    private var currentIndex by mutableIntStateOf(initialCurrentIndex)

    private var hiddenPinIds by mutableStateOf(initialHiddenPinIds)

    private var undoneCard by mutableStateOf<PlaceCard?>(null)

    /** `장소 가리기`로 빠진 카드를 제외한 현재 덱. 파생값이므로 따로 저장하지 않는다. */
    private val visibleCards: List<PlaceCard> by derivedStateOf {
        deck.filterNot { it.pinId in hiddenPinIds }
    }

    /** 최상단 카드. 덱을 다 넘겼거나 덱이 비면 null이다. */
    val currentCard: PlaceCard? by derivedStateOf { visibleCards.getOrNull(currentIndex) }

    /** 최상단 카드를 포함한 잔여 장수. */
    val remainingCount: Int by derivedStateOf { (visibleCards.size - currentIndex).coerceAtLeast(0) }

    /** 되돌릴 카드가 있는지. false면 우→좌 스와이프는 무동작이다. */
    val canUndo: Boolean by derivedStateOf { undoneCard != null }

    /** 전환 애니메이션 진행 여부. true면 스와이프 입력을 무시한다. */
    var isAnimating: Boolean by mutableStateOf(false)
        internal set

    /** 최상단 카드로부터 [depth]장 뒤의 카드. 뒷장을 실제 내용으로 그릴 때 쓴다. */
    internal fun cardAtDepth(depth: Int): PlaceCard? = visibleCards.getOrNull(currentIndex + depth)

    /** 최상단 카드를 현재 덱에서만 뺀다. 새 목록에 다시 들어오면 정상 노출된다. */
    fun hidePlace(pinId: String) {
        hiddenPinIds = hiddenPinIds + pinId
    }

    /**
     * 새 목록으로 덱을 구성한다. 같은 목록이 다시 들어오면 진행 상태를 건드리지 않고,
     * 다른 목록이면 전체를 초기화한다.
     */
    internal fun setCards(cards: List<PlaceCard>) {
        val next = cards.distinctBy { it.pinId }.take(MAX_DECK_SIZE)
        if (next.map { it.pinId } == deck.map { it.pinId }) return

        val isFirstBind = deck.isEmpty()
        deck = next
        if (isFirstBind) {
            // 프로세스 재생성 복원 경로. 저장해둔 pinId에 재주입된 카드 본문을 다시 붙인다.
            undoneCard = restoredUndonePinId?.let { pinId -> next.firstOrNull { it.pinId == pinId } }
            restoredUndonePinId = null
            currentIndex = currentIndex.coerceIn(0, visibleCards.size)
        } else {
            currentIndex = 0
            undoneCard = null
            hiddenPinIds = emptySet()
        }
    }

    /**
     * 최상단 카드를 넘겨 확인 처리하고 그 카드를 돌려준다. 호출자는 반환값으로 확인 신호를 보낸다.
     * 애니메이션 중이거나 넘길 카드가 없으면 아무 일도 하지 않고 null이다.
     */
    internal fun confirmCurrent(): PlaceCard? {
        if (isAnimating) return null
        val card = currentCard ?: return null
        currentIndex += 1
        undoneCard = card
        return card
    }

    /** 직전에 넘긴 카드 1장을 복구한다. 이미 나간 확인 신호는 되돌리지 않는다. */
    internal fun undo() {
        if (isAnimating || !canUndo) return
        currentIndex = (currentIndex - 1).coerceAtLeast(0)
        undoneCard = null
    }

    internal companion object {
        /**
         * 카드 본문은 저장하지 않는다. 진행 위치와 pinId만 남기고 본문은 재주입된 목록에서 복원한다.
         * 덕분에 [PlaceCard]가 `Parcelable`일 필요가 없다.
         */
        val Saver: Saver<CardDeckState, Any> = mapSaver(
            save = { state: CardDeckState ->
                mapOf(
                    KEY_CURRENT_INDEX to state.currentIndex,
                    KEY_HIDDEN_PIN_IDS to state.hiddenPinIds.toTypedArray(),
                    KEY_UNDONE_PIN_ID to state.undoneCard?.pinId,
                )
            },
            restore = { saved ->
                CardDeckState(
                    initialCurrentIndex = saved[KEY_CURRENT_INDEX] as? Int ?: 0,
                    initialHiddenPinIds = (saved[KEY_HIDDEN_PIN_IDS] as? Array<*>)
                        .orEmpty()
                        .filterIsInstance<String>()
                        .toSet(),
                    restoredUndonePinId = saved[KEY_UNDONE_PIN_ID] as? String,
                )
            },
        )
    }
}

@Composable
internal fun rememberCardDeckState(): CardDeckState =
    rememberSaveable(saver = CardDeckState.Saver) { CardDeckState() }

private const val MAX_DECK_SIZE = 10
private const val KEY_CURRENT_INDEX = "currentIndex"
private const val KEY_HIDDEN_PIN_IDS = "hiddenPinIds"
private const val KEY_UNDONE_PIN_ID = "undonePinId"
