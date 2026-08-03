package team.mino.core.designsystem.component.tooltip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import team.mino.core.designsystem.component.tooltip.token.TooltipTokens
import team.mino.core.designsystem.foundation.color.token.value

/**
 * [MinoTooltip]의 기본값 모음.
 *
 * Tooltip은 enabled 같은 상태가 없어 M3 `BadgeDefaults`처럼 Colors 클래스 없이
 * Defaults의 단일 값 프로퍼티로 색을 노출한다. 색은 토큰의 불투명도를 미리 곱해 반환한다.
 */
object MinoTooltipDefaults {
    /** 말풍선·화살표 배경색(Inverse, 88% 불투명). */
    val containerColor: Color
        @Composable @ReadOnlyComposable get() =
            TooltipTokens.ContainerColor.value.copy(alpha = TooltipTokens.ContainerOpacity)

    /** 배경 위에 얹는 검정 틴트 오버레이(Figma 원본 primary/normal #000000 @5%). */
    val overlayColor: Color
        @Composable @ReadOnlyComposable get() =
            TooltipTokens.OverlayColor.value.copy(alpha = TooltipTokens.OverlayOpacity)

    /** 라벨 색(Inverse, 불투명도 없이 선명하게). */
    val labelColor: Color
        @Composable @ReadOnlyComposable get() = TooltipTokens.LabelColor.value

    /** 단축키 텍스트 색(Inverse, 61% 불투명). */
    val shortcutColor: Color
        @Composable @ReadOnlyComposable get() =
            TooltipTokens.LabelColor.value.copy(alpha = TooltipTokens.ShortcutOpacity)

    /** 말풍선 최대 너비. */
    val maxWidth: Dp = TooltipTokens.MaxWidth
}
