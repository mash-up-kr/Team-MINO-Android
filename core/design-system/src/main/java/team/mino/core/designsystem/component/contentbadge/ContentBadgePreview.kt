package team.mino.core.designsystem.component.contentbadge

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.ArrowRight
import team.mino.core.designsystem.foundation.icons.icons.Check
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * Figma `Content Badge` 문서 페이지(16215:25294)의 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * `customize` 2건은 커스터마이즈 가이드라 API 축이 아니어서 블록을 만들지 않았다.
 */
@UiModePreviews
@Composable
private fun ContentBadgePreview() {
    PreviewPage {
        PreviewProperty(name = "variant", values = ContentBadgeVariant.entries.previewValues()) {
            PreviewRow {
                ContentBadgeVariant.entries.forEach { variant ->
                    MinoContentBadge(text = variant.name, variant = variant)
                }
            }
        }
        PreviewProperty(name = "icon") {
            PreviewRow {
                MinoContentBadge(text = "텍스트")
                MinoContentBadge(
                    text = "텍스트",
                    leadingIcon = { Icon(imageVector = MinoIcons.Check, contentDescription = null) },
                )
                MinoContentBadge(
                    text = "텍스트",
                    trailingIcon = { Icon(imageVector = MinoIcons.ArrowRight, contentDescription = null) },
                )
                MinoContentBadge(
                    text = "텍스트",
                    leadingIcon = { Icon(imageVector = MinoIcons.Check, contentDescription = null) },
                    trailingIcon = { Icon(imageVector = MinoIcons.ArrowRight, contentDescription = null) },
                )
            }
        }
        PreviewProperty(name = "size", values = ContentBadgeSize.entries.previewValues()) {
            PreviewRow {
                ContentBadgeSize.entries.forEach { size ->
                    MinoContentBadge(text = size.name, size = size)
                }
            }
        }
        PreviewProperty(name = "color", values = ContentBadgeColor.entries.previewValues()) {
            ContentBadgeVariant.entries.forEach { variant ->
                PreviewRow {
                    ContentBadgeColor.entries.forEach { color ->
                        MinoContentBadge(text = color.name, variant = variant, color = color)
                    }
                }
            }
        }
    }
}
