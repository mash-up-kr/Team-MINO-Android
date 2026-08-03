package team.mino.core.designsystem.component.tooltip

import androidx.compose.runtime.Composable
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * Figma `Tooltip` 문서 페이지(16764:137643)의 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * Figma는 `align`을 `position`의 하위 속성으로 두고 **세로 배치(top·bottom)와 가로 배치(left·right)에
 * 서로 다른 값 이름**(Left·Center·Right / Top·Center·Bottom)을 쓴다. 코드는 두 축을
 * [TooltipAlign](Start·Center·End) 하나로 합쳤으므로, 블록도 배치 계열별로 둘로 나눠 그린다.
 *
 * 앵커 기준 팝업 배치는 컴포넌트 범위 밖이다 — Figma 컴포넌트셋도 말풍선 모양만 정의한다.
 */
@UiModePreviews
@Composable
private fun TooltipPreview() {
    PreviewPage {
        PreviewProperty(name = "size", values = TooltipSize.entries.previewValues()) {
            PreviewRow {
                MinoTooltip(text = "메시지에 마침표를 찍어요.", size = TooltipSize.Medium)
                MinoTooltip(text = "역할", size = TooltipSize.Small)
            }
        }
        PreviewProperty(name = "position", values = TooltipPosition.entries.previewValues()) {
            TooltipPosition.entries.forEach { position ->
                MinoTooltip(text = "메시지에 마침표를 찍어요.", position = position)
            }
        }
        // 세로 배치 — Figma 값 이름은 Left · Center · Right
        PreviewProperty(
            name = "position = Top · Bottom → align",
            values = TooltipAlign.entries.previewValues(),
        ) {
            TooltipAlign.entries.forEach { align ->
                MinoTooltip(text = "메시지에 마침표를 찍어요.", position = TooltipPosition.Bottom, align = align)
            }
        }
        // 가로 배치 — Figma 값 이름은 Top · Center · Bottom
        PreviewProperty(
            name = "position = Left · Right → align",
            values = TooltipAlign.entries.previewValues(),
        ) {
            PreviewRow {
                TooltipAlign.entries.forEach { align ->
                    MinoTooltip(text = "메시지에", position = TooltipPosition.Left, align = align)
                }
            }
        }
        PreviewProperty(name = "shortcut", values = "False · True") {
            PreviewRow {
                MinoTooltip(text = "메시지에 마침표를 찍어요.")
                MinoTooltip(text = "메시지에 마침표를 찍어요.", shortcut = "⌘C")
            }
            PreviewRow {
                MinoTooltip(text = "역할", size = TooltipSize.Small)
                MinoTooltip(text = "역할", size = TooltipSize.Small, shortcut = "⌘C")
            }
            // 본문이 최대 너비를 넘으면 줄바꿈되고, 단축키는 한 줄로 남는다
            MinoTooltip(
                text = "본문이 최대 너비를 넘으면 줄바꿈되고, 단축키는 한 줄로 남는다.",
                shortcut = "⌘C",
            )
        }
    }
}
