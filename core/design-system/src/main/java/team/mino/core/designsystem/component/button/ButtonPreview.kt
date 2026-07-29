package team.mino.core.designsystem.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Bookmark
import team.mino.core.designsystem.foundation.icons.icons.Pencil
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun ButtonPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ButtonSize.entries.forEach { size ->
                ButtonStyle.entries.forEach { style ->
                    MinoButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "${size.name} ${style.name}",
                        onClick = {},
                        size = size,
                        style = style,
                    )
                }
            }
            // 비활성 — 알파만 낮아진다
            ButtonStyle.entries.forEach { style ->
                MinoButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "${style.name} disabled",
                    onClick = {},
                    enabled = false,
                    style = style,
                )
            }
            // 아이콘 슬롯 — LocalContentColor를 따라 글자와 같은 색으로 그려진다
            MinoButton(
                modifier = Modifier.fillMaxWidth(),
                text = "첫 장소 저장하기",
                onClick = {},
                size = ButtonSize.Medium,
                leadingIcon = { Icon(imageVector = MinoIcons.Bookmark, contentDescription = null) },
            )
            MinoButton(
                modifier = Modifier.fillMaxWidth(),
                text = "방 편집",
                onClick = {},
                style = ButtonStyle.OutlinedPrimary,
                leadingIcon = { Icon(imageVector = MinoIcons.Pencil, contentDescription = null) },
            )
        }
    }
}
