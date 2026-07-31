package team.mino.feature.main.placeholder.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * 탭 전환만 검증하기 위한 빈 화면. 각 탭의 실제 화면이 생기면 해당 화면으로 교체하고 이 파일은 제거한다.
 */
@Composable
internal fun MainTabPlaceholderScreen(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = MinoAndroidTheme.colors.labelNormal,
            style = MinoAndroidTheme.typography.body1NormalBold,
        )
    }
}
