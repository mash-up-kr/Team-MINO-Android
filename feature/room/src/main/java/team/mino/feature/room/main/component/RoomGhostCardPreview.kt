package team.mino.feature.room.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/** [RoomGhostCard]의 유일한 상태 — 클릭 가능한 CTA 카드라 상태 분기가 없다. */
@UiModePreviews
@Composable
private fun RoomGhostCardPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalNormal)
                .padding(20.dp),
        ) {
            RoomGhostCard(onClick = {})
        }
    }
}
