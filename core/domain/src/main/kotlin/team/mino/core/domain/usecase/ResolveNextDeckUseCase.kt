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
 * **탐색 축은 「한 정렬로 모든 방 → 다음 정렬」이다.** [DeckSort] 선언 순서로 정렬을 훑고, 각 정렬에서
 * [DeckContext.rooms] 순서로 방을 훑어 [DeckContext.exhausted]에 없고 `placeCount > 0`인 첫 칸을 고른다.
 * [DeckContext.currentSort]는 읽지 않는다 — 현재 정렬보다 앞선 칸이 남아 있으면 그리로 되돌아간다(TS-021).
 * [DeckContext.rooms]는 받은 순서 그대로 훑을 뿐 재배치하지 않는다(FR-012, TS-019a).
 */
class ResolveNextDeckUseCase @Inject constructor() {
    operator fun invoke(context: DeckContext): NextDeck {
        for (sort in DeckSort.entries) {
            for (room in context.rooms) {
                if (room.placeCount <= 0) continue
                if (DeckKey(roomId = room.id, sort = sort) in context.exhausted) continue

                return if (room.id == context.currentRoomId) {
                    NextDeck.SameRoom(sort)
                } else {
                    NextDeck.NextRoom(roomId = room.id, sort = sort)
                }
            }
        }
        return NextDeck.AllExhausted
    }
}
