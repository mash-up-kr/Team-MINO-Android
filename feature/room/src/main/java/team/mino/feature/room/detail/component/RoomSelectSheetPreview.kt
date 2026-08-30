@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomThumbnail
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** [RoomSelectSheet] 프리뷰 — 하나는 이미 저장됨(체크+비활성), 하나는 선택됨, 하나는 미선택. */
@UiModePreviews
@Composable
private fun RoomSelectSheetPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.materialDimmer),
            contentAlignment = Alignment.BottomCenter,
        ) {
            RoomSelectSheet(
                place = PREVIEW_PLACE,
                rooms = PREVIEW_ROOMS,
                alreadySavedRoomIds = persistentSetOf("group-2"),
                selectedRoomIds = persistentSetOf("group-1"),
                onRoomToggle = {},
                onCreateRoomClick = {},
                onShareClick = {},
                onDismiss = {},
            )
        }
    }
}

private val PREVIEW_PLACE = Place(
    id = "preview-place-1",
    name = "레이어스튜디오 10",
    address = "서울 성동구 상원4길 10",
    category = PlaceCategoryFilter.CAFE,
    thumbnailUrl = null,
    savedAt = Clock.System.now(),
    commentCount = 3,
    isGgukPick = false,
    distanceMeters = null,
    location = GeoPoint(latitude = 37.4979, longitude = 127.0276),
)

private val PREVIEW_ROOMS = persistentListOf(
    Room(
        id = "group-1",
        name = "민호야 잘하자",
        description = "팀 회식 장소 모음",
        color = RoomColor.CYAN,
        ownerId = "me",
        isPersonal = false,
        placeCount = 12,
        thumbnail = RoomThumbnail.ColorAndCharacter(color = "cyan"),
        memberSummary = RoomMemberSummary(visibleAvatars = emptyList(), overflowCount = 0),
        lastPlaceSavedAt = null,
        commentCount = 3,
    ),
    Room(
        id = "group-2",
        name = "매쉬업 화이팅",
        description = "팀원 모두가 좋아할 만한 술집 모음",
        color = RoomColor.ORANGE,
        ownerId = "me",
        isPersonal = false,
        placeCount = 2,
        thumbnail = RoomThumbnail.ColorAndCharacter(color = "orange"),
        memberSummary = RoomMemberSummary(visibleAvatars = emptyList(), overflowCount = 0),
        lastPlaceSavedAt = null,
        commentCount = 0,
    ),
    Room(
        id = "group-3",
        name = "언젠가 가야지",
        description = "저장만 하고 안 간 곳들",
        color = RoomColor.GREEN,
        ownerId = "me",
        isPersonal = false,
        placeCount = 3,
        thumbnail = RoomThumbnail.ColorAndCharacter(color = "green"),
        memberSummary = RoomMemberSummary(visibleAvatars = emptyList(), overflowCount = 0),
        lastPlaceSavedAt = null,
        commentCount = 0,
    ),
)
