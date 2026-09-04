@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomThumbnail
import team.mino.feature.room.detail.model.PlaceViewType
import team.mino.feature.room.main.model.BottomSheetLevel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** `Peek` — 88dp, 헤더 줄(아바타+더보기+닫기)과 [MinoHeaderRoom]만. */
@UiModePreviews
@Composable
private fun RoomDetailBottomSheetPeekPreview() {
    RoomDetailBottomSheetPreviewContainer(sheetLevel = BottomSheetLevel.PEEK)
}

/** `Half` — 444dp, 장소 목록까지 보인다(정렬줄은 지도 위 오버레이라 이 시트엔 없음). */
@UiModePreviews
@Composable
private fun RoomDetailBottomSheetHalfPreview() {
    RoomDetailBottomSheetPreviewContainer(sheetLevel = BottomSheetLevel.HALF)
}

/** `Full` — 화면 전체, 자체 정렬줄(꾹Pick+뷰타입 토글, 카테고리 탭)을 그린다. */
@UiModePreviews
@Composable
private fun RoomDetailBottomSheetFullPreview() {
    RoomDetailBottomSheetPreviewContainer(sheetLevel = BottomSheetLevel.FULL)
}

/** `Half` — 더보기 메뉴가 펼쳐진 상태(방장, 버튼 아래로 편다). */
@UiModePreviews
@Composable
private fun RoomDetailBottomSheetHalfMoreMenuPreview() {
    RoomDetailBottomSheetPreviewContainer(sheetLevel = BottomSheetLevel.HALF, showMoreMenu = true)
}

@Composable
private fun RoomDetailBottomSheetPreviewContainer(
    sheetLevel: BottomSheetLevel,
    modifier: Modifier = Modifier,
    showMoreMenu: Boolean = false,
) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.backgroundElevatedNormal),
        ) {
            RoomDetailBottomSheet(
                sheetLevel = sheetLevel,
                room = PREVIEW_ROOM,
                placeCount = PREVIEW_PLACES.size,
                sortOption = MapMarkerSortOption.ALL,
                categoryFilter = PlaceCategoryFilter.ALL,
                viewType = PlaceViewType.LIST,
                showMoreMenu = showMoreMenu,
                isOwner = true,
                isPersonalRoom = false,
                onDraggedUp = {},
                onDraggedDown = {},
                onSortSelected = {},
                onCategoryFilterSelected = {},
                onViewTypeSelected = {},
                onInviteClick = {},
                onCloseClick = {},
                onMoreMenuClick = {},
                onMoreMenuDismiss = {},
                onEditRoomClick = {},
                onLeaveClick = {},
            ) {
                PlaceCardList(
                    places = PREVIEW_PLACES,
                    onPlaceClick = {},
                    onPlaceMoreClick = {},
                )
            }
        }
    }
}

private val PREVIEW_ROOM = Room(
    id = "group-1",
    name = "매쉬업 화이팅",
    description = "팀원 모두가 좋아할 만한 술집 모음",
    color = RoomColor.ORANGE,
    ownerId = "me",
    isPersonal = false,
    placeCount = 2,
    thumbnail = RoomThumbnail.ColorAndCharacter(color = "orange"),
    memberSummary = RoomMemberSummary(visibleAvatars = persistentListOf(ProfileAvatar.Person1), overflowCount = 0),
    lastPlaceSavedAt = null,
    commentCount = 0,
)

private val PREVIEW_PLACES = persistentListOf(
    Place(
        id = "preview-place-1",
        placeId = "preview-place-1",
        name = "을지로 골뱅이",
        address = "서울 중구 을지로 12길 8",
        category = PlaceCategoryFilter.RESTAURANT,
        thumbnailUrl = null,
        savedAt = Clock.System.now(),
        commentCount = 0,
        isGgukPick = false,
        distanceMeters = null,
        location = GeoPoint(latitude = 37.4979, longitude = 127.0276),
    ),
    Place(
        id = "preview-place-2",
        placeId = "preview-place-2",
        name = "커피한약방",
        address = "서울 중구 수표로 13길 2",
        category = PlaceCategoryFilter.CAFE,
        thumbnailUrl = null,
        savedAt = Clock.System.now(),
        commentCount = 0,
        isGgukPick = false,
        distanceMeters = null,
        location = GeoPoint(latitude = 37.4979, longitude = 127.0276),
    ),
).toImmutableList()
