package team.mino.feature.sample.main.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.foundation.shadow.MinoShadow
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.feature.sample.main.component.token.InvitationTokens

/**
 * [MinoInvitationCard]·[MinoInvitation]의 기본값 모음. 색·타이포·그림자는 [InvitationTokens] 참고.
 */
object MinoInvitationDefaults {
    val cardShape: Shape get() = InvitationTokens.CardShape

    val cardBackgroundColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.backgroundElevatedNormal

    val cardShadow: MinoShadow
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.shadows.normalSmall

    val coverShape: Shape get() = InvitationTokens.CoverShape

    /** cover 배경색 기본값(중립 Fill). 초대장마다 다른 룸·카테고리 색을 쓰려면 호출부에서 덮어쓴다. */
    val coverBackgroundColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.fillAlternative

    val coverPlaceholderTint: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelAssistive

    val titleFont: TextStyle
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.typography.body1NormalBold

    val titleColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelNormal

    val descriptionFont: TextStyle
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.typography.label2Medium

    val descriptionColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelAlternative

    val countFont: TextStyle
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.typography.label2Bold

    val countColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelAlternative
}
