package team.mino.feature.room.placedetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatar
import team.mino.core.designsystem.component.avatar.MinoAvatarSize
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.MoreVertical
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.RoomColor
import team.mino.feature.room.R
import team.mino.feature.room.placedetail.model.PlaceCommentTime
import team.mino.feature.room.placedetail.model.PlaceCommentUiModel
import team.mino.feature.room.placedetail.model.placeCommentTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 코멘트 한 건. 작성자 아바타·닉네임이 한 줄이고 그 아래에 본문이 놓인다(spec FR-010).
 *
 * **본문의 높이를 제한하지 않는다.** 줄 수를 자르지도 [더보기]로 접지도 않아 항상 전문이 보인다
 * (spec FR-021·TS-027). 입력 상한이 200자여서 높이는 그 안에서 저절로 묶인다.
 *
 * **작성 시각은 본문 아래 오른쪽 끝에 붙는다**(spec FR-028). 표기는 [placeCommentTime]이 그리는 시점에 한 번
 * 고르고 다시 계산하지 않아, 목록을 띄워 둔 채로는 구간 경계를 넘어도 글자가 바뀌지 않는다(spec EC-028).
 *
 * **[⋮]는 [PlaceCommentUiModel.canDelete]가 `true`일 때만 그린다.** 남의 코멘트에는 비활성으로도 두지 않아
 * 삭제 진입점 자체가 없고, 그래서 눌러 보고 막히는 일이 생기지 않는다(spec FR-015·UX-008·TS-026).
 *
 * **아바타 그림을 아직 그리지 못한다.** 코멘트 응답이 주는 아바타가 `avatar: { color }`인데 프로필 쪽은
 * `avatar: { id }`라 어느 쪽이 정본인지 서버와 협의 중이고
 * (`docs/specs/place-detail/contracts/place-api.md` §5), 색에서 12종 중 하나를 고르는 대응표는 어디에도 없다.
 * 그래서 [MinoAvatar]의 기본 아바타로 그린다(spec EC-004). 정본이 정해지면 [PlaceCommentUiModel]과 이 자리만
 * 바뀐다.
 *
 * 메뉴가 열려 있는지는 이 컴포저블이 [rememberSaveable]로 든다. 화면의 상태가 아니라 이 항목의 표시 여부라
 * `PlaceDetailUiState`에 자리가 없고(`docs/specs/place-detail/contracts/place-detail-main-contract.md` §2),
 * 여기 두면 두 코멘트의 메뉴가 동시에 열리는 상태 자체가 만들어지지 않는다.
 *
 * @param observedAt 경과 시간을 잴 기준 시각. ViewModel이 코멘트 목록을 만들 때 한 번 읽어 상태에 실은 값이며,
 *   여기서 현재 시각을 다시 읽지 않는다
 *   (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §6.1).
 * @param onDeleteClick 메뉴에서 `댓글 삭제`를 골랐을 때. 확인 절차 없이 곧바로 지우는 것이 사양이라
 *   (spec FR-015·TS-025) 이 콜백과 실제 삭제 사이에 다시 묻는 단계를 두지 않는다.
 */
@OptIn(ExperimentalTime::class)
@Composable
internal fun PlaceCommentItem(
    comment: PlaceCommentUiModel,
    observedAt: Instant,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HeaderContentSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AvatarNicknameSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MinoAvatar(variant = MinoAvatarVariant.Person, size = MinoAvatarSize.Small)
                Text(
                    text = comment.nickname,
                    color = MinoAndroidTheme.colors.labelAlternative,
                    style = MinoAndroidTheme.typography.label1NormalMedium,
                )
            }
            if (comment.canDelete) {
                CommentMenuButton(onDeleteClick = onDeleteClick)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(ContentTimeSpacing)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = comment.content,
                color = MinoAndroidTheme.colors.labelNormal,
                style = MinoAndroidTheme.typography.label1NormalRegular,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = remember(comment.createdAt, observedAt) {
                    placeCommentTime(comment.createdAt, observedAt)
                }.label(),
                color = MinoAndroidTheme.colors.labelAlternative,
                style = MinoAndroidTheme.typography.caption2Regular,
                textAlign = TextAlign.End,
            )
        }
    }
}

/**
 * 고른 갈래에 문자열을 입힌다.
 *
 * 구간 판정([placeCommentTime])과 갈라 둔 것은 판정만 순수 함수로 남기기 위해서다 — 문자열은 리소스를 읽어야
 * 해서 컴포지션 밖에서 부를 수 없다.
 */
@Composable
private fun PlaceCommentTime.label(): String =
    when (this) {
        PlaceCommentTime.JustNow -> stringResource(R.string.placedetail_comment_time_just_now)
        is PlaceCommentTime.HoursAgo ->
            stringResource(R.string.placedetail_comment_time_hours_ago, hours)
        is PlaceCommentTime.DaysAgo ->
            stringResource(R.string.placedetail_comment_time_days_ago, days)
        is PlaceCommentTime.AbsoluteDate ->
            stringResource(R.string.placedetail_comment_time_date, year, month, day)
    }

/**
 * 내 코멘트에만 붙는 [⋮]와 그것이 여는 메뉴.
 *
 * 메뉴는 이 버튼을 앵커로 삼는 팝업이라 버튼과 한 자리에 묶여 있다 — 떼어 놓으면 앵커를 다시 전달해야 한다.
 */
@Composable
private fun CommentMenuButton(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuOpen by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        Icon(
            modifier = Modifier
                .size(MenuIconSize)
                .clip(CircleShape)
                .rippleSingleClickable(role = Role.Button) { isMenuOpen = true },
            imageVector = MinoIcons.MoreVertical,
            contentDescription = stringResource(R.string.placedetail_comment_menu),
            tint = MinoAndroidTheme.colors.labelNormal,
        )
        if (isMenuOpen) {
            PlaceCommentMenu(
                onDeleteClick = {
                    isMenuOpen = false
                    onDeleteClick()
                },
                onDismissRequest = { isMenuOpen = false },
            )
        }
    }
}

// Figma sm 변수 대응 — 토큰 미존재
private val HeaderContentSpacing = 10.dp

private val AvatarNicknameSpacing = 6.dp

private val ContentTimeSpacing = 4.dp

private val MenuIconSize = 18.dp

/** 남의 코멘트 — 하루가 채 지나지 않아 `N시간 전` 갈래가 뜬다. */
@OptIn(ExperimentalTime::class)
@UiModePreviews
@Composable
private fun PlaceCommentItemPreview() {
    MinoAndroidAppTheme {
        PlaceCommentItem(
            comment = PlaceCommentUiModel(
                id = "1",
                content = "친구가 남긴 코멘트입니다.",
                nickname = "서연",
                avatarColor = RoomColor.LIGHT_BLUE,
                canDelete = false,
                createdAt = PreviewObservedAt - 3.hours,
            ),
            observedAt = PreviewObservedAt,
            onDeleteClick = {},
        )
    }
}

/** 내 코멘트 — [⋮]가 붙고, 긴 본문이 잘리지 않는 것과 `NNNN년 NN월 NN일` 갈래를 함께 본다. */
@OptIn(ExperimentalTime::class)
@UiModePreviews
@Composable
private fun PlaceCommentItemDeletablePreview() {
    MinoAndroidAppTheme {
        PlaceCommentItem(
            comment = PlaceCommentUiModel(
                id = "2",
                content = "친구가 남긴 코멘트입니다.".repeat(12),
                nickname = "태훈",
                avatarColor = null,
                canDelete = true,
                createdAt = PreviewObservedAt - 30.days,
            ),
            observedAt = PreviewObservedAt,
            onDeleteClick = {},
        )
    }
}

@OptIn(ExperimentalTime::class)
private val PreviewObservedAt = Instant.parse("2027-02-01T12:00:00Z")
