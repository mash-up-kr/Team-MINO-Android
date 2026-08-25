package team.mino.core.designsystem.component.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import team.mino.core.designsystem.component.menu.token.MenuTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Check
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * [MinoMenu] 안에 나열하는 메뉴 아이템 셀.
 *
 * [variant]는 선택 상태를 무엇으로 나타낼지 가른다. [MenuItemVariant.Normal]은 라벨 자체가 강조
 * 스타일로 바뀌고, [MenuItemVariant.Radio]·[MenuItemVariant.Checkbox]는 라벨 앞에 표식이 붙는다.
 * 셋 다 [active]가 선택 여부이며, 접근성 시맨틱에도 `selected`로 실린다. [active]면 variant와
 * 무관하게 셀 배경에 옅은 딤(Figma `Menu/Resource/Item/Cell`의 `Active` 속성, `colors.activeBackgroundColor`)이
 * 항상 함께 깔린다.
 *
 * @param active 현재 선택된 값 표시.
 * @param caption 본문 아래 보조 설명. `null`이면 표시하지 않는다.
 * @param enabled `false`면 클릭이 막히고 셀 전체가 흐려진다.
 * @param variant 선택 상태 표현 방식. Figma `Variant` 속성에 대응.
 * @param contentPadding 셀 세로 패딩. [MinoMenuDefaults.ItemContentPadding](12dp) 또는
 *   [MinoMenuDefaults.ItemContentPaddingCompact](8dp). Figma는 이 값을 메뉴 단위 `Cell Padding`
 *   속성으로 두므로, 한 메뉴 안의 아이템에는 같은 값을 준다.
 * @param trailingContent 라벨 오른쪽 끝에 붙는 슬롯(Figma `Trailing Content`). `null`이면 자리를
 *   차지하지 않는다.
 */
@Composable
fun MinoMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
    caption: String? = null,
    variant: MenuItemVariant = MenuItemVariant.Normal,
    colors: MinoMenuItemColors = MinoMenuDefaults.itemColors(),
    contentPadding: PaddingValues = MinoMenuDefaults.ItemContentPadding,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MenuTokens.ItemShape)
            .then(if (active) Modifier.background(colors.activeBackgroundColor) else Modifier)
            .rippleSingleSelectable(
                selected = active,
                enabled = enabled,
                role = variant.role,
                onClick = onClick,
            ).alpha(if (enabled) 1f else MenuTokens.DisabledOpacity)
            .padding(horizontal = MenuTokens.ItemHorizontalPadding)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(MenuTokens.ControlLabelSpacing),
    ) {
        if (variant != MenuItemVariant.Normal) {
            MenuItemControl(variant = variant, active = active, colors = colors)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MenuTokens.LabelCaptionSpacing),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MenuTokens.LabelMinHeight)
                    .wrapContentHeight(Alignment.CenterVertically),
                text = text,
                color = colors.textColor(enabled = enabled, active = active, variant = variant),
                style = if (variant.highlightsActiveLabel && active) {
                    MenuTokens.ActiveLabelFont.value
                } else {
                    MenuTokens.LabelFont.value
                },
            )
            if (caption != null) {
                Text(
                    text = caption,
                    color = colors.captionColor(enabled = enabled),
                    style = MenuTokens.CaptionFont.value,
                )
            }
        }
        if (trailingContent != null) {
            Box(
                modifier = Modifier.heightIn(min = MenuTokens.LabelMinHeight),
                contentAlignment = Alignment.Center,
            ) {
                trailingContent()
            }
        }
    }
}

/**
 * 라벨 앞에 붙는 선택 표식. 상자 높이는 두 변형 모두 세로 패딩까지 더해 라벨 한 줄 높이(24dp)와 맞다.
 */
@Composable
private fun MenuItemControl(
    variant: MenuItemVariant,
    active: Boolean,
    colors: MinoMenuItemColors,
) {
    val containerColor = if (active) colors.activeControlColor else Color.Transparent
    val borderColor = if (active) colors.activeControlColor else colors.controlBorderColor

    when (variant) {
        MenuItemVariant.Radio ->
            Box(
                modifier = Modifier
                    .padding(
                        end = MenuTokens.ControlEndPadding,
                        top = MenuTokens.RadioVerticalPadding,
                        bottom = MenuTokens.RadioVerticalPadding,
                    ).size(MenuTokens.RadioSize)
                    .surface(
                        shape = CircleShape,
                        containerColor = containerColor,
                        borderColor = borderColor,
                        borderWidth = MenuTokens.ControlBorderWidth,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (active) {
                    Box(
                        modifier = Modifier
                            .size(MenuTokens.RadioDotSize)
                            .surface(shape = CircleShape, containerColor = colors.controlIconColor),
                    )
                }
            }

        MenuItemVariant.Checkbox ->
            Box(
                modifier = Modifier
                    .padding(end = MenuTokens.ControlEndPadding)
                    .padding(
                        horizontal = MenuTokens.CheckboxHorizontalPadding,
                        vertical = MenuTokens.CheckboxVerticalPadding,
                    ).size(MenuTokens.CheckboxSize)
                    .surface(
                        shape = MenuTokens.CheckboxShape,
                        containerColor = containerColor,
                        borderColor = borderColor,
                        borderWidth = MenuTokens.ControlBorderWidth,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (active) {
                    Icon(
                        modifier = Modifier.size(MenuTokens.CheckboxIconSize),
                        imageVector = MinoIcons.Check,
                        contentDescription = null,
                        tint = colors.controlIconColor,
                    )
                }
            }

        MenuItemVariant.Normal -> Unit
    }
}

/**
 * [MinoMenuItem]이 선택 상태를 나타내는 방식. Figma `Variant` 속성(Normal·Radio·Checkbox)에 대응.
 */
enum class MenuItemVariant {
    /** 표식 없이 라벨이 강조 색·굵기로 바뀐다. */
    Normal,

    /** 라벨 앞에 원형 표식. 여러 항목 중 하나만 고르는 자리에 쓴다. */
    Radio,

    /** 라벨 앞에 사각 표식. 여러 항목을 함께 고르는 자리에 쓴다. */
    Checkbox,
}

/** 표식이 선택을 나타내는 변형은 라벨을 강조하지 않는다(Figma에서 라벨 색·굵기가 그대로다). */
internal val MenuItemVariant.highlightsActiveLabel: Boolean
    get() = this == MenuItemVariant.Normal

private val MenuItemVariant.role: Role?
    get() =
        when (this) {
            MenuItemVariant.Normal -> null
            MenuItemVariant.Radio -> Role.RadioButton
            MenuItemVariant.Checkbox -> Role.Checkbox
        }
