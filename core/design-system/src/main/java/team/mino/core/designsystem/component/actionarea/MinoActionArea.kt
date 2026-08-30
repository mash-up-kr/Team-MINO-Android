package team.mino.core.designsystem.component.actionarea

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.actionarea.token.ActionAreaTokens
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.component.button.MinoTextButton
import team.mino.core.designsystem.component.button.TextButtonSize
import team.mino.core.designsystem.component.button.TextButtonStyle

/**
 * 화면 하단에 액션을 모아 두는 액션 영역(Figma `Action Area/Action Area`).
 *
 * 버튼 자체는 [MinoButton]·[MinoTextButton]이 그린다. 이 컴포넌트는 Figma가 정한 컨테이너
 * 패딩·배경과 각 액션의 배치·스타일을 소유한다. 그래서 액션이 하나뿐이어도 [MinoButton]을 직접
 * 쓰는 것과 다르다 — 20dp 컨테이너 패딩, 너비, 스타일을 호출부가 화면마다 다시 정하지 않는다.
 *
 * **세 액션은 서로 독립이다.** Figma가 `alternativeAction`·`subAction`을 각각의 불리언으로 두어
 * 메인 + 대체 + 보조를 동시에 세울 수 있고, 코드도 그대로 따른다.
 *
 * **자리마다의 배치와 스타일은 [variant]가 결정한다.** 같은 "보조 액션"이라도 세로 계열에서는
 * 배경 없는 텍스트 버튼이고 가로 계열에서는 테두리 버튼이다 — 자세한 규칙은 [ActionAreaVariant].
 *
 * @param variant 액션 묶음의 배치 계열(Figma `Actions` 리소스의 `Variant` 속성).
 * @param alternativeAction 대체 액션. null이면 그리지 않는다.
 * @param subAction 저강조 보조 액션. null이면 그리지 않는다.
 * @param caption 액션 묶음 **위**에 놓이는 가운데 정렬 설명 문구(Figma `caption`).
 * @param sticky 스크롤되는 콘텐츠 위에 떠서 고정되는 상태인지 여부(Figma `sticky` 속성).
 *   Figma에서 액션 영역의 배경을 그리는 레이어가 이 속성에 묶여 있어, **꺼져 있으면 배경이 없다.**
 *   콘텐츠 흐름 안에 놓이는 기본형은 화면 배경이 그대로 비쳐야 하므로 이쪽이 기본값이다.
 *   켜면 [MinoActionAreaDefaults.surfaceColor] 배경과, 콘텐츠가 딱 잘리지 않도록 상단 페이드가
 *   함께 그려진다.
 * @param extra 액션 **위**에 얹히는 부가 영역(Figma `extra`). 요약·안내·체크박스 등 화면마다 다른
 *   내용이 들어가 슬롯으로 연다. 붙이면 상단 모서리가 둥글어지고 배경이 깔린다.
 * @param divider [extra]가 있을 때 그 위에 긋는 1dp 구분선(Figma `divider`, 기본 켜짐).
 *   [extra]가 null이면 아무 효과가 없다 — Figma도 이 레이어를 `Extra=True` 변형에만 둔다.
 *
 * 하단 시스템 인셋(제스처 내비게이션 바 등) 대응은 **컴포넌트가 하지 않고 호출부에 맡긴다.**
 * `Scaffold`가 `contentWindowInsets`로 하단 인셋을 이미 소비하는 화면에서 컴포넌트가 한 번 더
 * 얹으면 여백이 두 겹으로 들어가는데, 그때 호출부가 끌 방법이 없기 때문이다. 화면 구조에 맞춰
 * [modifier]에 직접 얹는다.
 *
 * ```
 * MinoActionArea(
 *     modifier = Modifier.navigationBarsPadding(),
 *     mainAction = ActionAreaAction(text = "확인", onClick = { }),
 *     subAction = ActionAreaAction(text = "다음에 하기", onClick = { }),
 *     caption = "결제 시 이용약관에 동의하게 됩니다.",
 * )
 * ```
 *
 * Figma는 이 자리에 `Safe Area/Bottom`을 두고 `Type=Gesture`일 때 14px 고정 여백을 주지만,
 * 디자인 툴이 기기 인셋을 읽지 못해 박아둔 값이다. 안드로이드에서는 실제 인셋을 쓴다.
 */
@Composable
fun MinoActionArea(
    mainAction: ActionAreaAction,
    modifier: Modifier = Modifier,
    variant: ActionAreaVariant = ActionAreaVariant.Strong,
    alternativeAction: ActionAreaAction? = null,
    subAction: ActionAreaAction? = null,
    caption: String? = null,
    sticky: Boolean = false,
    divider: Boolean = true,
    extra: (@Composable () -> Unit)? = null,
) {
    ActionAreaContainer(
        modifier = modifier,
        sticky = sticky,
        caption = caption,
        divider = divider,
        extra = extra,
    ) {
        when (variant) {
            ActionAreaVariant.Strong ->
                StackedActions(
                    mainActionStyle = ButtonStyle.SolidPrimary,
                    mainAction = mainAction,
                    alternativeAction = alternativeAction,
                    subAction = subAction,
                )

            // Cancel은 배치가 Strong과 같고 메인 액션만 테두리 버튼으로 바뀐다
            ActionAreaVariant.Cancel ->
                StackedActions(
                    mainActionStyle = ButtonStyle.OutlinedAssistive,
                    mainAction = mainAction,
                    alternativeAction = alternativeAction,
                    subAction = subAction,
                )

            ActionAreaVariant.Neutral ->
                InlineActions(
                    mainAction = mainAction,
                    alternativeAction = alternativeAction,
                    subAction = subAction,
                )
        }
    }
}

/**
 * 세로로 쌓는 배치(Figma `Variant=Strong`·`Cancel`). 메인이 맨 위고 전부 폭을 꽉 채우며,
 * 보조 액션만 맨 아래 가운데에 글자 너비로 놓인다.
 */
@Composable
private fun StackedActions(
    mainActionStyle: ButtonStyle,
    mainAction: ActionAreaAction,
    alternativeAction: ActionAreaAction?,
    subAction: ActionAreaAction?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ActionAreaTokens.ActionColumnSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ActionButton(
            modifier = Modifier.fillMaxWidth(),
            action = mainAction,
            style = mainActionStyle,
        )
        if (alternativeAction != null) {
            ActionButton(
                modifier = Modifier.fillMaxWidth(),
                action = alternativeAction,
                style = ButtonStyle.OutlinedPrimary,
            )
        }
        if (subAction != null) {
            MinoTextButton(
                // 패딩이 클릭·리플 영역 바깥에 놓이도록 컴포넌트 자신의 contentPadding이 아니라
                // 여기서 얹는다. Figma도 버튼을 감싼 프레임의 여백이라 인터랙션 레이어에 안 들어간다.
                modifier = Modifier.padding(vertical = ActionAreaTokens.SubActionVerticalPadding),
                text = subAction.text,
                onClick = subAction.onClick,
                enabled = subAction.enabled,
                size = TextButtonSize.Small,
                style = TextButtonStyle.Assistive,
                leadingIcon = subAction.leadingIcon,
                trailingIcon = subAction.trailingIcon,
            )
        }
    }
}

/**
 * 가로로 늘어놓는 배치(Figma `Variant=Neutral`). 왼쪽부터 보조 · 대체 · 메인 순이고,
 * 보조만 글자 너비를 지키며 대체와 메인이 남는 폭을 똑같이 나눠 갖는다.
 */
@Composable
private fun InlineActions(
    mainAction: ActionAreaAction,
    alternativeAction: ActionAreaAction?,
    subAction: ActionAreaAction?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ActionAreaTokens.ActionRowSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (subAction != null) {
            ActionButton(action = subAction, style = ButtonStyle.OutlinedAssistive)
        }
        if (alternativeAction != null) {
            ActionButton(
                modifier = Modifier.weight(1f),
                action = alternativeAction,
                style = ButtonStyle.OutlinedPrimary,
            )
        }
        ActionButton(
            modifier = Modifier.weight(1f),
            action = mainAction,
            style = ButtonStyle.SolidPrimary,
        )
    }
}

@Composable
private fun ActionButton(
    action: ActionAreaAction,
    style: ButtonStyle,
    modifier: Modifier = Modifier,
) {
    MinoButton(
        modifier = modifier,
        text = action.text,
        onClick = action.onClick,
        enabled = action.enabled,
        style = style,
        leadingIcon = action.leadingIcon,
        trailingIcon = action.trailingIcon,
    )
}

/**
 * 액션 묶음의 배치 계열. Figma `Action Area/Resource/Actions`(16215:35697)의 `Variant` 축에 대응한다.
 *
 * Figma의 네 값 중 `Compact`만 뺐다 — 변형 이름에 **(Web Only)** 가 달려 있는 웹 전용이라
 * Android 구현 대상이 아니다. 종속 속성 `compactContent`도 함께 제외된다.
 *
 * 이 축은 강조 계열이 아니라 **배치**를 가른다. 그래서 같은 자리라도 계열에 따라 버튼이 달라진다.
 *
 * | 자리 | Strong·Cancel (세로) | Neutral (가로) |
 * |---|---|---|
 * | 메인 | Strong=[ButtonStyle.SolidPrimary] / Cancel=[ButtonStyle.OutlinedAssistive], 폭 꽉 채움 | [ButtonStyle.SolidPrimary], 균등 분할 |
 * | 대체 | [ButtonStyle.OutlinedPrimary], 폭 꽉 채움 | [ButtonStyle.OutlinedPrimary], 균등 분할 |
 * | 보조 | [MinoTextButton] (Assistive·Small), 가운데 | [ButtonStyle.OutlinedAssistive], 글자 너비 |
 */
enum class ActionAreaVariant {
    /** Figma `Variant=Strong`. 액션을 세로로 쌓고 전부 폭을 꽉 채운다. 메인이 맨 위다. */
    Strong,

    /**
     * Figma `Variant=Neutral`. 액션을 가로로 늘어놓고 **오른쪽 끝을 메인**으로 삼는다.
     * 보조 액션이 텍스트 버튼이 아니라 테두리 버튼이 되는 점이 세로 계열과 다르다.
     */
    Neutral,

    /**
     * Figma `Variant=Cancel`. 닫기·확인처럼 강조가 필요 없는 단일 액션 자리.
     * 메인 액션이 채운 버튼이 아니라 테두리 버튼([ButtonStyle.OutlinedAssistive])으로 바뀐다.
     * Figma 문서 페이지는 이 계열을 액션 하나짜리로만 예시하고 있다.
     */
    Cancel,
}

/**
 * 액션 영역에 놓이는 액션 하나. 세 자리가 모두 이 타입을 받는다.
 *
 * 크기는 담지 않는다. Figma 액션 영역은 모든 자리가 `Size=Large`로 고정이라, 크기를 열면
 * 디자인에 없는 조합이 만들어진다. 스타일도 담지 않는다 — 어느 자리에 어느 계열로 놓이는지가
 * 스타일을 결정하고, 그 결정은 [MinoActionArea]가 소유한다.
 *
 * @param leadingIcon 글자 앞 아이콘(Figma `Leading Icon`). 색은 [MinoButton]이 스타일에 맞춰
 *   `LocalContentColor`로 깔아주므로 `tint`를 따로 넘기지 않아도 된다.
 * @param trailingIcon 글자 뒤 아이콘(Figma `Trailing Icon`). 동작은 [leadingIcon]과 같다.
 */
@Immutable
data class ActionAreaAction(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val leadingIcon: (@Composable () -> Unit)? = null,
    val trailingIcon: (@Composable () -> Unit)? = null,
)
