package team.mino.core.designsystem.component.profileavatar

import androidx.compose.ui.unit.Dp
import team.mino.core.designsystem.component.profileavatar.token.ProfileAvatarTokens

/**
 * 디자인이 정의한 아바타 자리 두 곳.
 *
 * 지름과 테두리 두께가 한 쌍으로 정해져 있고 **두 두께가 지름에 비례하지 않아** 한쪽에서 계산해 낼 수 없다.
 * 그래서 자리를 열거해 두 값을 함께 들고 다닌다. 지름을 자유롭게 받으면 디자인에 없는 크기가 공개 API로
 * 열리고 그 자리의 테두리 두께를 지어내야 하므로, 정의된 자리만 고를 수 있게 한다.
 *
 * @property diameter 지름.
 * @property borderWidth 아바타를 두르는 테두리 두께. 테두리는 컴포넌트만 그린다.
 */
enum class MinoProfileAvatarSize(
    val diameter: Dp,
    internal val borderWidth: Dp,
) {
    /** 선택 그리드 한 칸. */
    Grid(ProfileAvatarTokens.GridSize, ProfileAvatarTokens.GridBorderWidth),

    /** 화면 상단에 크게 보여 주는 썸네일. */
    Thumbnail(ProfileAvatarTokens.ThumbnailSize, ProfileAvatarTokens.ThumbnailBorderWidth),
}
