package team.mino.feature.main.component

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import team.mino.core.designsystem.component.bottomnavigation.MinoBottomNavigation
import team.mino.core.designsystem.component.bottomnavigation.MinoBottomNavigationItem
import team.mino.feature.main.MainTab

@Composable
internal fun MainBottomBar(
    currentTab: MainTab?,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoBottomNavigation(modifier = modifier) {
        MainTab.entries.forEach { tab ->
            MinoBottomNavigationItem(
                selected = tab == currentTab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.labelRes),
                    )
                },
            )
        }
    }
}
