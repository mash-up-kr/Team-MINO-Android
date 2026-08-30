package team.mino.feature.sample.main.component.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatarGroupSize

/**
 * Invitation 컴포넌트 슬롯 → 크기/간격 토큰 매핑. Figma `invitation`(16108-23382, AOS) 기준.
 *
 * 색·타이포·그림자 토큰은 design-system 내부(`internal`) API라 여기서 직접 들고 있지 않고,
 * `MinoInvitationDefaults`가 `MinoAndroidTheme.colors`/`.typography`/`.shadows`로 직접 조회한다.
 */
internal object InvitationTokens {
    val CardWidth = 265.dp
    val CardShape: Shape = RoundedCornerShape(20.dp)
    val CardHorizontalPadding = 16.dp
    val CardVerticalPadding = 24.dp

    /** cover 블록과 텍스트 블록 사이 간격("base"). */
    val ContentSpacing = 16.dp

    /** 제목·부제 사이 간격("xs"). */
    val TextSpacing = 4.dp

    /** 아바타 그룹과 카운트 텍스트 사이 간격. */
    val FooterSpacing = 16.dp

    val CoverSize = 80.dp

    /** Figma 노드 실측값(md, 18.286px). */
    val CoverShape: Shape = RoundedCornerShape(18.29.dp)
    val CoverImagePadding = 14.dp

    val AvatarSize = MinoAvatarGroupSize.XSmall

    // 편지봉투(envelope)는 고정 에셋. 좌표는 Figma `invitation`(16108-23382) 노드 실측값(dp=px 가정).
    val EnvelopeSize = DpSize(308.dp, 322.dp)
    val EnvelopeBackTop = 62.23.dp
    val EnvelopeBackHeight = 167.46.dp
    val EnvelopeFrontTop = 154.27.dp
    val EnvelopeFrontHeight = 167.73.dp
}
