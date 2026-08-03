package team.mino.core.designsystem.component.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CircleCheckFill
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * Figma `Snackbar` 문서 페이지(16215:19528)의 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * 컴포넌트셋은 `Variant=Normal` 하나뿐이라 축은 부가 속성 넷이 전부다.
 * `heading`·`description`은 한 프레임 안의 **서로 독립인 두 텍스트 노드**라 각각 껐다 켠다.
 */
@UiModePreviews
@Composable
private fun SnackbarPreview() {
    PreviewPage {
        PreviewProperty(name = "heading", values = "True · False") {
            MinoSnackbar(message = "메시지에 마침표를 찍어요.", actionLabel = "텍스트")
            MinoSnackbar(
                message = null,
                description = "메시지가 두 줄 이상 길어지는 경우 예외적으로 사용해요.",
                actionLabel = "텍스트",
            )
        }
        PreviewProperty(name = "description", values = "False · True") {
            MinoSnackbar(message = "메시지에 마침표를 찍어요.", actionLabel = "텍스트")
            MinoSnackbar(
                message = "메시지에 마침표를 찍어요.",
                description = "설명은 필요할 때만 써요.",
                actionLabel = "텍스트",
            )
        }
        PreviewProperty(name = "icon", values = "False · True") {
            MinoSnackbar(message = "메시지에 마침표를 찍어요.", actionLabel = "텍스트")
            MinoSnackbar(
                message = "메시지에 마침표를 찍어요.",
                leadingIcon = rememberVectorPainter(MinoIcons.CircleCheckFill),
                actionLabel = "텍스트",
            )
            MinoSnackbar(
                message = "메시지에 마침표를 찍어요.",
                description = "설명은 필요할 때만 써요.",
                leadingIcon = rememberVectorPainter(MinoIcons.CircleCheckFill),
                actionLabel = "텍스트",
            )
        }
        PreviewProperty(name = "close button", values = "False · True") {
            MinoSnackbar(message = "메시지에 마침표를 찍어요.", actionLabel = "텍스트")
            MinoSnackbar(message = "메시지에 마침표를 찍어요.", actionLabel = "텍스트", onCloseClick = {})
        }
    }
}
