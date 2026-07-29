package team.mino.core.designsystem.component.actionarea

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
import team.mino.core.designsystem.foundation.icons.icons.Link
import team.mino.core.designsystem.foundation.icons.icons.Pencil
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
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
            )
            // 보조 액션 (가로) — 보조는 글자 너비, 메인이 남는 폭을 가져간다
            MinoSubActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인", onClick = {}),
                subAction = ActionAreaAction(text = "보조", onClick = {}),
            )
            // 대체 액션 (세로)
            MinoAlternativeActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
                alternativeAction = ActionAreaAction(text = "대체 액션", onClick = {}),
            )
            // 비활성 — 두 액션을 각각 끌 수 있다
            MinoSubActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인", onClick = {}, enabled = false),
                subAction = ActionAreaAction(text = "보조", onClick = {}, enabled = false),
            )
            // 아이콘 — 두 자리 모두에 열려 있다 (MU_디자인 985:40313)
            MinoSubActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(
                    text = "방 편집",
                    onClick = {},
                    leadingIcon = { Icon(imageVector = MinoIcons.Pencil, contentDescription = null) },
                ),
                subAction = ActionAreaAction(
                    text = "장소 추가",
                    onClick = {},
                    leadingIcon = { Icon(imageVector = MinoIcons.Link, contentDescription = null) },
                ),
            )
            // sticky — 배경과 상단 페이드가 함께 생긴다 (기본형은 배경이 없다)
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
                sticky = true,
            )
        }
    }
}
