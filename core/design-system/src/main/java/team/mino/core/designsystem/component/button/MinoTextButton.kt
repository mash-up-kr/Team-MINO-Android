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
import androidx.compose.ui.draw.clip
import team.mino.core.designsystem.component.button.token.contentPadding
import team.mino.core.designsystem.component.button.token.font
import team.mino.core.designsystem.component.button.token.iconSize
import team.mino.core.designsystem.component.button.token.iconTextSpacing
import team.mino.core.designsystem.component.button.token.shape
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * 배경도 테두리도 없이 글자만 있는 저강조 버튼(Figma `Button/Text`).
 *
 * [MinoButton]과 **다른 컴포넌트셋**이다. 이름만 비슷한 변형이 아니라 크기 축([TextButtonSize]는
 * Medium·Small 두 단계뿐)·글자 크기·인터랙션 레이어가 모두 따로 정의돼 있어, [ButtonSize]·
 * [ButtonStyle]과 토큰을 공유하지 않는다. 배경이 있는 버튼이 필요하면 [MinoButton]을 쓴다.
 *
 * 배경이 없어 시각 경계가 글자뿐이지만, 클릭·리플 영역은 Figma `Interaction` 레이어를 따라
 * 글자보다 좌우로 넓다(Medium 7dp / Small 6dp). 그만큼 컴포넌트 폭도 글자보다 넓으므로,
 * 글자 기준으로 정렬을 맞춰야 하는 자리에서는 호출부가 [contentPadding]으로 조정한다.
 *
 * 로딩(`Loading`) 속성은 [MinoButton]과 같은 이유로 아직 파라미터를 두지 않았다.
 *
 * @param enabled `false`면 클릭이 막히고 글자가 비활성 색으로 바뀐다(Figma `Disable` 속성).
 *   비활성 색은 두 [style]이 같다.
 * @param size Figma `Size` 속성. 패딩·글자 크기·아이콘 크기가 함께 바뀐다.
 * @param style Figma `Variant` 속성. 글자색만 가른다.
 * @param leadingIcon 글자 앞 아이콘(Figma `Leading Icon`). `null`이면 자리를 차지하지 않는다.
 *   슬롯 안에서는 `LocalContentColor`가 글자색으로 지정돼 있어, 색을 따로 넘기지 않은
 *   [androidx.compose.material3.Icon]은 글자와 같은 색으로 그려진다.
 * @param trailingIcon 글자 뒤 아이콘(Figma `Trailing Icon`). 동작은 [leadingIcon]과 같다.
 * @param contentPadding 콘텐츠 좌우·상하 패딩. 기본값은 [size]의 표준 패딩이다.
 */
@Composable
fun MinoTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: TextButtonSize = TextButtonSize.Medium,
    style: TextButtonStyle = TextButtonStyle.Primary,
    contentPadding: PaddingValues = size.contentPadding(),
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = MinoTextButtonDefaults.colors(style)
    val contentColor = MinoTextButtonDefaults.contentColor(colors, enabled)

    Row(
        // 배경이 없어 채울 것이 없지만, 리플이 Figma `Interaction` 레이어의 모서리를 따르도록 자른다.
        modifier = modifier
            .clip(size.shape())
            .rippleSingleClickable(enabled = enabled, onClick = onClick)
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
            Text(text = text, color = contentColor, style = size.font.value)
            if (trailingIcon != null) {
                Box(modifier = Modifier.size(size.iconSize), contentAlignment = Alignment.Center) {
                    trailingIcon()
                }
            }
        }
    }
}

/**
 * [MinoTextButton]의 크기. Figma `Button/Text`의 `Size` 속성에 대응한다.
 *
 * [ButtonSize]와 값이 겹치지 않는다 — Figma `Button/Text`에는 Large가 없고, 같은 이름의 Small도
 * 글자 크기가 다르다(Label1 14sp / `Button/Button`은 Label2 13sp).
 */
enum class TextButtonSize {
    /** Figma `Size=Medium`. 상하 패딩 4, Body1(16). 콘텐츠 안에 놓이는 텍스트 버튼. */
    Medium,

    /** Figma `Size=Small`. 상하 패딩 4, Label1(14). 액션 영역 보조 액션 등 좁은 자리. */
    Small,
}

/**
 * [MinoTextButton]의 시각 스타일. Figma `Button/Text`의 `Variant` 속성에 대응한다.
 *
 * [ButtonStyle]과 달리 배경 유무 축이 없어(항상 없다) 색 하나만 가른다.
 */
enum class TextButtonStyle {
    /** Figma `Variant=Primary`. 프라이머리 글자의 강조 텍스트 버튼. */
    Primary,

    /** Figma `Variant=Assistive`. 옅은 글자의 저강조 텍스트 버튼. */
    Assistive,
}
