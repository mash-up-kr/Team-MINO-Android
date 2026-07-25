package team.mino.core.designsystem.component.bottomnavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.bottomnavigation.token.BottomNavigationTokens

@Composable
fun MinoBottomNavigation(
    modifier: Modifier = Modifier,
    containerColor: Color = MinoBottomNavigationDefaults.containerColor,
    dividerColor: Color = MinoBottomNavigationDefaults.dividerColor,
    windowInsets: WindowInsets = MinoBottomNavigationDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .windowInsetsPadding(windowInsets),
    ) {
        HorizontalDivider(
            thickness = BottomNavigationTokens.DividerThickness,
            color = dividerColor,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            content = content,
        )
    }
}
