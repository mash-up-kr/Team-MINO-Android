@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomThumbnail
import team.mino.feature.room.main.model.BottomSheetLevel
import kotlin.time.ExperimentalTime

/** `Peek` — 핸들·헤더만, 방 카드 목록은 [content]가 호출되지 않아 자동으로 숨는다. */
@UiModePreviews
@Composable
private fun RoomListBottomSheetPeekPreview() {
    RoomListBottomSheetPreviewContainer(sheetLevel = BottomSheetLevel.PEEK)
}

/** `Half` — 공동방 1개 기준 고정 높이. */
@UiModePreviews
@Composable
private fun RoomListBottomSheetHalfPreview() {
    RoomListBottomSheetPreviewContainer(sheetLevel = BottomSheetLevel.HALF)
}

/** `Full` — 화면을 채우고 [+]·[X] 버튼이 나란히 뜬다. */
@UiModePreviews
@Composable
private fun RoomListBottomSheetFullPreview() {
    RoomListBottomSheetPreviewContainer(sheetLevel = BottomSheetLevel.FULL)
}

/** `Full`, 공동방 0개 — Figma `2661-157259`. 개인방 카드 아래로 Nudge가 남은 높이를 채운다. */
@UiModePreviews
@Composable
private fun RoomListBottomSheetFullNudgePreview() {
    RoomListBottomSheetPreviewContainer(sheetLevel = BottomSheetLevel.FULL, showNudge = true)
}

@Composable
private fun RoomListBottomSheetPreviewContainer(
    sheetLevel: BottomSheetLevel,
    showNudge: Boolean = false,
    modifier: Modifier = Modifier,
) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.backgroundElevatedNormal),
        ) {
            RoomListBottomSheet(
                sheetLevel = sheetLevel,
                groupRoomCount = 1,
                onDraggedUp = {},
                onDraggedDown = {},
                onAddRoomClick = {},
            ) {
                RoomListRoomCardList(
                    personalRoom = PERSONAL_ROOM,
                    groupRooms = if (showNudge) persistentListOf() else persistentListOf(GROUP_ROOM).toImmutableList(),
                    showNudge = showNudge,
                    onRoomCardClick = {},
                    onNudgeCreateClick = {},
                )
            }
        }
    }
}

private val PERSONAL_ROOM = Room(
    id = "personal",
    name = "내 장소",
    description = "",
    color = RoomColor.GRAY,
    ownerId = "me",
    isPersonal = true,
    placeCount = 0,
    thumbnail = RoomThumbnail.ColorAndCharacter(color = null),
    memberSummary = RoomMemberSummary(visibleAvatarUrls = emptyList(), overflowCount = 0),
    lastPlaceSavedAt = null,
    commentCount = 0,
)

private val GROUP_ROOM = Room(
    id = "group-1",
    name = "민호야 잘하자",
    description = "팀 회식 장소 모음",
    color = RoomColor.CYAN,
    ownerId = "me",
    isPersonal = false,
    placeCount = 12,
    thumbnail = RoomThumbnail.ColorAndCharacter(color = "cyan"),
    memberSummary = RoomMemberSummary(visibleAvatarUrls = persistentListOf(null, null), overflowCount = 0),
    lastPlaceSavedAt = null,
    commentCount = 3,
)
