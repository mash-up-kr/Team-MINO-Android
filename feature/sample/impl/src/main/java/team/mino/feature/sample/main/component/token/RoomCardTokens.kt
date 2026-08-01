package team.mino.feature.sample.main.component.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatarSize

/**
 * Card_Room 컴포넌트 슬롯 → 크기/간격 토큰 매핑. Figma `Card_Room`(15892-81881) 기준.
 *
 * 색·타이포 토큰은 design-system 내부(`internal`) API라 여기서 직접 들고 있지 않고,
 * `MinoRoomCardDefaults`가 `MinoAndroidTheme.colors`/`.typography`로 직접 조회한다.
 */
internal object RoomCardTokens {
    val VerticalPadding = 12.dp

    /** 커버와 텍스트 블록 사이 간격. */
    val CoverSpacing = 12.dp

    /** 텍스트 블록과 체크박스 사이 간격. */
    val TrailingSpacing = 12.dp

    /** 제목·메모 사이 간격("xs"). */
    val TitleMemoSpacing = 4.dp

    /** 메모·장소 개수 사이 간격("sm"). */
    val MemoPlaceCountSpacing = 8.dp

    val CoverSize = 80.dp

    val AvatarSize = MinoAvatarSize.XSmall

    /** 그룹방에서 겹쳐 보여줄 참여자 아바타 최대 개수. 초과분은 렌더하지 않는다. */
    const val MAX_AVATAR_COUNT = 5

    val CheckBoxSize = 26.dp
    val CheckBoxBorderWidth = 1.dp
    val CheckBoxIconSize = 18.dp

    /** 셰이프 스케일(Small/Medium/Large)에 없는 컴포넌트 전용 값. Figma 카드의 `xs` 토큰(4)을 따른다. */
    val CheckBoxShape: Shape = RoundedCornerShape(4.dp)
}
