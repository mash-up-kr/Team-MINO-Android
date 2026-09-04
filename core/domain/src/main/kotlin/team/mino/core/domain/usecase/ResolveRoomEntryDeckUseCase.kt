package team.mino.core.domain.usecase

import team.mino.core.domain.model.DeckContext
import team.mino.core.domain.model.DeckKey
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.NextDeck
import javax.inject.Inject

/**
 * 「홈 방 시트」에서 방을 직접 골랐을 때 무엇을 보여줄지 판정한다 — `contracts/home-ui.md` §4.1
 * 「수동 방 변경」이 단일 출처다(FR-024).
 *
 * [ResolveNextDeckUseCase](자동 전환)와 달리 탐색 범위를 [roomId] 하나로 한정한다. 부수효과도 I/O도
 * 없는 순수 함수다.
 *
 * **`NextRoom`을 절대 내지 않는다.** 그것이 "다른 방으로 넘기지 않는다"(FR-024·SC-008)의 코드
 * 표현이다. 저장 장소가 0개인 방은 세 정렬이 모두 후보 0건이므로 호출자가 [DeckContext.exhausted]에
 * 그 방의 세 [DeckKey]를 전부 채워 넘긴다고 전제한다 — 여기서 `placeCount`를 따로 검사하지 않는다
 * (EC-020·022).
 */
class ResolveRoomEntryDeckUseCase @Inject constructor() {
    operator fun invoke(context: DeckContext, roomId: String): NextDeck {
        val remainingSort =
            DeckSort.entries.firstOrNull { sort ->
                DeckKey(roomId = roomId, sort = sort) !in context.exhausted
            }

        return remainingSort?.let { NextDeck.SameRoom(it) } ?: NextDeck.AllExhausted
    }
}
