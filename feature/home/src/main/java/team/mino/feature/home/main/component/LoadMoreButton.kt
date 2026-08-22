package team.mino.feature.home.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Refresh
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * 카드덱 아래에 떠서 다음 장소 목록을 불러오는 버튼(Figma `002-2-3`의 `┗ Alternative Action`).
 *
 * Figma가 `Button/Button` 인스턴스를 쓰므로 [MinoButton]을 그대로 조합한다. 크기·스타일은 기본값
 * (`Large`·`SolidPrimary`)이 그대로 일치해 넘기지 않고, 인스턴스가 컴포넌트 기본 모서리를 덮어쓴
 * 완전한 pill만 바깥에서 [clip]으로 맞춘다 — [MinoButton]이 셰이프를 파라미터로 열지 않고,
 * 배경·리플이 모두 이 클립 안쪽에서 그려져 결과가 클립 모양을 따르기 때문이다.
 *
 * 노출 조건(C-08, 잔여 카드 2장 이하)은 이 버튼이 정하지 않는다. 언제 그릴지는 호출부(덱)가
 * 소유하고, 이 컴포저블은 [onClick]만 위로 올린다.
 */
@Composable
internal fun LoadMoreButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoButton(
        text = "이 방 장소 더 보기",
        onClick = onClick,
        modifier = modifier.clip(CircleShape),
        leadingIcon = { Icon(imageVector = MinoIcons.Refresh, contentDescription = null) },
    )
}

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun LoadMoreButtonPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative)
                .padding(16.dp),
        ) {
            LoadMoreButton(onClick = {})
        }
    }
}
