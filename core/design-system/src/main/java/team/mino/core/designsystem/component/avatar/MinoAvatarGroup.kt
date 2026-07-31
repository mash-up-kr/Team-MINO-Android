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
 * 여러 Avatar를 일부 겹쳐 나열하는 Avatar Group.
 *
 * Figma(MU_Wanted / Montage)의 `Avatar/Avatar Group`(15852-88488) 스펙을 따른다. 옅은 배경의
 * pill 컨테이너 안에서 각 아바타가 흰 링으로 경계를 구분하며 겹친다. `state`(add/default/more)를
 * 닫힌 타입으로 강제하지 않고, 맨 끝에 붙는 콘텐츠를 [overflowSlot]·[trailingSlot] 두 슬롯으로
 * 열어둬 호출부가 필요한 조합(멤버 추가 버튼·초과 인원 뱃지·커스텀 콘텐츠 등)을 자유롭게 구성한다.
 * Figma 스펙과 동일한 모양이 필요하면 [MinoAvatarGroupAddButton]·[MinoAvatarGroupOverflowBadge]를
 * 슬롯에 꽂아 쓴다.
 *
 * @param imageUrls 표시할 아바타들의 이미지 URL 목록(각각 null이면 placeholder).
 * @param variant 공통 형태.
 * @param size 공통 크기.
 * @param overflowSlot 아바타 스택 맨 끝에 다른 아바타와 같은 간격·링으로 겹쳐 붙는 슬롯(state=more류).
 *  아바타와 같은 [Shape]·[MinoAvatarSize]가 인자로 주어진다. null이면 표시하지 않는다.
 * @param trailingSlot 아바타 스택 밖에 별도 간격을 두고 붙는 슬롯(state=add류). null이면 표시하지 않는다.
 */
@Composable
fun MinoAvatarGroup(
    imageUrls: ImmutableList<String?>,
    modifier: Modifier = Modifier,
    variant: MinoAvatarVariant = MinoAvatarVariant.Person,
    size: MinoAvatarSize = MinoAvatarSize.Small,
    overflowSlot: (@Composable (shape: Shape, size: MinoAvatarSize) -> Unit)? = null,
    trailingSlot: (@Composable (shape: Shape, size: MinoAvatarSize) -> Unit)? = null,
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

            if (overflowSlot != null) {
                RingedGroupSlot(shape = shape, ringColor = ringColor) {
                    overflowSlot(shape, size)
                }
            }
        }

        if (trailingSlot != null) {
            trailingSlot(shape, size)
        }
    }
}

/** 아바타·[overflowSlot] 콘텐츠를 [AvatarTokens.GroupRingColor] 링으로 감싼다. */
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

/**
 * [MinoAvatarGroup]의 `trailingSlot`에 꽂는 멤버 추가 버튼(Figma state=add). 검정 원 배경에 흰 plus 아이콘.
 *
 * @param shape·[size] 그룹의 아바타와 동일한 값을 그대로 전달한다(슬롯 람다 인자로 받는다).
 */
@Composable
fun MinoAvatarGroupAddButton(
    onClick: () -> Unit,
    shape: Shape,
    size: MinoAvatarSize,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .surface(shape = shape, containerColor = MinoAvatarDefaults.addButtonBackgroundColor)
            .rippleSingleClickable(onClick = onClick),
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

/**
 * [MinoAvatarGroup]의 `overflowSlot`에 꽂는 초과 인원 뱃지(Figma state=more).
 *
 * @param label 뱃지에 표시할 텍스트(예: "99+", "+12"). 포맷 규칙은 호출부가 결정한다.
 * @param shape·[size] 그룹의 아바타와 동일한 값을 그대로 전달한다(슬롯 람다 인자로 받는다).
 */
@Composable
fun MinoAvatarGroupOverflowBadge(
    label: String,
    shape: Shape,
    size: MinoAvatarSize,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .surface(shape = shape, containerColor = MinoAvatarDefaults.overflowBackgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MinoAvatarDefaults.overflowLabelFont,
            color = MinoAvatarDefaults.overflowLabelColor,
        )
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
                trailingSlot = { shape, size ->
                    MinoAvatarGroupAddButton(onClick = {}, shape = shape, size = size)
                },
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
                overflowSlot = { shape, size ->
                    MinoAvatarGroupOverflowBadge(label = "99+", shape = shape, size = size)
                },
            )
        }
    }
}
