package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.component.avatar.MinoAvatarGroup
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.component.roomcard.token.RoomCardTokens
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * 방 목록의 방 카드(Figma `Card_Room`(15892-81881), `Show list cell=off`).
 *
 * 장소 개수 줄 끝에 참여자 아바타를 표시한다. 개인방이면 1개, 그룹방이면 최대
 * [RoomCardTokens.MAX_AVATAR_COUNT]개까지 겹쳐 보여주고 초과분은 렌더하지 않는다.
 *
 * @param placeCountLabel 저장된 장소 개수 텍스트(예: "장소 12개"). 포맷은 호출부가 결정한다.
 * @param memo 방 설명. null이면 Figma `Show memo=off`.
 * @param coverImageUrl 커버 이미지 URL. null이거나 로딩에 실패하면 placeholder 글리프를 표시한다.
 * @param participantImageUrls 참여자 아바타 이미지 URL 목록(각각 null이면 placeholder).
 */
@Composable
fun MinoRoomCard(
    title: String,
    placeCountLabel: String,
    participantImageUrls: ImmutableList<String?>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverImageUrl: String? = null,
    memo: String? = null,
) {
    val visibleImageUrls =
        remember(participantImageUrls) {
            participantImageUrls.take(RoomCardTokens.MAX_AVATAR_COUNT).toImmutableList()
        }

    RoomCardContent(
        title = title,
        placeCountLabel = placeCountLabel,
        coverImageUrl = coverImageUrl,
        memo = memo,
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(onClick = onClick)
            .padding(vertical = RoomCardTokens.VerticalPadding),
    ) {
        MinoAvatarGroup(
            imageUrls = visibleImageUrls,
            variant = MinoAvatarVariant.Person,
            size = RoomCardTokens.AvatarSize,
        )
    }
}
