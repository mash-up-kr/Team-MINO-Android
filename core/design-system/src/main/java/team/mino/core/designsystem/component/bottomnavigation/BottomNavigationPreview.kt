package team.mino.core.designsystem.component.bottomnavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun BottomNavigationPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorAccessKeyToken.BackgroundNormalAlternative.value)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MinoBottomNavigation(windowInsets = PreviewSafeAreaInsets) {
                MinoBottomNavigationItem(
                    selected = true,
                    onClick = {},
                    icon = { PreviewIcon() },
                    label = { Text(text = "홈") },
                )
                MinoBottomNavigationItem(
                    selected = false,
                    onClick = {},
                    icon = { PreviewIcon() },
                    label = { Text(text = "저장") },
                )
            }
            MinoBottomNavigation(windowInsets = PreviewSafeAreaInsets) {
                listOf("홈", "검색", "저장", "알림", "마이").forEachIndexed { index, label ->
                    MinoBottomNavigationItem(
                        selected = index == 0,
                        onClick = {},
                        icon = { PreviewIcon() },
                        label = { Text(text = label) },
                    )
                }
            }
        }
    }
}

// Figma Bottom Safe Area(14dp)를 정적으로 재현한 프리뷰 전용 인셋.
private val PreviewSafeAreaInsets = WindowInsets(bottom = 14.dp)

@Composable
private fun PreviewIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .background(color = LocalContentColor.current, shape = CircleShape),
    )
}
