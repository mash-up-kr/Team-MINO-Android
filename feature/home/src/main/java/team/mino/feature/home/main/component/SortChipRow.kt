package team.mino.feature.home.main.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.component.category.CategorySize
import team.mino.core.designsystem.component.category.CategoryVariant
import team.mino.core.designsystem.component.category.MinoCategory
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.DeckSort
import team.mino.feature.home.R

/**
 * 카드 위에 놓이는 정렬 칩 3종(spec FR-009).
 *
 * 순서는 [DeckSort]의 선언 순서를 그대로 따른다 — 그 순서가 곧 덱 전환의 우선순위라
 * 화면용 순서를 따로 두면 둘이 어긋난다.
 *
 * 좌우 여백은 호출부가 준다 — 시안이 칩 행을 화면 콘텐츠 폭에 맞춰 두므로 이 행만의 값이 아니다.
 *
 * @param sort 지금 보고 있는 덱의 정렬. 자동 전환으로 덱이 바뀌면 선택 표시도 함께 옮겨진다(spec UX-004).
 */
@Composable
internal fun SortChipRow(
    sort: DeckSort,
    onSelect: (DeckSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoCategory(
        items = DeckSort.entries.map { stringResource(it.labelRes) }.toImmutableList(),
        selectedIndex = sort.ordinal,
        onItemClick = { onSelect(DeckSort.entries[it]) },
        modifier = modifier,
        size = CategorySize.XLarge,
        variant = CategoryVariant.Normal,
    )
}

private val DeckSort.labelRes: Int
    get() =
        when (this) {
            DeckSort.GGUK_PICK -> R.string.home_sort_gguk_pick
            DeckSort.LATEST -> R.string.home_sort_latest
            DeckSort.NEAREST -> R.string.home_sort_nearest
        }

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun SortChipRowPreview() {
    MinoAndroidAppTheme {
        SortChipRow(sort = DeckSort.GGUK_PICK, onSelect = {})
    }
}
