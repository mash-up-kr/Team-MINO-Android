package team.mino.core.designsystem.component.chip.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.chip.ChipSize
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Chip 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 */
internal object ChipTokens {
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

/**
 * 크기마다 글자 크기가 갈린다. 칩 높이(24/32/36/40dp)는 `세로 패딩 × 2 + 행간`으로 떨어진다.
 */
internal val ChipSize.font: TypographyAccessKeyToken
    get() =
        when (this) {
            ChipSize.XSmall -> TypographyAccessKeyToken.Caption1Medium
            ChipSize.Small -> TypographyAccessKeyToken.Label1NormalMedium
            ChipSize.Medium, ChipSize.Large -> TypographyAccessKeyToken.Body2NormalMedium
        }

/** 리딩·트레일링 콘텐츠 슬롯의 한 변. Figma는 정사각 비율(1:1) 아이콘·썸네일을 넣는다. */
internal val ChipSize.contentSize: Dp
    get() =
        when (this) {
            ChipSize.XSmall -> 12.dp
            ChipSize.Small, ChipSize.Medium -> 14.dp
            ChipSize.Large -> 16.dp
        }

/** 콘텐츠 슬롯과 글자 사이 간격. */
internal val ChipSize.contentSpacing: Dp
    get() =
        when (this) {
            ChipSize.XSmall, ChipSize.Small -> 2.dp
            ChipSize.Medium, ChipSize.Large -> 3.dp
        }

/**
 * 글자 좌우에 추가로 붙는 여백. Figma가 글자를 `Wrapper` 프레임으로 한 번 감싸 주는 값이라
 * 칩 좌우 여백([contentPadding])과 슬롯↔글자 간격([contentSpacing]) 양쪽에 더해진다.
 */
internal val ChipSize.textHorizontalPadding: Dp
    get() =
        when (this) {
            ChipSize.XSmall -> 1.dp
            ChipSize.Small, ChipSize.Medium, ChipSize.Large -> 2.dp
        }
