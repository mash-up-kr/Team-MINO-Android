package team.mino.core.designsystem.component.roomthumbnail.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

/**
 * Room Thumbnail 컴포넌트 슬롯 → 디자인 토큰 키 매핑과, 대응 토큰이 없어 값으로 둔 치수.
 */
internal object RoomThumbnailTokens {
    val Size = 80.dp

    // Figma Radius 변수 대응 — 토큰 미존재
    val Shape: Shape = RoundedCornerShape(14.dp)

    /** 콜라주 셀 사이 간격. */
    val CellSpacing = 1.dp

    /** 이미지를 아직 못 받았거나 로딩에 실패한 셀의 배경. */
    val PlaceholderBackgroundColor = ColorAccessKeyToken.FillNormal

    val PlaceholderTint = ColorAccessKeyToken.LabelAssistive
}
