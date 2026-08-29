package team.mino.feature.onboarding.invite.screen

import androidx.compose.runtime.Composable
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/*
 * InviteScreen의 렌더 프리뷰.
 *
 * 장이 하나뿐인 것은 이 화면에 갈래가 없기 때문이다 — 링크를 확보했든 못 했든 같은 화면이라
 * 상태별 프리뷰를 만들 수 없다(contracts/onboarding-flow-ui.md §3.2·§3.4).
 */

@UiModePreviews
@Composable
private fun InviteScreenPreview() {
    MinoAndroidAppTheme {
        InviteScreen(
            onIntent = {},
            onClose = {},
        )
    }
}
