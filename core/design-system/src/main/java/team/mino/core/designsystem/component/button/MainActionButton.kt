package team.mino.core.designsystem.component.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import team.mino.core.designsystem.component.button.token.ButtonTokens
import team.mino.core.designsystem.foundation.shape.token.value
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/** 필수 메인 액션(Figma Main Action). 있는 그대로 채워진 배경의 강조 버튼. */
@Composable
internal fun MainActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    colors: MinoButtonColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else ButtonTokens.DisabledOpacity)
            .surface(
                shape = ButtonTokens.ButtonShape.value,
                containerColor = colors.mainContainerColor,
            ).rippleSingleClickable(enabled = enabled, onClick = onClick)
            .padding(ButtonTokens.ButtonPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = colors.mainContentColor, style = ButtonTokens.ButtonFont.value)
    }
}
