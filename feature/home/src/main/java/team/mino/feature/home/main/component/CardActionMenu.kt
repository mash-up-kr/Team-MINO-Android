package team.mino.feature.home.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import team.mino.core.designsystem.component.menu.MinoMenu
import team.mino.core.designsystem.component.menu.MinoMenuItem
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * 카드 `[...]`(⋮)로 여는 액션 메뉴. `다른 방 저장`·`장소 가리기` 두 항목만 나열한다(C-11).
 *
 * 상태를 갖지 않는다 — 열림 여부는 [expanded]로 받고, **어느 카드에 대한 메뉴인지도 알지 못한다**.
 * 카드와의 대응(UX-002)과 앵커 위치 계산은 이 메뉴를 부르는 덱이 소유하므로, 여기서는 [Popup]의
 * [alignment]·[offset]만 그대로 열어 둔다.
 *
 * [Popup]을 `focusable`로 띄우므로 바깥 탭·뒤로가기가 [onDismiss]로 들어오고(C-16), 메뉴가 열린
 * 동안의 바깥 제스처는 덱까지 내려가지 않는다(C-15).
 *
 * 두 콜백은 신호만 올린다. 실제 저장 흐름(`onSaveToOtherRoom`)과 덱에서의 제거는 호출부의 몫이다.
 */
@Composable
internal fun CardActionMenu(
    expanded: Boolean,
    onSaveToOtherRoom: () -> Unit,
    onHidePlace: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopEnd,
    offset: IntOffset = IntOffset.Zero,
) {
    if (!expanded) return

    Popup(
        alignment = alignment,
        offset = offset,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        CardActionMenuContent(
            onSaveToOtherRoom = onSaveToOtherRoom,
            onHidePlace = onHidePlace,
            modifier = modifier,
        )
    }
}

/** 팝업 껍데기를 뺀 메뉴 본체. [Popup]이 정적 프리뷰에 그려지지 않아 프리뷰도 이쪽을 쓴다. */
@Composable
private fun CardActionMenuContent(
    onSaveToOtherRoom: () -> Unit,
    onHidePlace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoMenu(modifier = modifier) {
        MinoMenuItem(text = "다른 방 저장", onClick = onSaveToOtherRoom)
        MinoMenuItem(text = "장소 가리기", onClick = onHidePlace)
    }
}

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun CardActionMenuPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative)
                .padding(16.dp),
        ) {
            CardActionMenuContent(onSaveToOtherRoom = {}, onHidePlace = {})
        }
    }
}
