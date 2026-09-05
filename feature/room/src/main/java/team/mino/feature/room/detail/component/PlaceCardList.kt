package team.mino.feature.room.detail.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.cardlocation.MinoCardLocationList
import team.mino.core.domain.model.Place
import team.mino.feature.room.detail.model.mockPlaceThumbnailUrl

/**
 * 리스트형 장소 카드([PlaceViewType.LIST], FR-007) 목록 — 좌측 썸네일 + 우측 이름·주소·카테고리·코멘트
 * 수. [LazyColumn] 항목으로 [MinoCardLocationList]를 그대로 쓴다.
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
internal fun PlaceCardList(
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
            PlaceListItem(
                place = place,
                onClick = { onPlaceClick(place) },
                onMoreClick = { onPlaceMoreClick(place) },
                actionMenu = { actionMenu(place) },
            )
        }
    }
}

/**
 * [PlaceCardList] 단일 항목 — [Place]를 [MinoCardLocationList]("Card_Location A") 파라미터로 매핑한다.
 *
 * [TBD] 참여자 아바타 그룹은 "이 장소를 저장/코멘트한 사람"을 보여주는 자리인데, [Place] 도메인 모델에는
 * 장소별 참여자 정보가 없다(방 전체 멤버([RoomMemberSummary])와는 다른 개념이라 그걸 대신 꽂으면 사실과
 * 다른 정보가 된다) — 백엔드가 필드를 확정하기 전까지 비워 둔다.
 *
 * [TBD] `place.thumbnailUrls`가 비어 있으면 [mockPlaceThumbnailUrl] 목업 사진으로 채운다 — 사진이 없는
 * 장소에서 폴백 글리프만 계속 보이면 카드형이 텅 비어 보인다는 실기기 확인 결과에 따른 임시 결정이다.
 */
@Composable
internal fun PlaceListItem(
    place: Place,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionMenu: @Composable () -> Unit = {},
) {
    MinoCardLocationList(
        title = place.name,
        address = place.address,
        commentCount = place.commentCount,
        onClick = onClick,
        onMoreClick = onMoreClick,
        modifier = modifier,
        thumbnailUrl = place.thumbnailUrls.firstOrNull() ?: mockPlaceThumbnailUrl(),
        actionMenu = actionMenu,
    )
}
