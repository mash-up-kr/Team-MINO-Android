package team.mino.core.designsystem.component.switch.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

/**
 * Switch 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 *
 * Figma 노드 `2410:114367`(Switch 컴포넌트셋) variant `2410:114368`/`2410:114369`와 대조
 * 완료(disable variant는 노드 접근 불가로 미검증).
 */
internal object SwitchTokens {
    val TrackWidth = 52.dp
    val TrackHeight = 32.dp
    val TrackShape: Shape = RoundedCornerShape(percent = 50)

    val ThumbSize = 24.dp
    private val ThumbPadding = 4.dp
    val ThumbUncheckedOffset = ThumbPadding
    val ThumbCheckedOffset = TrackWidth - ThumbSize - ThumbPadding

    val CheckedTrackColor = ColorAccessKeyToken.PrimaryNormal
    val CheckedThumbColor = ColorAccessKeyToken.StaticWhite
    val UncheckedTrackColor = ColorAccessKeyToken.FillStrong
    val UncheckedThumbColor = ColorAccessKeyToken.StaticWhite
    val DisabledTrackColor = ColorAccessKeyToken.InteractionDisable
    val DisabledThumbColor = ColorAccessKeyToken.LabelDisable
}
