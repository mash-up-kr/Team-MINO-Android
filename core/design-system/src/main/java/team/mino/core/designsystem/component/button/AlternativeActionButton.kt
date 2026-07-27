package team.mino.core.designsystem.component.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.button.token.ButtonTokens
import team.mino.core.designsystem.foundation.shape.token.value
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/** 메인 액션 아래 세로로 배치되는 대체 액션(Figma Alternative Action). 테두리만 있는 버튼. */
@Composable
internal fun AlternativeActionButton(
    action: ButtonAction,
    colors: MinoButtonColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .alpha(if (action.enabled) 1f else ButtonTokens.DisabledOpacity)
            .surface(
                shape = ButtonTokens.ButtonShape.value,
                containerColor = Color.Transparent,
                borderColor = colors.alternativeBorderColor,
                borderWidth = ButtonTokens.ButtonBorderWidth,
            ).rippleSingleClickable(enabled = action.enabled, onClick = action.onClick)
            .padding(ButtonTokens.ButtonPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = action.text, color = colors.alternativeContentColor, style = ButtonTokens.ButtonFont.value)
    }
}
