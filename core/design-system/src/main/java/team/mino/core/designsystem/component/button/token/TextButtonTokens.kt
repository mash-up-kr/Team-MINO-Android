package team.mino.core.designsystem.component.button.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.button.TextButtonSize
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Text Button 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Button/Text`(컴포넌트셋 16215:38291)의 8개 변형 실측값 기준.
 *
 * `Button/Button`과 다른 컴포넌트셋이라 [ButtonTokens]와 값을 공유하지 않는다.
 */
internal object TextButtonTokens {
    val PrimaryContentColor = ColorAccessKeyToken.PrimaryNormal
    val AssistiveContentColor = ColorAccessKeyToken.LabelAlternative

    /** 비활성은 두 스타일이 같은 색을 쓴다. */
    val DisabledContentColor = ColorAccessKeyToken.LabelDisable
}

// Figma 심볼의 프레임 폭은 글자 폭과 같고(수평 패딩 0), 그 위에 좌우로 튀어나온 `Interaction`
// 레이어가 따로 얹혀 있다(Medium ±7 / Small ±6). Compose는 리플을 컴포넌트 바운즈 밖으로
// 그릴 수 없으므로, 그 튀어나온 폭을 수평 패딩으로 흡수해 리플 영역과 터치 영역을 일치시킨다.
private val ContentPaddingBySize = mapOf(
    TextButtonSize.Medium to PaddingValues(horizontal = 7.dp, vertical = 4.dp),
    TextButtonSize.Small to PaddingValues(horizontal = 6.dp, vertical = 4.dp),
)

// Figma `Interaction` 레이어의 모서리. 두 크기가 같은 값이라 크기별로 나누지 않는다.
private val InteractionShape = RoundedCornerShape(6.dp)

internal fun TextButtonSize.contentPadding(): PaddingValues = ContentPaddingBySize.getValue(this)

internal fun TextButtonSize.shape(): Shape = InteractionShape

/** Figma `Leading Icon`·`Trailing Icon` 슬롯의 아이콘 박스 크기. */
internal val TextButtonSize.iconSize: Dp
    get() =
        when (this) {
            TextButtonSize.Medium -> 20.dp
            TextButtonSize.Small -> 16.dp
        }

/** 아이콘과 글자 사이 간격(Figma `Content` 프레임의 gap). 두 크기가 같다. */
internal val TextButtonSize.iconTextSpacing: Dp
    get() = 4.dp

/**
 * 글자 스타일. 굵기는 Bold 고정이고 크기만 갈린다.
 * `Button/Button`의 Small이 Label2(13sp)인 것과 달리 여기 Small은 Label1(14sp)이다.
 */
internal val TextButtonSize.font: TypographyAccessKeyToken
    get() =
        when (this) {
            TextButtonSize.Medium -> TypographyAccessKeyToken.Body1NormalBold
            TextButtonSize.Small -> TypographyAccessKeyToken.Label1NormalBold
        }
