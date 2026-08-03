package team.mino.core.designsystem.component.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * 아이콘만 있는 형태(Figma `Icon Only=True`)는 [MinoIconButton]이 그린다. 로딩(`Loading`) 속성은
 * 아직 쓰는 화면이 없어 파라미터를 두지 않았다. 필요해지면 디폴트 값을 가진 파라미터로 더해
 * 기존 호출부를 깨지 않고 확장한다.
 *
 * @param enabled `false`면 클릭이 막히고 [style]의 비활성 색으로 바뀐다(Figma `Disable` 속성).
 *   Solid 계열은 배경·글자가 모두 다른 토큰으로 바뀌고, Outlined 계열은 테두리를 유지한 채 글자만 바뀐다.
 * @param size Figma `Size` 속성. 패딩·모서리·글자 크기·아이콘 크기가 함께 바뀐다.
 * @param style Figma `Variant`·`Color` 조합에 대응. 자세한 매핑은 [ButtonStyle] 참고.
 * @param leadingIcon 글자 앞 아이콘(Figma `Leading Icon`). `null`이면 자리를 차지하지 않는다.
 *   슬롯 안에서는 `LocalContentColor`가 [style]의 글자색으로 지정돼 있어, 색을 따로 넘기지 않은
 *   [androidx.compose.material3.Icon]은 글자와 같은 색으로 그려진다.
 * @param trailingIcon 글자 뒤 아이콘(Figma `Trailing Icon`). 동작은 [leadingIcon]과 같다.
 * @param contentPadding 콘텐츠 좌우·상하 패딩. 기본값은 [size]의 표준 패딩([ButtonTokens]
 *   `ContentPaddingBySize`)이다. Figma 인스턴스가 컴포넌트 기본값을 덮어쓴 경우(예: Chip Room의
 *   정렬 버튼은 Medium 기본 20dp 대신 16dp 수평 패딩) 호출부가 실측값으로 넘긴다.
 */
@Composable
fun MinoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    style: ButtonStyle = ButtonStyle.SolidPrimary,
    contentPadding: PaddingValues = size.contentPadding(),
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = MinoButtonDefaults.colors(style)
    val contentColor = MinoButtonDefaults.contentColor(colors, enabled)

    Row(
        modifier = modifier
            .surface(
                shape = size.shape(),
                containerColor = MinoButtonDefaults.containerColor(colors, enabled),
                borderColor = colors.borderColor,
                borderWidth = ButtonTokens.BorderWidth,
            ).rippleSingleClickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = size.iconTextSpacing,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.size(size.iconSize), contentAlignment = Alignment.Center) {
                    leadingIcon()
                }
            }
            Text(text = text, color = contentColor, style = style.font(size).value)
            if (trailingIcon != null) {
                Box(modifier = Modifier.size(size.iconSize), contentAlignment = Alignment.Center) {
                    trailingIcon()
                }
            }
        }
    }
}

/**
 * [MinoButton]·[MinoIconButton]의 크기. Figma `Button/Button`의 `Size` 속성에 대응한다.
 */
enum class ButtonSize {
    /** Figma `Size=Large`. 패딩 12/28, 모서리 12, Body1. 액션 영역의 기본 크기. */
    Large,

    /** Figma `Size=Medium`. 패딩 9/20, 모서리 10, Body2. 콘텐츠 안에 놓이는 버튼. */
    Medium,

    /** Figma `Size=Small`. 패딩 7/14, 모서리 8, Label2. 메뉴 액션 영역 등 좁은 자리의 버튼. */
    Small,
}

/**
 * [MinoButton]·[MinoIconButton]의 시각 스타일. Figma `Button/Button`의 `Variant`·`Color` 두 속성
 * 조합에 대응하며, Figma가 정의한 네 조합을 모두 담는다.
 *
 * 두 속성을 각각의 파라미터로 열지 않고 조합 하나를 값으로 두는 이유: 두 축이 독립적이지 않아
 * 색이 배경 유무와 글자 굵기를 함께 바꾸기 때문이다. 조합을 값으로 두면 잘못된 짝이 생기지 않는다.
 */
enum class ButtonStyle {
    /** Figma `Variant=Solid, Color=Primary`. 채워진 배경의 최상위 강조 버튼. */
    SolidPrimary,

    /** Figma `Variant=Solid, Color=Assistive`. 옅은 회색 배경 + 일반 글자의 저강조 버튼. */
    SolidAssistive,

    /** Figma `Variant=Outlined, Color=Primary`. 테두리 + 프라이머리 글자. */
    OutlinedPrimary,

    /** Figma `Variant=Outlined, Color=Assistive`. 테두리 + 일반 글자의 저강조 버튼. */
    OutlinedAssistive,
}
