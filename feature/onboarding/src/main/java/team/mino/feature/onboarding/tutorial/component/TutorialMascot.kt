package team.mino.feature.onboarding.tutorial.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import team.mino.feature.onboarding.tutorial.model.TutorialStep

/**
 * 예시 카드의 위쪽 모서리에 걸터앉은 캐릭터. 포즈와 소품이 스텝마다 다르다.
 *
 * 장식이라 설명을 주지 않는다 — 옆의 안내 문구가 그 스텝이 무엇을 말하는지 이미 다 말한다.
 * 예시 이미지와 마찬가지로 누를 수 없다.
 *
 * **몸통이 카드 뒤로 들어가고 앞발만 카드 위로 올라오는 앞뒤 관계는 이미지에 구워져 있다.**
 * 카드 아래로 잠기는 부분이 지워진 채로 들어와, 호출부는 카드 위에 한 장만 얹으면 된다.
 *
 * 폭을 따라 늘어나지 않고 고정 크기로 그린다. 카드가 넓어져도 캐릭터는 함께 커지지 않는다.
 */
@Composable
internal fun TutorialMascot(
    step: TutorialStep,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(step.mascotRes),
        contentDescription = null,
        modifier = modifier.size(width = MascotWidth, height = MascotHeight),
    )
}

private val MascotWidth = 74.dp
private val MascotHeight = 80.dp
