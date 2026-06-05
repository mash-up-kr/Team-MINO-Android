package team.mino.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.LocalColorScheme
import team.mino.core.designsystem.foundation.color.provideColorScheme
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.shape.LocalShapes
import team.mino.core.designsystem.foundation.shape.Shapes
import team.mino.core.designsystem.foundation.shape.token.ShapeAccessKeyToken
import team.mino.core.designsystem.foundation.shape.token.value
import team.mino.core.designsystem.foundation.typography.LocalTypography
import team.mino.core.designsystem.foundation.typography.Typography
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.preview.UiModePreviews

@Composable
fun MinoAndroidAppTheme(content: @Composable () -> Unit) {
    val colorScheme: ColorScheme = provideColorScheme()
    MinoAndroidTheme(
        colors = colorScheme,
        content = content,
    )
}

@Composable
private fun MinoAndroidTheme(
    colors: ColorScheme = MinoAndroidTheme.colors,
    typography: Typography = MinoAndroidTheme.typography,
    shapes: Shapes = MinoAndroidTheme.shapes,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColorScheme provides colors,
        LocalTypography provides typography,
        LocalShapes provides shapes,
    ) {
        ProvideTextStyle(value = typography.bodyLarge, content = content)
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

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current
}

@UiModePreviews
@Composable
private fun MinoAndroidAppThemePreview() {
    MinoAndroidAppTheme {
        Text(
            modifier = Modifier
                .background(
                    shape = ShapeAccessKeyToken.Small.value,
                    color = ColorAccessKeyToken.PurpleGrey40.value,
                ).padding(4.dp),
            text = "Mino-Android",
            style = TypographyAccessKeyToken.BodyLarge.value.copy(
                color = ColorAccessKeyToken.Purple80.value,
            ),
        )
    }
}
