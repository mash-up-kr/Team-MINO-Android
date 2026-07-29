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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.textfield.token.TextFieldTokens
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CircleCheckFill
import team.mino.core.designsystem.foundation.icons.icons.CircleExclamationFill
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * 라벨 + 단일행 입력 + 헬퍼 메시지로 구성된 TextInput.
 *
 * Figma(MU_Wanted / Montage)의 `Textinput/Textfield` 스펙을 따른다.
 * 검증 상태([status])에 따라 테두리·헬퍼 색과 트레일링 아이콘(성공 체크 / 에러 느낌표)이 바뀌고,
 * 포커스 중 값이 있으면 Clear(x) 버튼을, [trailingButtonLabel]이 있으면 텍스트 버튼을 노출한다.
 *
 * @param value 현재 입력값.
 * @param onValueChange 입력 변경 콜백.
 * @param label 필드 위 라벨. null이면 표시하지 않는다.
 * @param placeholder 값이 없을 때 보여줄 안내 문구.
 * @param helperText 필드 아래 보조 메시지. [status]에 따라 색이 바뀐다.
 * @param status 검증 상태(Normal/Positive/Negative).
 * @param enabled false면 비활성 스타일 + 입력 차단.
 * @param showClearButton 포커스 + 값 존재 시 Clear(x) 버튼 노출 여부.
 * @param trailingButtonLabel 오른쪽 텍스트 버튼 라벨. null이면 숨김.
 * @param onTrailingButtonClick 텍스트 버튼 클릭 콜백.
 * @param colors 색 커스터마이징. 기본값은 [MinoTextFieldDefaults.colors].
 */
@Composable
fun MinoTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    status: MinoTextFieldStatus = MinoTextFieldStatus.Normal,
    enabled: Boolean = true,
    showClearButton: Boolean = true,
    trailingButtonLabel: String? = null,
    onTrailingButtonClick: () -> Unit = {},
    colors: MinoTextFieldColors = MinoTextFieldDefaults.colors(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(TextFieldTokens.LabelBoxSpacing)) {
        if (label != null) {
            Text(
                text = label,
                style = TextFieldTokens.LabelFont.value,
                color = colors.labelColor(enabled = enabled),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MinoTextFieldDefaults.shape)
                .background(colors.backgroundColor(enabled = enabled))
                .border(
                    width = TextFieldTokens.BorderWidth,
                    color = colors.borderColor(enabled = enabled, status = status, focused = isFocused),
                    shape = MinoTextFieldDefaults.shape,
                ).padding(
                    horizontal = TextFieldTokens.InputHorizontalPadding,
                    vertical = TextFieldTokens.InputVerticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(TextFieldTokens.ContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = TextFieldTokens.InputFont.value,
                        color = colors.placeholderColor,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    singleLine = true,
                    textStyle = TextFieldTokens.InputFont.value.copy(color = colors.textColor(enabled = enabled)),
                    cursorBrush = SolidColor(colors.textColor(enabled = enabled)),
                    interactionSource = interactionSource,
                )
            }

            TrailingStatusSlot(
                status = status,
                enabled = enabled,
                showClear = showClearButton && isFocused && value.isNotEmpty(),
                colors = colors,
                onClear = { onValueChange("") },
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

        if (helperText != null) {
            Text(
                text = helperText,
                style = TextFieldTokens.HelperFont.value,
                color = colors.helperColor(enabled = enabled, status = status),
            )
        }
    }
}

/**
 * 트레일링 상태 아이콘 슬롯. 우선순위: Positive/Negative 상태 아이콘 > Clear(x) 버튼.
 * (셋 다 없으면 아무것도 그리지 않는다)
 */
@Composable
private fun TrailingStatusSlot(
    status: MinoTextFieldStatus,
    enabled: Boolean,
    showClear: Boolean,
    colors: MinoTextFieldColors,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (status) {
        MinoTextFieldStatus.Positive -> Icon(
            imageVector = MinoIcons.CircleCheckFill,
            contentDescription = null,
            tint = colors.statusIconColor(status = status, enabled = enabled),
            modifier = modifier.size(TextFieldTokens.StatusIconSize),
        )

        MinoTextFieldStatus.Negative -> Icon(
            imageVector = MinoIcons.CircleExclamationFill,
            contentDescription = null,
            tint = colors.statusIconColor(status = status, enabled = enabled),
            modifier = modifier.size(TextFieldTokens.StatusIconSize),
        )

        MinoTextFieldStatus.Normal -> if (showClear) {
            Icon(
                imageVector = MinoIcons.Close,
                contentDescription = "입력 삭제",
                tint = colors.clearIconColor,
                modifier = modifier
                    .size(TextFieldTokens.ClearIconSize)
                    .clip(MinoTextFieldDefaults.shape)
                    .rippleSingleClickable(onClick = onClear),
            )
        }
    }
}

@UiModePreviews
@Composable
private fun MinoTextInputPreview() {
    MinoAndroidAppTheme {
        var normal by remember { mutableStateOf("") }
        var positive by remember { mutableStateOf("값") }
        var negative by remember { mutableStateOf("값") }
        Column(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MinoTextInput(
                value = normal,
                onValueChange = { normal = it },
                label = "주제",
                placeholder = "텍스트를 입력해 주세요.",
                helperText = "메시지에 마침표를 찍어요.",
            )
            MinoTextInput(
                value = positive,
                onValueChange = { positive = it },
                label = "주제",
                helperText = "성공 메시지를 나타내요.",
                status = MinoTextFieldStatus.Positive,
                trailingButtonLabel = "텍스트",
            )
            MinoTextInput(
                value = negative,
                onValueChange = { negative = it },
                label = "주제",
                helperText = "에러 메시지를 나타내요.",
                status = MinoTextFieldStatus.Negative,
            )
            MinoTextInput(
                value = "값",
                onValueChange = {},
                label = "주제",
                helperText = "메시지에 마침표를 찍어요.",
                enabled = false,
            )
        }
    }
}
