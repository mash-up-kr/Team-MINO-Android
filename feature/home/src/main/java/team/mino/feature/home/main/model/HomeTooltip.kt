package team.mino.feature.home.main.model

import androidx.compose.runtime.Immutable
import team.mino.core.domain.model.DeckSort

/**
 * 방 캐릭터 옆에 잠깐 떴다 사라지는 안내. 화면을 가리지 않으므로 조작을 막지 않는다(spec UX-003).
 *
 * 한 번에 하나만 뜬다 — 방 전환은 덱 전환을 동반하고 전환 직후 예고가 뒤이어 뜰 수 있어(spec EC-012)
 * 마지막 것이 이긴다. 노출 시간과 사라지는 시점은 이 타입이 아니라 `HomeViewModel`이 소유한다.
 *
 * 문구 조립은 화면의 몫이다. 여기 담는 것은 문구에 들어갈 값뿐이다.
 */
@Immutable
internal sealed interface HomeTooltip {
    /** 방이 바뀐 직후. 수동 변경과 자동 전환을 구분하지 않는다(spec FR-016). */
    data class RoomChanged(val roomName: String) : HomeTooltip

    /**
     * 잔여 카드가 얼마 남지 않아 **실제로 다음에 올** 대상을 미리 알린다(spec FR-015).
     * 가리킬 대상이 없으면 이 값을 만들지 않는다 — 「없음」을 뜻하는 갈래를 두지 않는 이유다.
     */
    sealed interface DeckAhead : HomeTooltip {
        /** 같은 방의 다음 정렬. */
        data class NextSort(val sort: DeckSort) : DeckAhead

        /** 현재 방을 다 봐 넘어갈 다음 방. */
        data class NextRoom(val roomName: String) : DeckAhead
    }
}
