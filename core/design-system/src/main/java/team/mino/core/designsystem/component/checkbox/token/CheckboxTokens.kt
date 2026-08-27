package team.mino.core.designsystem.component.checkbox.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

/**
 * Checkbox 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 */
internal object CheckboxTokens {
    val BoxSize = 18.dp
    val BoxBorderWidth = 1.5.dp

    val BoxShape: Shape = RoundedCornerShape(5.dp)

    /** 상자를 컨테이너 중앙에 놓는 여백. 탭 영역은 여기까지다. */
    val ContainerPadding = 4.dp

    /** 리플이 탭 영역 밖으로 더 번지는 폭. 컴포넌트가 차지하는 자리는 넓히지 않는다. */
    private val RippleExpansion = 8.dp

    /** 원형 리플의 반경 — 탭 영역을 [RippleExpansion]만큼 넓힌 지름의 절반. */
    val RippleRadius = (BoxSize + ContainerPadding * 2 + RippleExpansion) / 2

    val CheckmarkSize = 18.dp

    val CheckedContainerColor = ColorAccessKeyToken.PrimaryNormal
    val CheckedCheckmarkColor = ColorAccessKeyToken.InverseLabel

    /** 미체크 상자는 채우기 레이어 없이 테두리만 그린다. */
    val UncheckedContainerColor: Color = Color.Transparent
    val UncheckedBorderColor = ColorAccessKeyToken.LineNormalNormal
}
