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
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.home.R

/**
 * 볼 수 있는 장소가 애초에 하나도 없을 때 카드 자리에 놓이는 안내(spec FR-020·EC-011).
 *
 * 다 둘러본 [AllExhaustedContent]와 달리 **여기에만 `[공동방 만들기]` CTA가 붙는다** — 볼 것이
 * 없는 이유가 다 봐서가 아니라 아직 모은 장소가 없어서이므로, 다음 걸음이 방을 만드는 것이다.
 *
 * 이 화면은 Figma 시안이 없어 완료 안내의 레이아웃·타이포를 따르고 일러스트도 같은 것을 쓴다.
 *
 * @param onCreateRoom `[공동방 만들기]`를 눌렀을 때. 화면 전환 결정은 호출부가 한다.
 */
@Composable
internal fun EmptyContent(
    onCreateRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            text = stringResource(R.string.home_empty_message),
            style = MinoAndroidTheme.typography.label1NormalRegular,
            color = MinoAndroidTheme.colors.primaryNormal,
            textAlign = TextAlign.Center,
        )
        MinoButton(
            text = stringResource(R.string.home_empty_cta_create_room),
            onClick = onCreateRoom,
            size = ButtonSize.Medium,
        )
    }
}

private val ContentWidth = 294.dp

private val ContentSpacing = 20.dp

private val IllustrationSize = 249.dp

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun EmptyContentPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative)
                .padding(20.dp),
        ) {
            EmptyContent(onCreateRoom = {})
        }
    }
}
