package team.mino.feature.room.placedetail.component

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import team.mino.core.designsystem.component.menu.AnchoredDropdownPositionProvider
import team.mino.core.designsystem.component.menu.MinoMenu
import team.mino.core.designsystem.component.menu.MinoMenuDefaults
import team.mino.core.designsystem.component.menu.MinoMenuItem
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.room.R

/**
 * 내 코멘트의 [⋮]가 여는 메뉴. 항목은 `댓글 삭제` 하나뿐이다(spec FR-015).
 *
 * **고르면 곧바로 지운다.** 확인 모달을 거치지 않고 되돌리기도 제공하지 않는 것이 사양이므로
 * (spec TS-025·EC-013) 이 메뉴와 삭제 사이에 다른 화면을 끼우지 않는다.
 *
 * 호출부가 [⋮]를 감싼 자리에서 부르면 그 자리가 곧 앵커다. 앵커 대비 위치는
 * [AnchoredDropdownPositionProvider]가 정한다 — 방 상세의 장소 [⋮]·[더보기] 메뉴와 같은 규칙을 써야
 * 같은 손짓에서 메뉴가 같은 자리에 뜬다. 열림 여부는 호출부가 들고, 이 컴포저블은 열려 있을 때만 불린다.
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
    val density = LocalDensity.current
    val positionProvider = remember(density) { AnchoredDropdownPositionProvider(density, alignEnd = true) }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        CommentMenuContent(onDeleteClick = onDeleteClick, modifier = modifier)
    }
}

/**
 * 메뉴 본체. [Popup] 밖에서도 그릴 수 있게 떼어 두어 프리뷰가 같은 것을 본다.
 *
 * **너비를 못 박는다.** [MinoMenu]는 하한만 두고 폭을 콘텐츠에 맡기는데, [Popup] 안에서는 그 콘텐츠가
 * 창 폭을 상한으로 받아 항목이 화면 끝까지 퍼진다 — `PlaceActionMenu`·`RoomDetailSortMenu`가 같은 원인으로
 * 같은 조치를 하고 있어, 이 값을 [MinoMenu]가 직접 들도록 올리는 것이 남은 숙제다.
 */
@Composable
private fun CommentMenuContent(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoMenu(modifier = modifier.width(MenuWidth)) {
        MinoMenuItem(
            text = stringResource(R.string.placedetail_comment_delete),
            onClick = onDeleteClick,
            contentPadding = MinoMenuDefaults.ItemContentPaddingCompact,
        )
    }
}

private val MenuWidth = 140.dp

@UiModePreviews
@Composable
private fun PlaceCommentMenuPreview() {
    MinoAndroidAppTheme {
        // Popup은 정적 프리뷰에 그려지지 않아 메뉴 본체만 띄운다.
        CommentMenuContent(onDeleteClick = {})
    }
}
