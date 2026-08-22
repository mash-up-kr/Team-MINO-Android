package team.mino.core.designsystem.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.component.dialog.token.DialogTokens
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.typography.token.value

/**
 * [MinoDialog]의 기본값 모음.
 *
 * 상태(enabled 등)를 갖지 않는 컴포넌트라 M3 `BadgeDefaults`처럼 Colors 클래스 없이
 * Defaults의 단일 값 프로퍼티로 값을 노출한다.
 */
object MinoDialogDefaults {
    /** 컨테이너 기본 셰이프. */
    val shape: Shape = DialogTokens.ContainerShape

    /** 컨테이너 기본 배경색. */
    val containerColor: Color
        @Composable @ReadOnlyComposable get() = DialogTokens.ContainerColor.value

    /** 제목 기본 색. */
    val titleColor: Color
        @Composable @ReadOnlyComposable get() = DialogTokens.TitleColor.value

    /** 제목 기본 글자 스타일. */
    val titleTextStyle: TextStyle
        @Composable @ReadOnlyComposable get() = DialogTokens.TitleFont.value

    /** 본문 기본 색. */
    val messageColor: Color
        @Composable @ReadOnlyComposable get() = DialogTokens.MessageColor.value

    /** 본문 기본 글자 스타일. */
    val messageTextStyle: TextStyle
        @Composable @ReadOnlyComposable get() = DialogTokens.MessageFont.value
}
