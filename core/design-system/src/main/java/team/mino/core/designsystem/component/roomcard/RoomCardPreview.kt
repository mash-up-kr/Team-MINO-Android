package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.designsystem.component.roomthumbnail.MinoRoomThumbnail
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.UiModePreviews

private const val PREVIEW_TITLE = "내 방"
private const val PREVIEW_MEMO = "내가 꾹 저장한 장소"
private const val PREVIEW_PLACE_COUNT_LABEL = "장소 0개"

/**
 * `Card_Room`의 속성 축 프리뷰.
 *
 * `Show list cell` 축은 Figma에서 한 컴포넌트셋의 variant지만, 코드에서는 클릭 영역과 트레일링
 * 슬롯이 갈려 [MinoRoomCard]·[MinoRoomCheckBoxCard]·[MinoRoomChevronCard] 세 컴포넌트로 나뉜다.
 * 뒤의 둘은 같은 `on` variant이며 트레일링 슬롯의 인스턴스만 다르다.
 *
 * 만들지 않은 축:
 * - `checked` — Figma 문서 페이지에서 `Checkbox` 컴포넌트가 소유하는 축이라 블록을 따로 두지
 *   않고, `Show memo` 블록 안에서 두 상태를 함께 보인다.
 */
@UiModePreviews
@Composable
private fun RoomCardPreview() {
    PreviewPage {
        PreviewProperty(name = "Show list cell", values = "off · on") {
            MinoRoomCard(
                title = PREVIEW_TITLE,
                placeCountLabel = PREVIEW_PLACE_COUNT_LABEL,
                participantAvatars = persistentListOf(
                    MinoProfileAvatar.Person1,
                    MinoProfileAvatar.Person2,
                    MinoProfileAvatar.Person3,
                ),
                onClick = {},
                thumbnail = { PreviewThumbnail() },
                memo = PREVIEW_MEMO,
            )
            PreviewCheckBoxCard(checked = true, memo = PREVIEW_MEMO)
            MinoRoomChevronCard(
                title = PREVIEW_TITLE,
                placeCountLabel = PREVIEW_PLACE_COUNT_LABEL,
                onClick = {},
                thumbnail = { PreviewThumbnail() },
                memo = PREVIEW_MEMO,
            )
        }
        PreviewProperty(name = "Show memo", values = "on · off") {
            PreviewCheckBoxCard(checked = true, memo = PREVIEW_MEMO)
            PreviewCheckBoxCard(checked = false, memo = PREVIEW_MEMO)
            PreviewCheckBoxCard(checked = true, memo = null)
            PreviewCheckBoxCard(checked = false, memo = null)
            MinoRoomChevronCard(
                title = PREVIEW_TITLE,
                placeCountLabel = PREVIEW_PLACE_COUNT_LABEL,
                onClick = {},
                thumbnail = { PreviewThumbnail() },
            )
        }
    }
}

@Composable
private fun PreviewCheckBoxCard(
    checked: Boolean,
    memo: String?,
    modifier: Modifier = Modifier,
) {
    MinoRoomCheckBoxCard(
        title = PREVIEW_TITLE,
        placeCountLabel = PREVIEW_PLACE_COUNT_LABEL,
        checked = checked,
        onCheckedChange = {},
        onClick = {},
        modifier = modifier,
        thumbnail = { PreviewThumbnail() },
        memo = memo,
    )
}

/** 프리뷰는 네트워크를 타지 않으므로 사진 없이 폴백만 보이는 썸네일을 넣는다. */
@Composable
private fun PreviewThumbnail(modifier: Modifier = Modifier) {
    MinoRoomThumbnail(
        imageUrls = persistentListOf(),
        modifier = modifier,
        fallback = { PreviewFallback() },
    )
}

/**
 * 폴백은 `:core:common:ui`가 그리므로 여기서는 슬롯이 채워지는 것만 보이면 된다. 이 색은 디자인
 * 근거가 없는 프리뷰 전용 자리표시다.
 */
@Composable
private fun PreviewFallback(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(ColorAccessKeyToken.FillStrong.value))
}
