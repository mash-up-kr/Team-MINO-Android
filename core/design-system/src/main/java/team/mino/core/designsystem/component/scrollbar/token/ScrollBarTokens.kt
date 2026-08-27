package team.mino.core.designsystem.component.scrollbar.token

import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

/**
 * ScrollBar 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 *
 * 트랙은 배경을 갖지 않는다. 채워지는 것은 썸 하나뿐이라 색 슬롯도 하나다.
 */
internal object ScrollBarTokens {
    val ThumbColor = ColorAccessKeyToken.FillStrong

    /** 트랙 좌우 여백을 포함한 컴포넌트 전체 너비. */
    val Width = 9.dp

    /** 트랙이 컴포넌트 경계에서 물러나는 사방 여백. */
    val TrackPadding = 3.dp
}
