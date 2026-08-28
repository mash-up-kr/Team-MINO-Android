package team.mino.feature.home.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import team.mino.core.designsystem.component.menu.MinoMenu
import team.mino.core.designsystem.component.menu.MinoMenuDefaults
import team.mino.core.designsystem.component.menu.MinoMenuItem
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.home.R

/**
 * 카드의 `[...]`가 여는 액션 메뉴. 항목은 `다른 방 저장` 하나뿐이다(spec FR-005).
 *
 * **호출부가 `[...]` 버튼과 같은 레이아웃 노드 안에서 부른다** — 팝업이 그 노드를 앵커로 삼아
 * 바로 아래 오른쪽 맞춤으로 열리므로, 어느 카드에 대한 메뉴인지가 위치로 드러난다(spec UX-002).
 * 카드 덱 위에 겹쳐 그려야 해서 컨테이너 안에 인라인으로 놓지 않고 팝업으로 띄운다.
 *
 * 열림 여부는 이 컴포저블이 들지 않는다. `HomeUiState.actionMenuTarget`이 그 카드의 pinId일 때만
 * 호출부가 부른다.
 *
 * @param onSaveToAnotherRoom `다른 방 저장` 선택. 어느 방에 저장할지는 이어지는 흐름이 정한다.
 * @param onDismissRequest 메뉴 바깥 탭·뒤로가기. 스와이프로 닫는 경로는 `HomeViewModel`이 판정하므로
 *  여기서 다시 만들지 않는다(spec EC-004·005).
 */
@Composable
internal fun CardActionMenu(
    onSaveToAnotherRoom: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val endInsetPx = with(density) { MenuEndInset.roundToPx() }
    val anchorSpacingPx = with(density) { MenuAnchorSpacing.roundToPx() }
    val positionProvider = remember(endInsetPx, anchorSpacingPx) {
        BelowAnchorEndAligned(endInsetPx, anchorSpacingPx)
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        CardActionMenuContent(onSaveToAnotherRoom = onSaveToAnotherRoom, modifier = modifier)
    }
}

/** 팝업 없이도 그릴 수 있는 메뉴 본체. 프리뷰가 이걸 쓴다. */
@Composable
private fun CardActionMenuContent(
    onSaveToAnotherRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoMenu(modifier = modifier) {
        MinoMenuItem(
            text = stringResource(R.string.home_action_menu_save_to_another_room),
            onClick = onSaveToAnotherRoom,
            contentPadding = MinoMenuDefaults.ItemContentPaddingCompact,
        )
    }
}

/** 앵커 바로 아래, 오른쪽 끝을 앵커 오른쪽에서 조금 안쪽으로 물려 연다. */
private class BelowAnchorEndAligned(
    private val endInsetPx: Int,
    private val anchorSpacingPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset =
        IntOffset(
            x = anchorBounds.right - popupContentSize.width - endInsetPx,
            y = anchorBounds.bottom + anchorSpacingPx,
        )
}

private val MenuEndInset = 9.42.dp

private val MenuAnchorSpacing = 2.08.dp

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun CardActionMenuPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalNormal)
                .padding(20.dp),
        ) {
            CardActionMenuContent(onSaveToAnotherRoom = {})
        }
    }
}
