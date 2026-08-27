package team.mino.core.data.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 공유받은 링크를 방들에 핀으로 추가하는 요청.
 *
 * [url]은 공유받은 원문 URL 그대로다. 클라이언트는 도메인을 검사하지 않는다 — 지원 여부 판정은 서버가 하며,
 * 근거는 `docs/specs/shared-link-receiver/research.md` R-002.
 *
 * [roomIds]는 사용자가 고른 방 전부다. **한 요청이 방 전부를 담으므로 방마다 쪼개 보내지 않는다** — 서버가
 * 배열을 받고 방 단위 분해도 서버가 한다(같은 문서 R-021,
 * `docs/specs/shared-link-receiver/contracts/shared-place-save-api.md` §1.1).
 *
 * 비어 있는 [roomIds]로는 만들어지지 않는다. 고른 방이 없으면 `[저장하기]`가 비활성이다(FR-009).
 */
@Serializable
internal data class PinCreateRequest(
    val url: String,
    val roomIds: List<String>,
)
