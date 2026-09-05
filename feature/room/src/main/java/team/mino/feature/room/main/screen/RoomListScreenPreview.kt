@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomThumbnail
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.vm.RoomListUiState
import kotlin.time.ExperimentalTime

/*
 * RoomListScreen의 상태별 렌더 프리뷰.
 *
 * RoomListMap이 실제 Google Maps SurfaceView를 그리는데, :core:map의 MinoMap은 IDE 정적
 * 프리뷰(LocalInspectionMode)를 별도로 처리하지 않는다 — 그래서 지도 타일 자체는 정적 프리뷰에서
 * 빈 화면으로 보인다(알려진 한계, 이 프리뷰가 고치는 범위 밖). 그 위에 얹히는 바텀시트·정렬
 * 드롭다운·카테고리 칩·현재 위치 버튼은 지도와 무관하게 정상 렌더된다 — 이번에 고친 것들(딤 배경,
 * 칩 크기, 트리거 모양·순서, GPS 버튼 그림자·아이콘)을 눈으로 확인하는 용도는 그걸로 충분하다.
 */

/** 진입 기본값 — 공동방 없이 개인방만, Nudge·Ghost Card 노출. */
@UiModePreviews
@Composable
private fun RoomListScreenNoGroupRoomPreview() {
    RoomListScreenPreviewContainer(
        RoomListUiState(
            sheetLevel = BottomSheetLevel.HALF,
            personalRoom = PERSONAL_ROOM,
            showNudge = true,
            showGhostCard = true,
        ),
    )
}

/** 공동방 2개 이상 — Half 고정 높이(380dp)로 3번째 카드부터 스크롤 어포던스. */
@UiModePreviews
@Composable
private fun RoomListScreenManyGroupRoomsPreview() {
    RoomListScreenPreviewContainer(
        RoomListUiState(
            sheetLevel = BottomSheetLevel.HALF,
            personalRoom = PERSONAL_ROOM,
            groupRooms = listOf(GROUP_ROOM_1, GROUP_ROOM_2, GROUP_ROOM_3).toImmutableList(),
        ),
    )
}

/** `Full` — 지도 위 컨트롤이 숨고 방 카드 목록이 시트 전체를 채운다. */
@UiModePreviews
@Composable
private fun RoomListScreenFullPreview() {
    RoomListScreenPreviewContainer(
        RoomListUiState(
            sheetLevel = BottomSheetLevel.FULL,
            personalRoom = PERSONAL_ROOM,
            groupRooms = listOf(GROUP_ROOM_1, GROUP_ROOM_2).toImmutableList(),
        ),
    )
}

@Composable
private fun RoomListScreenPreviewContainer(
    state: RoomListUiState,
    modifier: Modifier = Modifier,
) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.backgroundNormalNormal),
        ) {
            RoomListScreen(state = state, onIntent = {})
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
    memberSummary = RoomMemberSummary(visibleAvatars = emptyList(), overflowCount = 0),
    lastPlaceSavedAt = null,
    commentCount = 0,
)

private val GROUP_ROOM_1 = Room(
    id = "group-1",
    name = "민호야 잘하자",
    description = "팀 회식 장소 모음",
    color = RoomColor.CYAN,
    ownerId = "me",
    isPersonal = false,
    placeCount = 12,
    thumbnail = RoomThumbnail.ColorAndCharacter(color = "cyan"),
    memberSummary = RoomMemberSummary(
        visibleAvatars = persistentListOf(ProfileAvatar.Person1, ProfileAvatar.Person2, ProfileAvatar.Person3),
        overflowCount = 0,
    ),
    lastPlaceSavedAt = null,
    commentCount = 3,
)

private val GROUP_ROOM_2 = Room(
    id = "group-2",
    name = "주말 산책 코스",
    description = "걷기 좋은 길만 모아요",
    color = RoomColor.LIME,
    ownerId = "me",
    isPersonal = false,
    placeCount = 4,
    thumbnail = RoomThumbnail.ColorAndCharacter(color = "lime"),
    memberSummary = RoomMemberSummary(visibleAvatars = persistentListOf(ProfileAvatar.Person1), overflowCount = 0),
    lastPlaceSavedAt = null,
    commentCount = 0,
)

private val GROUP_ROOM_3 = Room(
    id = "group-3",
    name = "카페 탐방",
    description = "",
    color = RoomColor.ORANGE,
    ownerId = "me",
    isPersonal = false,
    placeCount = 7,
    thumbnail = RoomThumbnail.ColorAndCharacter(color = "orange"),
    memberSummary = RoomMemberSummary(
        visibleAvatars = persistentListOf(ProfileAvatar.Person1, ProfileAvatar.Person2),
        overflowCount = 6,
    ),
    lastPlaceSavedAt = null,
    commentCount = 21,
)
