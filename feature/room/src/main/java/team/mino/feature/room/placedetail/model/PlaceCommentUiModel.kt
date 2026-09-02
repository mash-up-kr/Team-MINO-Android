@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.placedetail.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.domain.model.PlaceComment
import team.mino.core.domain.model.RoomColor
import java.time.ZoneId
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/**
 * 코멘트 한 줄이 그리는 것.
 *
 * 필드가 도메인 `PlaceComment`와 1:1이라 도메인 타입을 그대로 쓰지 못할 이유는 없다. 그럼에도 UiModel을 두는 것은
 * **아바타 표현이 서버와 협의 중**이라(`docs/specs/place-detail/contracts/place-api.md` §5) 정본이 정해졌을 때
 * 변경이 이 경계 안에서 끝나게 하기 위해서다
 * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §2.1).
 *
 * **표기 문자열이 아니라 시각 원본을 든다.** 구간 판정은 [placeCommentTime]이 그리는 시점에 하고, 그 결과도
 * 여기 담지 않는다(같은 문서 §6). 정렬은 서버가 준 순서를 그대로 따르므로 클라이언트가 [createdAt]으로 다시
 * 정렬하지 않는다.
 *
 * @property avatarColor 아바타 폴백이 쓰는 방 색. 색이 없는 작성자는 `null`이다.
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

/**
 * 코멘트 작성 시각을 어떤 문구로 쓸지 고른 결과.
 *
 * **문구 자체는 담지 않는다.** 어느 갈래인지와 그 갈래가 쓰는 수만 들고, 문자열은 이것을 받는 컴포저블이
 * `:feature:room`의 리소스에서 꺼낸다(`docs/specs/place-detail/research.md` D22).
 */
internal sealed interface PlaceCommentTime {
    data object JustNow : PlaceCommentTime

    data class HoursAgo(val hours: Int) : PlaceCommentTime

    data class DaysAgo(val days: Int) : PlaceCommentTime

    data class AbsoluteDate(val year: Int, val month: Int, val day: Int) : PlaceCommentTime
}

/**
 * 경과 시간을 네 구간 중 하나로 끊는다(spec FR-028).
 *
 * **입력이 둘인 순수 함수다.** 「지금」을 여기서 읽지 않고 [observedAt]으로 받는다 — 함수가 현재 시각을 직접
 * 읽으면 컴포지션마다 결과가 달라져 "실시간으로 다시 계산하지 않는다"(spec EC-028)를 확인할 수단이 사라진다.
 * [observedAt]은 ViewModel이 코멘트 목록을 만들 때 한 번 읽어 상태에 실은 값이다
 * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §6.1).
 *
 * **음수 경과도 1시간 미만으로 흡수한다**(spec EC-029). 기기 시각이 서버보다 뒤처져 [createdAt]이 미래로
 * 들어와도 `-1시간 전` 같은 표기가 새어 나가지 않게 하는 하한이다.
 *
 * 7일 이상은 절대 날짜라 시간대가 필요하다. 기기의 시간대로 끊는 것이 사용자가 보는 달력과 같다 — 구간
 * 판정 쪽은 [Instant] 비교라 시간대의 영향을 받지 않는다(`docs/specs/place-detail/research.md` D22).
 */
internal fun placeCommentTime(
    createdAt: Instant,
    observedAt: Instant,
): PlaceCommentTime {
    val elapsed = observedAt - createdAt
    return when {
        elapsed < 1.hours -> PlaceCommentTime.JustNow
        elapsed < 1.days -> PlaceCommentTime.HoursAgo(elapsed.inWholeHours.toInt())
        elapsed < 7.days -> PlaceCommentTime.DaysAgo(elapsed.inWholeDays.toInt())
        else -> {
            val date = createdAt.toJavaInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            PlaceCommentTime.AbsoluteDate(
                year = date.year,
                month = date.monthValue,
                day = date.dayOfMonth,
            )
        }
    }
}
