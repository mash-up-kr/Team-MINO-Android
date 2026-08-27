package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.component.category.MinoCategory
import team.mino.core.designsystem.component.menu.MinoMenu
import team.mino.core.designsystem.component.menu.MinoMenuItem
import team.mino.core.designsystem.component.roomcard.token.ChipRoomTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CaretDown

/**
 * 리소스 목록 상단에서 정렬·카테고리 필터를 함께 다루는 컴포넌트(Figma `Chip_Room`, 15852:88538).
 *
 * Figma `edit` boolean 변형은 [expanded]로 대응한다 — `false`면 `edit=off`(닫힌 정렬 버튼 +
 * 카테고리 칩), `true`면 `edit=on`(정렬 버튼 아래 [MinoMenu] 드롭다운이 추가로 뜬 상태)이다.
 *
 * 정렬 버튼은 이미 검증된 [MinoButton]([ButtonStyle.OutlinedAssistive], [ButtonSize.Medium])을,
 * 카테고리 칩 리스트는 [MinoCategory]를 그대로 조합한다 — Figma의 `Button/Button`·
 * `Category/Category` 인스턴스와 동일한 컴포넌트라 별도 구현을 두지 않는다.
 *
 * 드롭다운은 정렬 버튼만 감싸는 [Box]를 앵커로 [Popup]을 띄운다. Figma 실측상 메뉴가 버튼 바로
 * 아래(세로 간격 0)·버튼과 같은 시작선(가로 오프셋 0)에 붙어, `Popup`의 기본 `alignment`
 * ([Alignment.BottomStart])와 오프셋 0이 그대로 일치한다.
 *
 * `Popup`은 Android Studio의 정적(비인터랙티브) Compose 프리뷰에 렌더링되지 않는다 — 실제 실행
 * 화면이나 인터랙티브 프리뷰에서만 보인다. `expanded=true`로 정적 프리뷰를 찍어도 드롭다운은 보이지
 * 않으니, 배치를 눈으로 확인하려면 `ChipRoomPreview.kt`의 목업 프리뷰(`Popup` 없이 버튼·메뉴를
 * `Column`으로 직접 쌓은 것)를 참고한다.
 *
 * @param sortOptions 정렬 메뉴에 나열할 옵션 문구. 정렬 버튼의 라벨은 `sortOptions[selectedSortIndex]`다.
 * @param expanded 정렬 드롭다운이 열려 있는지. 열림 여부는 호출부가 소유한다.
 * @param onSortButtonClick 정렬 버튼 클릭 콜백. 보통 [expanded]를 뒤집는 데 쓴다.
 * @param onDismissSortMenu 드롭다운 바깥을 눌러 닫으려 할 때 호출된다.
 */
@Composable
fun MinoChipRoom(
    sortOptions: ImmutableList<String>,
    selectedSortIndex: Int,
    expanded: Boolean,
    onSortButtonClick: () -> Unit,
    onSortOptionClick: (Int) -> Unit,
    onDismissSortMenu: () -> Unit,
    categories: ImmutableList<String>,
    selectedCategoryIndex: Int,
    onCategoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(ChipRoomTokens.ContentPadding),
        horizontalArrangement = Arrangement.spacedBy(ChipRoomTokens.SortCategorySpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            MinoButton(
                text = sortOptions[selectedSortIndex],
                onClick = onSortButtonClick,
                size = ButtonSize.Medium,
                style = ButtonStyle.OutlinedAssistive,
                contentPadding = ChipRoomTokens.SortButtonContentPadding,
                trailingIcon = { Icon(imageVector = MinoIcons.CaretDown, contentDescription = null) },
            )

            if (expanded) {
                SortDropdown(
                    sortOptions = sortOptions,
                    selectedSortIndex = selectedSortIndex,
                    onSortOptionClick = onSortOptionClick,
                    onDismissRequest = onDismissSortMenu,
                )
            }
        }

        MinoCategory(
            modifier = Modifier.weight(1f),
            items = categories,
            selectedIndex = selectedCategoryIndex,
            onItemClick = onCategoryClick,
        )
    }
}

/** 정렬 버튼 바로 아래에 붙는 옵션 목록 팝업(Figma `edit=on`). */
@Composable
private fun SortDropdown(
    sortOptions: ImmutableList<String>,
    selectedSortIndex: Int,
    onSortOptionClick: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Popup(
        alignment = Alignment.BottomStart,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        MinoMenu(modifier = modifier.width(ChipRoomTokens.SortDropdownWidth)) {
            sortOptions.forEachIndexed { index, option ->
                MinoMenuItem(
                    text = option,
                    onClick = { onSortOptionClick(index) },
                    active = index == selectedSortIndex,
                )
            }
        }
    }
}
