package team.mino.core.designsystem.component.profileavatar.token

import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

/**
 * Profile Avatar 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 *
 * 아바타가 놓이는 자리는 프로필 화면 상단의 썸네일과 선택 그리드의 한 칸 둘뿐이고,
 * 디자인이 두 자리에 각각 다른 크기와 테두리 두께를 준다. 두 두께는 크기에 비례하지 않아
 * 한쪽에서 계산해 낼 수 없으므로 자리마다 값을 따로 든다.
 */
internal object ProfileAvatarTokens {
    /** 선택 그리드 한 칸의 지름. */
    val GridSize = 70.dp

    /** 화면 상단에 크게 보여 주는 썸네일의 지름. */
    val ThumbnailSize = 120.dp

    /**
     * 아바타를 두르는 테두리. 그림에는 굽혀 있지 않아 컴포넌트가 그려야 디자인과 같아진다.
     */
    val BorderColor = ColorAccessKeyToken.LineNormalAlternative
    val GridBorderWidth = 1.25.dp
    val ThumbnailBorderWidth = 5.dp
}
