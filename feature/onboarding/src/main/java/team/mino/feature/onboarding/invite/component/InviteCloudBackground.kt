package team.mino.feature.onboarding.invite.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import team.mino.feature.onboarding.R

/**
 * 친구 초대 화면 아래쪽에 깔리는 구름 워터마크.
 *
 * 화면의 맨 뒤에 놓여 본문과 액션 영역이 그 위를 지나간다 — 액션 영역이 자기 배경을 깔지 않는
 * 기본형이라 이 워터마크가 버튼 뒤까지 그대로 비친다.
 *
 * 장식이라 [Image]에 설명을 주지 않는다. 읽어 줄 내용이 없는 그림이 초점을 가져가면 화면을
 * 훑는 순서가 길어진다.
 *
 * 폭을 꽉 채우고 높이는 비율로 따라오게 두어, 디자인 기준 폭과 다른 화면에서도 구름의 곡선이
 * 찌그러지지 않는다. 자리는 호출자가 [modifier]로 정한다.
 */
@Composable
internal fun InviteCloudBackground(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.invite_cloud_background),
        contentDescription = null,
        modifier = modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth,
    )
}
