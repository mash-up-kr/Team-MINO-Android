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

/**
 * Figma `Button` 문서 페이지(16215:35809)의 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * Figma는 `variant`(Solid·Outlined)와 `color`(Primary·Assistive)를 별개의 축으로 두지만
 * 코드는 둘을 [ButtonStyle] 한 축으로 합쳤다. 그래서 두 블록 모두 [ButtonStyle] 조합으로 그린다.
 *
 * Figma에 있으나 블록을 만들지 않은 축:
 * - `loading` — 스피너 애셋·애니메이션이 없어 미구현
 * - `interaction`(hover·press·focus) — Android는 리플로 대체된다
 * - `customize` — 커스터마이즈 가이드라 API 축이 아니다
 */
@UiModePreviews
@Composable
private fun ButtonPreview() {
    PreviewPage {
        PreviewProperty(name = "variant", values = "Solid · Outlined") {
            PreviewRow {
                MinoButton(text = "Solid", onClick = {}, style = ButtonStyle.SolidPrimary)
                MinoButton(text = "Outlined", onClick = {}, style = ButtonStyle.OutlinedPrimary)
            }
        }
        PreviewProperty(name = "color", values = "Primary · Assistive") {
            PreviewRow {
                MinoButton(text = "Primary", onClick = {}, style = ButtonStyle.SolidPrimary)
                MinoButton(text = "Assistive", onClick = {}, style = ButtonStyle.SolidAssistive)
            }
            PreviewRow {
                MinoButton(text = "Primary", onClick = {}, style = ButtonStyle.OutlinedPrimary)
                MinoButton(text = "Assistive", onClick = {}, style = ButtonStyle.OutlinedAssistive)
            }
        }
        PreviewProperty(name = "size", values = "Small · Medium · Large") {
            PreviewRow {
                MinoButton(text = "Small", onClick = {}, size = ButtonSize.Small)
                MinoButton(text = "Medium", onClick = {}, size = ButtonSize.Medium)
                MinoButton(text = "Large", onClick = {}, size = ButtonSize.Large)
            }
        }
        // 아이콘은 LocalContentColor를 따라 글자와 같은 색으로 그려진다
        PreviewProperty(name = "icon") {
            PreviewRow {
                MinoButton(text = "텍스트", onClick = {}, size = ButtonSize.Medium)
                MinoButton(
                    text = "텍스트",
                    onClick = {},
                    size = ButtonSize.Medium,
                    leadingIcon = { Icon(imageVector = MinoIcons.Bookmark, contentDescription = null) },
                )
                MinoButton(
                    text = "텍스트",
                    onClick = {},
                    size = ButtonSize.Medium,
                    trailingIcon = { Icon(imageVector = MinoIcons.ArrowRight, contentDescription = null) },
                )
                MinoButton(
                    text = "텍스트",
                    onClick = {},
                    size = ButtonSize.Medium,
                    leadingIcon = { Icon(imageVector = MinoIcons.Bookmark, contentDescription = null) },
                    trailingIcon = { Icon(imageVector = MinoIcons.ArrowRight, contentDescription = null) },
                )
            }
        }
        // Figma는 한 컴포넌트의 속성이지만 코드는 MinoIconButton으로 나뉜다 — 크기·스타일 토큰은 공유한다
        PreviewProperty(name = "iconOnly", values = "False · True") {
            ButtonSize.entries.forEach { size ->
                PreviewRow {
                    MinoButton(text = "텍스트", onClick = {}, size = size)
                    MinoIconButton(
                        onClick = {},
                        size = size,
                        icon = { Icon(imageVector = MinoIcons.Bookmark, contentDescription = null) },
                    )
                }
            }
        }
        // Solid는 배경·글자가, Outlined는 글자만 비활성 토큰으로 바뀐다
        PreviewProperty(name = "disable", values = "False · True") {
            ButtonStyle.entries.forEach { style ->
                PreviewRow {
                    MinoButton(text = style.name, onClick = {}, size = ButtonSize.Medium, style = style)
                    MinoButton(
                        text = style.name,
                        onClick = {},
                        enabled = false,
                        size = ButtonSize.Medium,
                        style = style,
                    )
                }
            }
        }
    }
}
