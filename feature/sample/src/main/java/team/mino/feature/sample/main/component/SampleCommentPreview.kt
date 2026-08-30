package team.mino.feature.sample.main.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun SampleCommentPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            SampleComment(authorName = "이름", commentText = SAMPLE_COMMENT_SHORT)
            Spacer(modifier = Modifier.height(16.dp))
            SampleComment(authorName = "이름", commentText = SAMPLE_COMMENT_LONG)
        }
    }
}
