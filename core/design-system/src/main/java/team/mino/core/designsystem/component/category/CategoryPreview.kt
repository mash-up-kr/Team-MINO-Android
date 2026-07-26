package team.mino.core.designsystem.component.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

private val SampleItems = persistentListOf("전체", "카페", "맛집", "액티비티", "숙소", "공연·전시", "체험", "여행")

@UiModePreviews
@Composable
private fun CategoryPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            var selectedIndex by remember { mutableIntStateOf(0) }
            MinoCategory(
                modifier = Modifier.fillMaxWidth(),
                items = SampleItems,
                selectedIndex = selectedIndex,
                onItemClick = { selectedIndex = it },
                trailingIconBadge = true,
                content = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(ColorAccessKeyToken.LabelNormal.value),
                    )
                },
            )
            // 아이콘 버튼 없이
            MinoCategory(
                modifier = Modifier.fillMaxWidth(),
                items = SampleItems,
                selectedIndex = 2,
                onItemClick = {},
            )
        }
    }
}
