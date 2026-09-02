package team.mino.core.designsystem.component.roomcard.token

import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatarGroupSize
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Card_Room 컴포넌트 슬롯 → 디자인 토큰 키 매핑과, 대응 토큰이 없어 값으로 둔 치수.
 */
internal object RoomCardTokens {
    val VerticalPadding = 12.dp

    /** 썸네일·텍스트 블록·트레일링 슬롯을 잇는 간격. */
    val ItemSpacing = 12.dp

    /** 텍스트 블록의 고정 높이. 제목 묶음과 장소 개수 줄을 위아래 끝에 붙인다. */
    val ContentHeight = 78.dp

    // Figma xs 변수 대응 — 토큰 미존재
    val TitleMemoSpacing = 4.dp

    // Figma xs 변수 대응 — 토큰 미존재
    val PlaceCountTrailingSpacing = 4.dp

    val AvatarSize = MinoAvatarGroupSize.XSmall

    /** 카드 오른쪽 끝 꺽쇠의 크기. 체크박스가 차지하는 자리와 같은 크기다. */
    val ChevronSize = 26.dp

    /** 그룹방에서 겹쳐 보여줄 참여자 아바타 최대 개수. 초과분은 렌더하지 않는다. */
    const val MAX_AVATAR_COUNT = 5

    val TitleFont = TypographyAccessKeyToken.Body1NormalBold
    val TitleColor = ColorAccessKeyToken.LabelNormal

    val MemoFont = TypographyAccessKeyToken.Label2Medium
    val MemoColor = ColorAccessKeyToken.LabelAlternative

    val PlaceCountFont = TypographyAccessKeyToken.Label2Bold
    val PlaceCountColor = ColorAccessKeyToken.LabelAlternative

    val ChevronColor = ColorAccessKeyToken.LabelAlternative
}
