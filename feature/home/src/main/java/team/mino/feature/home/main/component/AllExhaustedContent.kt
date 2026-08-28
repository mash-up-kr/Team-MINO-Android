package team.mino.feature.home.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.home.R

/**
 * 모든 방의 모든 덱을 다 본 뒤 카드 자리에 놓이는 안내(spec FR-014).
 *
 * 상단 셸(방 뱃지·인사 문구·정렬 칩)은 그대로 남으므로 이 컴포저블은 카드 자리만 채운다.
 * **CTA 버튼을 두지 않는다** — 여기서 다시 볼 것을 고르는 길은 정렬 칩과 방 변경뿐이다(spec EC-010).
 * 버튼이 붙는 쪽은 볼 장소가 애초에 없던 [EmptyContent]다(spec EC-011).
 */
@Composable
internal fun AllExhaustedContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(ContentWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ContentSpacing),
    ) {
        Image(
            painter = painterResource(R.drawable.home_all_exhausted_illustration),
            contentDescription = null,
            modifier = Modifier.size(IllustrationSize),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = stringResource(R.string.home_all_exhausted_message),
            style = MinoAndroidTheme.typography.label1NormalRegular,
            color = MinoAndroidTheme.colors.primaryNormal,
            textAlign = TextAlign.Center,
        )
    }
}

private val ContentWidth = 294.dp

// Figma base lg 변수 대응 — 토큰 미존재
private val ContentSpacing = 20.dp

private val IllustrationSize = 249.dp

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun AllExhaustedContentPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative)
                .padding(20.dp),
        ) {
            AllExhaustedContent()
        }
    }
}
