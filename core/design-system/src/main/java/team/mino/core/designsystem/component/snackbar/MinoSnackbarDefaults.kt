package team.mino.core.designsystem.component.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import team.mino.core.designsystem.component.snackbar.token.SnackbarTokens
import team.mino.core.designsystem.foundation.color.token.value

/**
 * [MinoSnackbar]의 기본값 모음.
 *
 * Snackbar는 enabled 같은 상태가 없어 M3 `BadgeDefaults`처럼 Colors 클래스 없이
 * Defaults의 단일 값 프로퍼티로 색을 노출한다. 색은 토큰의 불투명도를 미리 곱해 반환한다.
 */
object MinoSnackbarDefaults {
    /** 컨테이너 기본 셰이프. */
    val shape: Shape = SnackbarTokens.ContainerShape

    /** 컨테이너 기본 배경색(Inverse, 불투명도 적용). */
    val containerColor: Color
        @Composable @ReadOnlyComposable get() =
            SnackbarTokens.ContainerColor.value.copy(alpha = SnackbarTokens.ContainerOpacity)

    /** 컨테이너 위에 얹는 검정 틴트 오버레이(Figma 원본 primary/normal #000000 @5%). */
    val overlayColor: Color
        @Composable @ReadOnlyComposable get() =
            SnackbarTokens.OverlayColor.value.copy(alpha = SnackbarTokens.OverlayOpacity)

    /** 메시지·설명 기본 색(Inverse, 불투명도 적용). */
    val contentColor: Color
        @Composable @ReadOnlyComposable get() =
            SnackbarTokens.ContentColor.value.copy(alpha = SnackbarTokens.ContentOpacity)

    /** 액션 텍스트 버튼 색(Inverse, 불투명도 없이 선명하게). */
    val actionColor: Color
        @Composable @ReadOnlyComposable get() = SnackbarTokens.ContentColor.value

    /** 닫기 버튼 아이콘 색(Inverse, 61% 불투명). */
    val closeColor: Color
        @Composable @ReadOnlyComposable get() =
            SnackbarTokens.ContentColor.value.copy(alpha = SnackbarTokens.CloseOpacity)

    /** 액션 버튼 리플 셰이프. */
    val actionShape: Shape = SnackbarTokens.ActionShape

    /** 컨테이너 최대 너비. */
    val maxWidth: Dp = SnackbarTokens.MaxWidth
}
