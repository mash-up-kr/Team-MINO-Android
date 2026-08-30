package team.mino.core.designsystem.component.cardlocation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.component.cardlocation.token.CardLocationTokens
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.typography.token.value

/**
 * [MinoCardLocationList]·[MinoCardLocationCollage]가 공유하는 기본값. 상태(선택·비활성 등)가 없는
 * 컴포넌트라 `Colors` 클래스 없이 단일 값 프로퍼티만 노출한다.
 */
object MinoCardLocationDefaults {
    val titleFont: TextStyle
        @Composable @ReadOnlyComposable get() = CardLocationTokens.TitleFont.value

    val titleColor: Color
        @Composable @ReadOnlyComposable get() = CardLocationTokens.TitleColor.value

    val addressFont: TextStyle
        @Composable @ReadOnlyComposable get() = CardLocationTokens.AddressFont.value

    val addressColor: Color
        @Composable @ReadOnlyComposable get() = CardLocationTokens.AddressColor.value

    val commentCountFont: TextStyle
        @Composable @ReadOnlyComposable get() = CardLocationTokens.CommentCountFont.value

    val commentCountColor: Color
        @Composable @ReadOnlyComposable get() = CardLocationTokens.CommentCountColor.value

    val moreIconColor: Color
        @Composable @ReadOnlyComposable get() = CardLocationTokens.MoreIconColor.value

    val thumbnailBackgroundColor: Color
        @Composable @ReadOnlyComposable get() = CardLocationTokens.ThumbnailBackgroundColor.value

    val thumbnailBorderColor: Color
        @Composable @ReadOnlyComposable get() = CardLocationTokens.ThumbnailBorderColor.value

    val thumbnailPlaceholderTint: Color
        @Composable @ReadOnlyComposable get() = CardLocationTokens.ThumbnailPlaceholderTint.value
}
