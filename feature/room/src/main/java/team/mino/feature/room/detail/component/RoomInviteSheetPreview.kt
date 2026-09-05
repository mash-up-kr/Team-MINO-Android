@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomMember
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomThumbnail
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** [RoomInviteSheet] 프리뷰 — 초대 코드 발급 완료 상태, 참여자 3명. */
@UiModePreviews
@Composable
private fun RoomInviteSheetPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.materialDimmer),
            contentAlignment = Alignment.BottomCenter,
        ) {
            RoomInviteSheet(
                room = PREVIEW_ROOM,
                inviteCode = "MOCK05",
                roomMembers = PREVIEW_INVITE_MEMBERS,
                onDismiss = {},
                onInviteClick = {},
                onCopyLinkClick = {},
            )
        }
    }
}

/** [RoomInviteSheet] 프리뷰 — 초대 코드 발급 중 상태(버튼 비활성). */
@UiModePreviews
@Composable
private fun RoomInviteSheetIssuingPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.materialDimmer),
            contentAlignment = Alignment.BottomCenter,
        ) {
            RoomInviteSheet(
                room = PREVIEW_ROOM,
                inviteCode = null,
                roomMembers = PREVIEW_INVITE_MEMBERS,
                onDismiss = {},
                onInviteClick = {},
                onCopyLinkClick = {},
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
private val PREVIEW_ROOM = Room(
    id = "preview-room",
    name = "민호야 잘하자",
    description = "",
    color = RoomColor.RED,
    ownerId = "user-me",
    isPersonal = false,
    placeCount = 9,
    thumbnail = RoomThumbnail.ColorAndCharacter(color = "red"),
    memberSummary = RoomMemberSummary(visibleAvatars = emptyList(), overflowCount = 0),
    lastPlaceSavedAt = null,
    commentCount = 0,
)

private val PREVIEW_INVITE_MEMBERS = persistentListOf(
    RoomMember(
        userId = "user-me",
        nickname = "나",
        avatar = ProfileAvatar.Default,
        isOwner = true,
        joinedAt = Clock.System.now(),
    ),
    RoomMember(
        userId = "user-2",
        nickname = "박지훈",
        avatar = ProfileAvatar.Person2,
        isOwner = false,
        joinedAt = Clock.System.now(),
    ),
    RoomMember(
        userId = "user-3",
        nickname = "채윤지",
        avatar = ProfileAvatar.Person7,
        isOwner = false,
        joinedAt = Clock.System.now(),
    ),
)
