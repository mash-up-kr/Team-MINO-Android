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

/** [PlaceCardGrid]/[PlaceGridItem] 프리뷰 — [PlaceCardListPreview]와 같은 패턴. */
@UiModePreviews
@Composable
private fun PlaceCardGridPreview() {
    MinoAndroidAppTheme {
        PlaceCardGrid(
            places = PREVIEW_GRID_PLACES,
            onPlaceClick = {},
            onPlaceMoreClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .background(MinoAndroidTheme.colors.backgroundElevatedNormal),
        )
    }
}

private val PREVIEW_GRID_PLACES = persistentListOf(
    Place(
        id = "preview-grid-place-1",
        placeId = "preview-grid-place-1",
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
        id = "preview-grid-place-2",
        placeId = "preview-grid-place-2",
        name = "온기정육식당",
        address = "서울 성동구 성수이로 20",
        category = PlaceCategoryFilter.RESTAURANT,
        thumbnailUrl = "https://example.com/preview-thumbnail.jpg",
        savedAt = Clock.System.now(),
        commentCount = 0,
        isGgukPick = true,
        distanceMeters = null,
        location = GeoPoint(latitude = 37.4979, longitude = 127.0276),
    ),
    Place(
        id = "preview-grid-place-3",
        placeId = "preview-grid-place-3",
        name = "카페 노티드 성수",
        address = "서울 성동구 아차산로 15길 11",
        category = PlaceCategoryFilter.CAFE,
        thumbnailUrl = null,
        savedAt = Clock.System.now(),
        commentCount = 12,
        isGgukPick = false,
        distanceMeters = null,
        location = GeoPoint(latitude = 37.4979, longitude = 127.0276),
    ),
)
