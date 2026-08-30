package team.mino.core.common.ui.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.theme.MinoAndroidTheme

/** [MinoScaffold]의 기본값. 셸 전체에 걸리는 표준을 여기 한 곳에서 공급한다. */
object MinoScaffoldDefaults {
    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MinoAndroidTheme.colors.backgroundNormalNormal
}
