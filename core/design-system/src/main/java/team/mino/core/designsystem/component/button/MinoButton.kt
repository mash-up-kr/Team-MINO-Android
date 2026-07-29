package team.mino.core.designsystem.component.button

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
import androidx.compose.ui.draw.alpha
import team.mino.core.designsystem.component.button.token.ButtonTokens
import team.mino.core.designsystem.component.button.token.contentPadding
import team.mino.core.designsystem.component.button.token.font
import team.mino.core.designsystem.component.button.token.iconSize
import team.mino.core.designsystem.component.button.token.iconTextSpacing
import team.mino.core.designsystem.component.button.token.shape
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 하나의 행동을 실행하는 버튼(Figma `Button/Button`). 이 컴포넌트는 버튼 하나만 그리고,
 * 여러 버튼의 배치·배경은 `MinoActionArea` 같은 상위 컴포넌트가 조합해 만든다.
 *
 * 로딩(`Loading`)·아이콘 전용(`Icon Only`) 속성은 아직 쓰는 화면이 없어 파라미터를 두지 않았다.
 * 필요해지면 디폴트 값을 가진 파라미터로 더해 기존 호출부를 깨지 않고 확장한다.
 *
 * @param enabled `false`면 클릭이 막히고, 색 슬롯을 따로 두는 대신 알파를 낮춰 비활성을
 *   표현한다(Figma `Disable` 속성).
 * @param size Figma `Size` 속성. 패딩·모서리·글자 크기·아이콘 크기가 함께 바뀐다.
 * @param style Figma `Variant`·`Color` 조합에 대응. 자세한 매핑은 [ButtonStyle] 참고.
 * @param leadingIcon 글자 앞 아이콘(Figma `Leading Icon`). `null`이면 자리를 차지하지 않는다.
 *   슬롯 안에서는 `LocalContentColor`가 [style]의 글자색으로 지정돼 있어, 색을 따로 넘기지 않은
 *   [androidx.compose.material3.Icon]은 글자와 같은 색으로 그려진다.
 * @param trailingIcon 글자 뒤 아이콘(Figma `Trailing Icon`). 동작은 [leadingIcon]과 같다.
 */
@Composable
fun MinoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    style: ButtonStyle = ButtonStyle.SolidPrimary,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = MinoButtonDefaults.colors(style)

    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else ButtonTokens.DisabledOpacity)
            .surface(
                shape = size.shape(),
                containerColor = colors.containerColor,
                borderColor = colors.borderColor,
                borderWidth = ButtonTokens.BorderWidth,
            ).rippleSingleClickable(enabled = enabled, onClick = onClick)
            .padding(size.contentPadding()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = size.iconTextSpacing,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.size(size.iconSize), contentAlignment = Alignment.Center) {
                    leadingIcon()
                }
            }
            Text(text = text, color = colors.contentColor, style = style.font(size).value)
            if (trailingIcon != null) {
                Box(modifier = Modifier.size(size.iconSize), contentAlignment = Alignment.Center) {
                    trailingIcon()
                }
            }
        }
    }
}

/**
 * [MinoButton]의 크기. Figma `Button/Button`의 `Size` 속성에 대응한다.
 *
 * Figma에는 Small도 있지만 쓰는 화면이 확인된 두 개만 담는다. 새 크기가 디자인에 등장하면 값을 추가한다.
 */
enum class ButtonSize {
    /** Figma `Size=Large`. 패딩 12/28, 모서리 12, Body1. 액션 영역의 기본 크기. */
    Large,

    /** Figma `Size=Medium`. 패딩 9/20, 모서리 10, Body2. 콘텐츠 안에 놓이는 버튼. */
    Medium,
}

/**
 * [MinoButton]의 시각 스타일. Figma `Button/Button`의 `Variant`·`Color` 두 속성 조합에 대응한다.
 *
 * 두 속성을 각각의 파라미터로 열지 않고 조합 하나를 값으로 두는 이유: Figma가 정의한 전체 조합 중
 * 실제로 쓰이는 것만 담아 검증되지 않은 조합(예: Solid+Assistive)을 만들지 않기 위해서다.
 * 새 조합이 디자인에 등장하면 값을 추가한다.
 */
enum class ButtonStyle {
    /** Figma `Variant=Solid, Color=Primary`. 채워진 배경의 최상위 강조 버튼. */
    SolidPrimary,

    /** Figma `Variant=Outlined, Color=Primary`. 테두리 + 프라이머리 글자. */
    OutlinedPrimary,

    /** Figma `Variant=Outlined, Color=Assistive`. 테두리 + 일반 글자의 저강조 버튼. */
    OutlinedAssistive,
}
