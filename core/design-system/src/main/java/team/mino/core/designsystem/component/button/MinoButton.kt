package team.mino.core.designsystem.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.button.token.ButtonTokens
import team.mino.core.designsystem.foundation.shape.token.value
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 화면 하단에 고정하는 액션 영역(Figma `Action Area/Action Area`). 메인 액션은 필수이고,
 * [secondaryAction]으로 보조 액션을 하나 더할 수 있다(Figma Variant=Neutral·Strong에 대응).
 *
 * - [ButtonSecondaryAction.Sub]: 메인 액션 옆에 가로로 배치되는 저강조 보조 액션.
 * - [ButtonSecondaryAction.Alternative]: 메인 액션 아래 세로로 배치되는 대체 액션.
 *
 * 하단 시스템 인셋(제스처 내비게이션 바 등) 대응은 컴포넌트가 강제하지 않는다. 호출부가
 * 화면 구조(Scaffold의 `contentWindowInsets` 처리 여부 등)에 맞춰 [modifier]에
 * `Modifier.navigationBarsPadding()`을 직접 얹어 적용한다.
 *
 * @param divider 컨텐츠와 경계가 자연스럽게 이어지도록 상단에 그라데이션 페이드를 그릴지 여부
 *   (Figma `Divider` 속성 — 실선이 아니라 배경색으로 사라지는 그라데이션 마스크다).
 */
@Composable
fun MinoButton(
    mainActionText: String,
    onMainActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    mainActionEnabled: Boolean = true,
    secondaryAction: ButtonSecondaryAction? = null,
    divider: Boolean = true,
    colors: MinoButtonColors = MinoButtonDefaults.colors(),
) {
    val containerColor = colors.containerColor
    val dividerBrush = remember(containerColor) {
        Brush.verticalGradient(listOf(Color.Transparent, containerColor))
    }

    Column(modifier = modifier) {
        if (divider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonTokens.GradientHeight)
                    .background(dividerBrush),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor)
                .padding(ButtonTokens.ContainerPadding),
            verticalArrangement = Arrangement.spacedBy(ButtonTokens.ActionColumnSpacing),
        ) {
            when (secondaryAction) {
                is ButtonSecondaryAction.Sub -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(ButtonTokens.ActionRowSpacing)) {
                        SubActionButton(action = secondaryAction.action, colors = colors)
                        MainActionButton(
                            modifier = Modifier.weight(1f),
                            text = mainActionText,
                            onClick = onMainActionClick,
                            enabled = mainActionEnabled,
                            colors = colors,
                        )
                    }
                }
                is ButtonSecondaryAction.Alternative -> {
                    MainActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = mainActionText,
                        onClick = onMainActionClick,
                        enabled = mainActionEnabled,
                        colors = colors,
                    )
                    AlternativeActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        action = secondaryAction.action,
                        colors = colors,
                    )
                }
                null -> {
                    MainActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = mainActionText,
                        onClick = onMainActionClick,
                        enabled = mainActionEnabled,
                        colors = colors,
                    )
                }
            }
        }
    }
}

/**
 * [MinoButton]의 `secondaryAction`에 전달하는 보조 액션 종류. [Sub]와 [Alternative]는
 * 서로 다른 레이아웃(가로/세로)이라 동시에 지정할 수 없으므로 하나의 슬롯으로 표현한다.
 */
sealed class ButtonSecondaryAction {
    /** 메인 액션 옆에 가로로 배치되는 저강조 보조 액션(Figma Sub Action). */
    class Sub(val action: ButtonAction) : ButtonSecondaryAction()

    /** 메인 액션 아래 세로로 배치되는 대체 액션(Figma Alternative Action). */
    class Alternative(val action: ButtonAction) : ButtonSecondaryAction()
}

/**
 * [ButtonSecondaryAction]에 담기는 보조 액션 정보.
 */
class ButtonAction(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

/** 필수 메인 액션(Figma Main Action). 있는 그대로 채워진 배경의 강조 버튼. */
@Composable
private fun MainActionButton(
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
                borderWidth = ButtonTokens.ButtonBorderWidth,
            ).rippleSingleClickable(enabled = enabled, onClick = onClick)
            .padding(ButtonTokens.ButtonPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = colors.mainContentColor, style = ButtonTokens.ButtonFont.value)
    }
}

/** 메인 액션 옆에 가로로 배치되는 저강조 보조 액션(Figma Sub Action). 테두리만 있는 버튼. */
@Composable
private fun SubActionButton(
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
                borderColor = colors.subBorderColor,
                borderWidth = ButtonTokens.ButtonBorderWidth,
            ).rippleSingleClickable(enabled = action.enabled, onClick = action.onClick)
            .padding(ButtonTokens.ButtonPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = action.text, color = colors.subContentColor, style = ButtonTokens.SubButtonFont.value)
    }
}

/** 메인 액션 아래 세로로 배치되는 대체 액션(Figma Alternative Action). 테두리만 있는 버튼. */
@Composable
private fun AlternativeActionButton(
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
