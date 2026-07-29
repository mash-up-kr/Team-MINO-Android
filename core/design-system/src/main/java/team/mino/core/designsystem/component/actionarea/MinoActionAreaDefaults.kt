package team.mino.core.designsystem.component.actionarea

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.actionarea.token.ActionAreaTokens
import team.mino.core.designsystem.foundation.color.token.value

/**
 * 액션 영역 3종([MinoActionArea]·[MinoSubActionArea]·[MinoAlternativeActionArea])의 기본값 모음.
 *
 * 상태에 따라 달라지는 색이 없어 `Colors` 클래스 없이 단일 값 프로퍼티만 둔다
 * (`MinoBottomNavigationDefaults.containerColor`와 같은 형태).
 */
object MinoActionAreaDefaults {
    /**
     * `sticky = true`일 때만 깔리는 배경. 콘텐츠 위에 떠 있는 표면이라 Normal이 아니라 Elevated를 쓴다.
     * 상단 페이드도 이 색이 투명에서 차오르는 형태로 그려진다.
     */
    val stickyContainerColor: Color
        @Composable @ReadOnlyComposable get() = ActionAreaTokens.StickyContainerColor.value
}
