package team.mino.feature.onboarding.tutorial.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import team.mino.feature.onboarding.tutorial.model.TutorialStep

/**
 * 스텝마다 갈리는 예시 이미지 카드.
 *
 * **누를 수 없다.** 이미지를 눌러 열 것이 이 화면에 없어 클릭 처리를 붙이지 않는다
 * (`docs/specs/onboarding-flow/contracts/onboarding-flow-ui.md` §4.2). 설명도 달지 않는다 —
 * 바로 위 안내 문구가 같은 내용을 글로 말하고 있어, 읽어 주면 같은 말이 두 번 나온다.
 *
 * 다섯 장 모두 배경에서 띄우지 않는다. 테두리는 이미지에 구워져 있어 이쪽에서 따로 그리지 않는다.
 *
 * 카드의 가로세로 비는 이미지 자체의 비와 같다. 폭이 좁아 그 비를 지킬 수 없는 화면에서는
 * 호출부가 준 높이 안에 맞춰 줄어든다.
 */
@Composable
internal fun TutorialExampleImage(
    step: TutorialStep,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(step.exampleImageRes),
        contentDescription = null,
        modifier = modifier
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(RoundedCornerShape(CardRadius)),
    )
}

private val CardRadius = 16.dp

private const val CARD_ASPECT_RATIO = 305f / 420f
