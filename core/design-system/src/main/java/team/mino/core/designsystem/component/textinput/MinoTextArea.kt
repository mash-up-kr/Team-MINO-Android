package team.mino.core.designsystem.component.textinput

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import team.mino.core.designsystem.component.textinput.token.TextInputTokens
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.text.graphemeLength

/**
 * TextArea의 높이 정책.
 *
 * - [Normal] 내용에 따라 늘어난다(최소 1줄).
 * - [Limit] 최대 [TextInputTokens.AreaMaxLines]줄까지 늘고, 넘치면 내부 스크롤.
 * - [Fixed] 항상 [TextInputTokens.AreaMaxLines]줄 고정 + 내부 스크롤.
 *
 * Figma `Resize` variant 축에 대응한다.
 */
enum class MinoTextAreaResize {
    Normal,
    Limit,
    Fixed,
}

/**
 * [MinoTextArea]의 검증 상태.
 *
 * - [Normal] 기본 상태.
 * - [Negative] 에러(빨강 테두리·헬퍼). TextInput과 달리 상태 아이콘은 붙지 않는다.
 *
 * Figma `Textinput/Textarea`의 `Status` variant 축에 대응한다. **성공(Positive) 상태가 없다** —
 * 컴포넌트셋 30개 변형이 전부 Normal 아니면 Negative이고, 문서 페이지도 두 값만 표기한다.
 * Textfield와는 서로를 물지 않는 형제 컴포넌트셋이라 [MinoTextFieldStatus]와 축을 공유하지 않는다.
 */
enum class MinoTextAreaStatus {
    Normal,
    Negative,
}

/**
 * 라벨 + 멀티행 입력 + 하단 영역(글자수 카운터·액션)으로 구성된 TextArea.
 *
 * Figma(MU_Wanted / Montage)의 `Textinput/Textarea` 스펙을 따른다.
 * 입력 영역 아래 하단 영역(Figma `Bottom`)에 좌측 그룹(글자수 카운터 + [bottomLeadingContent])과
 * 우측 그룹([bottomTrailingContent])을 두고, [status]가 Negative면 빨강 테두리·헬퍼로 표시한다.
 *
 * 상태는 [TextFieldState]로 호이스팅한다(멀티행 커서 추적 스크롤을 위해 최신 BasicTextField API 사용).
 * 호출부는 `rememberTextFieldState()`로 상태를 만들어 전달하고, 값은 `state.text`로 읽는다.
 *
 * 하단 우측의 액션은 Figma가 `Button/Text` 인스턴스를 쓰므로 호출부가
 * [MinoTextButton][team.mino.core.designsystem.component.button.MinoTextButton]을 [bottomTrailingContent]에 넣어 조립한다.
 *
 * @param state 입력 상태. 커서·선택 정보를 포함해 커서 이동 시 자동으로 스크롤을 따라간다.
 * @param status 검증 상태([MinoTextAreaStatus]). TextInput과 달리 성공 상태가 없다.
 * @param label 필드 위 라벨. null이면 라벨 줄 자체를 그리지 않는다.
 * @param required 라벨 뒤에 필수 표시(`*`)를 붙인다(Figma `requiredBadge`). [label]이 있을 때만 보인다.
 * @param maxLength 최대 글자수. 카운터 분모이며 초과 입력은 차단한다. 세는 단위는 코드 유닛이 아니라
 *   사용자가 보는 글자(UAX #29 grapheme cluster)다.
 * @param resize 높이 정책([MinoTextAreaResize]).
 * @param showBottom 하단 영역 표시 여부(Figma `bottom`). false면 카운터·슬롯이 모두 사라진다.
 * @param showCounter 하단 좌측 글자수 카운터 표시 여부(Figma `leadingContent`).
 * @param bottomLeadingContent 카운터 **뒤**에 이어 붙는 좌측 슬롯(Figma `Leading Content 2·3`).
 * @param bottomTrailingContent 하단 우측 슬롯(Figma `Trailing Content`). 항목 사이 간격은 컴포넌트가 준다.
 * @param colors 색 커스터마이징. 기본값은 [MinoTextInputDefaults.colors].
 */
@Composable
fun MinoTextArea(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    placeholder: String? = null,
    helperText: String? = null,
    status: MinoTextAreaStatus = MinoTextAreaStatus.Normal,
    enabled: Boolean = true,
    maxLength: Int = MinoTextInputDefaults.MaxLength,
    resize: MinoTextAreaResize = MinoTextAreaResize.Normal,
    showBottom: Boolean = true,
    showCounter: Boolean = true,
    bottomLeadingContent: (@Composable RowScope.() -> Unit)? = null,
    bottomTrailingContent: (@Composable RowScope.() -> Unit)? = null,
    colors: MinoTextInputColors = MinoTextInputDefaults.colors(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scrollState = rememberScrollState()
    val shape = MinoTextInputDefaults.shape
    val focused = isFocused && enabled
    val negative = status == MinoTextAreaStatus.Negative
    val contentColor = colors.textColor(enabled = enabled)
    val containerShadow = MinoTextInputDefaults.containerShadow(enabled = enabled)
    val inputFont = TextInputTokens.AreaInputFont.value
    // 입력 컴포넌트는 타자마다 재구성되므로 아래 넷을 매번 새로 만들지 않는다
    val inputTextStyle = remember(inputFont, contentColor) { inputFont.copy(color = contentColor) }
    val cursorBrush = remember(contentColor) { SolidColor(contentColor) }
    val inputTransformation = remember(maxLength) { MaxGraphemeLengthTransformation(maxLength) }
    val graphemeCount = remember(state.text) { state.text.graphemeLength() }

    // 줄 수로 높이를 제어한다. BasicTextField(state, scrollState)가 커서를 따라 스크롤을 내려준다.
    val lineLimits = remember(resize) {
        when (resize) {
            MinoTextAreaResize.Normal -> TextFieldLineLimits.MultiLine(minHeightInLines = 1)
            MinoTextAreaResize.Limit ->
                TextFieldLineLimits.MultiLine(minHeightInLines = 1, maxHeightInLines = TextInputTokens.AreaMaxLines)
            MinoTextAreaResize.Fixed ->
                TextFieldLineLimits.MultiLine(
                    minHeightInLines = TextInputTokens.AreaMaxLines,
                    maxHeightInLines = TextInputTokens.AreaMaxLines,
                )
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(TextInputTokens.LabelBoxSpacing)) {
        if (label != null) {
            MinoTextInputHeading(label = label, required = required, enabled = enabled, colors = colors)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (containerShadow != null) Modifier.dropShadow(shape, containerShadow) else Modifier)
                .surface(
                    shape = shape,
                    containerColor = colors.backgroundColor(enabled = enabled),
                    borderColor = colors.borderColor(enabled = enabled, negative = negative, focused = focused),
                    borderWidth = MinoTextInputDefaults.borderWidth(focused = focused),
                ).padding(TextInputTokens.AreaPadding),
            verticalArrangement = Arrangement.spacedBy(TextInputTokens.AreaBottomSpacing),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TextInputTokens.InputTextHorizontalPadding),
            ) {
                if (state.text.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = inputFont,
                        color = colors.placeholderColor,
                    )
                }
                BasicTextField(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    textStyle = inputTextStyle,
                    cursorBrush = cursorBrush,
                    interactionSource = interactionSource,
                    lineLimits = lineLimits,
                    scrollState = scrollState,
                    inputTransformation = inputTransformation,
                )
            }

            if (showBottom) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TextInputTokens.AreaBottomSlotHeight),
                    horizontalArrangement = Arrangement.spacedBy(TextInputTokens.AreaBottomContentSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(TextInputTokens.AreaBottomSlotSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (showCounter) {
                            Text(
                                text = "$graphemeCount/$maxLength",
                                modifier = Modifier
                                    .padding(horizontal = TextInputTokens.AreaBottomSlotHorizontalPadding),
                                style = TextInputTokens.CounterFont.value,
                                color = colors.counterColor,
                            )
                        }
                        bottomLeadingContent?.invoke(this)
                    }

                    if (bottomTrailingContent != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TextInputTokens.AreaBottomSlotSpacing),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            bottomTrailingContent(this)
                        }
                    }
                }
            }
        }

        if (helperText != null) {
            Text(
                text = helperText,
                style = TextInputTokens.HelperFont.value,
                color = colors.helperColor(enabled = enabled, negative = negative),
            )
        }
    }
}
