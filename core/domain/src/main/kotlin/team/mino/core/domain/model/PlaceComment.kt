package team.mino.core.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 장소에 달린 코멘트 하나.
 *
 * [canDelete]는 **[⋮] 노출 여부의 유일한 근거다.** 작성자가 나인지 클라이언트가 다시 따지지 않는다 —
 * `docs/specs/place-detail/research.md` D6.
 *
 * [createdAt]은 서버가 준 절대 시각 그대로다. `방금`·`N시간 전`·`N일 전`·`NNNN년 NN월 NN일`로 끊는 구간
 * 판정과 문구 조립은 feature의 UI 매핑이 한다(`docs/specs/place-detail/data-model.md` §2) — 도메인은
 * 표시 문구를 갖지 않는다. 정렬에도 쓰지 않는다 — 나열 순서는 서버가 준 그대로다(같은 스펙 research.md D11).
 */
@OptIn(ExperimentalTime::class)
data class PlaceComment(
    val id: String,
    val content: String,
    val createdAt: Instant,
    val author: PlaceCommentAuthor,
    val canDelete: Boolean,
)

/**
 * 코멘트 작성자.
 *
 * [PlaceRegistrant]와 필드가 같지만 별개 타입으로 둔다. 두 값은 서로 다른 응답에서 오고, 한쪽이 늘어날 때
 * 다른 쪽이 끌려가지 않게 한다.
 */
data class PlaceCommentAuthor(
    val userId: String,
    val nickname: String,
    val avatarColor: RoomColor?,
)

/**
 * 코멘트 한 페이지.
 *
 * 서버가 **역방향 페이징**이다 — [page] 0이 최신이고, 위로 스크롤할 때 다음 페이지(더 오래된 것)를 목록 앞에 붙인다
 * (`docs/specs/place-detail/research.md` D11). 화면의 나열은 오래된 것이 위이고(FR-010),
 * [comments]는 페이지 안 순서(오래된 것이 먼저)를 서버가 준 그대로 유지한다.
 *
 * [hasOlder]는 서버 `pagination`에서 Mapper가 도출한다. 화면이 "더 받을 이전 페이지가 있는지"만 알면 되므로
 * 전체 개수나 페이지 수를 올리지 않는다.
 */
data class PlaceCommentPage(
    val comments: List<PlaceComment>,
    val page: Int,
    val hasOlder: Boolean,
)
