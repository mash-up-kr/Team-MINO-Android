package team.mino.core.designsystem.component.bottomnavigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import team.mino.core.designsystem.component.bottomnavigation.token.BottomNavigationTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.theme.MinoAndroidTheme

object MinoBottomNavigationDefaults {
    val containerColor: Color
        @Composable @ReadOnlyComposable get() = BottomNavigationTokens.ContainerColor.value

    val dividerColor: Color
        @Composable @ReadOnlyComposable get() = BottomNavigationTokens.DividerColor.value

    val windowInsets: WindowInsets
        @Composable get() =
            WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)

    @Composable
    @ReadOnlyComposable
    fun itemColors(): MinoBottomNavigationItemColors = MinoAndroidTheme.colors.defaultBottomNavigationItemColors

    @Composable
    @ReadOnlyComposable
    fun itemColors(
        contentColor: Color = Color.Unspecified,
        selectedContentColor: Color = Color.Unspecified,
    ): MinoBottomNavigationItemColors =
        MinoAndroidTheme.colors.defaultBottomNavigationItemColors.copy(
            contentColor = contentColor,
            selectedContentColor = selectedContentColor,
        )

    internal val ColorScheme.defaultBottomNavigationItemColors: MinoBottomNavigationItemColors
        get() =
            defaultBottomNavigationItemColorsCached
                ?: MinoBottomNavigationItemColors(
                    contentColor = fromToken(BottomNavigationTokens.ContentColor),
                    selectedContentColor = fromToken(BottomNavigationTokens.SelectedContentColor),
                ).also { defaultBottomNavigationItemColorsCached = it }
}

@Immutable
class MinoBottomNavigationItemColors(
    val contentColor: Color,
    val selectedContentColor: Color,
) {
    fun copy(
        contentColor: Color = this.contentColor,
        selectedContentColor: Color = this.selectedContentColor,
    ): MinoBottomNavigationItemColors =
        MinoBottomNavigationItemColors(
            contentColor = contentColor.takeOrElse { this.contentColor },
            selectedContentColor = selectedContentColor.takeOrElse { this.selectedContentColor },
        )

    @Stable
    internal fun contentColor(selected: Boolean): Color = if (selected) selectedContentColor else contentColor

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoBottomNavigationItemColors) return false

        if (contentColor != other.contentColor) return false
        if (selectedContentColor != other.selectedContentColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            contentColor,
            selectedContentColor,
        ).contentHashCode()
}
