package team.mino.core.designsystem.component.textinput

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import team.mino.core.designsystem.component.contentbadge.ContentBadgeSize
import team.mino.core.designsystem.component.contentbadge.MinoContentBadge
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Search
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * Figma `Textinput` 문서 페이지(16215:31109)의 `Textfield` 섹션 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * Figma에 있으나 블록을 만들지 않은 축:
 * - `focus` — 실제 포커스에서만 드러나 정적 프리뷰로 담을 수 없다. 포커스는 테두리를
 *   `Primary/Normal` 43% · 2dp로 바꾼다
 * - `autoComplete` — 입력 아래 Menu 팝업을 띄우는 웹 전용 속성
 * - `interaction`(hover·focus) — Android는 리플로 대체된다
 * - `background` — Platform=Android가 확정돼 축이 아니라 고정값(반투명 배경)이 됐다
 */
@UiModePreviews
@Composable
private fun TextFieldPreview() {
    PreviewPage {
        PreviewProperty(name = "status", values = MinoTextFieldStatus.entries.previewValues()) {
            MinoTextFieldStatus.entries.forEach { status ->
                TextFieldPreviewItem(status = status, helperText = "메시지에 마침표를 찍어요.")
            }
        }
        // Figma의 active는 값이 들어찬 상태를 가리킨다. 값이 있고 포커스면 지우기 버튼이 함께 뜬다.
        PreviewProperty(name = "active", values = "False · True") {
            TextFieldPreviewItem(initialValue = "")
            TextFieldPreviewItem(initialValue = "값")
        }
        PreviewProperty(name = "disable", values = "False · True") {
            TextFieldPreviewItem()
            TextFieldPreviewItem(enabled = false)
        }
        PreviewProperty(name = "heading", values = "False · True") {
            TextFieldPreviewItem(label = null)
            TextFieldPreviewItem(label = "주제")
        }
        // 라벨 뒤 4dp 간격의 별표 하나(Label1 Medium 14sp, Status/Negative)
        PreviewProperty(name = "requiredBadge", values = "False · True") {
            TextFieldPreviewItem(required = false)
            TextFieldPreviewItem(required = true)
        }
        PreviewProperty(name = "description", values = "False · True") {
            TextFieldPreviewItem(helperText = null)
            TextFieldPreviewItem(helperText = "메시지에 마침표를 찍어요.")
        }
        // 22dp 정사각 슬롯. LocalContentColor로 입력 글자색을 물려받는다.
        PreviewProperty(name = "icon", values = "False · True") {
            TextFieldPreviewItem()
            TextFieldPreviewItem(
                leadingContent = {
                    Icon(painter = rememberVectorPainter(MinoIcons.Search), contentDescription = null)
                },
            )
        }
        // 입력 상자와 맞닿는 박스형 버튼. 배경·테두리·그림자는 바깥 컨테이너가 한 번에 그리고,
        // 버튼은 경계선만 직접 긋는다.
        PreviewProperty(name = "trailingButton", values = "False · True") {
            TextFieldPreviewItem()
            TextFieldPreviewItem(trailingButtonLabel = "텍스트")
        }
        PreviewProperty(
            name = "trailingButton.variant",
            values = MinoTextFieldButtonVariant.entries.previewValues(),
        ) {
            MinoTextFieldButtonVariant.entries.forEach { variant ->
                TextFieldPreviewItem(trailingButtonLabel = variant.name, trailingButtonVariant = variant)
            }
        }
        PreviewProperty(name = "trailingButton.disable", values = "False · True") {
            TextFieldPreviewItem(trailingButtonLabel = "텍스트")
            TextFieldPreviewItem(trailingButtonLabel = "텍스트", enabled = false)
        }
        // Figma는 이 자리를 trailingContent·extra 두 칸으로 두는데, 첫 칸은 상태 아이콘·지우기 버튼이
        // 이미 점유하므로 호출부 슬롯은 두 번째 칸이다. 프리셋은 호출부가 조립한다.
        PreviewProperty(name = "trailingContent", values = "False · True") {
            TextFieldPreviewItem()
            TextFieldPreviewItem(
                trailingContent = {
                    MinoContentBadge(text = "60", size = ContentBadgeSize.XSmall)
                },
            )
        }
    }
}

/** Figma 인스턴스 하나에 대응하는 프리뷰 항목. 입력값을 각자 들고 있어 타이핑해 볼 수 있다. */
@Composable
private fun TextFieldPreviewItem(
    modifier: Modifier = Modifier,
    initialValue: String = "값",
    label: String? = "주제",
    required: Boolean = false,
    helperText: String? = null,
    status: MinoTextFieldStatus = MinoTextFieldStatus.Normal,
    enabled: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    trailingButtonLabel: String? = null,
    trailingButtonVariant: MinoTextFieldButtonVariant = MinoTextFieldButtonVariant.Normal,
) {
    var value by remember { mutableStateOf(initialValue) }
    MinoTextField(
        value = value,
        onValueChange = { value = it },
        modifier = modifier,
        label = label,
        required = required,
        placeholder = "텍스트를 입력해 주세요.",
        helperText = helperText,
        status = status,
        enabled = enabled,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        trailingButtonLabel = trailingButtonLabel,
        trailingButtonVariant = trailingButtonVariant,
    )
}
