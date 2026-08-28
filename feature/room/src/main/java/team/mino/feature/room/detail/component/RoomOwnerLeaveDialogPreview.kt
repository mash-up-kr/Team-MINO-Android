@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.detail.component

import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.model.RoomMember
import team.mino.feature.room.detail.vm.LeaveDialogState
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** [RoomOwnerLeaveDialog] — 1인 방(자동 삭제 경고) 상태. */
@UiModePreviews
@Composable
private fun RoomOwnerLeaveDialogSingleMemberPreview() {
    MinoAndroidAppTheme {
        RoomOwnerLeaveDialog(
            leaveDialogState = LeaveDialogState.ConfirmOwnerSingle,
            roomMembers = persistentListOf(),
            selectedMemberId = null,
            onMemberSelected = {},
            onConfirm = {},
            onCancel = {},
        )
    }
}

/** [RoomOwnerLeaveDialog] — 위임 대상 선택 상태. */
@UiModePreviews
@Composable
private fun RoomOwnerLeaveDialogDelegatePreview() {
    MinoAndroidAppTheme {
        RoomOwnerLeaveDialog(
            leaveDialogState = LeaveDialogState.DelegateOwner,
            roomMembers = PREVIEW_MEMBERS,
            selectedMemberId = PREVIEW_MEMBERS[0].userId,
            onMemberSelected = {},
            onConfirm = {},
            onCancel = {},
        )
    }
}

private val PREVIEW_MEMBERS = persistentListOf(
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
