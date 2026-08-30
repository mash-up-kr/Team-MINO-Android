package team.mino.core.designsystem.component.bottomnavigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import team.mino.core.designsystem.component.bottomnavigation.token.BottomNavigationTokens
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable

@Composable
fun RowScope.MinoBottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    colors: MinoBottomNavigationItemColors = MinoBottomNavigationDefaults.itemColors(),
) {
    val contentColor = colors.contentColor(selected = selected)

    Column(
        modifier = modifier
            .weight(1f)
            .rippleSingleSelectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = BottomNavigationTokens.ItemVerticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BottomNavigationTokens.IconLabelSpacing),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Box(
                modifier = Modifier.size(BottomNavigationTokens.IconSize),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            ProvideTextStyle(
                value = BottomNavigationTokens.LabelFont.value,
                content = label,
            )
        }
    }
}
