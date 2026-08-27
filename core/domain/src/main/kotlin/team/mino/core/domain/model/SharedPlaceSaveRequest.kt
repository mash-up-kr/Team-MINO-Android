package team.mino.core.domain.model

/**
 * 한 번의 공유에서 사용자가 확정한 저장 예약.
 *
 * **이 타입 하나가 요청 하나다.** 서버가 [roomIds] 배열을 받으므로 방마다 쪼개지 않으며, 방 단위로 갈라
 * 저장하고 성패를 확정하는 것은 서버의 몫이다 — `docs/specs/shared-link-receiver/research.md` R-021.
 *
 * [url]은 공유받은 원문 그대로다. 지원하는 출처인지의 판정은 서버가 하므로 여기서 형식이나 도메인을 좁히지 않는다.
 *
 * [roomIds]는 비어 있지 않다. 고른 방이 없으면 저장 버튼 자체가 비활성이라 이 타입이 만들어지지 않는다.
 */
data class SharedPlaceSaveRequest(
    val url: String,
    val roomIds: List<String>,
)
