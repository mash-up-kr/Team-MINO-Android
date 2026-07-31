package team.mino.core.designsystem.component.avatar.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Avatar 컴포넌트 슬롯 → 디자인 토큰 키 매핑. Figma `Avatar/Avatar`(16215-25725) 기준.
 */
internal object AvatarTokens {
    val BackgroundColor = ColorAccessKeyToken.FillAlternative
    val BorderColor = ColorAccessKeyToken.LineNormalAlternative
    val PlaceholderTint = ColorAccessKeyToken.LabelAssistive
    val BorderWidth = 1.dp

    /** Company·Academy(둥근 사각형)의 코너 반경 비율(%). Person은 원형이라 미적용. */
    val SquircleCornerPercent = 28

    /** Avatar Group에서 아바타를 감싸는 배경 링. Figma `Avatar/Avatar Group`(15852-88488) 기준. */
    val GroupRingColor = ColorAccessKeyToken.BackgroundNormalNormal
    val GroupRingWidth = 1.5.dp

    /** Avatar Group 겹침 폭. Figma는 사이즈 변형 없이 고정 -6px을 쓴다. */
    val GroupOverlap = 6.dp

    /** Avatar Group 전체를 감싸는 pill 컨테이너. */
    val GroupContainerBackground = ColorAccessKeyToken.FillNormal
    val GroupContainerPadding = 4.dp
    val GroupContainerShape: Shape = RoundedCornerShape(percent = 50)

    /** 아바타 스택과 [team.mino.core.designsystem.component.avatar.MinoAvatarGroupTrailing.Add] 사이 간격. */
    val GroupTrailingSpacing = 8.dp

    /** [team.mino.core.designsystem.component.avatar.MinoAvatarGroupTrailing.Add] 버튼(멤버 추가). */
    val AddButtonBackgroundColor = ColorAccessKeyToken.PrimaryNormal
    val AddButtonIconColor = ColorAccessKeyToken.InversePrimary
    val AddButtonIconSize = 18.dp

    /** [team.mino.core.designsystem.component.avatar.MinoAvatarGroupTrailing.Overflow] 뱃지(초과 인원). */
    val OverflowBackgroundColor = ColorAccessKeyToken.BackgroundElevatedAlternative
    val OverflowLabelColor = ColorAccessKeyToken.LabelAlternative
    val OverflowLabelFont = TypographyAccessKeyToken.Label2Bold
}
