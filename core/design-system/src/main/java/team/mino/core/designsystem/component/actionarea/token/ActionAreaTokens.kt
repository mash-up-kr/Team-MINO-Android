package team.mino.core.designsystem.component.actionarea.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.shape.token.ShapeAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Action Area 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Action Area/Action Area` 실측값 기준.
 */
internal object ActionAreaTokens {
    val ContainerColor = ColorAccessKeyToken.BackgroundNormalNormal
    val ContainerPadding = PaddingValues(20.dp)
    val ActionRowSpacing = 12.dp
    val ActionColumnSpacing = 8.dp

    // Figma 컴포넌트 설명상 상단 경계는 하드 라인이 아니라 그라데이션 마스크로 표현된다.
    val GradientHeight = 24.dp

    val ButtonShape = ShapeAccessKeyToken.Medium
    val ButtonPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
    val ButtonBorderWidth = 1.dp
    val ButtonFont = TypographyAccessKeyToken.Body1NormalBold
    val SubButtonFont = TypographyAccessKeyToken.Body1NormalMedium

    val MainContainerColor = ColorAccessKeyToken.PrimaryNormal

    // InverseLabel은 MainContainerColor(다크모드에 흰색으로 뒤집힘)와 함께 반전되어 항상 대비를 유지한다.
    // StaticWhite는 모드와 무관하게 항상 흰색이라 다크모드에서 흰 배경 위 흰 글자가 되므로 쓰지 않는다.
    val MainContentColor = ColorAccessKeyToken.InverseLabel

    val SubContentColor = ColorAccessKeyToken.LabelNormal
    val SubBorderColor = ColorAccessKeyToken.LineNormalNeutral

    val AlternativeContentColor = ColorAccessKeyToken.PrimaryNormal
    val AlternativeBorderColor = ColorAccessKeyToken.LineNormalNeutral

    val DisabledOpacity = AtomicOpacityToken.Opacity43
}
