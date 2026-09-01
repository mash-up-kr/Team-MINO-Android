package team.mino.feature.room.detail.component

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import team.mino.core.designsystem.component.menu.AnchoredDropdownPositionProvider
import team.mino.core.designsystem.component.menu.MinoMenu
import team.mino.core.designsystem.component.menu.MinoMenuItem

/**
 * 장소 카드 더보기[⋮] 메뉴(FR-008, FR-014) — "다른 방에 공유"·"장소 삭제" 2항목만 고정한다.
 *
 * Figma 더보기 메뉴 목업(node 3222-87768)에는 "장소 이동"이 함께 보이지만, spec.md Q&A(EC-007)가
 * PRD 기준 2항목만 확정했고 그 세 번째 항목은 재사용 Menu 컴포넌트의 커스터마이징 안 된 기본 라벨로
 * 확인됐다 — 그래서 이 컴포넌트는 애초에 그 항목을 받는 파라미터 자체를 두지 않는다.
 *
 * `:core:design-system`의 [MinoMenu]·[MinoMenuItem]을 그대로 조립한다 — 이 메뉴 자체가 커스텀 시각
 * 요소 없이 기본 컴포넌트 두 칸만 나열하는 조합이라 별도로 대조할 치수·색이 없다(`RoomListScreen`의
 * `RoomListSortMenu`·`MinoChipRoom`의 `SortDropdown`과 같은 판단).
 *
 * 트리거(카드의 더보기 아이콘 버튼)는 이 컴포저블이 그리지 않는다 — 호출부가 트리거를 그린 자리 바로
 * 옆(`PlaceCardList`·`PlaceCardGrid`의 `Box { Icon(...); actionMenu() }`)에 이 컴포저블을 함께 두면, 그
 * 작은 `Box`가 곧 이 [Popup]의 앵커가 된다. [AnchoredDropdownPositionProvider]가 그 앵커의 오른쪽 끝에
 * 메뉴 오른쪽 끝을 맞추고 8dp만큼 띄워 버튼 **아래로** 편다 — 기본 `Popup(alignment = TopStart)`을
 * 그대로 쓰면 메뉴가 화면 오른쪽 끝에 딱 붙어 뜨고 버튼과 거의 겹치는 결함이 있었다(실기기 스크린샷으로
 * 확인, `RoomMoreMenu`와 같은 원인·같은 조치).
 *
 * @param expanded 메뉴가 펼쳐져 있는지. 열림 여부는 호출부가 소유한다.
 * @param onDismiss 메뉴 바깥을 눌러 닫으려 할 때 호출된다.
 * @param onShareClick "다른 방에 공유" 클릭(FR-009 진입점).
 * @param onDeleteClick "장소 삭제" 클릭(FR-010 진입점).
 */
@Composable
internal fun PlaceActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!expanded) return

    val density = LocalDensity.current
    val positionProvider = remember(density) { AnchoredDropdownPositionProvider(density, alignEnd = true) }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        // Figma `Menu/Menu`(e.g. `3261:204039`) 실측 — designedWidth 140px 고정. 너비를 안 주면 hug
        // 계산이 라벨 폭보다 넓게 퍼져 Figma보다 가로가 길어 보인다(`RoomDetailSortMenu`와 같은 이유·같은 값).
        MinoMenu(modifier = modifier.width(140.dp)) {
            MinoMenuItem(text = "다른 방에 공유", onClick = onShareClick)
            MinoMenuItem(text = "장소 삭제", onClick = onDeleteClick)
        }
    }
}
