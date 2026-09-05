package team.mino.core.data.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 코멘트를 남기는 요청 — `POST /api/v1/pins/{pinId}/comments`.
 *
 * 대상 핀은 본문이 아니라 경로의 `pinId`가 가리키므로 [content] 하나뿐이다.
 *
 * **[content]를 클라이언트가 다듬지 않는다.** 앞뒤 공백 제거는 서버가 하고, 200자 상한은 입력
 * 컴포저블이 201자째를 받지 않는 것으로 이미 막힌다
 * (`docs/specs/place-detail/contracts/comment-api.md` §2).
 */
@Serializable
internal data class CommentCreateRequest(
    val content: String,
)
