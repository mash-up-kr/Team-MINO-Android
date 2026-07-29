package team.mino.core.designsystem.component.avatar.token

import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

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

    /** Avatar Group에서 아바타를 감싸는 배경 링. */
    val GroupRingColor = ColorAccessKeyToken.BackgroundNormalNormal
    val GroupRingWidth = 2.dp

    /** Avatar Group 겹침 비율(크기의 1/N만큼 앞 아바타가 뒤를 덮는다). */
    val GroupOverlapDivisor = 3
}
