package team.mino.core.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import team.mino.core.designsystem.component.button.token.OutlinedIconButtonTokens
import team.mino.core.designsystem.foundation.color.token.value

/**
 * [MinoOutlinedIconButton]의 기본값 모음.
 *
 * 이 컴포넌트셋은 enabled 같은 상태 축이 없어 M3 `BadgeDefaults`처럼 Colors 클래스 없이
 * Defaults의 단일 값 프로퍼티로 색을 노출한다.
 */
object MinoOutlinedIconButtonDefaults {
    /** 아이콘 색. 슬롯 안에서 `LocalContentColor`로도 제공된다. */
    val contentColor: Color
        @Composable @ReadOnlyComposable get() = OutlinedIconButtonTokens.ContentColor.value

    /** 테두리 색. */
    val borderColor: Color
        @Composable @ReadOnlyComposable get() = OutlinedIconButtonTokens.BorderColor.value

    /** 테두리 두께. */
    val borderWidth: Dp get() = OutlinedIconButtonTokens.BorderWidth

    /** 원형 클리핑 셰이프. */
    val shape: Shape get() = OutlinedIconButtonTokens.Shape

    /** 아이콘 둘레 패딩. */
    val contentPadding: PaddingValues get() = OutlinedIconButtonTokens.ContentPadding

    /** 아이콘 슬롯 크기. */
    val iconSize: Dp get() = OutlinedIconButtonTokens.IconSize
}
