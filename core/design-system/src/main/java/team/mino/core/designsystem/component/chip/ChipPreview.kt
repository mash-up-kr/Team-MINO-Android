package team.mino.core.designsystem.component.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * Figma `Chip` 문서 페이지(16215:41950)의 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * Figma에 있으나 블록을 만들지 않은 축:
 * - `interaction`(hover·press·focus) — Android는 리플로 대체된다
 * - `customize` — 커스터마이즈 가이드라 API 축이 아니다
 */
@UiModePreviews
@Composable
private fun ChipPreview() {
    PreviewPage {
        PreviewProperty(name = "size", values = ChipSize.entries.previewValues()) {
            PreviewRow {
                ChipSize.entries.forEach { size ->
                    MinoChip(text = size.name, onClick = {}, size = size)
                }
            }
        }
        PreviewProperty(name = "variant", values = ChipVariant.entries.previewValues()) {
            PreviewRow {
                ChipVariant.entries.forEach { variant ->
                    MinoChip(text = variant.name, onClick = {}, variant = variant)
                }
            }
        }
        PreviewProperty(name = "active", values = "False · True") {
            ChipVariant.entries.forEach { variant ->
                PreviewRow {
                    MinoChip(text = "선택 안 됨", onClick = {}, variant = variant, active = false)
                    MinoChip(text = "선택됨", onClick = {}, variant = variant, active = true)
                }
            }
        }
        // Figma는 아이콘·썸네일을 모두 허용해 코드도 아이콘 전용이 아닌 정사각 슬롯으로 열었다.
        // 슬롯 크기는 칩 크기가 강제하고, 안쪽 Icon은 LocalContentColor로 글자색을 물려받는다.
        PreviewProperty(name = "content", values = "Icon · Thumbnail") {
            PreviewRow {
                MinoChip(text = "텍스트", onClick = {})
                MinoChip(text = "텍스트", onClick = {}, leadingContent = { ChipPreviewIcon() })
                MinoChip(text = "텍스트", onClick = {}, trailingContent = { ChipPreviewIcon() })
                MinoChip(
                    text = "텍스트",
                    onClick = {},
                    leadingContent = { ChipPreviewIcon() },
                    trailingContent = { ChipPreviewIcon() },
                )
            }
            PreviewRow {
                MinoChip(text = "텍스트", onClick = {})
                MinoChip(text = "텍스트", onClick = {}, leadingContent = { ChipPreviewThumbnail() })
                MinoChip(text = "텍스트", onClick = {}, trailingContent = { ChipPreviewThumbnail() })
                MinoChip(
                    text = "텍스트",
                    onClick = {},
                    leadingContent = { ChipPreviewThumbnail() },
                    trailingContent = { ChipPreviewThumbnail() },
                )
            }
            // 슬롯이 칩 크기를 따라간다 (XSmall 12 · Small 14 · Medium 14 · Large 16dp)
            PreviewRow {
                ChipSize.entries.forEach { size ->
                    MinoChip(
                        text = size.name,
                        onClick = {},
                        size = size,
                        active = true,
                        leadingContent = { ChipPreviewIcon() },
                    )
                }
            }
        }
        PreviewProperty(name = "disable", values = "False · True") {
            ChipVariant.entries.forEach { variant ->
                PreviewRow {
                    MinoChip(text = variant.name, onClick = {}, variant = variant)
                    MinoChip(text = variant.name, onClick = {}, variant = variant, enabled = false)
                }
            }
        }
    }
}

@Composable
private fun ChipPreviewIcon(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier.fillMaxSize(),
        imageVector = MinoIcons.Close,
        contentDescription = null,
    )
}

/** 썸네일 자체는 호출부가 그린다. Figma도 1dp 라운딩만 정해 두고 내용은 열어 둔다. */
@Composable
private fun ChipPreviewThumbnail(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(1.dp))
            .background(ColorAccessKeyToken.PrimaryNormal.value),
    )
}
