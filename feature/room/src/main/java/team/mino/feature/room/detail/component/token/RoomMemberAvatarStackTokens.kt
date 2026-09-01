package team.mino.feature.room.detail.component.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatarSize

/**
 * [RoomMemberAvatarStack] 슬롯 → 크기/간격 토큰 매핑. Figma `Avatar`(15852-88489) 기준.
 *
 * 색·타이포 토큰은 design-system의 시맨틱 홀더(`MinoAndroidTheme.colors`/`.typography`)로 컴포넌트가
 * 직접 조회한다 — 여기는 그 홀더로 표현할 수 없는 치수·간격만 둔다.
 */
internal object RoomMemberAvatarStackTokens {
    /** pill 컨테이너. */
    val ContainerShape: Shape = RoundedCornerShape(percent = 50)
    val ContainerPadding = 4.dp

    /** 겹침 폭. 아바타·초과 인원 뱃지·초대 버튼 모두 같은 pill 안이라 셋에 똑같이 적용된다. */
    val Overlap = 6.dp

    val AvatarSize = MinoAvatarSize.Small

    val AddButtonIconSize = 18.dp
}
