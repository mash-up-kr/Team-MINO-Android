package team.mino.feature.room.detail.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.component.cardlocation.MinoCardLocationCollage
import team.mino.core.domain.model.Place
import team.mino.feature.room.detail.model.mockPlaceThumbnailUrl

/**
 * 카드형 장소 카드([PlaceViewType.CARD], FR-007) 목록 — [PlaceCardList]와 같은 세로 1열
 * [LazyColumn]이다. "카드형"은 열 배치가 아니라 사진이 크게 보이는 카드 스타일을 뜻한다(디자인 시스템
 * `Card_Location B` 대조 결과).
 *
 * @param onPlaceClick 장소 선택([SCR-006] 이동, [FR-001] 유저 플로우 1-4). 실제 네비게이션 배선은 호출부
 *   책임.
 * @param onPlaceMoreClick 카드 더보기[⋮] 클릭([FR-008]). 실제 메뉴([PlaceActionMenu]) 렌더링은
 *   [actionMenu] 슬롯이 담당한다 — 트리거 바로 옆에 붙어야 `Popup` 기본 정렬로 앵커링되기 때문이다.
 * @param onLoadMore 스크롤이 끝에 가까워졌을 때([LazyListLoadMoreEffect]) — 다음 페이지를 이어 받는다
 *   (`RoomDetailIntent.OnPlacesLoadMore`).
 * @param actionMenu 더보기 버튼 자리 옆에 함께 그릴 메뉴(호출부가 `expanded` 여부를 판단해 넘긴다).
 */
@Composable
internal fun PlaceCardGrid(
    places: ImmutableList<Place>,
    onPlaceClick: (Place) -> Unit,
    onPlaceMoreClick: (Place) -> Unit,
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit = {},
    actionMenu: @Composable (Place) -> Unit = {},
) {
    val listState = rememberLazyListState()
    LazyListLoadMoreEffect(listState = listState, itemCount = places.size, onLoadMore = onLoadMore)
    LazyColumn(state = listState, modifier = modifier.fillMaxWidth()) {
        items(items = places, key = { it.id }) { place ->
            PlaceGridItem(
                place = place,
                onClick = { onPlaceClick(place) },
                onMoreClick = { onPlaceMoreClick(place) },
                actionMenu = { actionMenu(place) },
            )
        }
    }
}

/**
 * [PlaceCardGrid] 단일 항목 — [Place]를 [MinoCardLocationCollage]("Card_Location B") 파라미터로
 * 매핑한다.
 *
 * [Place.thumbnailUrls]의 앞 두 장을 순서대로 슬롯에 채운다 — 서버가 한 장소에 여러 장을 내려주면
 * (`Pin.images`) 두 슬롯 모두 실제 사진으로 채워야지, 첫 장을 두 슬롯에 중복으로 넣으면 안 된다(실기기
 * 확인 — 서로 다른 장소인데 사진이 겹쳐 보였다). 사진이 한 장도 없으면 [PlaceListItem]과 같은 이유로
 * 두 슬롯 다 [mockPlaceThumbnailUrl] 목업 사진으로 채운다. 참여자 아바타 그룹도 [PlaceListItem]과 같은
 * 이유로 비워 둔다.
 */
@Composable
internal fun PlaceGridItem(
    place: Place,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionMenu: @Composable () -> Unit = {},
) {
    val mockThumbnailUrl = mockPlaceThumbnailUrl()
    val thumbnailUrls = remember(place.thumbnailUrls, mockThumbnailUrl) {
        place.thumbnailUrls
            .takeIf { it.isNotEmpty() }
            ?.take(2)
            ?.toImmutableList()
            ?: persistentListOf(mockThumbnailUrl, mockThumbnailUrl)
    }

    MinoCardLocationCollage(
        title = place.name,
        address = place.address,
        commentCount = place.commentCount,
        onClick = onClick,
        onMoreClick = onMoreClick,
        modifier = modifier,
        thumbnailUrls = thumbnailUrls,
        actionMenu = actionMenu,
    )
}
