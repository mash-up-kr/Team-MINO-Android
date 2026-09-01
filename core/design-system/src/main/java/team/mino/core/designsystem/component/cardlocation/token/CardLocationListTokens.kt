package team.mino.core.designsystem.component.cardlocation.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * `Card_Location A`([team.mino.core.designsystem.component.cardlocation.MinoCardLocationList])
 * 전용 치수. 공유 치수는 [CardLocationTokens]가 소유한다.
 */
internal object CardLocationListTokens {
    val ThumbnailSize = 94.dp
    val ThumbnailShape: Shape = RoundedCornerShape(4.7.dp)

    val ThumbnailBorderWidth = 1.dp

    /** [title, address] 묶음 줄과 [comment, avatar] 묶음 줄 사이 간격. */
    val ContentGroupSpacing = 24.dp
}
