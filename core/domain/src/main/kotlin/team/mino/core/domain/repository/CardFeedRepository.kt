package team.mino.core.domain.repository

import team.mino.core.domain.model.PlaceCard

interface CardFeedRepository {
    /**
     * [roomId] 방의 카드덱을 가져온다. 최대 10장이며, 이미 본 카드를 제외하는 개인별 큐레이션은 서버가 한다.
     * 실패하면 도메인 예외를 전파한다 — 소비는 호출자 ViewModel의 책임이다.
     */
    suspend fun getCards(roomId: String): List<PlaceCard>

    /**
     * 사용자가 [pinId] 카드를 확인했음을 기록한다. 서버 큐레이션의 제외 조건이자 `친구들이 많이 본 곳` 집계 원천이다.
     *
     * 실패해도 예외를 던지지 않는다. 카드 넘김은 이미 일어났고 되돌릴 수 없으므로 사용자 흐름을 막아서는 안 된다.
     * 기록 실패의 대가는 다음 덱의 중복 노출뿐이니 구현체가 조용히 흘려야 한다.
     */
    suspend fun recordAccess(pinId: String)
}
