package team.mino.feature.room.detail.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * [listState]의 마지막으로 보이는 항목이 끝에서 [LOAD_MORE_THRESHOLD]개 이내로 들어오면 [onLoadMore]를
 * 부른다. 장소 목록 페이징([PlaceCardList]/[PlaceCardGrid], `RoomDetailViewModel.onPlacesLoadMore`,
 * "api 낭비 없게") 전용 — 다음 페이지 호출 자체의 중복 방지(로딩 중·더 없음 판정)는 ViewModel이 한다.
 */
@Composable
internal fun LazyListLoadMoreEffect(
    listState: LazyListState,
    itemCount: Int,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(listState, itemCount) {
        snapshotFlow { lastVisibleItemIndex(listState) }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (itemCount > 0 && lastVisibleIndex != null && lastVisibleIndex >= itemCount - LOAD_MORE_THRESHOLD) {
                    onLoadMore()
                }
            }
    }
}

private fun lastVisibleItemIndex(listState: LazyListState): Int? =
    listState.layoutInfo.visibleItemsInfo
        .lastOrNull()
        ?.index

private const val LOAD_MORE_THRESHOLD = 5
