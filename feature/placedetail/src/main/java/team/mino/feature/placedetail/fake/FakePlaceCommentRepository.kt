package team.mino.feature.placedetail.fake

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import team.mino.core.domain.model.PlaceComment
import team.mino.core.domain.model.PlaceCommentPage
import team.mino.core.domain.repository.PlaceCommentRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * **이번 UI 라운드 한정 [PlaceCommentRepository] 구현이다. `tasks.md` T063이 `fake/` 패키지를 통째로 지운다.**
 *
 * API 연결 없이 화면을 검증하기 위한 것이며, 실제 구현은 `:core:data`가 갖는다(T060). 초기 데이터는
 * [FakePlaceDetailData]에서 오고, 작성·삭제 결과는 이 인스턴스가 들고 있어 화면에서 이어 볼 수 있다.
 *
 * **역방향 페이징을 서버와 같은 방향으로 흉내 낸다** — `page 0`이 최신이고 페이지 안은 오래된 것이 먼저다
 * (`docs/specs/place-detail/research.md` D11). 45건 샘플이 [FakePlaceDetailData.PAGE_SIZE] 기준 세 페이지라
 * 페이지 경계가 실제로 생긴다.
 */
@Singleton
internal class FakePlaceCommentRepository @Inject constructor() : PlaceCommentRepository {
    private val mutex = Mutex()
    private val commentsByPinId = mutableMapOf<String, MutableList<PlaceComment>>()
    private var addedCount = 0

    /**
     * 오래된 것이 먼저인 전량에서 뒤에서부터 [FakePlaceDetailData.PAGE_SIZE]씩 잘라 돌려준다. 잘라낸 페이지
     * 안의 순서는 뒤집지 않는다 — 페이지 사이의 배치는 화면이 정한다.
     */
    override suspend fun getComments(
        pinId: String,
        page: Int,
    ): PlaceCommentPage {
        delay(LOAD_DELAY_MS)
        val all = mutex.withLock { commentsOf(pinId).toList() }
        val pageSize = FakePlaceDetailData.PAGE_SIZE
        val end = (all.size - page * pageSize).coerceAtLeast(0)
        val start = (end - pageSize).coerceAtLeast(0)
        return PlaceCommentPage(
            comments = all.subList(start, end),
            page = page,
            hasOlder = start > 0,
        )
    }

    /** 만들어진 코멘트를 돌려준다(FR-014). 화면은 이 값을 목록 맨 아래에 덧붙인다. */
    override suspend fun addComment(
        pinId: String,
        content: String,
    ): PlaceComment {
        delay(ADD_DELAY_MS)
        return mutex.withLock {
            addedCount++
            val created =
                PlaceComment(
                    id = "fake-comment-added-$addedCount",
                    content = content,
                    author = ME,
                    canDelete = true,
                )
            commentsOf(pinId) += created
            created
        }
    }

    override suspend fun deleteComment(
        pinId: String,
        commentId: String,
    ) {
        delay(DELETE_DELAY_MS)
        mutex.withLock { commentsOf(pinId).removeAll { it.id == commentId } }
    }

    private fun commentsOf(pinId: String): MutableList<PlaceComment> =
        commentsByPinId.getOrPut(pinId) { FakePlaceDetailData.commentsOf(pinId).toMutableList() }

    private companion object {
        const val LOAD_DELAY_MS = 500L
        const val ADD_DELAY_MS = 400L
        const val DELETE_DELAY_MS = 300L

        /**
         * 새로 쓴 코멘트의 작성자.
         *
         * 실제로는 서버가 내 프로필로 채워 준다. [FakePlaceDetailData]가 든 「나」를 그대로 가리켜, [⋮]가 붙는
         * 항목이 한 사람으로 모이는 것을 값의 일치가 아니라 같은 참조가 보장하게 한다.
         */
        val ME = FakePlaceDetailData.ME
    }
}
