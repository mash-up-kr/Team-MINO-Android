package team.mino.core.designsystem.component.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun MenuPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MinoMenu(modifier = Modifier.fillMaxWidth()) {
                MinoMenuItem(text = "기본", onClick = {})
                MinoMenuItem(text = "활성", onClick = {}, active = true)
                MinoMenuItem(text = "캡션", onClick = {}, caption = "설명")
                MinoMenuItem(text = "활성 + 캡션", onClick = {}, active = true, caption = "설명")
                MinoMenuItem(text = "비활성", onClick = {}, enabled = false)
                MinoMenuItem(text = "비활성 + 캡션", onClick = {}, enabled = false, caption = "설명")
            }
            MinoMenu(modifier = Modifier.fillMaxWidth()) {
                MinoMenuItem(
                    text = "축소 패딩",
                    onClick = {},
                    contentPadding = MinoMenuDefaults.ItemContentPaddingCompact,
                )
                MinoMenuItem(
                    text = "축소 패딩 + 활성",
                    onClick = {},
                    active = true,
                    contentPadding = MinoMenuDefaults.ItemContentPaddingCompact,
                )
                MinoMenuItem(
                    text = "축소 패딩 + 캡션",
                    onClick = {},
                    caption = "설명",
                    contentPadding = MinoMenuDefaults.ItemContentPaddingCompact,
                )
            }
        }
    }
}
