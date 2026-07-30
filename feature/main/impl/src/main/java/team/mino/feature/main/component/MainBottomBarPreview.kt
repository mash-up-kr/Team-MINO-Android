package team.mino.feature.main.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.main.MainTab

@UiModePreviews
@Composable
private fun MainBottomBarPreview() {
    MinoAndroidAppTheme {
        MainBottomBar(
            modifier = Modifier.fillMaxWidth(),
            currentTab = MainTab.HOME,
            onTabSelected = {},
        )
    }
}
