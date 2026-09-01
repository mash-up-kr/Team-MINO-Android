package team.mino.core.designsystem.component.cardlocation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import team.mino.core.designsystem.component.cardlocation.token.CardLocationTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Bubble
import team.mino.core.designsystem.foundation.icons.icons.MoreVertical
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * [MinoCardLocationList]·[MinoCardLocationCollage]가 공유하는 [제목·주소 + 더보기 버튼] 줄.
 */
@Composable
internal fun CardLocationTitleRow(
    title: String,
    address: String,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionMenu: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CardLocationTokens.ContentSpacing),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CardLocationTokens.TitleAddressSpacing),
        ) {
            Text(
                text = title,
                style = MinoCardLocationDefaults.titleFont,
                color = MinoCardLocationDefaults.titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = address,
                style = MinoCardLocationDefaults.addressFont,
                color = MinoCardLocationDefaults.addressColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box {
            Icon(
                modifier = Modifier
                    .size(CardLocationTokens.MoreButtonSize)
                    .rippleSingleClickable(onClick = onMoreClick)
                    .padding((CardLocationTokens.MoreButtonSize - CardLocationTokens.MoreIconSize) / 2),
                imageVector = MinoIcons.MoreVertical,
                contentDescription = "더보기",
                tint = MinoCardLocationDefaults.moreIconColor,
            )
            actionMenu()
        }
    }
}

/**
 * [MinoCardLocationList]·[MinoCardLocationCollage]가 공유하는 [코멘트 아이콘+수 + 아바타 그룹] 줄.
 *
 * Figma `Avatar/Avatar Group` 슬롯 — 참여자 아바타를 넣을지는 호출부가 정한다. 넘기지 않으면
 * 코멘트 수만 보인다.
 */
@Composable
internal fun CardLocationCommentRow(
    commentCount: Int,
    modifier: Modifier = Modifier,
    avatarGroup: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CardLocationTokens.MetaIconTextSpacing),
    ) {
        Icon(
            modifier = Modifier.size(CardLocationTokens.MetaIconSize),
            imageVector = MinoIcons.Bubble,
            contentDescription = null,
            tint = MinoCardLocationDefaults.commentCountColor,
        )
        Text(
            text = commentCount.toString(),
            style = MinoCardLocationDefaults.commentCountFont,
            color = MinoCardLocationDefaults.commentCountColor,
        )

        if (avatarGroup != null) {
            avatarGroup()
        }
    }
}
