package team.mino.core.designsystem.component.invitation.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatarSize
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.shadow.token.ShadowAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Invitation 컴포넌트 슬롯 → 디자인 토큰 키 매핑. Figma `invitation`(16108-23382, AOS) 기준.
 */
internal object InvitationTokens {
    val CardWidth = 265.dp
    val CardShape: Shape = RoundedCornerShape(20.dp)
    val CardBackgroundColor = ColorAccessKeyToken.BackgroundElevatedNormal
    val CardShadow = ShadowAccessKeyToken.NormalSmall
    val CardHorizontalPadding = 16.dp
    val CardVerticalPadding = 24.dp

    /** cover 블록과 텍스트 블록 사이 간격("base"). */
    val ContentSpacing = 16.dp

    /** 제목·부제 사이 간격("xs"). */
    val TextSpacing = 4.dp

    /** 아바타 그룹과 카운트 텍스트 사이 간격. */
    val FooterSpacing = 16.dp

    val CoverSize = 80.dp

    /** Figma 노드 실측값(md, 18.286px). 셰이프 스케일(Small/Medium/Large)에 없는 컴포넌트 전용 값. */
    val CoverShape: Shape = RoundedCornerShape(18.29.dp)
    val CoverImagePadding = 14.dp
    val CoverPlaceholderTint = ColorAccessKeyToken.LabelAssistive

    val TitleFont = TypographyAccessKeyToken.Body1NormalBold
    val TitleColor = ColorAccessKeyToken.LabelNormal
    val DescriptionFont = TypographyAccessKeyToken.Label2Medium
    val DescriptionColor = ColorAccessKeyToken.LabelAlternative
    val CountFont = TypographyAccessKeyToken.Label2Bold
    val CountColor = ColorAccessKeyToken.LabelAlternative

    val AvatarSize = MinoAvatarSize.XSmall

    // 편지봉투(envelope)는 고정 에셋. 좌표는 Figma `invitation`(16108-23382) 노드 실측값(dp=px 가정).
    val EnvelopeSize = DpSize(308.dp, 322.dp)
    val EnvelopeBackTop = 62.23.dp
    val EnvelopeBackHeight = 167.46.dp
    val EnvelopeFrontTop = 154.27.dp
    val EnvelopeFrontHeight = 167.73.dp
}
