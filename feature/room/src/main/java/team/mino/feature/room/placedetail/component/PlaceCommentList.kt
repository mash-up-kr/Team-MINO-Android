package team.mino.feature.room.placedetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.RoomColor
import team.mino.feature.room.component.SheetDividerThickness
import team.mino.feature.room.placedetail.model.PlaceCommentUiModel
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * `친구들의 코멘트` 영역의 본문. 오래된 코멘트가 위고 새 코멘트가 아래다(spec FR-010).
 *
 * **[comments]를 여기서 다시 정렬하지 않는다.** 받은 순서가 곧 화면 순서다 — 서버가 준 페이지 안의 순서와
 * ViewModel이 페이지를 잇는 방향이 이미 그 나열을 만든다(`docs/specs/place-detail/research.md` D11).
 *
 * **자체 스크롤 컨테이너를 만들지 않는다.** 목록과 입력 영역은 시트 콘텐츠와 함께 스크롤되며 시트가 스크롤 축을
 * 하나만 갖는다(spec EC-015). 여기에 `LazyColumn`이나 `verticalScroll`을 두면 축이 둘로 갈려 안쪽이 바깥쪽을
 * 삼킨다.
 *
 * **비어 있으면 [PlaceCommentEmpty]로 넘긴다.** 마지막 코멘트를 지운 순간 빈 상태로 돌아가는 것이 spec EC-014인데,
 * 그 판정의 근거는 목록이 비었다는 사실 하나뿐이라 목록 밖에 두면 근거와 표시가 갈린다. 호출부는 갈래를 다시
 * 만들지 않고 이 컴포저블 하나만 부른다.
 *
 * @param commentsObservedAt 각 코멘트의 경과 시간을 잴 기준 시각. 목록이 다시 만들어질 때만 갱신되는 값이라
 *   여기서 현재 시각을 읽지 않고 그대로 흘려보낸다
 *   (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §6.1).
 * @param hasOlderComments 더 오래된 페이지가 남아 있는지. 남아 있지 않으면 [onLoadOlderComments]를 부르지 않는다.
 * @param onLoadOlderComments 목록 맨 위 코멘트가 화면에 들어왔을 때. 같은 페이지를 두 번 받지 않도록 막는 것은
 *   ViewModel이므로 여기서 재진입을 세지 않는다.
 * @param onDeleteComment 내 코멘트의 [⋮] → `댓글 삭제`. 삭제할 코멘트의 id를 넘긴다.
 */
@OptIn(ExperimentalTime::class)
@Composable
internal fun PlaceCommentList(
    comments: ImmutableList<PlaceCommentUiModel>,
    commentsObservedAt: Instant,
    hasOlderComments: Boolean,
    onLoadOlderComments: () -> Unit,
    onDeleteComment: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 빈 상태도 이 Column 안에서 갈린다. [modifier]를 두 갈래에 각각 주입하면 한 인스턴스가 형제 둘에
    // 흘러 들어가므로, 받은 modifier를 쓰는 자리를 이 루트 하나로 둔다.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ItemSpacing),
    ) {
        if (comments.isEmpty()) {
            PlaceCommentEmpty()
            return@Column
        }

        // 맨 위 코멘트가 보이면 이전 페이지를 요청한다. 페이지가 앞에 붙으면 맨 위가 다른 코멘트로 바뀌므로
        // 그 id를 함께 키로 삼아, 새로 올라온 맨 위에서도 같은 판정이 한 번 더 일어나게 한다.
        var isTopCommentVisible by remember { mutableStateOf(false) }
        val topCommentId = comments.first().id
        LaunchedEffect(topCommentId, isTopCommentVisible, hasOlderComments) {
            if (isTopCommentVisible && hasOlderComments) onLoadOlderComments()
        }

        comments.forEachIndexed { index, comment ->
            if (index > 0) {
                HorizontalDivider(
                    thickness = SheetDividerThickness,
                    color = MinoAndroidTheme.colors.lineNormalNeutral,
                )
            }
            PlaceCommentItem(
                // 더 받을 페이지가 없으면 관측을 떼어 낸다. 붙여 두면 스크롤할 때마다 좌표 변환이 돌면서
                // 아무 결과도 낳지 않는다.
                modifier = if (hasOlderComments && comment.id == topCommentId) {
                    Modifier.reportVisibility { isTopCommentVisible = it }
                } else {
                    Modifier
                },
                comment = comment,
                observedAt = commentsObservedAt,
                onDeleteClick = { onDeleteComment(comment.id) },
            )
        }
    }
}

/**
 * 이 요소가 화면에 조금이라도 걸쳐 있는지를 알린다.
 *
 * 창 기준 경계는 스크롤 컨테이너가 잘라 낸 뒤의 값이라, 스크롤 밖으로 밀려난 요소는 빈 사각형이 된다. 목록이
 * 스크롤 축을 갖지 않으므로 `LazyListState`로는 같은 것을 알 수 없다.
 */
private fun Modifier.reportVisibility(onChange: (Boolean) -> Unit): Modifier =
    onGloballyPositioned { coordinates -> onChange(!coordinates.boundsInWindow().isEmpty) }

private val ItemSpacing = 20.dp

@OptIn(ExperimentalTime::class)
private val PreviewObservedAt = Instant.parse("2027-02-01T12:00:00Z")

@OptIn(ExperimentalTime::class)
@UiModePreviews
@Composable
private fun PlaceCommentListPreview() {
    MinoAndroidAppTheme {
        PlaceCommentList(
            commentsObservedAt = PreviewObservedAt,
            comments = persistentListOf(
                PlaceCommentUiModel(
                    id = "1",
                    content = "친구가 남긴 코멘트입니다.",
                    nickname = "서연",
                    avatarColor = RoomColor.LIGHT_BLUE,
                    canDelete = false,
                    createdAt = PreviewObservedAt - 20.minutes,
                ),
                PlaceCommentUiModel(
                    id = "2",
                    content = "친구가 남긴 코멘트입니다.".repeat(4),
                    nickname = "태훈",
                    avatarColor = null,
                    canDelete = true,
                    createdAt = PreviewObservedAt - 3.hours,
                ),
                PlaceCommentUiModel(
                    id = "3",
                    content = "친구가 남긴 코멘트입니다.",
                    nickname = "예린",
                    avatarColor = RoomColor.PURPLE,
                    canDelete = false,
                    createdAt = PreviewObservedAt - 3.days,
                ),
            ),
            hasOlderComments = false,
            onLoadOlderComments = {},
            onDeleteComment = {},
        )
    }
}

/** 코멘트가 0건일 때 빈 상태로 넘어가는 갈래(spec FR-011·EC-014). */
@OptIn(ExperimentalTime::class)
@UiModePreviews
@Composable
private fun PlaceCommentListEmptyPreview() {
    MinoAndroidAppTheme {
        PlaceCommentList(
            comments = persistentListOf(),
            commentsObservedAt = PreviewObservedAt,
            hasOlderComments = false,
            onLoadOlderComments = {},
            onDeleteComment = {},
        )
    }
}
