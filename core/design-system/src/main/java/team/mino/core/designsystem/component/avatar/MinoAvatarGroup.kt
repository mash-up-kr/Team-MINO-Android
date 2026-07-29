package team.mino.core.designsystem.component.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.component.avatar.token.AvatarTokens
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * 여러 Avatar를 일부 겹쳐 나열하는 Avatar Group.
 *
 * Figma(MU_Wanted / Montage)의 `Avatar/Avatar Group` 스펙을 따른다. 각 아바타 사이에 배경색 링을
 * 둬 경계를 구분하고, 앞 아바타가 뒤 아바타 위로 겹친다.
 *
 * @param imageUrls 표시할 아바타들의 이미지 URL 목록(각각 null이면 placeholder).
 * @param variant 공통 형태.
 * @param size 공통 크기.
 *
 * 최대 노출 개수·초과(`+N`) 표기는 정책 미정(spec Open Questions TBD-1)이라 전달된 목록을 모두 렌더한다.
 */
@Composable
fun MinoAvatarGroup(
    imageUrls: ImmutableList<String?>,
    modifier: Modifier = Modifier,
    variant: MinoAvatarVariant = MinoAvatarVariant.Person,
    size: MinoAvatarSize = MinoAvatarSize.Small,
) {
    val overlap = size.dp / AvatarTokens.GroupOverlapDivisor
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(-overlap),
    ) {
        imageUrls.forEachIndexed { index, url ->
            Box(
                modifier = Modifier
                    .zIndex((imageUrls.size - index).toFloat())
                    .clip(MinoAvatarDefaults.shape(variant))
                    .background(AvatarTokens.GroupRingColor.value)
                    .padding(AvatarTokens.GroupRingWidth),
            ) {
                MinoAvatar(
                    variant = variant,
                    size = size,
                    imageUrl = url,
                )
            }
        }
    }
}

@UiModePreviews
@Composable
private fun MinoAvatarGroupPreview() {
    MinoAndroidAppTheme {
        Row(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
        ) {
            MinoAvatarGroup(
                imageUrls = persistentListOf(null, null, null, null),
                variant = MinoAvatarVariant.Person,
                size = MinoAvatarSize.Small,
            )
        }
    }
}
