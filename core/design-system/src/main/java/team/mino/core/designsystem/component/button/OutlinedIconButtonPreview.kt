package team.mino.core.designsystem.component.button

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.ArrowLeft
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * Figma `Button/Icon/Outlined` 프리뷰.
 *
 * 다른 컴포넌트 프리뷰와 달리 속성 블록을 여러 개 쌓지 않는다. 이 컴포넌트셋에는 화면에 쓰인
 * 인스턴스가 드러내는 축이 없어(size·style·상태 모두 없음) 슬롯 축 하나만 남는다.
 */
@UiModePreviews
@Composable
private fun OutlinedIconButtonPreview() {
    PreviewPage {
        PreviewProperty(name = "icon") {
            PreviewRow {
                MinoOutlinedIconButton(onClick = {}) {
                    Icon(imageVector = MinoIcons.Close, contentDescription = null)
                }
                MinoOutlinedIconButton(onClick = {}) {
                    Icon(imageVector = MinoIcons.ArrowLeft, contentDescription = null)
                }
            }
        }
    }
}
