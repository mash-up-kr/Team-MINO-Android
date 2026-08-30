@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * [PlaceCardList]/[PlaceListItem] 프리뷰 — 디자인 시스템 `Card_Location A`(`15852-88674`)와 실기기 대조
 * 없이도 레이아웃을 바로 확인할 수 있게 둔다. `RoomListBottomSheetPreview`와 같은 패턴
 * ([UiModePreviews] + [MinoAndroidAppTheme] 래핑).
 */
@UiModePreviews
@Composable
private fun PlaceCardListPreview() {
    MinoAndroidAppTheme {
        PlaceCardList(
            places = PREVIEW_PLACES,
            onPlaceClick = {},
            onPlaceMoreClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.backgroundElevatedNormal),
        )
    }
}

/** 카드 한 장만 놓고 여백·타이포를 확대해 보는 프리뷰. */
@UiModePreviews
@Composable
private fun PlaceListItemPreview() {
    MinoAndroidAppTheme {
        PlaceListItem(
            place = PREVIEW_PLACES[0],
            onClick = {},
            onMoreClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.backgroundElevatedNormal),
        )
    }
}

private val PREVIEW_PLACES = persistentListOf(
    Place(
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
    ),
    Place(
        id = "preview-place-2",
        name = "온기정육식당",
        address = "서울 성동구 성수이로 20",
        category = PlaceCategoryFilter.RESTAURANT,
        thumbnailUrl = null,
        savedAt = Clock.System.now(),
        commentCount = 0,
        isGgukPick = true,
        distanceMeters = null,
        location = GeoPoint(latitude = 37.4979, longitude = 127.0276),
    ),
)
