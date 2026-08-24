package team.mino.core.designsystem.component.roomcard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoHeaderRoom]의 기본값 모음. 크기·간격은 `HeaderRoomTokens` 참고.
 *
 * Header Room은 enabled 같은 상태가 없어 M3 `BadgeDefaults`처럼 Colors 클래스 없이
 * Defaults의 단일 값 프로퍼티로 색·타이포를 노출한다.
 */
object MinoHeaderRoomDefaults {
    val titleColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelStrong

    val titleFont: TextStyle
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.typography.title3Bold

    val memoColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelNeutral

    val memoFont: TextStyle
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.typography.label1NormalRegular

    /** 위치 개수 텍스트·위치 아이콘·썸네일 아이콘이 공유하는 색(Figma 실측 `Semantic/Label/Alternative`). */
    val resourceColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelAlternative

    val resourceFont: TextStyle
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.typography.label1NormalRegular

    val dividerColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.lineSolidAlternative
}
