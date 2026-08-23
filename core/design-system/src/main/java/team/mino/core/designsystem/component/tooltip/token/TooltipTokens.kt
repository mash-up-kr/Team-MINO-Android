package team.mino.core.designsystem.component.tooltip.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.tooltip.TooltipSize
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Tooltip 컴포넌트 슬롯 → 디자인 토큰 키 매핑. Figma `Tooltip/Tooltip`(node 16764-137783) 실측값 기준.
 *
 * 배경·라벨 모두 Inverse 계열이라 라이트/다크에 따라 함께 반전된다. 오버레이만 Figma 원본
 * (primary/normal #000000 @5%)대로 항상 검정 틴트다 — Snackbar와 같은 처리다.
 */
internal object TooltipTokens {
    val ContainerColor = ColorAccessKeyToken.InverseBackground
    val ContainerOpacity = AtomicOpacityToken.Opacity88
    val OverlayColor = ColorAccessKeyToken.StaticBlack
    val OverlayOpacity = AtomicOpacityToken.Opacity5

    val LabelColor = ColorAccessKeyToken.InverseLabel
    val ShortcutOpacity = AtomicOpacityToken.Opacity61

    /** 두 사이즈 모두 `콘텐츠 최대 너비 + 좌우 패딩`이 280dp로 같다(Medium 256+24, Small 264+16). */
    val MaxWidth = 280.dp
}

// 사이즈당 한 번만 만들어 재사용한다. when으로 매 호출마다 새로 만들지 않는다.
private val ContentPaddingBySize = mapOf(
    TooltipSize.Medium to PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    TooltipSize.Small to PaddingValues(horizontal = 8.dp, vertical = 5.dp),
)

// 크기마다 모서리가 다르다(Figma 실측).
private val ShapeBySize = mapOf(
    TooltipSize.Medium to RoundedCornerShape(8.dp),
    TooltipSize.Small to RoundedCornerShape(6.dp),
)

internal fun TooltipSize.contentPadding(): PaddingValues = ContentPaddingBySize.getValue(this)

internal fun TooltipSize.shape(): Shape = ShapeBySize.getValue(this)

internal val TooltipSize.font: TypographyAccessKeyToken
    get() =
        when (this) {
            TooltipSize.Medium -> TypographyAccessKeyToken.Label1NormalMedium
            TooltipSize.Small -> TypographyAccessKeyToken.Caption2Medium
        }

/** 말풍선 최소 너비. 라벨이 짧아도 이보다 좁아지지 않는다. */
internal val TooltipSize.minWidth: Dp
    get() =
        when (this) {
            TooltipSize.Medium -> 64.dp
            TooltipSize.Small -> 36.dp
        }

/** 라벨과 단축키 사이 간격. */
internal val TooltipSize.contentSpacing: Dp
    get() =
        when (this) {
            TooltipSize.Medium -> 6.dp
            TooltipSize.Small -> 4.dp
        }

/** 화살표 밑변 길이. */
internal val TooltipSize.arrowLength: Dp
    get() =
        when (this) {
            TooltipSize.Medium -> 20.dp
            TooltipSize.Small -> 14.dp
        }

/**
 * 말풍선 밖으로 돌출하는 화살표 두께. Figma 실측 5.924dp(Medium)·5dp(Small)를 반올림했다.
 *
 * Figma의 화살표 프레임은 이보다 2dp(Medium)·1dp(Small) 두꺼우나 그 여백은 투명하다.
 * 보이지 않는 여백이라 컴포넌트 바운즈에 포함하지 않는다.
 */
internal val TooltipSize.arrowDepth: Dp
    get() =
        when (this) {
            TooltipSize.Medium -> 6.dp
            TooltipSize.Small -> 5.dp
        }

/** 화살표가 말풍선 모서리에서 떨어지는 최소 거리. */
internal val TooltipSize.arrowInset: Dp
    get() =
        when (this) {
            TooltipSize.Medium -> 8.dp
            TooltipSize.Small -> 5.dp
        }
