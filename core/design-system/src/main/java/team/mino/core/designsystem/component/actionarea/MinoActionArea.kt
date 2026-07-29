package team.mino.core.designsystem.component.actionarea

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.actionarea.token.ActionAreaTokens
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton

/**
 * 화면 하단에 고정하는 액션 영역(Figma `Action Area/Action Area`). 메인 액션은 필수이고,
 * [secondaryAction]으로 보조 액션을 하나 더할 수 있다.
 *
 * 버튼 자체는 [MinoButton]이 그린다. 이 컴포넌트는 배경과 버튼 배치만 책임진다.
 *
 * - [ActionAreaSecondaryAction.Sub]: 메인 액션 옆에 가로로 배치되는 저강조 보조 액션.
 * - [ActionAreaSecondaryAction.Alternative]: 메인 액션 아래 세로로 배치되는 대체 액션.
 *
 * Figma 컴포넌트셋의 `Extra=False` 변형만 구현한다. 컴포넌트셋에 `Divider` 속성이 선언돼 있지만
 * 실제 레이어는 `Extra=True` 변형에만 있고(`Semantic/Line/Normal/Neutral` 1px 상단 실선),
 * 이 변형에는 구분선이 없어 파라미터를 두지 않았다. `Extra=True`(설명 영역이 얹힌 형태)를
 * 구현하게 되면 그때 디폴트 값을 가진 파라미터로 더한다.
 *
 * @param sticky 스크롤되는 콘텐츠 위에 떠서 고정되는 상태인지 여부(Figma `Sticky` 속성).
 *   Figma에서 액션 영역의 배경을 그리는 레이어가 이 속성에 묶여 있어, **꺼져 있으면 배경이 없다.**
 *   콘텐츠 흐름 안에 놓이는 기본형은 화면 배경이 그대로 비쳐야 하므로 이쪽이 기본값이다.
 *   켜면 [MinoActionAreaDefaults.stickyContainerColor] 배경과, 콘텐츠가 딱 잘리지 않도록
 *   상단 페이드가 함께 그려진다.
 *
 * 하단 시스템 인셋(제스처 내비게이션 바 등) 대응은 **컴포넌트가 하지 않고 호출부에 맡긴다.**
 * `Scaffold`가 `contentWindowInsets`로 하단 인셋을 이미 소비하는 화면에서 컴포넌트가 한 번 더
 * 얹으면 여백이 두 겹으로 들어가는데, 그때 호출부가 끌 방법이 없기 때문이다. 화면 구조에 맞춰
 * [modifier]에 직접 얹는다.
 *
 * ```
 * MinoActionArea(
 *     modifier = Modifier.navigationBarsPadding(),
 *     mainActionText = "확인",
 *     onMainActionClick = { },
 * )
 * ```
 *
 * Figma는 이 자리에 `Safe Area/Bottom`을 두고 `Type=Gesture`일 때 14px 고정 여백을 주지만,
 * 디자인 툴이 기기 인셋을 읽지 못해 박아둔 값이다. 안드로이드에서는 실제 인셋을 쓴다.
 */
@Composable
fun MinoActionArea(
    mainActionText: String,
    onMainActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    mainActionEnabled: Boolean = true,
    secondaryAction: ActionAreaSecondaryAction? = null,
    sticky: Boolean = false,
) {
    val mainAction: @Composable (Modifier) -> Unit = { buttonModifier ->
        MinoButton(
            modifier = buttonModifier,
            text = mainActionText,
            onClick = onMainActionClick,
            enabled = mainActionEnabled,
            style = ButtonStyle.SolidPrimary,
        )
    }

    // sticky가 아니면 배경이 없다. Figma에서 배경 레이어가 Sticky 속성에 묶여 있어, 기본형은
    // 콘텐츠 흐름 안에서 화면 배경이 그대로 비치는 상태다.
    val containerColor = if (sticky) MinoActionAreaDefaults.stickyContainerColor else Color.Transparent

    Column(modifier = modifier) {
        if (sticky) {
            val fadeBrush = remember(containerColor) {
                Brush.verticalGradient(
                    *Array(ActionAreaTokens.StickyGradientAlphaStops.size) { index ->
                        val (position, alpha) = ActionAreaTokens.StickyGradientAlphaStops[index]
                        position to containerColor.copy(alpha = alpha)
                    },
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ActionAreaTokens.StickyGradientHeight)
                    .background(fadeBrush),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor)
                .padding(ActionAreaTokens.ContainerPadding),
            verticalArrangement = Arrangement.spacedBy(ActionAreaTokens.ActionColumnSpacing),
        ) {
            when (secondaryAction) {
                is ActionAreaSecondaryAction.Sub -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(ActionAreaTokens.ActionRowSpacing)) {
                        MinoButton(
                            text = secondaryAction.action.text,
                            onClick = secondaryAction.action.onClick,
                            enabled = secondaryAction.action.enabled,
                            style = ButtonStyle.OutlinedAssistive,
                        )
                        mainAction(Modifier.weight(1f))
                    }
                }

                is ActionAreaSecondaryAction.Alternative -> {
                    mainAction(Modifier.fillMaxWidth())
                    MinoButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = secondaryAction.action.text,
                        onClick = secondaryAction.action.onClick,
                        enabled = secondaryAction.action.enabled,
                        style = ButtonStyle.OutlinedPrimary,
                    )
                }

                null -> mainAction(Modifier.fillMaxWidth())
            }
        }
    }
}

/**
 * [MinoActionArea]의 `secondaryAction`에 전달하는 보조 액션 종류. [Sub]와 [Alternative]는
 * 서로 다른 레이아웃(가로/세로)이라 동시에 지정할 수 없으므로 하나의 슬롯으로 표현한다.
 *
 * 호출부가 인자로 매번 새 인스턴스를 만들어도 [MinoActionArea]가 리컴포지션을 건너뛸 수 있도록
 * 구현체를 `data class`로 두어 값 동등성을 갖게 한다.
 */
@Immutable
sealed class ActionAreaSecondaryAction {
    abstract val action: ActionAreaAction

    /** 메인 액션 옆에 가로로 배치되는 저강조 보조 액션(Figma Sub Action). */
    data class Sub(override val action: ActionAreaAction) : ActionAreaSecondaryAction()

    /** 메인 액션 아래 세로로 배치되는 대체 액션(Figma Alternative Action). */
    data class Alternative(override val action: ActionAreaAction) : ActionAreaSecondaryAction()
}

/**
 * [ActionAreaSecondaryAction]에 담기는 보조 액션 정보.
 */
@Immutable
data class ActionAreaAction(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)
