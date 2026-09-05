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
 * 화면 더보기[⋮] 메뉴(FR-012·FR-013) — `isOwner`·`isPersonalRoom`에 따라 노출 항목이 갈린다
 * (`docs/specs/room-detail/contracts/room-detail-main-contract.md` "분기 규칙 — 더보기 메뉴 항목").
 *
 * | isOwner | isPersonalRoom | 노출 항목 |
 * |---|---|---|
 * | true | false | 방 편집, 나가기 |
 * | false | false | 나가기 |
 * | true/false | true(개인방) | 항목 없음 |
 *
 * 메뉴는 더보기[⋮] 버튼 오른쪽 끝에 맞춰 뜨되, 위/아래 방향은 시트 단계마다 다르다(실기기 스크린샷 +
 * Figma 대조로 확정):
 * - `Peek`(`2542:125409`, 시트 높이 88dp뿐이라 버튼 아래로 펼 공간이 없음): 버튼 **위쪽으로** 걸치듯
 *   뜬다(시트 상단 경계를 넘어 지도 위로 살짝 겹친다).
 * - `Half`/`Full`(시트가 충분히 커서 버튼 아래에 펼 공간이 있음): 버튼 **아래쪽**에 표준 드롭다운처럼
 *   뜬다.
 *
 * [AnchoredDropdownPositionProvider]가 [expandUpward]에 따라 이 위치를 계산한다 — 기본
 * `Popup(alignment = TopStart)`를 그대로 쓰면 앵커 계산이 이 버튼이 아니라 훨씬 위(지도 쪽)로 어긋나는
 * 결함이 있었다.
 *
 * `RoomMoreMenu`를 호출하는 지점(더보기 버튼을 감싼 `Box`, [RoomDetailHeaderRow] 참고)이 곧 이 [Popup]의
 * 앵커가 된다 — 그래서 반드시 그 버튼 바로 옆에서 호출해야 한다.
 *
 * @param expanded 메뉴가 펼쳐져 있는지. 열림 여부는 호출부가 소유한다.
 * @param isOwner 이 방의 방장인지(EC-006).
 * @param isPersonalRoom 개인방인지(EC-002·EC-005) — 개인방이면 방장 개념이 없어 [isOwner]와
 *   무관하게 메뉴 자체를 그리지 않는다.
 * @param expandUpward `true`면 버튼 위로, `false`면 버튼 아래로 편다(`Peek`만 `true`).
 * @param onDismiss 메뉴 바깥을 눌러 닫으려 할 때 호출된다.
 * @param onEditRoomClick "방 편집" 클릭(FR-012 진입점, 방장 전용).
 * @param onLeaveClick "방 나가기" 클릭(FR-013 진입점, [SYS-007]).
 */
@Composable
internal fun RoomMoreMenu(
    expanded: Boolean,
    isOwner: Boolean,
    isPersonalRoom: Boolean,
    expandUpward: Boolean,
    onDismiss: () -> Unit,
    onEditRoomClick: () -> Unit,
    onLeaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!expanded || isPersonalRoom) return

    val density = LocalDensity.current
    val positionProvider = remember(density, expandUpward) {
        AnchoredDropdownPositionProvider(density, alignEnd = true, expandUpward = expandUpward)
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        // Figma `3025:131624` 실측 — designedWidth 140px 고정. 너비를 안 주면 hug 계산이 라벨 폭보다
        // 넓게 퍼져 Figma보다 가로가 길어 보인다(`RoomDetailSortMenu`와 같은 이유·같은 값).
        MinoMenu(modifier = modifier.width(140.dp)) {
            if (isOwner) {
                MinoMenuItem(text = "방 편집", onClick = onEditRoomClick)
            }
            MinoMenuItem(text = "방 나가기", onClick = onLeaveClick)
        }
    }
}
