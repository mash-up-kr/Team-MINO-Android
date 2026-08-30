package team.mino.feature.onboarding.tutorial.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * 지금 몇 번째 스텝을 보고 있는지 알리는 원형 배지.
 *
 * 숫자 색을 흰색으로 고정하지 않는다 — 배경으로 쓰는 색이 모드에 따라 뒤집혀, 고정하면 밝은
 * 배경 위 밝은 글자가 되는 조합이 생긴다. 채운 버튼(`MinoButton`의 Solid 계열)이 같은 이유로
 * 같은 선택을 하고 있다.
 */
@Composable
internal fun TutorialStepBadge(
    number: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(BadgeSize)
            .background(
                color = MinoAndroidTheme.colors.primaryNormal,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = MinoAndroidTheme.typography.body2NormalBold,
            color = MinoAndroidTheme.colors.inverseLabel,
        )
    }
}

private val BadgeSize = 28.dp
