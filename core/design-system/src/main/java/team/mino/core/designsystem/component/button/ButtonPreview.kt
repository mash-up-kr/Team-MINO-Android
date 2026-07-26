package team.mino.core.designsystem.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun ButtonPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorAccessKeyToken.BackgroundNormalAlternative.value)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 메인 액션 단일형
            MinoButton(
                modifier = Modifier.fillMaxWidth(),
                mainActionText = "메인 액션",
                onMainActionClick = {},
            )
            // 메인 + 보조 액션형 (가로)
            MinoButton(
                modifier = Modifier.fillMaxWidth(),
                mainActionText = "메인",
                onMainActionClick = {},
                secondaryAction = ButtonSecondaryAction.Sub(ButtonAction(text = "보조", onClick = {})),
            )
            // 메인 + 대체 액션형 (세로)
            MinoButton(
                modifier = Modifier.fillMaxWidth(),
                mainActionText = "메인 액션",
                onMainActionClick = {},
                secondaryAction = ButtonSecondaryAction.Alternative(
                    ButtonAction(text = "대체 액션", onClick = {}),
                ),
            )
            // 비활성 상태
            MinoButton(
                modifier = Modifier.fillMaxWidth(),
                mainActionText = "메인",
                onMainActionClick = {},
                mainActionEnabled = false,
                secondaryAction = ButtonSecondaryAction.Sub(
                    ButtonAction(text = "보조", onClick = {}, enabled = false),
                ),
            )
            // Divider(상단 그라데이션) 없이
            MinoButton(
                modifier = Modifier.fillMaxWidth(),
                mainActionText = "메인 액션",
                onMainActionClick = {},
                divider = false,
            )
        }
    }
}
