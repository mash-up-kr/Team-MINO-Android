package team.mino.core.designsystem.component.chip.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.chip.ChipSize
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Chip 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 */
internal object ChipTokens {
    val TextFont = TypographyAccessKeyToken.Body2NormalMedium
    val BorderWidth = 1.dp

    /** 비활성(active=false) 상태 공통 글자색. */
    val InactiveContentColor = ColorAccessKeyToken.LabelAlternative

    /** Solid·active=true 배경(원시 검정 #000000). */
    val SolidActiveContainerColor = ColorAccessKeyToken.PrimaryNormal

    /** Solid·active=true 글자색(원시 화이트 #F7F7F8). */
    val SolidActiveContentColor = ColorAccessKeyToken.InverseLabel

    /** Solid·active=false 배경(중립 회색 5% 틴트). */
    val SolidInactiveContainerColor = ColorAccessKeyToken.FillAlternative

    /** Outlined 테두리 색(비활성 상태 공통). */
    val OutlinedBorderColor = ColorAccessKeyToken.LineNormalNeutral

    /** Outlined·active=true 글자색. */
    val OutlinedActiveContentColor = ColorAccessKeyToken.PrimaryNormal

    val DisabledContentColor = ColorAccessKeyToken.LabelDisable
    val DisabledContainerColor = ColorAccessKeyToken.InteractionDisable

    // Outlined·active=true는 시맨틱 토큰이 아니라 PrimaryNormal에 알파를 곱해 표현한다
    // (Figma 실측: 배경 검정 5%, 테두리 검정 43% — 기존 Fill/Line 계열과 다른 검정 기반 틴트).
    val ActiveTintOpacity = AtomicOpacityToken.Opacity5
    val ActiveBorderOpacity = AtomicOpacityToken.Opacity43
}

// 사이즈당 한 번만 만들어 재사용한다. when으로 매 호출마다 새로 만들지 않는다.
private val ContentPaddingBySize = mapOf(
    ChipSize.XSmall to PaddingValues(horizontal = 7.dp, vertical = 4.dp),
    ChipSize.Small to PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    ChipSize.Medium to PaddingValues(horizontal = 11.dp, vertical = 7.dp),
    ChipSize.Large to PaddingValues(horizontal = 12.dp, vertical = 9.dp),
)

private val ShapeBySize = mapOf(
    ChipSize.XSmall to RoundedCornerShape(6.dp),
    ChipSize.Small to RoundedCornerShape(8.dp),
    ChipSize.Medium to RoundedCornerShape(10.dp),
    ChipSize.Large to RoundedCornerShape(10.dp),
)

internal fun ChipSize.contentPadding(): PaddingValues = ContentPaddingBySize.getValue(this)

internal fun ChipSize.shape(): Shape = ShapeBySize.getValue(this)
