package team.mino.core.designsystem.component.roomthumbnail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import team.mino.core.designsystem.component.roomthumbnail.token.RoomThumbnailTokens
import team.mino.core.designsystem.foundation.color.token.value

/**
 * [MinoRoomThumbnail]의 기본값 모음.
 *
 * 상태(선택·비활성 등)가 없는 컴포넌트라 `Colors` 클래스를 두지 않고 단일 값 프로퍼티만 노출한다.
 */
object MinoRoomThumbnailDefaults {
    /** 썸네일 한 변의 길이. 폴백 슬롯도 이 정사각형 안에 들어간다. */
    val size: Dp = RoomThumbnailTokens.Size

    /** 썸네일 전체 모서리. 콜라주 셀은 이 모양으로 잘린다. */
    val shape: Shape = RoomThumbnailTokens.Shape

    val placeholderBackgroundColor: Color
        @Composable @ReadOnlyComposable get() = RoomThumbnailTokens.PlaceholderBackgroundColor.value

    val placeholderTint: Color
        @Composable @ReadOnlyComposable get() = RoomThumbnailTokens.PlaceholderTint.value
}
