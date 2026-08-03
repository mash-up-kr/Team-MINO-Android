package team.mino.core.designsystem.component.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.button.token.ButtonTokens
import team.mino.core.designsystem.component.button.token.iconOnlyContentPadding
import team.mino.core.designsystem.component.button.token.iconOnlyIconSize
import team.mino.core.designsystem.component.button.token.shape
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 아이콘 하나만 담는 정사각 버튼(Figma `Button/Button`의 `Icon Only=True`).
 *
 * Figma는 글자 버튼과 한 컴포넌트로 묶고 `Icon Only` 속성으로 가르지만, 코드는 컴포넌트를 나눈다 —
 * 한 함수로 두면 `text`와 아이콘 슬롯 중 하나가 반드시 무시되는 상태가 생긴다. [ButtonSize]·
 * [ButtonStyle]·색 토큰은 [MinoButton]과 그대로 공유하므로 두 컴포넌트의 스타일은 항상 함께 움직인다.
 *
 * 크기는 [size]가 정하는 패딩과 아이콘 크기로 결정된다(Large 48 / Medium 40 / Small 32dp 정사각).
 * 아이콘은 글자 옆에 놓이는 [MinoButton]의 아이콘보다 한 단계 크다.
 *
 * @param enabled `false`면 클릭이 막히고 [style]의 비활성 색으로 바뀐다. 자세한 내용은 [MinoButton] 참고.
 * @param size Figma `Size` 속성. 패딩·모서리·아이콘 크기가 함께 바뀐다.
 * @param style Figma `Variant`·`Color` 조합. 자세한 매핑은 [ButtonStyle] 참고.
 * @param contentPadding 아이콘 둘레 패딩. 기본값은 [size]의 표준 패딩이다.
 * @param icon 슬롯 안에서는 `LocalContentColor`가 [style]의 콘텐츠 색으로 지정돼 있어, 색을 따로
 *   넘기지 않은 [androidx.compose.material3.Icon]은 그 색으로 그려진다.
 */
@Composable
fun MinoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    style: ButtonStyle = ButtonStyle.SolidPrimary,
    contentPadding: PaddingValues = size.iconOnlyContentPadding(),
    icon: @Composable () -> Unit,
) {
    val colors = MinoButtonDefaults.colors(style)

    Box(
        modifier = modifier
            .surface(
                shape = size.shape(),
                containerColor = MinoButtonDefaults.containerColor(colors, enabled),
                borderColor = colors.borderColor,
                borderWidth = ButtonTokens.BorderWidth,
            ).rippleSingleClickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides MinoButtonDefaults.contentColor(colors, enabled)) {
            Box(modifier = Modifier.size(size.iconOnlyIconSize), contentAlignment = Alignment.Center) {
                icon()
            }
        }
    }
}
