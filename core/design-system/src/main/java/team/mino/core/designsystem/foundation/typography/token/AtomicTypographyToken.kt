package team.mino.core.designsystem.foundation.typography.token

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import team.mino.core.designsystem.R

internal object AtomicTypographyToken {
    val FontFamilySuite = FontFamily(
        Font(R.font.suite_regular, FontWeight.Normal),
        Font(R.font.suite_medium, FontWeight.Medium),
        Font(R.font.suite_bold, FontWeight.Bold),
    )

    val WeightBold = FontWeight.Bold
    val WeightMedium = FontWeight.Medium
    val WeightRegular = FontWeight.Normal

    val Size11 = 11.sp
    val Size12 = 12.sp
    val Size13 = 13.sp
    val Size14 = 14.sp
    val Size15 = 15.sp
    val Size16 = 16.sp
    val Size17 = 17.sp
    val Size18 = 18.sp
    val Size20 = 20.sp
    val Size22 = 22.sp
    val Size24 = 24.sp
    val Size28 = 28.sp
    val Size32 = 32.sp
    val Size36 = 36.sp
    val Size40 = 40.sp
    val Size56 = 56.sp

    val LineHeight14 = 14.sp
    val LineHeight16 = 16.sp
    val LineHeight18 = 18.sp
    val LineHeight20 = 20.sp
    val LineHeight22 = 22.sp
    val LineHeight24 = 24.sp
    val LineHeight26 = 26.sp
    val LineHeight28 = 28.sp
    val LineHeight30 = 30.sp
    val LineHeight32 = 32.sp
    val LineHeight38 = 38.sp
    val LineHeight44 = 44.sp
    val LineHeight48 = 48.sp
    val LineHeight52 = 52.sp
    val LineHeight72 = 72.sp

    // 이름의 숫자는 퍼센트 * 100 (예: Minus319 = -3.19% = -0.0319em)
    val LetterSpacingMinus319 = (-0.0319).em
    val LetterSpacingMinus282 = (-0.0282).em
    val LetterSpacingMinus270 = (-0.027).em
    val LetterSpacingMinus253 = (-0.0253).em
    val LetterSpacingMinus236 = (-0.0236).em
    val LetterSpacingMinus230 = (-0.023).em
    val LetterSpacingMinus194 = (-0.0194).em
    val LetterSpacingMinus120 = (-0.012).em
    val LetterSpacingMinus020 = (-0.002).em
    val LetterSpacingZero = 0.0.em
    val LetterSpacingPlus057 = 0.0057.em
    val LetterSpacingPlus096 = 0.0096.em
    val LetterSpacingPlus145 = 0.0145.em
    val LetterSpacingPlus194 = 0.0194.em
    val LetterSpacingPlus252 = 0.0252.em
    val LetterSpacingPlus311 = 0.0311.em
}
