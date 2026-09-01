package team.mino.feature.room.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/** [RoomNudgeSheet]의 유일한 상태 — 문구·버튼 하나뿐인 정적 화면이라 상태 분기가 없다. */
@UiModePreviews
@Composable
private fun RoomNudgeSheetPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.backgroundElevatedNormal),
        ) {
            RoomNudgeSheet(onCreateClick = {})
        }
    }
}

/** [RoomNudgeAutoSheet]의 유일한 상태 — 딤·핸들·문구·버튼 2개뿐인 정적 화면이라 상태 분기가 없다. */
@UiModePreviews
@Composable
private fun RoomNudgeAutoSheetPreview() {
    MinoAndroidAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            RoomNudgeAutoSheet(visible = true, onCreateClick = {}, onDismissRequest = {})
        }
    }
}
