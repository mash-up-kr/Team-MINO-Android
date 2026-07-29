package team.mino.core.designsystem.component.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.textfield.token.TextFieldTokens
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * TextArea의 높이 정책.
 *
 * - [Normal] 내용에 따라 늘어난다(최소 1줄).
 * - [Limit] 최대 [TextFieldTokens.AreaMaxLines]줄까지 늘고, 넘치면 내부 스크롤.
 * - [Fixed] 항상 [TextFieldTokens.AreaMaxLines]줄 고정 + 내부 스크롤.
 *
 * Figma `Resize` variant 축에 대응한다.
 */
enum class MinoTextAreaResize {
    Normal,
    Limit,
    Fixed,
}

/**
 * 라벨 + 멀티행 입력 + 글자수 카운터 + 헬퍼로 구성된 TextArea.
 *
 * Figma(MU_Wanted / Montage)의 `Textinput/Textarea` 스펙을 따른다.
 * 입력 영역 하단에 `현재/최대` 카운터(좌)와 선택적 텍스트 버튼(우)을 두고,
 * [status]가 Negative면 빨강 테두리·헬퍼로 표시한다. (TextArea는 Positive 상태가 없다)
 *
 * 상태는 [TextFieldState]로 호이스팅한다(멀티행 커서 추적 스크롤을 위해 최신 BasicTextField API 사용).
 * 호출부는 `rememberTextFieldState()`로 상태를 만들어 전달하고, 값은 `state.text`로 읽는다.
 *
 * @param state 입력 상태. 커서·선택 정보를 포함해 커서 이동 시 자동으로 스크롤을 따라간다.
 * @param maxLength 최대 글자수. 카운터 분모이며 초과 입력은 [InputTransformation.maxLength]로 차단한다.
 * @param resize 높이 정책([MinoTextAreaResize]).
 * @param colors 색 커스터마이징. 기본값은 [MinoTextFieldDefaults.colors].
 */
@Composable
fun MinoTextArea(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    status: MinoTextFieldStatus = MinoTextFieldStatus.Normal,
    enabled: Boolean = true,
    maxLength: Int = MinoTextFieldDefaults.MaxLength,
    resize: MinoTextAreaResize = MinoTextAreaResize.Normal,
    trailingButtonLabel: String? = null,
    onTrailingButtonClick: () -> Unit = {},
    colors: MinoTextFieldColors = MinoTextFieldDefaults.colors(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scrollState = rememberScrollState()

    // 줄 수로 높이를 제어한다. BasicTextField(state, scrollState)가 커서를 따라 스크롤을 내려준다.
    val lineLimits = when (resize) {
        MinoTextAreaResize.Normal -> TextFieldLineLimits.MultiLine(minHeightInLines = 1)
        MinoTextAreaResize.Limit ->
            TextFieldLineLimits.MultiLine(minHeightInLines = 1, maxHeightInLines = TextFieldTokens.AreaMaxLines)
        MinoTextAreaResize.Fixed ->
            TextFieldLineLimits.MultiLine(
                minHeightInLines = TextFieldTokens.AreaMaxLines,
                maxHeightInLines = TextFieldTokens.AreaMaxLines,
            )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(TextFieldTokens.LabelBoxSpacing)) {
        if (label != null) {
            Text(
                text = label,
                style = TextFieldTokens.LabelFont.value,
                color = colors.labelColor(enabled = enabled),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MinoTextFieldDefaults.shape)
                .background(colors.backgroundColor(enabled = enabled))
                .border(
                    width = TextFieldTokens.BorderWidth,
                    color = colors.borderColor(enabled = enabled, status = status, focused = isFocused),
                    shape = MinoTextFieldDefaults.shape,
                ).padding(TextFieldTokens.AreaPadding),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (state.text.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = TextFieldTokens.AreaInputFont.value,
                        color = colors.placeholderColor,
                    )
                }
                BasicTextField(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    textStyle = TextFieldTokens.AreaInputFont.value.copy(color = colors.textColor(enabled = enabled)),
                    cursorBrush = SolidColor(colors.textColor(enabled = enabled)),
                    interactionSource = interactionSource,
                    lineLimits = lineLimits,
                    scrollState = scrollState,
                    inputTransformation = InputTransformation.maxLength(maxLength),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TextFieldTokens.AreaBottomSpacing),
                horizontalArrangement = Arrangement.spacedBy(TextFieldTokens.ContentSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${state.text.length}/$maxLength",
                    modifier = Modifier.weight(1f),
                    style = TextFieldTokens.CounterFont.value,
                    color = colors.counterColor,
                )
                if (trailingButtonLabel != null) {
                    MinoTextFieldTrailingButton(
                        label = trailingButtonLabel,
                        enabled = enabled,
                        colors = colors,
                        onClick = onTrailingButtonClick,
                    )
                }
            }
        }

        if (helperText != null) {
            Text(
                text = helperText,
                style = TextFieldTokens.HelperFont.value,
                color = colors.helperColor(enabled = enabled, status = status),
            )
        }
    }
}

@UiModePreviews
@Composable
private fun MinoTextAreaPreview() {
    MinoAndroidAppTheme {
        val normalState = rememberTextFieldState()
        val negativeState = rememberTextFieldState(initialText = "입력된 내용")
        Column(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MinoTextArea(
                state = normalState,
                label = "주제",
                placeholder = "메시지를 입력해 주세요.",
                helperText = "메시지에 마침표를 찍어요.",
                trailingButtonLabel = "텍스트",
            )
            MinoTextArea(
                state = negativeState,
                label = "주제",
                helperText = "메시지에 마침표를 찍어요.",
                status = MinoTextFieldStatus.Negative,
                resize = MinoTextAreaResize.Fixed,
                trailingButtonLabel = "텍스트",
            )
        }
    }
}
