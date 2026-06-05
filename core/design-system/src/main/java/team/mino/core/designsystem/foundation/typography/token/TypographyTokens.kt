package team.mino.core.designsystem.foundation.typography.token

import androidx.compose.ui.text.TextStyle

internal object TypographyTokens {
    val BodyLarge = TextStyle(
        fontFamily = AtomicTypographyToken.FontFamilyDefault,
        fontWeight = AtomicTypographyToken.WeightNormal,
        fontSize = AtomicTypographyToken.Size16,
        lineHeight = AtomicTypographyToken.LineHeight24,
        letterSpacing = AtomicTypographyToken.LetterSpacing05,
    )
}
