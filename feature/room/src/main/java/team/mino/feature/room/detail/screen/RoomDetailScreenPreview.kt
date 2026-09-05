@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.detail.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
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
import team.mino.feature.room.detail.vm.RoomDetailUiState
import team.mino.feature.room.main.model.BottomSheetLevel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/*
 * RoomDetailScreen의 상태별 렌더 프리뷰. RoomDetailMap이 실제 Google Maps SurfaceView를 그리는데
 * :core:map의 MinoMap은 IDE 정적 프리뷰(LocalInspectionMode)를 별도로 처리하지 않아 지도 타일 자체는
 * 빈 화면으로 보인다(RoomListScreenPreview와 같은 알려진 한계, 이 프리뷰가 고치는 범위 밖). 그 위에
 * 얹히는 바텀시트·정렬 오버레이·GPS 버튼은 지도와 무관하게 정상 렌더된다.
 */

/** `Peek` — 88dp, 헤더 줄만. 정렬 오버레이·GPS 버튼은 지도 위에 뜬다. */
@UiModePreviews
@Composable
private fun RoomDetailScreenPeekPreview() {
    RoomDetailScreenPreviewContainer(sheetLevel = BottomSheetLevel.PEEK)
}

/** `Half` — 444dp, 장소 목록까지 보인다. */
@UiModePreviews
@Composable
private fun RoomDetailScreenHalfPreview() {
    RoomDetailScreenPreviewContainer(sheetLevel = BottomSheetLevel.HALF)
}

/** `Full` — 화면 전체를 채워 지도 위 컨트롤(정렬 오버레이·GPS 버튼)이 숨는다. */
@UiModePreviews
@Composable
private fun RoomDetailScreenFullPreview() {
    RoomDetailScreenPreviewContainer(sheetLevel = BottomSheetLevel.FULL)
}

/** 장소가 없는 방 — [EC-001] 빈 상태 문구. */
@UiModePreviews
@Composable
private fun RoomDetailScreenEmptyPlacesPreview() {
    RoomDetailScreenPreviewContainer(sheetLevel = BottomSheetLevel.HALF, places = persistentListOf())
}

@Composable
private fun RoomDetailScreenPreviewContainer(
    sheetLevel: BottomSheetLevel,
    modifier: Modifier = Modifier,
    places: ImmutableList<Place> = PREVIEW_PLACES,
) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.backgroundNormalNormal),
        ) {
            RoomDetailScreen(
                state = RoomDetailUiState(
                    room = PREVIEW_ROOM,
                    sheetLevel = sheetLevel,
                    places = places,
                    sortOption = MapMarkerSortOption.ALL,
                    categoryFilter = PlaceCategoryFilter.ALL,
                    viewType = PlaceViewType.LIST,
                    isOwner = true,
                    isPersonalRoom = false,
                ),
                onIntent = {},
                onCurrentLocationClick = {},
            )
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
        thumbnailUrls = emptyList(),
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
        thumbnailUrls = emptyList(),
        savedAt = Clock.System.now(),
        commentCount = 0,
        isGgukPick = false,
        distanceMeters = null,
        location = GeoPoint(latitude = 37.4979, longitude = 127.0276),
    ),
).toImmutableList()
