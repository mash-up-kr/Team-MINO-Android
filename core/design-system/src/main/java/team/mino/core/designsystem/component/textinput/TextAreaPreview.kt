package team.mino.core.designsystem.component.textinput

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.button.MinoTextButton
import team.mino.core.designsystem.component.button.TextButtonStyle
import team.mino.core.designsystem.component.contentbadge.ContentBadgeSize
import team.mino.core.designsystem.component.contentbadge.MinoContentBadge
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * Figma `Textinput` 문서 페이지(16215:31109)의 `Textarea` 섹션 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * Figma에 있으나 블록을 만들지 않은 축:
 * - `focus` — 실제 포커스에서만 드러나 정적 프리뷰로 담을 수 없다
 * - `overflow` — 스크롤 자체는 `resize`가 처리한다. 이 속성이 무엇을 더 요구하는지 미확인
 * - `background` — Platform=Android가 확정돼 축이 아니라 고정값이 됐다
 *
 * `status`가 `Normal`·`Negative` 둘뿐인 것은 Textfield와 다른 점이다. 두 컴포넌트는 Figma에서
 * 서로를 인스턴스로 물지 않는 형제 컴포넌트셋이고, `Status`도 각자 선언한 별개 축이다.
 */
@UiModePreviews
@Composable
private fun TextAreaPreview() {
    PreviewPage {
        PreviewProperty(name = "status", values = MinoTextAreaStatus.entries.previewValues()) {
            MinoTextAreaStatus.entries.forEach { status ->
                TextAreaPreviewItem(status = status, helperText = "메시지에 마침표를 찍어요.")
            }
        }
        // Normal은 내용만큼 늘어나고, Limit은 3줄까지 늘어난 뒤 스크롤, Fixed는 항상 3줄이다
        PreviewProperty(name = "resize", values = MinoTextAreaResize.entries.previewValues()) {
            MinoTextAreaResize.entries.forEach { resize ->
                TextAreaPreviewItem(initialText = "여러 줄 입력\n두 번째 줄\n세 번째 줄\n네 번째 줄", resize = resize)
            }
        }
        PreviewProperty(name = "active", values = "False · True") {
            TextAreaPreviewItem(initialText = "")
            TextAreaPreviewItem(initialText = "입력된 내용")
        }
        PreviewProperty(name = "disable", values = "False · True") {
            TextAreaPreviewItem()
            TextAreaPreviewItem(enabled = false)
        }
        PreviewProperty(name = "heading", values = "False · True") {
            TextAreaPreviewItem(label = null)
            TextAreaPreviewItem(label = "주제")
        }
        // 헤딩은 Textfield와 MinoTextInputHeading 한 벌을 공유한다
        PreviewProperty(name = "requiredBadge", values = "False · True") {
            TextAreaPreviewItem(required = false)
            TextAreaPreviewItem(required = true)
        }
        PreviewProperty(name = "description", values = "False · True") {
            TextAreaPreviewItem(helperText = null)
            TextAreaPreviewItem(helperText = "메시지에 마침표를 찍어요.")
        }
        // 하단 영역 자체를 끈다. 높이는 24dp 고정이다.
        PreviewProperty(name = "bottom", values = "False · True") {
            TextAreaPreviewItem(showBottom = false)
            TextAreaPreviewItem(showBottom = true)
        }
        // Figma도 글자수 카운터가 leadingContent의 기본 프리셋이고, 그 뒤로 항목이 더 붙는 구조다
        PreviewProperty(name = "leadingContent", values = "Character Counter · Badge") {
            TextAreaPreviewItem()
            TextAreaPreviewItem(
                bottomLeadingContent = {
                    MinoContentBadge(text = "임시 저장됨", size = ContentBadgeSize.XSmall)
                },
            )
            TextAreaPreviewItem(
                showCounter = false,
                bottomLeadingContent = {
                    MinoContentBadge(text = "임시 저장됨", size = ContentBadgeSize.XSmall)
                },
            )
        }
        // Figma 원본이 `Button/Text` 인스턴스임을 실측으로 확인해 MinoTextButton을 그대로 넣는다
        PreviewProperty(name = "trailingContent", values = "False · True") {
            TextAreaPreviewItem()
            TextAreaPreviewItem(
                bottomTrailingContent = {
                    MinoTextButton(text = "텍스트", onClick = {})
                },
            )
            TextAreaPreviewItem(
                bottomTrailingContent = {
                    MinoTextButton(text = "취소", onClick = {}, style = TextButtonStyle.Assistive)
                    MinoTextButton(text = "텍스트", onClick = {})
                },
            )
        }
    }
}

/** Figma 인스턴스 하나에 대응하는 프리뷰 항목. 입력 상태를 각자 들고 있어 타이핑해 볼 수 있다. */
@Composable
private fun TextAreaPreviewItem(
    modifier: Modifier = Modifier,
    initialText: String = "입력된 내용",
    label: String? = "주제",
    required: Boolean = false,
    helperText: String? = null,
    status: MinoTextAreaStatus = MinoTextAreaStatus.Normal,
    enabled: Boolean = true,
    resize: MinoTextAreaResize = MinoTextAreaResize.Normal,
    showBottom: Boolean = true,
    showCounter: Boolean = true,
    bottomLeadingContent: (@Composable RowScope.() -> Unit)? = null,
    bottomTrailingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    MinoTextArea(
        state = rememberTextFieldState(initialText = initialText),
        modifier = modifier,
        label = label,
        required = required,
        placeholder = "메시지를 입력해 주세요.",
        helperText = helperText,
        status = status,
        enabled = enabled,
        resize = resize,
        showBottom = showBottom,
        showCounter = showCounter,
        bottomLeadingContent = bottomLeadingContent,
        bottomTrailingContent = bottomTrailingContent,
    )
}
