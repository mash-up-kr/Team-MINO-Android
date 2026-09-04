package team.mino.feature.room.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.common.ui.component.RoomThumbnailFallback
import team.mino.core.designsystem.R
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.component.avatar.MinoAvatar
import team.mino.core.designsystem.component.avatar.MinoAvatarSize
import team.mino.core.designsystem.component.button.MinoOutlinedIconButton
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.foundation.icons.icons.Link
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomMember
import team.mino.core.domain.model.RoomThumbnail
import team.mino.feature.room.component.toMinoRoomColor
import team.mino.feature.room.main.model.image

/**
 * [RoomInviteSheet] 치수 토큰. 시트 chrome(핸들·모서리)은 `component/RoomShareSheet`와 동일 패턴을
 * 재사용한다 — 같은 시트 계열이라 값도 같다.
 *
 * Figma `004-4-2_친구 초대 클릭`(node `2542-125843`) 대조 결과 — 헤더 줄(방 커버 46×46 + 이름 + 닫기),
 * 참여자 목록 176dp 고정, 하단 액션 영역(초대하기·링크 복사하기 두 버튼)으로 구성된다.
 *
 * 헤더와 참여자 목록 사이는 구분선이 아니라 **12dp 여백**이다(node `2542-125863` 대조) — 이전에는
 * `HorizontalDivider`를 넣었는데 Figma에는 그 선이 없다. 참여자 행도 고정 높이(48dp) 대신 아바타를
 * 상하 12dp 패딩으로 감싸는 형태다(같은 노드 "Frame 124" 대조) — 아바타 48dp + 상하 12dp = 행 높이 72dp.
 */
private object RoomInviteSheetTokens {
    val SheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val HandleSize = DpSize(38.dp, 4.dp)
    val HandleShape = RoundedCornerShape(4.dp)
    val HandleTopPadding = 8.dp
    val HandleBottomPadding = 8.dp

    val HeaderHorizontalPadding = 20.dp
    val HeaderHeight = 60.dp
    val HeaderSpacing = 12.dp
    val HeaderThumbnailSize = 46.dp
    val HeaderToListSpacing = 12.dp

    val MemberListHeight = 176.dp
    val MemberRowVerticalPadding = 12.dp
    val MemberRowHorizontalPadding = 20.dp
    val MemberRowSpacing = 12.dp
}

/**
 * [친구 +] 초대 바텀시트([SYS-006] Flow B, FR-011) — 방 헤더 + 참여자 전체 목록 + 초대 액션.
 *
 * 이 컴포저블은 두 버튼의 콜백만 올려보낸다 — 링크 조립·클립보드 복사·OS 공유 시트 연동은
 * `RoomDetailViewModel`·`RoomDetailRoute`가 처리한다(`RoomDetailSideEffect.ShareInviteLink`·
 * `CopyInviteLink`). Figma `3261-204321`의 "초대하기" 전용 화면 전환만 [SYS-006] 전용 spec이 아직
 * 없어 범위 밖이다(research.md D11) — 지금은 OS 공유 시트로 대신한다. [inviteCode]가 아직 없으면
 * (발급 중) 두 버튼을 비활성화한다 — Figma 컴포넌트가 갖는 `Loading` 상태를 지금은 비활성 처리로 대신한다.
 *
 * @param room 초대를 보내는 방. `null`이면 아직 로드되지 않은 상태라 헤더를 비워 둔다.
 * @param inviteCode 서버가 발급한 초대 코드. `null`이면 아직 발급 중인 상태로 본다.
 * @param roomMembers 방 참여자 전체 목록(`RoomRepository.getMembers`, [FR-011]).
 * @param onInviteClick "초대하기" 클릭.
 * @param onCopyLinkClick "링크 복사하기" 클릭.
 */
@Composable
internal fun RoomInviteSheet(
    room: Room?,
    inviteCode: String?,
    roomMembers: ImmutableList<RoomMember>,
    onDismiss: () -> Unit,
    onInviteClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 단일 단계(레벨 전환 없음) 시트라 heights는 1개짜리 목록이다 — 그 경우 RoomDetailDraggableSheet는
    // 위로 끌어도(onDraggedUp, 이미 최고단이라 무시) 반응하지 않고, 아래로 끌면(이미 최하단이라)
    // 곧장 onDismiss로 이어진다(#290, #144).
    RoomDetailDraggableSheet(
        levelIndex = 0,
        heights = persistentListOf(RoomDetailSheetHeight.WrapContent),
        onDraggedUp = {},
        onDraggedDown = {},
        onDismiss = onDismiss,
        modifier = modifier,
        shape = RoomInviteSheetTokens.SheetShape,
        handle = { RoomInviteDragHandle() },
        header = { RoomInviteHeader(room = room, onCloseClick = onDismiss) },
        content = {
            Spacer(modifier = Modifier.height(RoomInviteSheetTokens.HeaderToListSpacing))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(RoomInviteSheetTokens.MemberListHeight),
            ) {
                items(items = roomMembers, key = { it.userId }) { member ->
                    RoomInviteMemberRow(member = member)
                }
            }

            MinoActionArea(
                mainAction = ActionAreaAction(
                    text = "초대하기",
                    onClick = onInviteClick,
                    enabled = inviteCode != null,
                ),
                alternativeAction = ActionAreaAction(
                    text = "링크 복사하기",
                    onClick = onCopyLinkClick,
                    enabled = inviteCode != null,
                    leadingIcon = { Icon(imageVector = MinoIcons.Link, contentDescription = null) },
                ),
                sticky = true,
            )
        },
    )
}

/** 드래그 핸들. 실제 드래그 감지는 [RoomDetailDraggableSheet]가 갖고, 이 컴포저블은 시각 요소만 그린다. */
@Composable
private fun RoomInviteDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = RoomInviteSheetTokens.HandleTopPadding,
                bottom = RoomInviteSheetTokens.HandleBottomPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(RoomInviteSheetTokens.HandleSize.width, RoomInviteSheetTokens.HandleSize.height)
                .background(color = MinoAndroidTheme.colors.fillNormal, shape = RoomInviteSheetTokens.HandleShape),
        )
    }
}

/** 방 커버 46dp 원형 썸네일 + 방 이름 + 닫기(X) 버튼. */
@Composable
private fun RoomInviteHeader(
    room: Room?,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RoomInviteSheetTokens.HeaderHeight)
            .padding(horizontal = RoomInviteSheetTokens.HeaderHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(RoomInviteSheetTokens.HeaderSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomInviteHeaderThumbnail(
            thumbnail = room?.thumbnail,
            modifier = Modifier.size(RoomInviteSheetTokens.HeaderThumbnailSize),
        )
        Text(
            text = room?.name.orEmpty(),
            style = MinoAndroidTheme.typography.body1NormalBold,
            color = MinoAndroidTheme.colors.labelNormal,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        MinoOutlinedIconButton(onClick = onCloseClick) {
            Icon(imageVector = MinoIcons.Close, contentDescription = "닫기")
        }
    }
}

/**
 * 방 커버 원형 썸네일. [RoomThumbnail.Collage]는 첫 장만 쓴다 — 이 자리는 46dp라 콜라주 4분할이
 * 오히려 뭉개져 보인다. 사진이 없으면(`ColorAndCharacter`) 방 대표 색 캐릭터로 대신한다.
 */
@Composable
private fun RoomInviteHeaderThumbnail(
    thumbnail: RoomThumbnail?,
    modifier: Modifier = Modifier,
) {
    when (thumbnail) {
        is RoomThumbnail.Collage ->
            MinoAsyncImage(
                imageUrl = thumbnail.imageUrls.firstOrNull(),
                fallback = painterResource(R.drawable.ic_avatar_company),
                fallbackTint = MinoAndroidTheme.colors.labelAlternative,
                modifier = modifier.clip(CircleShape),
            )

        is RoomThumbnail.ColorAndCharacter ->
            RoomThumbnailFallback(
                color = thumbnail.color?.toMinoRoomColor(),
                modifier = modifier.clip(CircleShape),
            )

        null -> Box(modifier = modifier.clip(CircleShape).background(MinoAndroidTheme.colors.fillNormal))
    }
}

/** 참여자 목록 한 행 — 48×48dp 아바타 + 닉네임. 체크박스 없는 순수 조회 행이다. */
@Composable
private fun RoomInviteMemberRow(
    member: RoomMember,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = RoomInviteSheetTokens.MemberRowHorizontalPadding,
                vertical = RoomInviteSheetTokens.MemberRowVerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(RoomInviteSheetTokens.MemberRowSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MinoAvatar(
            profileAvatar = member.avatar.image,
            size = MinoAvatarSize.Large,
        )
        Text(
            text = member.nickname,
            style = MinoAndroidTheme.typography.label1NormalMedium,
            color = MinoAndroidTheme.colors.labelAlternative,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
