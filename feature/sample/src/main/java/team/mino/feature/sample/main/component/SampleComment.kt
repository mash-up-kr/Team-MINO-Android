package team.mino.feature.sample.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatar
import team.mino.core.designsystem.component.avatar.MinoAvatarSize
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

private const val COLLAPSED_MAX_LINES = 4
private const val EXPANDED_MAX_LINES = 7

internal const val SAMPLE_COMMENT_SHORT = "친구가 남긴 코멘트입니다."
internal val SAMPLE_COMMENT_LONG = "친구가 남긴 코멘트입니다. ".repeat(20)

/**
 * 프로필·이름·본문으로 구성된 댓글. Figma `comment`(15852-88585) 스펙을 따른다.
 *
 * Figma는 `normal`/`half`/`full` 세 상태를 예시로 보여주지만 상태를 나누는 버튼·텍스트 레이어가
 * 없어, [commentText]가 [COLLAPSED_MAX_LINES]줄을 넘길 때만 자체적으로 "더보기" 탭 대상이 되도록
 * 구현했다. 안 넘치면 `normal`처럼 전체가 보이고, 넘치면 기본은 `half`(4줄)로 접혀 있다가 탭하면
 * `full`(최대 7줄, Figma 140px 상한과 동일)로 펼쳐지고 다시 탭하면 접힌다.
 *
 * @param authorName 작성자 이름.
 * @param commentText 댓글 본문.
 * @param avatarImageUrl 작성자 프로필 이미지 URL. null이면 placeholder.
 */
@Composable
internal fun SampleComment(
    authorName: String,
    commentText: String,
    modifier: Modifier = Modifier,
    avatarImageUrl: String? = null,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isOverflowing by remember(commentText) { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MinoAvatar(
                variant = MinoAvatarVariant.Person,
                size = MinoAvatarSize.Small,
                imageUrl = avatarImageUrl,
            )
            Text(
                text = authorName,
                style = MinoAndroidTheme.typography.label1NormalMedium,
                color = MinoAndroidTheme.colors.labelAlternative,
            )
        }

        Text(
            text = commentText,
            modifier = Modifier.rippleSingleClickable(
                enabled = isOverflowing,
                onClick = { isExpanded = !isExpanded },
            ),
            style = MinoAndroidTheme.typography.label1NormalRegular,
            color = MinoAndroidTheme.colors.labelNormal,
            maxLines = if (isExpanded) EXPANDED_MAX_LINES else COLLAPSED_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!isExpanded) {
                    isOverflowing = result.hasVisualOverflow
                }
            },
        )
    }
}
