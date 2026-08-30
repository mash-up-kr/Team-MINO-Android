package team.mino.core.designsystem.component.button.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

/**
 * Outlined Icon Button 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Button/Icon/Outlined` 실측값 기준.
 */
internal object OutlinedIconButtonTokens {
    val ContentColor = ColorAccessKeyToken.LabelNormal
    val BorderColor = ColorAccessKeyToken.LineNormalNeutral

    /** 완전한 원. 크기와 무관하게 반지름이 짧은 변의 절반이다. */
    val Shape: Shape = CircleShape

    /**
     * 테두리 두께·패딩·아이콘 크기는 `Button/Button`의 아이콘 전용 Medium과 값이 같아
     * 그쪽 치수 토큰을 그대로 공유한다. 두 컴포넌트셋의 치수 대응은 아래 세 줄에만 기록하며,
     * Figma에서 값이 갈라지면 여기서 끊는다
     * (docs/adr/2026-08-03-category-item-dedicated-chip.md의 치수 공유 방침과 같다).
     */
    val BorderWidth: Dp = ButtonTokens.BorderWidth
    val ContentPadding: PaddingValues = ButtonSize.Medium.iconOnlyContentPadding()
    val IconSize: Dp = ButtonSize.Medium.iconOnlyIconSize
}
