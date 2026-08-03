package team.mino.core.designsystem.component.button

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.ArrowRight
import team.mino.core.designsystem.foundation.icons.icons.Bookmark
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * Figma `Button/Text` 컴포넌트셋(16215:38291)의 축을 옮긴 프리뷰.
 *
 * `Button/Button`과 **별개의 컴포넌트셋**이라 프리뷰도 따로 둔다. 문서 페이지가 없어 축 순서는
 * 컴포넌트셋의 변형 이름(`Variant`·`Size`·`Disable`)을 따르고, 부가 속성 `leadingIcon`·
 * `trailingIcon`을 `icon` 블록으로 덧붙였다.
 *
 * `loading`은 [MinoButton]과 같은 이유(스피너 애셋·애니메이션 필요)로 블록을 만들지 않았다.
 */
@UiModePreviews
@Composable
private fun TextButtonPreview() {
    PreviewPage {
        PreviewProperty(name = "variant", values = TextButtonStyle.entries.previewValues()) {
            PreviewRow {
                TextButtonStyle.entries.forEach { style ->
                    MinoTextButton(text = style.name, onClick = {}, style = style)
                }
            }
        }
        PreviewProperty(name = "size", values = TextButtonSize.entries.previewValues()) {
            PreviewRow {
                TextButtonSize.entries.forEach { size ->
                    MinoTextButton(text = size.name, onClick = {}, size = size)
                }
            }
        }
        PreviewProperty(name = "icon") {
            PreviewRow {
                MinoTextButton(text = "텍스트", onClick = {})
                MinoTextButton(
                    text = "텍스트",
                    onClick = {},
                    leadingIcon = { Icon(imageVector = MinoIcons.Bookmark, contentDescription = null) },
                )
                MinoTextButton(
                    text = "텍스트",
                    onClick = {},
                    trailingIcon = { Icon(imageVector = MinoIcons.ArrowRight, contentDescription = null) },
                )
                MinoTextButton(
                    text = "텍스트",
                    onClick = {},
                    leadingIcon = { Icon(imageVector = MinoIcons.Bookmark, contentDescription = null) },
                    trailingIcon = { Icon(imageVector = MinoIcons.ArrowRight, contentDescription = null) },
                )
            }
        }
        // 비활성은 두 스타일이 같은 색(Label/Disable)으로 모인다
        PreviewProperty(name = "disable", values = "False · True") {
            TextButtonStyle.entries.forEach { style ->
                PreviewRow {
                    MinoTextButton(text = style.name, onClick = {}, style = style)
                    MinoTextButton(text = style.name, onClick = {}, enabled = false, style = style)
                }
            }
        }
    }
}
