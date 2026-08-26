package team.mino.feature.roomform.form.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColorChip
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.RoomColor
import team.mino.feature.roomform.form.model.chip

/**
 * 방 대표 색을 고르는 칩 그리드.
 *
 * 칩은 자기 한 칸만 안다 — 배치와 단일 선택 규칙은 이 컴포넌트가 갖는다.
 *
 * **이미 고른 칩을 다시 눌러도 선택이 풀리지 않는다.** 칩은 언제나 자기 색을 올려보내고 해제를
 * 뜻하는 값은 올라가지 않는다.
 *
 * 라벨은 이 그리드 바깥이다. 다른 입력 필드의 제목과 나란히 놓이므로 화면이 갖는다.
 *
 * @param selectedColor 지금 고른 색. `null`이면 아직 고르지 않은 것이라 모든 칩이 미선택이다.
 * @param onColorSelect 누른 칩의 색. 이미 고른 색이 다시 올라올 수 있다.
 */
@Composable
internal fun RoomColorPalette(
    selectedColor: RoomColor?,
    onColorSelect: (RoomColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(RowSpacing),
    ) {
        ChipRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { (color, chip) ->
                    MinoRoomColorChip(
                        color = chip,
                        selected = color == selectedColor,
                        onSelect = { onColorSelect(color) },
                        role = Role.RadioButton,
                    )
                }
            }
        }
    }
}

private const val COLUMN_COUNT = 4

private val RowSpacing = 10.dp

/**
 * 그리드가 순회할 칸. 색의 목록도 순서도 [RoomColor.selectable]이 갖는다 — 여기에 다시 나열하지 않는다.
 *
 * 칸마다 도메인 색과 칩을 함께 들고 있어, 누른 칩을 도메인 색으로 되돌리는 역방향 표가 필요 없다.
 * 칩이 없는 색은 애초에 [RoomColor.selectable]에 없어 걸러지는 것이 없다.
 */
private val ChipRows: List<List<Pair<RoomColor, MinoRoomColor>>> =
    RoomColor.selectable.mapNotNull { color -> color.chip?.let { color to it } }.chunked(COLUMN_COUNT)

@UiModePreviews
@Composable
private fun RoomColorPalettePreview() {
    MinoAndroidAppTheme {
        var selected by remember { mutableStateOf<RoomColor?>(RoomColor.CYAN) }
        RoomColorPalette(
            selectedColor = selected,
            onColorSelect = { selected = it },
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalNormal)
                .padding(20.dp),
        )
    }
}
