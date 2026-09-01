package team.mino.feature.room.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.avatar.MinoAvatar
import team.mino.core.designsystem.component.avatar.MinoAvatarDefaults
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.feature.room.detail.component.token.RoomMemberAvatarStackTokens

/**
 * 방 상세 헤더의 멤버 아바타 pill — 아바타를 겹쳐 담고, 끝에 초과 인원 뱃지·초대(+) 버튼을 같은
 * 겹침 폭으로 붙인다.
 *
 * Figma `Avatar`(15852-88489, state=add·default·more) 스펙을 따른다. `MinoAvatarGroup`(배경 없이
 * 아바타만 겹치는 디자인 시스템 형태)과 달리 이 pill 조립은 Figma 디자인 시스템 컴포넌트가 아니라
 * 화면 레벨 조합이라(`docs/conventions/component-asset-placement.md` §1.2) `core:design-system`이
 * 아니라 이 feature가 소유한다.
 *
 * 방 멤버는 URL이 아니라 그림 식별자([MinoProfileAvatar])로만 표시하므로(`GET
 * /rooms/{roomId}/members`가 이미지 URL을 내려주지 않는다), `imageUrls`가 아니라 [profileAvatars]를
 * 받는다.
 *
 * @param profileAvatars 표시할 멤버 아바타 목록(이미 최대 4개로 잘려 온다, [RoomMemberSummary]).
 * @param overflowLabel 스택 끝에 겹쳐 붙는 초과 인원 뱃지 문구(Figma `state=more`, 예: "+5", "+99+").
 *   `null`이면 뱃지를 숨긴다.
 * @param onInviteClick 초대(+) 버튼 콜백(Figma `state=add`). `null`이면 버튼을 숨긴다.
 */
@Composable
internal fun RoomMemberAvatarStack(
    profileAvatars: ImmutableList<MinoProfileAvatar>,
    modifier: Modifier = Modifier,
    overflowLabel: String? = null,
    onInviteClick: (() -> Unit)? = null,
) {
    val ringColor = MinoAvatarDefaults.groupRingColor
    val slotShape = MinoAvatarDefaults.shape(MinoAvatarVariant.Person)

    Row(
        modifier = modifier
            .surface(
                shape = RoomMemberAvatarStackTokens.ContainerShape,
                containerColor = MinoAndroidTheme.colors.fillNormal,
            ).padding(RoomMemberAvatarStackTokens.ContainerPadding),
        horizontalArrangement = Arrangement.spacedBy(-RoomMemberAvatarStackTokens.Overlap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        profileAvatars.forEach { avatar ->
            RingedSlot(ringColor = ringColor, shape = slotShape) {
                MinoAvatar(
                    size = RoomMemberAvatarStackTokens.AvatarSize,
                    profileAvatar = avatar,
                )
            }
        }

        if (overflowLabel != null) {
            RingedSlot(ringColor = ringColor, shape = slotShape) {
                AvatarSlot(
                    containerColor = MinoAndroidTheme.colors.backgroundElevatedAlternative,
                    shape = slotShape,
                ) {
                    Text(
                        text = overflowLabel,
                        style = MinoAndroidTheme.typography.label2Bold,
                        color = MinoAndroidTheme.colors.labelAlternative,
                    )
                }
            }
        }

        if (onInviteClick != null) {
            AvatarSlot(
                containerColor = MinoAndroidTheme.colors.primaryNormal,
                shape = slotShape,
                onClick = onInviteClick,
            ) {
                Icon(
                    imageVector = MinoIcons.Plus,
                    contentDescription = "친구 초대",
                    tint = MinoAndroidTheme.colors.inversePrimary,
                    modifier = Modifier.size(RoomMemberAvatarStackTokens.AddButtonIconSize),
                )
            }
        }
    }
}

/** 아바타·뱃지를 흰 링으로 감싼다. 링 두께·색은 디자인 시스템 Avatar Group과 같은 값을 쓴다. */
@Composable
private fun RingedSlot(
    ringColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .surface(shape = shape, containerColor = ringColor)
            .padding(MinoAvatarDefaults.groupRingWidth),
    ) {
        content()
    }
}

/** 아바타와 같은 크기·모양의 원형 자리. 초과 인원 뱃지와 초대 버튼이 공유한다. */
@Composable
private fun AvatarSlot(
    containerColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(RoomMemberAvatarStackTokens.AvatarSize.dp)
            .surface(shape = shape, containerColor = containerColor)
            .then(if (onClick != null) Modifier.rippleSingleClickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
