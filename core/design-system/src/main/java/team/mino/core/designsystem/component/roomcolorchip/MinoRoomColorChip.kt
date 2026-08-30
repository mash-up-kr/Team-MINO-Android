package team.mino.core.designsystem.component.roomcolorchip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import team.mino.core.designsystem.component.roomcolorchip.token.RoomColorChipTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CheckThick
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 방을 대표하는 색 하나를 고르는 칩(Figma `state = off · on` 컴포넌트셋 12개).
 *
 * 선택되면 채움색이 짙은 톤으로 바뀌고 테두리가 사라지며 체크 아이콘이 얹힌다. 도형의 크기와
 * 차지하는 자리는 두 상태가 같다.
 *
 * **칩은 자기 한 칸만 안다.** 그리드 배치와 단일 선택 규칙은 호출부가 갖는다
 * (`docs/adr/2026-08-14-room-color-palette-in-design-system.md` 참조).
 *
 * @param selected 선택 여부. `Modifier.rippleSingleSelectable`로 접근성 시맨틱에 노출된다.
 * @param onSelect 칩을 눌렀을 때. 이미 선택된 칩을 다시 눌렀을 때의 처리도 호출부의 몫이다.
 * @param role 접근성 시맨틱이 이 칩을 무엇으로 읽을지. 여러 칩 중 하나만 고르는 그리드라면 호출부가
 *   `Role.RadioButton`을 넘긴다. 칩 혼자서는 자기가 단일 선택인지 다중 선택인지 알 수 없어 기본값이
 *   `null`이다.
 */
@Composable
fun MinoRoomColorChip(
    color: MinoRoomColor,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    role: Role? = null,
    colors: MinoRoomColorChipColors = MinoRoomColorChipDefaults.colors(color),
) {
    val containerColor = MinoRoomColorChipDefaults.containerColor(colors, selected)
    val borderColor = MinoRoomColorChipDefaults.borderColor(colors, selected)
    val shape = RoomColorChipTokens.Shape

    Box(
        modifier = modifier
            .size(RoomColorChipTokens.Size)
            .dropShadow(shape = shape, shadow = RoomColorChipTokens.ContainerShadow)
            .surface(
                shape = shape,
                containerColor = containerColor,
                borderColor = borderColor,
                borderWidth = RoomColorChipTokens.BorderWidth,
            ).rippleSingleSelectable(selected = selected, role = role, onClick = onSelect),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                modifier = Modifier.size(RoomColorChipTokens.CheckIconSize),
                imageVector = MinoIcons.CheckThick,
                contentDescription = null,
                tint = colors.selectedContentColor,
            )
        }
    }
}
