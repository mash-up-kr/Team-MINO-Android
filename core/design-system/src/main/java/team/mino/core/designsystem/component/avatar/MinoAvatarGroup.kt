package team.mino.core.designsystem.component.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.component.avatar.token.AvatarTokens
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * [MinoAvatarGroup] 맨 끝(trailing) 슬롯. Figma `Avatar/Avatar Group`(15852-88488)의
 * `state` variant(add/default/more)에 대응한다.
 */
sealed interface MinoAvatarGroupTrailing {
    /** 아바타만 표시(state=default). */
    data object None : MinoAvatarGroupTrailing

    /** 멤버 추가 버튼(state=add). 아바타 스택과 별도 간격을 두고 붙는다. */
    data class Add(val onClick: () -> Unit) : MinoAvatarGroupTrailing

    /**
     * 초과 인원 뱃지(state=more). 아바타 스택 맨 끝에 다른 아바타와 같은 간격으로 겹쳐 붙는다.
     *
     * @param label 뱃지에 표시할 텍스트(예: "99+", "+12"). 포맷 규칙은 호출부가 결정한다.
     */
    data class Overflow(val label: String) : MinoAvatarGroupTrailing
}

/**
 * 여러 Avatar를 일부 겹쳐 나열하는 Avatar Group.
 *
 * Figma(MU_Wanted / Montage)의 `Avatar/Avatar Group`(15852-88488) 스펙을 따른다. 옅은 배경의
 * pill 컨테이너 안에서 각 아바타가 흰 링으로 경계를 구분하며 겹치고, [trailing]으로 멤버 추가
 * 버튼(state=add) 또는 초과 인원 뱃지(state=more)를 덧붙일 수 있다.
 *
 * @param imageUrls 표시할 아바타들의 이미지 URL 목록(각각 null이면 placeholder).
 * @param variant 공통 형태.
 * @param size 공통 크기.
 * @param trailing 맨 끝 슬롯. 기본은 [MinoAvatarGroupTrailing.None](state=default).
 */
@Composable
fun MinoAvatarGroup(
    imageUrls: ImmutableList<String?>,
    modifier: Modifier = Modifier,
    variant: MinoAvatarVariant = MinoAvatarVariant.Person,
    size: MinoAvatarSize = MinoAvatarSize.Small,
    trailing: MinoAvatarGroupTrailing = MinoAvatarGroupTrailing.None,
) {
    val shape = MinoAvatarDefaults.shape(variant)
    val ringColor = MinoAvatarDefaults.groupRingColor

    Row(
        modifier = modifier
            .surface(shape = AvatarTokens.GroupContainerShape, containerColor = MinoAvatarDefaults.groupContainerColor)
            .padding(AvatarTokens.GroupContainerPadding),
        horizontalArrangement = Arrangement.spacedBy(AvatarTokens.GroupTrailingSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(-AvatarTokens.GroupOverlap)) {
            imageUrls.forEachIndexed { index, url ->
                RingedGroupSlot(
                    shape = shape,
                    ringColor = ringColor,
                    modifier = Modifier.zIndex((imageUrls.size - index).toFloat()),
                ) {
                    MinoAvatar(
                        variant = variant,
                        size = size,
                        imageUrl = url,
                    )
                }
            }

            when (trailing) {
                MinoAvatarGroupTrailing.None, is MinoAvatarGroupTrailing.Add -> Unit
                is MinoAvatarGroupTrailing.Overflow -> {
                    RingedGroupSlot(shape = shape, ringColor = ringColor) {
                        Box(
                            modifier = Modifier
                                .size(size.dp)
                                .surface(shape = shape, containerColor = MinoAvatarDefaults.overflowBackgroundColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = trailing.label,
                                style = MinoAvatarDefaults.overflowLabelFont,
                                color = MinoAvatarDefaults.overflowLabelColor,
                            )
                        }
                    }
                }
            }
        }

        when (trailing) {
            MinoAvatarGroupTrailing.None, is MinoAvatarGroupTrailing.Overflow -> Unit
            is MinoAvatarGroupTrailing.Add -> {
                Box(
                    modifier = Modifier
                        .size(size.dp)
                        .surface(shape = shape, containerColor = MinoAvatarDefaults.addButtonBackgroundColor)
                        .rippleSingleClickable(onClick = trailing.onClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MinoIcons.Plus,
                        contentDescription = "추가",
                        tint = MinoAvatarDefaults.addButtonIconColor,
                        modifier = Modifier.size(AvatarTokens.AddButtonIconSize),
                    )
                }
            }
        }
    }
}

/** 아바타·[MinoAvatarGroupTrailing.Overflow] 뱃지를 [AvatarTokens.GroupRingColor] 링으로 감싼다. */
@Composable
private fun RingedGroupSlot(
    shape: Shape,
    ringColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .surface(shape = shape, containerColor = ringColor)
            .padding(AvatarTokens.GroupRingWidth),
    ) {
        content()
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

@UiModePreviews
@Composable
private fun MinoAvatarGroupAddPreview() {
    MinoAndroidAppTheme {
        Row(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
        ) {
            MinoAvatarGroup(
                imageUrls = persistentListOf(null),
                variant = MinoAvatarVariant.Person,
                size = MinoAvatarSize.Small,
                trailing = MinoAvatarGroupTrailing.Add(onClick = {}),
            )
        }
    }
}

@UiModePreviews
@Composable
private fun MinoAvatarGroupOverflowPreview() {
    MinoAndroidAppTheme {
        Row(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
        ) {
            MinoAvatarGroup(
                imageUrls = persistentListOf(null, null, null),
                variant = MinoAvatarVariant.Person,
                size = MinoAvatarSize.Small,
                trailing = MinoAvatarGroupTrailing.Overflow(label = "99+"),
            )
        }
    }
}
