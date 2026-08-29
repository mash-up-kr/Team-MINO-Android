package team.mino.core.designsystem.component.pagination.token

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

/**
 * Pagination Dots 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 *
 * 선택/비선택은 색 슬롯이 갈리는 것이 아니라 **한 색의 불투명도**로만 갈린다. 점의 크기와
 * 모양은 두 상태가 같다.
 */
internal object PaginationDotsTokens {
    /** 점 하나의 한 변. 선택 여부와 무관하게 같다. */
    val DotSize = 10.dp

    /** 점과 점 사이 간격. */
    val DotSpacing = 10.dp

    val DotShape: Shape = CircleShape

    val DotColor = ColorAccessKeyToken.LabelNormal

    /** 비선택 점에 얹히는 불투명도. 선택 점은 색 그대로다. */
    val UnselectedDotOpacity = AtomicOpacityToken.Opacity16
}
