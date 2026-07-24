package team.mino.core.designsystem.foundation.typography

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
// 시스템 폰트 배율이 커져도 텍스트 크기가 고정(1sp = 1dp)되어 UiMode 프리뷰와 동일해야 한다
@Preview(fontScale = 2f, name = "FontScale2x")
@Composable
private fun TypographyPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TypographyAccessKeyToken.entries.forEach { token ->
                Text(
                    text = token.name,
                    style = token.value.copy(
                        color = ColorAccessKeyToken.LabelNormal.value,
                    ),
                )
            }
        }
    }
}
