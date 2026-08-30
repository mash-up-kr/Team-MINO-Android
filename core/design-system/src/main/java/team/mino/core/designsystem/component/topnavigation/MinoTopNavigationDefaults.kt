package team.mino.core.designsystem.component.topnavigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.topnavigation.token.TopNavigationTokens
import team.mino.core.designsystem.foundation.color.token.value

/**
 * [MinoTopNavigation]의 기본값.
 *
 * `Mino*Colors` 클래스를 두지 않는다 — [MinoTopNavigation]이 색 오버라이드 파라미터를
 * 열지 않아 묶어 둘 대상이 없기 때문이다.
 * 오버라이드가 필요해지면 그때 `colors` 파라미터와 함께 Colors 클래스로 옮긴다.
 */
object MinoTopNavigationDefaults {
    val titleColor: Color
        @Composable @ReadOnlyComposable get() = TopNavigationTokens.TitleColor.value

    val backIconColor: Color
        @Composable @ReadOnlyComposable get() = TopNavigationTokens.BackIconColor.value

    val actionLabelColor: Color
        @Composable @ReadOnlyComposable get() = TopNavigationTokens.ActionLabelColor.value

    val actionIconColor: Color
        @Composable @ReadOnlyComposable get() = TopNavigationTokens.ActionIconColor.value

    /** 아이콘 액션의 눌림 오버레이 색. 리플이 이 색으로 그려진다. */
    val actionIconOverlayColor: Color
        @Composable @ReadOnlyComposable get() = TopNavigationTokens.ActionIconOverlayColor.value
}
