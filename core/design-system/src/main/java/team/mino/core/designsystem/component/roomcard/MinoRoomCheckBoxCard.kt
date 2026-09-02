package team.mino.core.designsystem.component.roomcard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.checkbox.MinoCheckbox
import team.mino.core.designsystem.component.checkbox.MinoCheckboxColors
import team.mino.core.designsystem.component.checkbox.MinoCheckboxDefaults

/**
 * 선택 모드의 방 카드(Figma `Card_Room`, `Show list cell=on`).
 *
 * 아바타 자리에 체크박스가 오른쪽 끝으로 붙는다. 카드 본문 탭([onClick])과 체크박스 탭
 * ([onCheckedChange])의 클릭 영역이 갈려 있어, 둘을 같은 동작으로 볼지는 호출부가 정한다.
 *
 * @param placeCountLabel 저장된 장소 개수 텍스트(예: "장소 12개"). 포맷은 호출부가 결정한다.
 * @param thumbnail 카드 왼쪽 썸네일 슬롯. 사진 콜라주와 폴백 중 무엇을 그릴지는 호출부가 정한다.
 * @param memo 방 설명. null이면 Figma `Show memo=off`.
 */
@Composable
fun MinoRoomCheckBoxCard(
    title: String,
    placeCountLabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    thumbnail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    memo: String? = null,
    colors: MinoCheckboxColors = MinoCheckboxDefaults.colors(),
) {
    RoomCardRow(
        title = title,
        placeCountLabel = placeCountLabel,
        memo = memo,
        onClick = onClick,
        thumbnail = thumbnail,
        modifier = modifier,
    ) {
        MinoCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = colors,
        )
    }
}
