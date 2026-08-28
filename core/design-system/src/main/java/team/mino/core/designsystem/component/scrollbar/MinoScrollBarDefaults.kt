package team.mino.core.designsystem.component.scrollbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import team.mino.core.designsystem.component.scrollbar.token.ScrollBarTokens
import team.mino.core.designsystem.foundation.color.token.value

/**
 * [MinoScrollBar]의 기본값 모음.
 *
 * ScrollBar는 enabled 같은 상태가 없어 M3 `BadgeDefaults`처럼 Colors 클래스 없이
 * Defaults의 단일 값 프로퍼티로 색을 노출한다.
 */
object MinoScrollBarDefaults {
    /** 스크롤 위치를 나타내는 썸 색. 트랙은 칠하지 않는다. */
    val thumbColor: Color
        @Composable @ReadOnlyComposable get() = ScrollBarTokens.ThumbColor.value

    /** 트랙 좌우 여백을 포함한 컴포넌트 전체 너비. */
    val width: Dp = ScrollBarTokens.Width

    /** 트랙이 컴포넌트 경계에서 물러나는 사방 여백. */
    val trackPadding: Dp = ScrollBarTokens.TrackPadding
}
