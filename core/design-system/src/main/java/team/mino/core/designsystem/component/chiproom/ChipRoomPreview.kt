package team.mino.core.designsystem.component.chiproom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.component.chiproom.token.ChipRoomTokens
import team.mino.core.designsystem.component.menu.MinoMenu
import team.mino.core.designsystem.component.menu.MinoMenuItem
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CaretDown
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

private val SortOptions = persistentListOf("거리순", "오래된순", "최신순", "코멘트순")
private val Categories = persistentListOf("전체", "카페", "음식점")
private val PreviewSelectedSortIndex = 2

@Composable
private fun ChipRoomSample(
    initiallyExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    MinoAndroidAppTheme {
        var selectedSortIndex by remember { mutableIntStateOf(PreviewSelectedSortIndex) }
        var expanded by remember { mutableStateOf(initiallyExpanded) }
        var selectedCategoryIndex by remember { mutableIntStateOf(0) }

        MinoChipRoom(
            modifier = modifier
                .fillMaxWidth()
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value),
            sortOptions = SortOptions,
            selectedSortIndex = selectedSortIndex,
            expanded = expanded,
            onSortButtonClick = { expanded = !expanded },
            onSortOptionClick = {
                selectedSortIndex = it
                expanded = false
            },
            onDismissSortMenu = { expanded = false },
            categories = Categories,
            selectedCategoryIndex = selectedCategoryIndex,
            onCategoryClick = { selectedCategoryIndex = it },
        )
    }
}

@UiModePreviews
@Composable
private fun MinoChipRoomEditOffPreview() {
    ChipRoomSample(initiallyExpanded = false)
}

@UiModePreviews
@Composable
private fun MinoChipRoomEditOnPreview() {
    ChipRoomSample(initiallyExpanded = true)
}

/**
 * 정렬 버튼 아래 펼쳐진 드롭다운 모습(Figma `edit=on`, 드롭다운 콘텐츠는 `Container` 15852:88545)을
 * 정적 프리뷰로 본다.
 *
 * [MinoChipRoomEditOnPreview]는 실제 컴포넌트([MinoChipRoom])를 그대로 쓰지만, 드롭다운은
 * `Popup`으로 띄우는 탓에 Android Studio의 정적(비인터랙티브) Compose 프리뷰에는 렌더링되지
 * 않는다 — `Popup`은 실제 실행 중인 화면이나 인터랙티브 프리뷰에서만 보인다. 그래서 이 프리뷰는
 * 버튼과 드롭다운을 `Popup` 없이 `Column`으로 직접 쌓아 같은 배치를 흉내만 낸다(실제 컴포넌트
 * 구현이 아니라 QA용 목업).
 */
@UiModePreviews
@Composable
private fun MinoChipRoomSortDropdownAnchoredPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(ChipRoomTokens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            MinoButton(
                text = SortOptions[PreviewSelectedSortIndex],
                onClick = {},
                size = ButtonSize.Medium,
                style = ButtonStyle.OutlinedAssistive,
                contentPadding = ChipRoomTokens.SortButtonContentPadding,
                trailingIcon = { Icon(imageVector = MinoIcons.CaretDown, contentDescription = null) },
            )
            MinoMenu(modifier = Modifier.width(ChipRoomTokens.SortDropdownWidth)) {
                SortOptions.forEachIndexed { index, option ->
                    MinoMenuItem(text = option, onClick = {}, active = index == PreviewSelectedSortIndex)
                }
            }
        }
    }
}
