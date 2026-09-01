package team.mino.core.designsystem.component.profileavatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import team.mino.core.designsystem.R

/**
 * 번들 아바타 한 종을 자리에 꽉 채워 그린다. `null`은 아직 고르지 않은 상태이며 기본 아바타로 간다.
 *
 * 번들 아바타를 그리는 컴포넌트가 [MinoProfileAvatarImage]와 `MinoAvatar` 둘이라, **어느 그림을
 * 어떻게 그리는지는 여기 한 곳만 안다.** 자리를 두르는 테두리·배경·클릭은 각 컴포넌트가 갖는다.
 */
@Composable
internal fun ProfileAvatarPainting(
    avatar: MinoProfileAvatar?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(avatar?.drawableRes ?: R.drawable.profile_avatar_default),
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}
