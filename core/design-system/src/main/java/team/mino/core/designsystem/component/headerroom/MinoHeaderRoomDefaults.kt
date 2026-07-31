package team.mino.core.designsystem.component.headerroom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.headerroom.token.HeaderRoomTokens
import team.mino.core.designsystem.foundation.color.token.value

/**
 * [MinoHeaderRoom]의 기본값 모음.
 *
 * Header Room은 enabled 같은 상태가 없어 M3 `BadgeDefaults`처럼 Colors 클래스 없이
 * Defaults의 단일 값 프로퍼티로 색을 노출한다.
 */
object MinoHeaderRoomDefaults {
    val titleColor: Color
        @Composable @ReadOnlyComposable get() = HeaderRoomTokens.TitleColor.value

    val memoColor: Color
        @Composable @ReadOnlyComposable get() = HeaderRoomTokens.MemoColor.value

    /** 위치 개수 텍스트·위치 아이콘·썸네일 아이콘이 공유하는 색. */
    val resourceColor: Color
        @Composable @ReadOnlyComposable get() = HeaderRoomTokens.ResourceColor.value

    val dividerColor: Color
        @Composable @ReadOnlyComposable get() = HeaderRoomTokens.DividerColor.value
}
