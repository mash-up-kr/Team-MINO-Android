package team.mino.core.designsystem.component.invitation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.component.invitation.token.InvitationTokens
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.shadow.MinoShadow
import team.mino.core.designsystem.foundation.shadow.token.value
import team.mino.core.designsystem.foundation.typography.token.value

/**
 * [MinoInvitationCard]·[MinoInvitation]의 기본값 모음.
 */
object MinoInvitationDefaults {
    val cardShape: Shape get() = InvitationTokens.CardShape

    val cardBackgroundColor: Color
        @Composable @ReadOnlyComposable get() = InvitationTokens.CardBackgroundColor.value

    val cardShadow: MinoShadow
        @Composable @ReadOnlyComposable get() = InvitationTokens.CardShadow.value

    val coverShape: Shape get() = InvitationTokens.CoverShape

    /** cover 배경색 기본값(중립 Fill). 초대장마다 다른 룸·카테고리 색을 쓰려면 호출부에서 덮어쓴다. */
    val coverBackgroundColor: Color
        @Composable @ReadOnlyComposable get() = ColorAccessKeyToken.FillAlternative.value

    val coverPlaceholderTint: Color
        @Composable @ReadOnlyComposable get() = InvitationTokens.CoverPlaceholderTint.value

    val titleFont: TextStyle
        @Composable @ReadOnlyComposable get() = InvitationTokens.TitleFont.value

    val titleColor: Color
        @Composable @ReadOnlyComposable get() = InvitationTokens.TitleColor.value

    val descriptionFont: TextStyle
        @Composable @ReadOnlyComposable get() = InvitationTokens.DescriptionFont.value

    val descriptionColor: Color
        @Composable @ReadOnlyComposable get() = InvitationTokens.DescriptionColor.value

    val countFont: TextStyle
        @Composable @ReadOnlyComposable get() = InvitationTokens.CountFont.value

    val countColor: Color
        @Composable @ReadOnlyComposable get() = InvitationTokens.CountColor.value
}
