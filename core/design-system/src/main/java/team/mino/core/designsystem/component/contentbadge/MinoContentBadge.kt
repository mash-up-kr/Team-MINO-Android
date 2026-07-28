package team.mino.core.designsystem.component.contentbadge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.contentbadge.token.ContentBadgeTokens
import team.mino.core.designsystem.component.contentbadge.token.contentPadding
import team.mino.core.designsystem.component.contentbadge.token.font
import team.mino.core.designsystem.component.contentbadge.token.iconSize
import team.mino.core.designsystem.component.contentbadge.token.shape
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 정보를 항목별로 분류할 때 쓰는 낮은 시각 위계의 정적 라벨(Figma `Content Badge/Content Badge`).
 * 클릭 동작이 없는 순수 표시용 컴포넌트다.
 */
@Composable
fun MinoContentBadge(
    text: String,
    modifier: Modifier = Modifier,
    size: ContentBadgeSize = ContentBadgeSize.Small,
    variant: ContentBadgeVariant = ContentBadgeVariant.Solid,
    color: ContentBadgeColor = ContentBadgeColor.Neutral,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = MinoContentBadgeDefaults.colors(color)
    val containerColor = if (variant == ContentBadgeVariant.Solid) colors.containerColor else Color.Transparent
    val borderColor = if (variant == ContentBadgeVariant.Outlined) colors.borderColor else null

    Row(
        modifier = modifier
            .surface(
                shape = size.shape(),
                containerColor = containerColor,
                borderColor = borderColor,
                borderWidth = ContentBadgeTokens.BorderWidth,
            ).padding(size.contentPadding()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ContentBadgeTokens.IconTextSpacing),
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.size(size.iconSize), contentAlignment = Alignment.Center) {
                    leadingIcon()
                }
            }
            Text(text = text, color = colors.contentColor, style = size.font.value)
            if (trailingIcon != null) {
                Box(modifier = Modifier.size(size.iconSize), contentAlignment = Alignment.Center) {
                    trailingIcon()
                }
            }
        }
    }
}

/** [MinoContentBadge]의 크기. Figma `Size` 속성(XSmall·Small·Medium)에 대응. */
enum class ContentBadgeSize {
    XSmall,
    Small,
    Medium,
}

/** [MinoContentBadge]의 배경 스타일. Figma `Variant` 속성(Solid·Outlined)에 대응. */
enum class ContentBadgeVariant {
    Solid,
    Outlined,
}

/** [MinoContentBadge]의 색 스타일. Figma `Color` 속성(Neutral·Accent)에 대응. */
enum class ContentBadgeColor {
    /** 중립. `Fill/Normal`·`Label/Alternative`·`Line/Normal/Neutral`을 쓴다. */
    Neutral,

    /** 강조. `Accent/Foreground/Cyan` 한 색에서 배경·테두리를 알파로 파생한다. */
    Accent,
}
