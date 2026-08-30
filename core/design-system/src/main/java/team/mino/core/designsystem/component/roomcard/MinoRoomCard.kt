package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.component.avatar.MinoAvatarGroup
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.designsystem.component.roomcard.token.RoomCardTokens
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * 방 목록의 방 카드(Figma `Card_Room`, `Show list cell=off`).
 *
 * 장소 개수 줄 끝에 참여자 아바타를 표시한다. 개인방이면 1개, 그룹방이면 최대
 * [RoomCardTokens.MAX_AVATAR_COUNT]개까지 겹쳐 보여주고 초과분은 렌더하지 않는다.
 *
 * @param placeCountLabel 저장된 장소 개수 텍스트(예: "장소 12개"). 포맷은 호출부가 결정한다.
 * @param thumbnail 카드 왼쪽 썸네일 슬롯. 사진 콜라주와 폴백 중 무엇을 그릴지는 호출부가 정한다.
 * @param memo 방 설명. null이면 Figma `Show memo=off`.
 * @param participantAvatars 참여자 아바타 목록 — 서버가 `avatar.color`로 내려주는 번들 아바타다.
 */
@Composable
fun MinoRoomCard(
    title: String,
    placeCountLabel: String,
    participantAvatars: ImmutableList<MinoProfileAvatar>,
    onClick: () -> Unit,
    thumbnail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    memo: String? = null,
) {
    val visibleAvatars =
        remember(participantAvatars) {
            participantAvatars.take(RoomCardTokens.MAX_AVATAR_COUNT).toImmutableList()
        }

    RoomCardContent(
        title = title,
        placeCountLabel = placeCountLabel,
        memo = memo,
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(onClick = onClick)
            .padding(vertical = RoomCardTokens.VerticalPadding),
        thumbnail = thumbnail,
    ) {
        MinoAvatarGroup(
            profileAvatars = visibleAvatars,
            size = RoomCardTokens.AvatarSize,
        )
    }
}
