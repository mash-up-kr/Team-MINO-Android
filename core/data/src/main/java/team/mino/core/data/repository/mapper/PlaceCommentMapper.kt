@file:OptIn(ExperimentalTime::class)

package team.mino.core.data.repository.mapper

import team.mino.core.data.network.dto.response.CommentAuthorResponse
import team.mino.core.data.network.dto.response.CommentPageResponse
import team.mino.core.data.network.dto.response.CommentResponse
import team.mino.core.domain.model.PlaceComment
import team.mino.core.domain.model.PlaceCommentAuthor
import team.mino.core.domain.model.PlaceCommentPage
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 코멘트 한 페이지를 도메인으로 읽는다.
 *
 * **`hasNext`를 `hasOlder`로 바꿔 든다.** 서버는 "더 받을 다음 페이지가 있는가"를 말하지만 역방향 페이징이라
 * 그 다음 페이지가 곧 더 **이전** 코멘트다 — 화면의 물음에 맞춘 이름이며 뒤집는 자리는 여기 하나다
 * (`docs/specs/place-detail/research.md` D11, `contracts/comment-api.md` §1.2).
 *
 * **순서를 건드리지 않는다.** 페이지 안은 오래된 것이 먼저이고 그대로 싣는다. 페이지 사이의 배치(앞에 붙일지
 * 뒤에 붙일지)는 화면이 정한다. `createdAt`으로 다시 정렬하지도 않는다.
 *
 * 서버 키 `data`가 도메인에서 `comments`가 되는 것도 이 자리의 몫이다 — DTO는 서버 이름을 그대로 든다.
 */
internal fun CommentPageResponse.toDomain(): PlaceCommentPage =
    PlaceCommentPage(
        comments = data.map(CommentResponse::toDomain),
        page = pagination.page,
        hasOlder = pagination.hasNext,
    )

/**
 * 코멘트 한 건을 도메인으로 읽는다. 목록과 작성(201) 응답이 같은 스키마여서 두 경로가 이 함수를 공유한다
 * (`docs/specs/place-detail/contracts/comment-api.md` §2).
 *
 * [PlaceComment.createdAt]은 서버가 준 절대 시각을 `Instant.parse`로 옮기기만 한다 — `PlaceMapper`가
 * `savedAt`에 쓰는 것과 같은 형태다. **표기 환산(`방금`·`N시간 전`·`N일 전`·`NNNN년 NN월 NN일`)을 여기서
 * 하지 않는다.** 구간 판정과 문구 조립은 feature의 UI 매핑이 한다(같은 계약 §1.3, `data-model.md` §2).
 */
internal fun CommentResponse.toDomain(): PlaceComment =
    PlaceComment(
        id = id,
        content = content,
        createdAt = Instant.parse(createdAt),
        author = author.toDomain(),
        canDelete = canDelete,
    )

/**
 * 작성자를 도메인으로 읽는다.
 *
 * **유저 식별자 키가 어긋나는 것을 여기서 흡수한다** — 코멘트 응답은 `author.id`, 핀 상세 응답은
 * `createdBy.userId`인데 도메인은 둘 다 `userId`다(`contracts/place-api.md` §1.3).
 *
 * 아바타 색은 [toRoomColorOrNull]이 13색 팔레트로 읽고 모르는 값은 `null`로 떨어진다 — 서버 두 자리의 enum
 * 제약이 어긋나 있는 동안의 방어이며, `null`은 기본 아바타로 그려진다.
 */
private fun CommentAuthorResponse.toDomain(): PlaceCommentAuthor =
    PlaceCommentAuthor(
        userId = id,
        nickname = nickname,
        avatarColor = avatar.toRoomColorOrNull(),
    )
