package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/pins/{pinId}/comments` 응답 **본문 전체**.
 *
 * 이 엔드포인트만 [MinoResponse]로 벗기지 않는다. 서버가 `data`와 **나란히** [pagination]을 싣기 때문에
 * (`{ "data": [...], "pagination": {...} }`) 봉투 타입으로는 형태를 표현할 수 없다. 봉투 ADR
 * (`docs/adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md`)이 금지한 것은 알맹이만 감싸는
 * 의미 없는 래퍼(`XxxListResponse(val data: ...)`)이고, 이 타입은 도메인이 실제로 읽는 값
 * ([PaginationResponse.hasNext] → `PlaceCommentPage.hasOlder`)을 함께 든다.
 *
 * `data`라는 이름은 서버 키 그대로다. 도메인으로 옮길 때 `comments`가 되며 그 이름은 Mapper가 준다.
 *
 * 역방향 페이징이다 — `page` 0이 최신이고, 한 페이지 안에서는 오래된 코멘트가 먼저 온다.
 * 클라이언트가 재정렬하지 않는다
 * (`docs/specs/place-detail/contracts/comment-api.md` §1.2).
 */
@Serializable
internal data class CommentPageResponse(
    val data: List<CommentResponse> = emptyList(),
    val pagination: PaginationResponse,
)

/**
 * 코멘트 한 건. 목록(`GET /api/v1/pins/{pinId}/comments`)과 작성(`POST` 201)의 `data`가 같은 스키마여서
 * 두 응답이 이 타입을 공유한다 — 작성 응답을 그대로 목록 끝에 붙일 수 있는 근거다(FR-014).
 *
 * [createdAt]은 ISO-8601 문자열로 받는다. `kotlin.time.Instant`로 옮기는 것은 Mapper의 몫이고
 * (`docs/specs/place-detail/data-model.md` §2), `방금`·`N시간 전` 같은 표기 환산은 그보다 더 위인
 * feature의 UI 매핑이 한다(`contracts/comment-api.md` §1.3). DTO는 서버가 준 절대 시각만 든다.
 *
 * [canDelete]는 서버가 내리는 판정이다. 클라이언트가 작성자를 다시 따지지 않는다
 * (`docs/specs/place-detail/research.md` D6).
 *
 * 계약은 `docs/specs/place-detail/contracts/comment-api.md` §1이 소유한다.
 */
@Serializable
internal data class CommentResponse(
    val id: String,
    val content: String,
    val createdAt: String,
    val author: CommentAuthorResponse,
    val canDelete: Boolean,
)

/**
 * [CommentResponse.author]의 서버 표현.
 *
 * 유저 식별자 키가 `userId`가 아니라 [id]다 — 핀 상세의 [PinDetailCreatedByResponse]와 다르다.
 * 두 응답의 이름이 실제로 어긋나 있어 DTO도 각자 서버를 그대로 비춘다.
 *
 * [avatar]의 색을 enum으로 좁히지 않는 이유는 [PinDetailCreatedByResponse]와 같다 — 서버 두 자리의
 * 제약이 어긋나 있고, 팔레트 해석과 모르는 값의 처리는 Mapper가 한다.
 */
@Serializable
internal data class CommentAuthorResponse(
    val id: String,
    val nickname: String,
    val avatar: AvatarResponse? = null,
)
