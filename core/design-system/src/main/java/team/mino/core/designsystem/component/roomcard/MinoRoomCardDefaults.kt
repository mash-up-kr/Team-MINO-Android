package team.mino.core.designsystem.component.roomcard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.component.roomcard.token.RoomCardTokens
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.typography.token.value

/**
 * [MinoRoomCard]·[MinoRoomCheckBoxCard]의 기본값 모음. 크기·간격은 [RoomCardTokens] 참고.
 *
 * 카드 본문은 상태를 갖지 않아 `Colors` 클래스 없이 단일 값 프로퍼티만 노출한다. 체크박스의
 * 상태별 색은 `MinoCheckboxDefaults`가 소유한다.
 */
object MinoRoomCardDefaults {
    val titleFont: TextStyle
        @Composable @ReadOnlyComposable get() = RoomCardTokens.TitleFont.value

    val titleColor: Color
        @Composable @ReadOnlyComposable get() = RoomCardTokens.TitleColor.value

    val memoFont: TextStyle
        @Composable @ReadOnlyComposable get() = RoomCardTokens.MemoFont.value

    val memoColor: Color
        @Composable @ReadOnlyComposable get() = RoomCardTokens.MemoColor.value

    val placeCountFont: TextStyle
        @Composable @ReadOnlyComposable get() = RoomCardTokens.PlaceCountFont.value

    val placeCountColor: Color
        @Composable @ReadOnlyComposable get() = RoomCardTokens.PlaceCountColor.value
}
