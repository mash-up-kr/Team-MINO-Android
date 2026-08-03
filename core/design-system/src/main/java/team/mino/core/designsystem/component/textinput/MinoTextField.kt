package team.mino.core.designsystem.component.textinput

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import team.mino.core.designsystem.component.textinput.token.TextInputTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CircleCheckFill
import team.mino.core.designsystem.foundation.icons.icons.CircleExclamationFill
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.foundation.shadow.token.value
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 라벨 + 단일행 입력 + 헬퍼 메시지로 구성된 단일행 텍스트 입력.
 *
 * Figma(MU_Wanted / Montage)의 `Textinput/Textfield` 스펙을 따른다.
 * 검증 상태([status])에 따라 테두리·헬퍼 색과 트레일링 아이콘(성공 체크 / 에러 느낌표)이 바뀌고,
 * 포커스 중 값이 있으면 Clear(x) 버튼을, [trailingButtonLabel]이 있으면 박스형 버튼을 노출한다.
 *
 * 입력 상자 배경은 **반투명**이라 뒤에 깔린 화면 배경이 비친다(Figma `background=Android`).
 *
 * @param value 현재 입력값.
 * @param onValueChange 입력 변경 콜백.
 * @param label 필드 위 라벨. null이면 라벨 줄 자체를 그리지 않는다.
 * @param required 라벨 뒤에 필수 표시(`*`)를 붙인다(Figma `requiredBadge`). [label]이 있을 때만 보인다.
 * @param placeholder 값이 없을 때 보여줄 안내 문구.
 * @param helperText 필드 아래 보조 메시지. [status]에 따라 색이 바뀐다.
 * @param status 검증 상태(Normal/Positive/Negative).
 * @param enabled false면 비활성 스타일 + 입력 차단.
 * @param showClearButton 포커스 + 값 존재 시 Clear(x) 버튼 노출 여부.
 * @param leadingContent 입력 글자 **앞**에 놓이는 슬롯(Figma `icon`). 22dp 정사각으로 잘린다.
 *   슬롯 안에서는 `LocalContentColor`가 입력 글자색으로 지정돼 있다.
 * @param trailingContent 입력 글자 **뒤**, 상태 아이콘보다 더 오른쪽에 놓이는 슬롯(Figma `trailingContent`).
 *   24dp 정사각이다. 상태 아이콘·Clear 버튼은 [status]가 소유하므로 이 슬롯과 자리가 겹치지 않는다.
 * @param trailingButtonLabel 오른쪽 박스형 버튼 라벨. null이면 숨김.
 * @param trailingButtonVariant 박스형 버튼의 시각 스타일(Figma `trailingButton.variant`).
 * @param onTrailingButtonClick 박스형 버튼 클릭 콜백.
 * @param colors 색 커스터마이징. 기본값은 [MinoTextInputDefaults.colors].
 */
@Composable
fun MinoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    placeholder: String? = null,
    helperText: String? = null,
    status: MinoTextFieldStatus = MinoTextFieldStatus.Normal,
    enabled: Boolean = true,
    showClearButton: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    trailingButtonLabel: String? = null,
    trailingButtonVariant: MinoTextFieldButtonVariant = MinoTextFieldButtonVariant.Normal,
    onTrailingButtonClick: () -> Unit = {},
    colors: MinoTextInputColors = MinoTextInputDefaults.colors(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = MinoTextInputDefaults.shape
    val focused = isFocused && enabled
    val negative = status == MinoTextFieldStatus.Negative
    val borderColor = colors.borderColor(enabled = enabled, negative = negative, focused = focused)
    val contentColor = colors.textColor(enabled = enabled)
    val containerShadow = MinoTextInputDefaults.containerShadow(enabled = enabled)
    val inputFont = TextInputTokens.InputFont.value
    // 입력 컴포넌트는 타자마다 재구성되므로 TextStyle·Brush를 매번 새로 만들지 않는다
    val inputTextStyle = remember(inputFont, contentColor) { inputFont.copy(color = contentColor) }
    val cursorBrush = remember(contentColor) { SolidColor(contentColor) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(TextInputTokens.LabelBoxSpacing)) {
        if (label != null) {
            MinoTextInputHeading(label = label, required = required, enabled = enabled, colors = colors)
        }

        // 배경·테두리·그림자는 입력 상자와 트레일링 버튼을 함께 감싸 한 덩어리로 그린다(Figma `Wrapper`).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (containerShadow != null) Modifier.dropShadow(shape, containerShadow) else Modifier)
                .surface(
                    shape = shape,
                    containerColor = colors.backgroundColor(enabled = enabled),
                    borderColor = borderColor,
                    borderWidth = MinoTextInputDefaults.borderWidth(focused = focused),
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(TextInputTokens.InputPadding)
                    .heightIn(min = TextInputTokens.InputMinContentHeight),
                horizontalArrangement = Arrangement.spacedBy(TextInputTokens.ContentSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    if (leadingContent != null) {
                        Box(
                            modifier = Modifier
                                .padding(TextInputTokens.LeadingContentPadding)
                                .size(TextInputTokens.LeadingContentSize),
                            contentAlignment = Alignment.Center,
                        ) {
                            leadingContent()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = TextInputTokens.InputTextHorizontalPadding),
                    ) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = inputFont,
                                color = colors.placeholderColor,
                            )
                        }
                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = enabled,
                            singleLine = true,
                            textStyle = inputTextStyle,
                            cursorBrush = cursorBrush,
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

                    if (trailingContent != null) {
                        Box(
                            modifier = Modifier.size(TextInputTokens.TrailingContentSize),
                            contentAlignment = Alignment.Center,
                        ) {
                            trailingContent()
                        }
                    }
                }
            }

            if (trailingButtonLabel != null) {
                MinoTextFieldTrailingButton(
                    label = trailingButtonLabel,
                    variant = trailingButtonVariant,
                    enabled = enabled,
                    dividerColor = borderColor,
                    colors = colors,
                    onClick = onTrailingButtonClick,
                )
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

/**
 * 트레일링 상태 아이콘 슬롯. 우선순위: Positive/Negative 상태 아이콘 > Clear(x) 버튼.
 * (셋 다 없으면 아무것도 그리지 않는다)
 */
@Composable
private fun TrailingStatusSlot(
    status: MinoTextFieldStatus,
    enabled: Boolean,
    showClear: Boolean,
    colors: MinoTextInputColors,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusIcon = when (status) {
        MinoTextFieldStatus.Positive -> MinoIcons.CircleCheckFill
        MinoTextFieldStatus.Negative -> MinoIcons.CircleExclamationFill
        MinoTextFieldStatus.Normal -> null
    }

    when {
        statusIcon != null -> Icon(
            imageVector = statusIcon,
            contentDescription = null,
            tint = colors.statusIconColor(status = status, enabled = enabled),
            modifier = modifier.size(TextInputTokens.StatusIconSize),
        )

        showClear -> Icon(
            imageVector = MinoIcons.Close,
            contentDescription = "입력 삭제",
            tint = colors.clearIconColor,
            modifier = modifier
                .size(TextInputTokens.ClearIconSize)
                .clip(MinoTextInputDefaults.shape)
                .rippleSingleClickable(onClick = onClear),
        )
    }
}
