package team.mino.core.designsystem.component.actionarea

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.actionarea.token.ActionAreaTokens
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton

/**
 * 메인 액션 옆에 보조 액션을 가로로 붙이는 액션 영역(Figma `Sub Action`).
 *
 * [subAction]은 글자 너비만큼만 차지하고 [mainAction]이 남는 폭을 모두 가져간다. 보조 액션은
 * [ButtonStyle.OutlinedAssistive]로, 메인 액션은 [ButtonStyle.SolidPrimary]로 고정이다 —
 * 어느 자리에 놓이느냐가 스타일을 결정하므로 호출부가 바꾸지 않는다.
 *
 * 컨테이너 패딩·[sticky] 배경 등 나머지 동작은 [MinoActionArea]와 같다.
 *
 * ```
 * MinoSubActionArea(
 *     mainAction = ActionAreaAction(text = "방 편집", onClick = { }),
 *     subAction = ActionAreaAction(text = "장소 추가", onClick = { }),
 * )
 * ```
 */
@Composable
fun MinoSubActionArea(
    mainAction: ActionAreaAction,
    subAction: ActionAreaAction,
    modifier: Modifier = Modifier,
    sticky: Boolean = false,
) {
    ActionAreaContainer(modifier = modifier, sticky = sticky) {
        Row(horizontalArrangement = Arrangement.spacedBy(ActionAreaTokens.ActionRowSpacing)) {
            MinoButton(
                text = subAction.text,
                onClick = subAction.onClick,
                enabled = subAction.enabled,
                style = ButtonStyle.OutlinedAssistive,
                leadingIcon = subAction.leadingIcon,
                trailingIcon = subAction.trailingIcon,
            )
            MinoButton(
                modifier = Modifier.weight(1f),
                text = mainAction.text,
                onClick = mainAction.onClick,
                enabled = mainAction.enabled,
                style = ButtonStyle.SolidPrimary,
                leadingIcon = mainAction.leadingIcon,
                trailingIcon = mainAction.trailingIcon,
            )
        }
    }
}
