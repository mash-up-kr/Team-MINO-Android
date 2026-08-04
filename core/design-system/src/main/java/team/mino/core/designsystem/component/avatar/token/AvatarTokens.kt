package team.mino.core.designsystem.component.avatar.token

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatarGroupSize
import team.mino.core.designsystem.component.avatar.MinoAvatarSize
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

/**
 * Avatar 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Avatar/Avatar`(16215-25725)·`Avatar/Avatar Group`(16215-26148) 실측값 기준.
 */
internal object AvatarTokens {
    val BackgroundColor = ColorAccessKeyToken.FillAlternative
    val BorderColor = ColorAccessKeyToken.LineNormalAlternative
    val PlaceholderTint = ColorAccessKeyToken.LabelAssistive
    val BorderWidth = 1.dp

    /** Company·Academy(둥근 사각형)의 코너 반경 비율(%). Person은 원형이라 미적용. */
    val SquircleCornerPercent = 28

    /**
     * 알림 배지 자리 크기. Figma는 배지 프레임을 아바타 **우상단 모서리에 중심을 맞춰** 얹기 때문에
     * 절반이 아바타 밖으로 나간다. 아바타 크기와 무관하게 20dp 고정이다.
     */
    val PushBadgeSize = 20.dp

    /**
     * Avatar Group에서 아바타를 감싸는 링. 아바타 **바깥**에 붙어 슬롯이 그만큼 커진다.
     * [BorderWidth] 보더 위에 덧그리면 두 선이 겹쳐 아바타 경계가 사라지므로 자리를 따로 준다.
     */
    val GroupRingColor = ColorAccessKeyToken.BackgroundNormalNormal
    val GroupRingWidth = 1.5.dp
}

/**
 * Avatar Group 크기 → 아바타 지름. Figma 그룹은 다섯 크기 중 **두 개만** 정의한다.
 */
internal val MinoAvatarGroupSize.avatarSize: MinoAvatarSize
    get() =
        when (this) {
            MinoAvatarGroupSize.XSmall -> MinoAvatarSize.XSmall
            MinoAvatarGroupSize.Small -> MinoAvatarSize.Small
        }

/** 앞 아바타를 파고드는 겹침 폭. */
internal val MinoAvatarGroupSize.overlap: Dp
    get() =
        when (this) {
            MinoAvatarGroupSize.XSmall -> 6.dp
            MinoAvatarGroupSize.Small -> 8.dp
        }

/** 아바타 스택과 트레일링 슬롯 사이 간격. */
internal val MinoAvatarGroupSize.trailingSpacing: Dp
    get() =
        when (this) {
            MinoAvatarGroupSize.XSmall -> 8.dp
            MinoAvatarGroupSize.Small -> 10.dp
        }
