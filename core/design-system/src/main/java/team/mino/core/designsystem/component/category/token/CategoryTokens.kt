package team.mino.core.designsystem.component.category.token

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.category.CategorySize
import team.mino.core.designsystem.component.chip.ChipSize
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

/**
 * Category 컴포넌트 슬롯 → 디자인 토큰 키 매핑. Figma `Category/Category`(node 16215-22127) 실측값 기준.
 *
 * 항목 칩의 **치수는 Chip과 완전히 같아** [chipSize] 매핑으로 `ChipSize` 토큰을 그대로 쓴다.
 * 색만 Category 전용이다. 두 컴포넌트를 합치지 않은 근거는
 * `docs/adr/2026-08-03-category-item-dedicated-chip.md` 참조.
 */
internal object CategoryTokens {
    val GradientEdgeWidth = 48.dp
    val ChipBorderWidth = 1.dp

    /** `Horizontal Padding=True`일 때 스크롤 콘텐츠 좌우에 붙는 여백. 크기와 무관하게 고정이다. */
    val HorizontalPadding = 20.dp

    /** 스크롤 영역과 트레일링 슬롯 사이 간격. */
    val TrailingSpacing = 20.dp

    val NormalActiveContainerColor = ColorAccessKeyToken.LabelStrong
    val NormalActiveContentColor = ColorAccessKeyToken.InverseLabel
    val NormalInactiveContainerColor = ColorAccessKeyToken.BackgroundNormalNormal

    // Alternative는 시맨틱 토큰이 아니라 Primary 한 색에서 배경·테두리를 알파로 파생한다
    // (Chip의 Outlined variant와 같은 방식·같은 값이다).
    val AlternativeActiveColor = ColorAccessKeyToken.PrimaryNormal
    val AlternativeActiveTintOpacity = AtomicOpacityToken.Opacity5
    val AlternativeActiveBorderOpacity = AtomicOpacityToken.Opacity43

    val InactiveContentColor = ColorAccessKeyToken.LabelAlternative
    val InactiveBorderColor = ColorAccessKeyToken.LineNormalNeutral
}

/**
 * Category 크기 → 항목 칩 크기. **이름이 한 단계씩 밀려 있다** — Figma가 Category 리소스 칩을
 * `XSmall/Small/Normal/Large`로, 독립 Chip을 `XSmall/Small/Medium/Large`로 따로 부르기 때문이다.
 * 실제 치수(48×24 · 57×32 · 65×36 · 67×40)는 네 단계 모두 두 컴포넌트가 완전히 같다.
 */
internal val CategorySize.chipSize: ChipSize
    get() =
        when (this) {
            CategorySize.Small -> ChipSize.XSmall
            CategorySize.Medium -> ChipSize.Small
            CategorySize.Large -> ChipSize.Medium
            CategorySize.XLarge -> ChipSize.Large
        }

/** 항목 사이 간격. */
internal val CategorySize.chipSpacing: Dp
    get() =
        when (this) {
            CategorySize.Small -> 4.dp
            CategorySize.Medium -> 6.dp
            CategorySize.Large -> 8.dp
            CategorySize.XLarge -> 10.dp
        }

/** `verticalPadding=true`일 때 위아래에 붙는 여백. */
internal val CategorySize.verticalPadding: Dp
    get() =
        when (this) {
            CategorySize.Small, CategorySize.Medium -> 8.dp
            CategorySize.Large, CategorySize.XLarge -> 10.dp
        }

/** 트레일링 슬롯의 한 변. Figma는 이 자리에 아이콘 버튼을 둔다. */
internal val CategorySize.trailingSize: Dp
    get() =
        when (this) {
            CategorySize.Small -> 20.dp
            CategorySize.Medium -> 22.dp
            CategorySize.Large, CategorySize.XLarge -> 24.dp
        }
