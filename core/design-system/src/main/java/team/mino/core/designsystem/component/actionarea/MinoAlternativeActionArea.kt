package team.mino.core.designsystem.component.actionarea

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton

/**
 * 메인 액션 아래에 대체 액션을 세로로 쌓는 액션 영역(Figma `Alternative Action`).
 *
 * 두 액션 모두 폭을 꽉 채운다. 대체 액션은 [ButtonStyle.OutlinedPrimary]로, 메인 액션은
 * [ButtonStyle.SolidPrimary]로 고정이다 — 어느 자리에 놓이느냐가 스타일을 결정하므로 호출부가
 * 바꾸지 않는다. 같은 테두리 버튼이라도 [MinoSubActionArea]의 보조 액션과 위계가 달라 스타일이
 * 다르다는 점에 주의한다.
 *
 * 컨테이너 패딩·[sticky] 배경 등 나머지 동작은 [MinoActionArea]와 같다.
 *
 * ```
 * MinoAlternativeActionArea(
 *     mainAction = ActionAreaAction(text = "결제하기", onClick = { }),
 *     alternativeAction = ActionAreaAction(text = "나중에 하기", onClick = { }),
 * )
 * ```
 */
@Composable
fun MinoAlternativeActionArea(
    mainAction: ActionAreaAction,
    alternativeAction: ActionAreaAction,
    modifier: Modifier = Modifier,
    sticky: Boolean = false,
) {
    ActionAreaContainer(modifier = modifier, sticky = sticky) {
        MinoButton(
            modifier = Modifier.fillMaxWidth(),
            text = mainAction.text,
            onClick = mainAction.onClick,
            enabled = mainAction.enabled,
            style = ButtonStyle.SolidPrimary,
            leadingIcon = mainAction.leadingIcon,
        )
        MinoButton(
            modifier = Modifier.fillMaxWidth(),
            text = alternativeAction.text,
            onClick = alternativeAction.onClick,
            enabled = alternativeAction.enabled,
            style = ButtonStyle.OutlinedPrimary,
            leadingIcon = alternativeAction.leadingIcon,
        )
    }
}
