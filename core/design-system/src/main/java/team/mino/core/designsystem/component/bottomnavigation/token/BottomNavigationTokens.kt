package team.mino.core.designsystem.component.bottomnavigation.token

import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Bottom Navigation 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Bottom Navigation/Bottom Navigation` Platform=Android 변형(node 16215-20676) 실측값 기준.
 */
internal object BottomNavigationTokens {
    val ContainerColor = ColorAccessKeyToken.BackgroundNormalNormal
    val DividerColor = ColorAccessKeyToken.LineNormalNeutral
    val DividerThickness = 1.dp

    val ItemVerticalPadding = 9.dp
    val IconSize = 24.dp
    val IconLabelSpacing = 6.dp

    val ContentColor = ColorAccessKeyToken.InteractionInactive
    val SelectedContentColor = ColorAccessKeyToken.PrimaryNormal
    val LabelFont = TypographyAccessKeyToken.Caption1Medium
}
