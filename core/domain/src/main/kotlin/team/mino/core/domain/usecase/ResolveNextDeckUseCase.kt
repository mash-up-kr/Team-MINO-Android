package team.mino.core.domain.usecase

import team.mino.core.domain.model.DeckContext
import team.mino.core.domain.model.DeckKey
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.NextDeck
import javax.inject.Inject

/**
 * 다음에 무엇을 보여줄지 판정한다 — 전환 규칙의 단일 출처(`contracts/home-ui.md` §4.1, research.md R-003).
 *
 * 판정 순서와 반환값은 그 계약이 소유하며 여기 다시 적지 않는다.
 *
 * 부수효과도 I/O도 없다. 상태도 들지 않는다 — 전환 시점마다 다시 부르는 함수이기 때문이다(FR-011).
 *
 * [DeckContext.currentSort]는 읽지 않는다. 1단계는 [DeckSort] 선언 순서를 **처음부터 전부** 훑으므로
 * 현재 정렬보다 앞선 덱이 남아 있으면 그리로 되돌아간다(TS-015).
 */
class ResolveNextDeckUseCase @Inject constructor() {
    operator fun invoke(context: DeckContext): NextDeck {
        val remainingSort =
            DeckSort.entries.firstOrNull { sort ->
                DeckKey(roomId = context.currentRoomId, sort = sort) !in context.exhausted
            }
        if (remainingSort != null) return NextDeck.SameRoom(remainingSort)

        // 현재 방 **다음** 자리부터다. 앞으로 되돌아가지 않는다(FR-012, EC-010).
        val nextRoom =
            context.rooms
                .asSequence()
                .dropWhile { it.id != context.currentRoomId }
                .drop(1)
                .firstOrNull { it.placeCount > 0 }

        return nextRoom?.let { NextDeck.NextRoom(it.id) } ?: NextDeck.AllExhausted
    }
}
