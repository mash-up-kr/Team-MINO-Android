package team.mino.feature.mypage.profile.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun AvatarGridPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        var selectedAvatarId by remember { mutableIntStateOf(0) }
        AvatarGrid(
            modifier = modifier.padding(20.dp),
            selectedAvatarId = selectedAvatarId,
            onAvatarSelected = { selectedAvatarId = it },
        )
    }
}
