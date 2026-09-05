@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.placedetail.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.domain.model.PlaceComment
import team.mino.core.domain.model.RoomColor
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 코멘트 한 줄이 그리는 것.
 *
 * 필드가 도메인 `PlaceComment`와 1:1이라 도메인 타입을 그대로 쓰지 못할 이유는 없다. 그럼에도 UiModel을 두는 것은
 * **아바타 표현이 서버와 협의 중**이라(`docs/specs/place-detail/contracts/place-api.md` §5) 정본이 정해졌을 때
 * 변경이 이 경계 안에서 끝나게 하기 위해서다
 * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §2.1).
 *
 * **표기 문자열이 아니라 시각 원본을 든다.** 구간 판정은 [team.mino.core.common.kotlin.util.elapsedTime]이
 * 그리는 시점에 하고, 그 결과도
 * 여기 담지 않는다(같은 문서 §6). 정렬은 서버가 준 순서를 그대로 따르므로 클라이언트가 [createdAt]으로 다시
 * 정렬하지 않는다.
 *
 * @property avatarColor 서버가 아바타 대신 주는 색. 색이 없는 작성자는 `null`이다. **아직 어디에도 그리지
 *   않는다** — 시안의 코멘트 아바타는 기본 실루엣(`Avatar/Avatar`)이고 색을 쓰는 표현이 없다. 값을 여기까지
 *   들고 오는 것은 정본이 정해졌을 때 변경이 이 경계 안에서 끝나게 하려는 것이며(위 문단), 그 협의가
 *   `docs/specs/place-detail/contracts/place-api.md` §5에 열려 있다.
 * @property canDelete [⋮] 노출 여부의 유일한 근거다. 작성자 id와 내 id를 비교해 다시 판정하지 않는다
 *   (spec FR-015, `docs/specs/place-detail/research.md` D6).
 */
@Immutable
internal data class PlaceCommentUiModel(
    val id: String,
    val content: String,
    val nickname: String,
    val avatarColor: RoomColor?,
    val canDelete: Boolean,
    val createdAt: Instant,
)

/**
 * 도메인 코멘트를 화면이 쓰는 형태로 옮긴다.
 *
 * ViewModel과 프리뷰가 같은 것을 부른다 — 두 벌로 두면 필드가 늘 때 한쪽만 따라가고, 갈라져도 컴파일은
 * 통과해서 프리뷰가 실제와 다른 값을 그린다.
 */
internal fun PlaceComment.toUiModel(): PlaceCommentUiModel =
    PlaceCommentUiModel(
        id = id,
        content = content,
        nickname = author.nickname,
        avatarColor = author.avatarColor,
        canDelete = canDelete,
        createdAt = createdAt,
    )

internal fun List<PlaceComment>.toUiModels(): ImmutableList<PlaceCommentUiModel> =
    map { it.toUiModel() }.toImmutableList()
