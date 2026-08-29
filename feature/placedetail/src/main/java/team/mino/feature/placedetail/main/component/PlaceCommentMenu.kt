package team.mino.feature.placedetail.main.component

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
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.placedetail.R

/**
 * 내 코멘트의 [⋮]가 여는 메뉴. 항목은 `댓글 삭제` 하나뿐이다(spec FR-015).
 *
 * **고르면 곧바로 지운다.** 확인 모달을 거치지 않고 되돌리기도 제공하지 않는 것이 사양이므로
 * (spec TS-025·EC-013) 이 메뉴와 삭제 사이에 다른 화면을 끼우지 않는다.
 *
 * 호출부가 [⋮]를 감싼 자리에서 부르면 그 자리를 앵커로 삼아 바로 아래·오른쪽 끝에 맞춰 뜬다. 열림 여부는
 * 호출부가 들고, 이 컴포저블은 열려 있을 때만 불린다.
 *
 * @param onDeleteClick `댓글 삭제`를 골랐을 때.
 * @param onDismissRequest 메뉴 바깥을 눌러 닫으려 할 때.
 */
@Composable
internal fun PlaceCommentMenu(
    onDeleteClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val anchorSpacingPx = with(LocalDensity.current) { AnchorSpacing.roundToPx() }
    val positionProvider = remember(anchorSpacingPx) { CommentMenuPositionProvider(anchorSpacingPx) }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        MinoMenu(modifier = modifier) {
            MinoMenuItem(
                text = stringResource(R.string.placedetail_comment_delete),
                onClick = onDeleteClick,
                contentPadding = MinoMenuDefaults.ItemContentPaddingCompact,
            )
        }
    }
}

/**
 * 앵커 바로 아래에, 앵커의 오른쪽 끝에 오른쪽을 맞춰 놓는다.
 *
 * `Popup(alignment = …)`으로는 이 배치가 나오지 않는다 — 그 오버로드는 앵커와 팝업의 같은 모서리를 겹쳐
 * 놓아 메뉴가 [⋮] 위로 올라온다.
 */
private class CommentMenuPositionProvider(private val verticalSpacingPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset =
        IntOffset(
            x = anchorBounds.right - popupContentSize.width,
            y = anchorBounds.bottom + verticalSpacingPx,
        )
}

private val AnchorSpacing = 9.dp

@UiModePreviews
@Composable
private fun PlaceCommentMenuPreview() {
    MinoAndroidAppTheme {
        // Popup은 정적 프리뷰에 그려지지 않아 메뉴 본체만 띄운다.
        MinoMenu {
            MinoMenuItem(
                text = stringResource(R.string.placedetail_comment_delete),
                onClick = {},
                contentPadding = MinoMenuDefaults.ItemContentPaddingCompact,
            )
        }
    }
}
