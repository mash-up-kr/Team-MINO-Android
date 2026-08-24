package team.mino.core.designsystem.component.roomcard.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/**
 * Header Room 컴포넌트 슬롯 → 크기/간격 토큰 매핑. Figma `Header_Room`(15852:88515) 기준.
 *
 * 색·타이포 토큰(`ColorAccessKeyToken`·`TypographyAccessKeyToken`)은 design-system 내부(`internal`)
 * API라 여기서 직접 들고 있지 않고, `MinoHeaderRoomDefaults`가 `MinoAndroidTheme.colors`/
 * `.typography`(public API)로 직접 조회한다.
 */
internal object HeaderRoomTokens {
    val ContentPadding = PaddingValues(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    val DividerThickness = 1.dp

    /** title·memo 사이 간격. */
    val TitleMemoSpacing = 4.dp

    /** title·memo 블록과 하단 row 사이 간격. */
    val ContentRowSpacing = 10.dp

    /** 위치 아이콘·개수 텍스트 사이 간격. */
    val LocationIconTextSpacing = 2.dp

    val LocationIconSize = 18.dp

    val ThumbnailIconSize = 24.dp
}
