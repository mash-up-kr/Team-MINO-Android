package team.mino.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.LocalColorScheme
import team.mino.core.designsystem.foundation.color.provideColorScheme
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.shadow.LocalShadows
import team.mino.core.designsystem.foundation.shadow.Shadows
import team.mino.core.designsystem.foundation.typography.LocalTypography
import team.mino.core.designsystem.foundation.typography.Typography
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.preview.UiModePreviews

@Composable
fun MinoAndroidAppTheme(content: @Composable () -> Unit) {
    val colorScheme: ColorScheme = provideColorScheme()
    // 시스템 폰트 배율을 무시하고 1sp = 1dp로 고정한다 (docs/adr/2026-07-24-fixed-font-scale.md)
    CompositionLocalProvider(
        LocalDensity provides Density(LocalDensity.current.density, fontScale = 1f),
    ) {
        MinoAndroidTheme(
            colors = colorScheme,
            content = content,
        )
    }
}

@Composable
private fun MinoAndroidTheme(
    colors: ColorScheme = MinoAndroidTheme.colors,
    typography: Typography = MinoAndroidTheme.typography,
    shadows: Shadows = MinoAndroidTheme.shadows,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColorScheme provides colors,
        LocalTypography provides typography,
        LocalShadows provides shadows,
    ) {
        ProvideTextStyle(value = typography.body1NormalRegular, content = content)
    }
}

object MinoAndroidTheme {
    val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val shadows: Shadows
        @Composable
        @ReadOnlyComposable
        get() = LocalShadows.current
}

@UiModePreviews
@Composable
private fun MinoAndroidAppThemePreview() {
    MinoAndroidAppTheme {
        Text(
            modifier = Modifier
                .background(
                    shape = RoundedCornerShape(8.dp),
                    color = ColorAccessKeyToken.BackgroundNormalNormal.value,
                ).padding(4.dp),
            text = "Mino-Android",
            style = TypographyAccessKeyToken.Body1NormalBold.value.copy(
                color = ColorAccessKeyToken.LabelNormal.value,
            ),
        )
    }
}
