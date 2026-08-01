package team.mino.feature.sample.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Check
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.feature.sample.main.component.token.RoomCardTokens

/**
 * 선택 모드의 방 카드(Figma `Card_Room`(15892-81881), `Show list cell=on`).
 *
 * 아바타 자리에 체크박스가 오른쪽 끝으로 붙는다. 카드 본문 탭([onClick])과 체크박스 탭
 * ([onCheckedChange])은 서로 다른 동작이라 클릭 영역을 분리한다.
 *
 * @param placeCountLabel 저장된 장소 개수 텍스트(예: "장소 12개"). 포맷은 호출부가 결정한다.
 * @param memo 방 설명. null이면 Figma `Show memo=off`.
 * @param coverImageUrl 커버 이미지 URL. null이거나 로딩에 실패하면 placeholder 글리프를 표시한다.
 */
@Composable
fun MinoRoomCheckBoxCard(
    title: String,
    placeCountLabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverImageUrl: String? = null,
    memo: String? = null,
    colors: MinoRoomCheckBoxCardColors = MinoRoomCardDefaults.checkBoxColors(),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(onClick = onClick)
            .padding(vertical = RoomCardTokens.VerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(RoomCardTokens.TrailingSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomCardContent(
            title = title,
            placeCountLabel = placeCountLabel,
            coverImageUrl = coverImageUrl,
            memo = memo,
            modifier = Modifier.weight(1f),
        )

        RoomCheckBox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = colors,
        )
    }
}

/**
 * 카드 전용 체크박스. 디자인 시스템에 Checkbox 컴포넌트가 아직 없어 여기서 최소 형태로 그린다.
 */
@Composable
private fun RoomCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: MinoRoomCheckBoxCardColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(RoomCardTokens.CheckBoxSize)
            .surface(
                shape = MinoRoomCardDefaults.checkBoxShape,
                containerColor = colors.containerColor(checked),
                borderColor = colors.borderColor(checked),
                borderWidth = RoomCardTokens.CheckBoxBorderWidth,
            ).rippleSingleSelectable(
                selected = checked,
                role = Role.Checkbox,
                onClick = { onCheckedChange(!checked) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = MinoIcons.Check,
            contentDescription = null,
            tint = colors.checkmarkColor(checked),
            modifier = Modifier.size(RoomCardTokens.CheckBoxIconSize),
        )
    }
}
