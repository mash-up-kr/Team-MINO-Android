package team.mino.feature.sample.main.component.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatarSize

/**
 * MemberAvatarStack 컴포넌트 슬롯 → 크기/간격 토큰 매핑. Figma `Avatar`(15852-88488) 기준.
 *
 * 색·타이포 토큰은 design-system 내부(`internal`) API라 여기서 직접 들고 있지 않고,
 * 컴포넌트가 `MinoAndroidTheme.colors`/`.typography`로 직접 조회한다.
 */
internal object MemberAvatarStackTokens {
    /** pill 컨테이너. */
    val ContainerShape: Shape = RoundedCornerShape(percent = 50)
    val ContainerPadding = 4.dp

    /** 아바타 스택과 추가 버튼 사이 간격. */
    val TrailingSpacing = 8.dp

    /** 아바타 겹침 폭. 디자인 시스템 Avatar Group과 달리 크기별로 갈리지 않고 6dp 고정이다. */
    val Overlap = 6.dp

    /** 아바타를 감싸는 흰 링. 바깥에 붙어 슬롯이 아바타보다 링 두께만큼 커진다. */
    val RingWidth = 1.5.dp

    val AvatarSize = MinoAvatarSize.Small

    val AddButtonIconSize = 18.dp
}
