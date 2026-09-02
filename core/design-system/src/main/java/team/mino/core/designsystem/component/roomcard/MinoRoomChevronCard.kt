package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.roomcard.token.RoomCardTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.ChevronRight

/**
 * 이동을 알리는 방 카드(Figma `Card_Room`, `Show list cell=on`에서 트레일링 슬롯을 꺽쇠로
 * 바꾼 형태).
 *
 * [MinoRoomCheckBoxCard]와 같은 자리를 쓰지만 고르는 카드가 아니라 **누르면 넘어가는** 카드다.
 * 클릭 영역이 카드 하나뿐이라 [onClick] 말고 다른 콜백을 두지 않는다.
 *
 * @param placeCountLabel 저장된 장소 개수 텍스트(예: "장소 12개"). 포맷은 호출부가 결정한다.
 * @param thumbnail 카드 왼쪽 썸네일 슬롯. 사진 콜라주와 폴백 중 무엇을 그릴지는 호출부가 정한다.
 * @param memo 방 설명. null이면 Figma `Show memo=off`.
 */
@Composable
fun MinoRoomChevronCard(
    title: String,
    placeCountLabel: String,
    onClick: () -> Unit,
    thumbnail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    memo: String? = null,
) {
    RoomCardRow(
        title = title,
        placeCountLabel = placeCountLabel,
        memo = memo,
        onClick = onClick,
        thumbnail = thumbnail,
        modifier = modifier,
    ) {
        Icon(
            modifier = Modifier.size(RoomCardTokens.ChevronSize),
            imageVector = MinoIcons.ChevronRight,
            contentDescription = null,
            tint = MinoRoomCardDefaults.chevronColor,
        )
    }
}
