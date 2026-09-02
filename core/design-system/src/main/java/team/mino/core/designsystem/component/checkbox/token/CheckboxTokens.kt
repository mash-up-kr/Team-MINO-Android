package team.mino.core.designsystem.component.checkbox.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
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

    /**
     * 비활성 체크박스의 불투명도.
     *
     * Figma가 비활성 상태를 색이 아니라 불투명도로 정의한다 — `2862-175313`("004-2-3 다른 방에
     * 공유_full_4개"의 이미 저장된 방 카드)의 `Checkbox` 노드가 `opacity 43%`이고, 그 안의 색 토큰은
     * 체크 상태와 같은 `Primary/Normal`·`Static/White`다. 그래서 `MinoCheckboxColors`에 `enabled` 축을
     * 늘리지 않고 컴포넌트가 자신을 흐리게 그린다. 값은 불투명도 스케일에서 가져온다 —
     * `MenuTokens.DisabledOpacity`가 같은 「비활성 흐리기」를 같은 토큰으로 쓴다.
     */
    val DisabledOpacity = AtomicOpacityToken.Opacity43
}
