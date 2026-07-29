package team.mino.core.designsystem.component.actionarea

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
private fun ActionAreaPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorAccessKeyToken.BackgroundNormalAlternative.value)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 메인 액션 단일형
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainActionText = "메인 액션",
                onMainActionClick = {},
            )
            // 메인 + 보조 액션형 (가로)
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainActionText = "메인",
                onMainActionClick = {},
                secondaryAction = ActionAreaSecondaryAction.Sub(
                    ActionAreaAction(text = "보조", onClick = {}),
                ),
            )
            // 메인 + 대체 액션형 (세로)
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainActionText = "메인 액션",
                onMainActionClick = {},
                secondaryAction = ActionAreaSecondaryAction.Alternative(
                    ActionAreaAction(text = "대체 액션", onClick = {}),
                ),
            )
            // 비활성 상태
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainActionText = "메인",
                onMainActionClick = {},
                mainActionEnabled = false,
                secondaryAction = ActionAreaSecondaryAction.Sub(
                    ActionAreaAction(text = "보조", onClick = {}, enabled = false),
                ),
            )
            // sticky — 배경과 상단 페이드가 함께 생긴다 (기본형은 배경이 없다)
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainActionText = "메인 액션",
                onMainActionClick = {},
                sticky = true,
            )
        }
    }
}
