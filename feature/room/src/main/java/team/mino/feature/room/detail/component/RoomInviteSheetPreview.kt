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
import team.mino.core.domain.model.RoomMember
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
                inviteCode = "MOCK05",
                roomMembers = PREVIEW_INVITE_MEMBERS,
                onDismiss = {},
            )
        }
    }
}

/** [RoomInviteSheet] 프리뷰 — 초대 코드 발급 중 상태. */
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
                inviteCode = null,
                roomMembers = PREVIEW_INVITE_MEMBERS,
                onDismiss = {},
            )
        }
    }
}

private val PREVIEW_INVITE_MEMBERS = persistentListOf(
    RoomMember(userId = "user-me", nickname = "나", avatarUrl = null, isOwner = true, joinedAt = Clock.System.now()),
    RoomMember(userId = "user-2", nickname = "박지훈", avatarUrl = null, isOwner = false, joinedAt = Clock.System.now()),
    RoomMember(userId = "user-3", nickname = "채윤지", avatarUrl = null, isOwner = false, joinedAt = Clock.System.now()),
)
