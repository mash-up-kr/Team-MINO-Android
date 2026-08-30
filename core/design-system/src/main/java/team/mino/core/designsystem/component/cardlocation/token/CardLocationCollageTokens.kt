package team.mino.core.designsystem.component.cardlocation.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * `Card_Location B`([team.mino.core.designsystem.component.cardlocation.MinoCardLocationCollage])
 * 전용 치수. 공유 치수는 [CardLocationTokens]가 소유한다.
 */
internal object CardLocationCollageTokens {
    /** [title, address] 줄 · 사진 줄 · [comment, avatar] 줄 사이 간격. */
    val ContentGroupSpacing = 12.dp

    val ThumbnailShape: Shape = RoundedCornerShape(12.dp)
    val ThumbnailAspectRatio = 4f / 5f
    val ThumbnailSpacing = 8.dp
    val ThumbnailBorderWidth = 1.dp

    /** 사진 줄에 나란히 놓이는 슬롯 개수. */
    const val THUMBNAIL_SLOT_COUNT = 2
}
