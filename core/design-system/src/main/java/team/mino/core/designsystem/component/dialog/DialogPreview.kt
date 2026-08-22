package team.mino.core.designsystem.component.dialog

import androidx.compose.runtime.Composable
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * [MinoDialog]의 카탈로그 프리뷰.
 *
 * 대응하는 Figma 문서 페이지를 찾지 못해([team.mino.core.designsystem.component.dialog.token.DialogTokens]
 * 참조) 축을 나누지 않고 단일 예시만 둔다.
 */
@UiModePreviews
@Composable
private fun DialogPreview() {
    PreviewPage {
        PreviewProperty(name = "default") {
            MinoDialog(
                title = "위치 권한이 필요해요",
                message = "설정 화면에서 위치 권한을 허용해 주세요.",
                confirmLabel = "설정으로 이동",
                onConfirmClick = {},
                cancelLabel = "취소",
                onCancelClick = {},
                onDismissRequest = {},
            )
        }
    }
}
