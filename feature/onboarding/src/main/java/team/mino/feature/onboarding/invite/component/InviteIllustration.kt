package team.mino.feature.onboarding.invite.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import team.mino.feature.onboarding.R

/**
 * 친구 초대 화면 가운데의 캐릭터 일러스트.
 *
 * 장식이라 설명을 주지 않는다 — 옆의 제목과 본문이 이 화면이 무엇을 말하는지 이미 다 말한다.
 *
 * 폭을 따라 늘어나지 않고 고정 크기로 그린다. 화면이 넓어져도 캐릭터가 함께 커지지 않는 것이
 * 디자인이고, 그래서 세로 여백만 늘고 준다.
 */
@Composable
internal fun InviteIllustration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.invite_illustration),
        contentDescription = null,
        modifier = modifier.size(width = IllustrationWidth, height = IllustrationHeight),
    )
}

private val IllustrationWidth = 234.dp
private val IllustrationHeight = 288.dp
