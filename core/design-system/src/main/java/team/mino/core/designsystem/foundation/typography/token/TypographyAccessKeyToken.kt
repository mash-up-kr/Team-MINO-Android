package team.mino.core.designsystem.foundation.typography.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.foundation.typography.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

internal enum class TypographyAccessKeyToken {
    Display1Bold,
    Display1Medium,
    Display1Regular,
    Display2Bold,
    Display2Medium,
    Display2Regular,
    Display3Bold,
    Display3Medium,
    Display3Regular,
    Title1Bold,
    Title1Medium,
    Title1Regular,
    Title2Bold,
    Title2Medium,
    Title2Regular,
    Title3Bold,
    Title3Medium,
    Title3Regular,
    Heading1Bold,
    Heading1Medium,
    Heading1Regular,
    Heading2Bold,
    Heading2Medium,
    Heading2Regular,
    Headline1Bold,
    Headline1Medium,
    Headline1Regular,
    Headline2Bold,
    Headline2Medium,
    Headline2Regular,
    Body1NormalBold,
    Body1NormalMedium,
    Body1NormalRegular,
    Body1ReadingBold,
    Body1ReadingMedium,
    Body1ReadingRegular,
    Body2NormalBold,
    Body2NormalMedium,
    Body2NormalRegular,
    Body2ReadingBold,
    Body2ReadingMedium,
    Body2ReadingRegular,
    Label1NormalBold,
    Label1NormalMedium,
    Label1NormalRegular,
    Label1ReadingBold,
    Label1ReadingMedium,
    Label1ReadingRegular,
    Label2Bold,
    Label2Medium,
    Label2Regular,
    Caption1Bold,
    Caption1Medium,
    Caption1Regular,
    Caption2Bold,
    Caption2Medium,
    Caption2Regular,
}

internal val TypographyAccessKeyToken.value: TextStyle
    @Composable
    @ReadOnlyComposable
    get() = MinoAndroidTheme.typography.fromToken(this)
