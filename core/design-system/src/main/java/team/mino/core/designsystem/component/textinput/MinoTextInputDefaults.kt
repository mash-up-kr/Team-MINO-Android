package team.mino.core.designsystem.component.textinput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import team.mino.core.designsystem.component.textinput.token.TextInputTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.foundation.shadow.MinoShadow
import team.mino.core.designsystem.foundation.shadow.token.value
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * [MinoTextField]·[MinoTextArea]의 기본값 모음.
 *
 * Figma가 두 컴포넌트를 `Textinput/` 한 가족으로 묶고 배경·테두리·그림자 리소스를 공유시키므로,
 * 이름도 가족 단위인 `TextInput` 접두사를 쓴다. 단일 컴포넌트에만 해당하는 타입은
 * `MinoTextField*` / `MinoTextArea*`로 갈라 둔다.
 */
object MinoTextInputDefaults {
    /** 필드 컨테이너 기본 셰이프. */
    val shape: Shape = TextInputTokens.ContainerShape

    /** TextArea 최대 글자수 기본값. */
    val MaxLength: Int = TextInputTokens.DefaultMaxLength

    /** 테두리 두께. Figma는 포커스 상태에서만 두껍게 그린다. */
    internal fun borderWidth(focused: Boolean): Dp =
        if (focused) TextInputTokens.FocusedBorderWidth else TextInputTokens.BorderWidth

    /**
     * 컨테이너 드롭 섀도. 비활성에는 걸리지 않아 null을 돌려준다.
     *
     * Figma가 배경·테두리·그림자를 `Textinput/` 가족의 **공유 리소스**로 두므로, 상태에 따른
     * 해석도 두 컴포넌트가 각자 갖지 않고 여기서 한 번만 정한다([borderWidth]와 같은 자리다).
     */
    @Composable
    @ReadOnlyComposable
    internal fun containerShadow(enabled: Boolean): MinoShadow? =
        if (enabled) TextInputTokens.ContainerShadow.value else null

    /** 기본 [MinoTextInputColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(): MinoTextInputColors = MinoAndroidTheme.colors.defaultTextInputColors

    /**
     * 기본값에서 일부만 바꾼 [MinoTextInputColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        labelColor: Color = Color.Unspecified,
        disabledLabelColor: Color = Color.Unspecified,
        requiredColor: Color = Color.Unspecified,
        textColor: Color = Color.Unspecified,
        disabledTextColor: Color = Color.Unspecified,
        placeholderColor: Color = Color.Unspecified,
        backgroundColor: Color = Color.Unspecified,
        disabledBackgroundColor: Color = Color.Unspecified,
        borderColor: Color = Color.Unspecified,
        disabledBorderColor: Color = Color.Unspecified,
        focusedBorderColor: Color = Color.Unspecified,
        errorBorderColor: Color = Color.Unspecified,
        helperColor: Color = Color.Unspecified,
        errorHelperColor: Color = Color.Unspecified,
        counterColor: Color = Color.Unspecified,
        trailingButtonColor: Color = Color.Unspecified,
        assistiveTrailingButtonColor: Color = Color.Unspecified,
        disabledTrailingButtonColor: Color = Color.Unspecified,
        positiveIconColor: Color = Color.Unspecified,
        negativeIconColor: Color = Color.Unspecified,
        disabledIconColor: Color = Color.Unspecified,
        clearIconColor: Color = Color.Unspecified,
    ): MinoTextInputColors =
        MinoAndroidTheme.colors.defaultTextInputColors.copy(
            labelColor = labelColor,
            disabledLabelColor = disabledLabelColor,
            requiredColor = requiredColor,
            textColor = textColor,
            disabledTextColor = disabledTextColor,
            placeholderColor = placeholderColor,
            backgroundColor = backgroundColor,
            disabledBackgroundColor = disabledBackgroundColor,
            borderColor = borderColor,
            disabledBorderColor = disabledBorderColor,
            focusedBorderColor = focusedBorderColor,
            errorBorderColor = errorBorderColor,
            helperColor = helperColor,
            errorHelperColor = errorHelperColor,
            counterColor = counterColor,
            trailingButtonColor = trailingButtonColor,
            assistiveTrailingButtonColor = assistiveTrailingButtonColor,
            disabledTrailingButtonColor = disabledTrailingButtonColor,
            positiveIconColor = positiveIconColor,
            negativeIconColor = negativeIconColor,
            disabledIconColor = disabledIconColor,
            clearIconColor = clearIconColor,
        )

    internal val ColorScheme.defaultTextInputColors: MinoTextInputColors
        get() =
            defaultTextInputColorsCached
                ?: MinoTextInputColors(
                    labelColor = fromToken(TextInputTokens.LabelColor),
                    disabledLabelColor = fromToken(TextInputTokens.DisabledLabelColor),
                    requiredColor = fromToken(TextInputTokens.RequiredColor),
                    textColor = fromToken(TextInputTokens.TextColor),
                    disabledTextColor = fromToken(TextInputTokens.DisabledTextColor),
                    placeholderColor = fromToken(TextInputTokens.PlaceholderColor),
                    backgroundColor = fromToken(TextInputTokens.BackgroundColor),
                    disabledBackgroundColor = fromToken(TextInputTokens.DisabledBackgroundColor),
                    borderColor = fromToken(TextInputTokens.BorderColor),
                    disabledBorderColor = fromToken(TextInputTokens.DisabledBorderColor),
                    // Figma는 색 토큰 위에 레이어 불투명도를 한 번 더 곱한다.
                    focusedBorderColor = fromToken(TextInputTokens.FocusedBorderColor)
                        .scaleAlpha(TextInputTokens.FocusedBorderOpacity),
                    errorBorderColor = fromToken(TextInputTokens.ErrorBorderColor),
                    helperColor = fromToken(TextInputTokens.HelperColor),
                    errorHelperColor = fromToken(TextInputTokens.ErrorHelperColor),
                    counterColor = fromToken(TextInputTokens.CounterColor)
                        .scaleAlpha(TextInputTokens.CounterOpacity),
                    trailingButtonColor = fromToken(TextInputTokens.TrailingButtonColor),
                    assistiveTrailingButtonColor = fromToken(TextInputTokens.AssistiveTrailingButtonColor),
                    disabledTrailingButtonColor = fromToken(TextInputTokens.DisabledTrailingButtonColor),
                    positiveIconColor = fromToken(TextInputTokens.PositiveIconColor),
                    negativeIconColor = fromToken(TextInputTokens.NegativeIconColor),
                    disabledIconColor = fromToken(TextInputTokens.DisabledIconColor),
                    clearIconColor = fromToken(TextInputTokens.ClearIconColor),
                ).also { defaultTextInputColorsCached = it }
}

/** 시맨틱 토큰이 이미 들고 있는 알파에 레이어 불투명도를 곱한다(Figma의 레이어 opacity와 같은 계산). */
private fun Color.scaleAlpha(factor: Float): Color = copy(alpha = alpha * factor)

/**
 * [MinoTextField] 오른쪽에 붙는 박스형 트레일링 버튼의 시각 스타일.
 *
 * Figma `Textinput/Resource/Textfield/Button`(16215-32904)의 `variant` 축에 대응한다.
 * 글자색과 굵기만 가른다.
 */
enum class MinoTextFieldButtonVariant {
    /** Figma `variant=Normal`. Primary 색 + Bold. */
    Normal,

    /** Figma `variant=Assistive`. Label/Normal + Medium으로 한 단계 낮춘 강조. */
    Assistive,
}

/**
 * [MinoTextField]·[MinoTextArea]의 상태별 색. 슬롯이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 *
 * 두 컴포넌트가 색을 공유하는 근거는 Figma다 — 배경·테두리·그림자를 같은 리소스 컴포넌트로 물고 있다.
 * 반면 검증 상태 축은 각자 따로다([MinoTextFieldStatus] / [MinoTextAreaStatus]).
 *
 * 상태 우선순위(Figma): Disable > Negative > Focus > 기본. Negative는 포커스 여부와 무관하게 빨강 유지.
 */
@Immutable
class MinoTextInputColors(
    val labelColor: Color,
    val disabledLabelColor: Color,
    val requiredColor: Color,
    val textColor: Color,
    val disabledTextColor: Color,
    val placeholderColor: Color,
    val backgroundColor: Color,
    val disabledBackgroundColor: Color,
    val borderColor: Color,
    val disabledBorderColor: Color,
    val focusedBorderColor: Color,
    val errorBorderColor: Color,
    val helperColor: Color,
    val errorHelperColor: Color,
    val counterColor: Color,
    val trailingButtonColor: Color,
    val assistiveTrailingButtonColor: Color,
    val disabledTrailingButtonColor: Color,
    val positiveIconColor: Color,
    val negativeIconColor: Color,
    val disabledIconColor: Color,
    val clearIconColor: Color,
) {
    fun copy(
        labelColor: Color = this.labelColor,
        disabledLabelColor: Color = this.disabledLabelColor,
        requiredColor: Color = this.requiredColor,
        textColor: Color = this.textColor,
        disabledTextColor: Color = this.disabledTextColor,
        placeholderColor: Color = this.placeholderColor,
        backgroundColor: Color = this.backgroundColor,
        disabledBackgroundColor: Color = this.disabledBackgroundColor,
        borderColor: Color = this.borderColor,
        disabledBorderColor: Color = this.disabledBorderColor,
        focusedBorderColor: Color = this.focusedBorderColor,
        errorBorderColor: Color = this.errorBorderColor,
        helperColor: Color = this.helperColor,
        errorHelperColor: Color = this.errorHelperColor,
        counterColor: Color = this.counterColor,
        trailingButtonColor: Color = this.trailingButtonColor,
        assistiveTrailingButtonColor: Color = this.assistiveTrailingButtonColor,
        disabledTrailingButtonColor: Color = this.disabledTrailingButtonColor,
        positiveIconColor: Color = this.positiveIconColor,
        negativeIconColor: Color = this.negativeIconColor,
        disabledIconColor: Color = this.disabledIconColor,
        clearIconColor: Color = this.clearIconColor,
    ): MinoTextInputColors =
        MinoTextInputColors(
            labelColor = labelColor.takeOrElse { this.labelColor },
            disabledLabelColor = disabledLabelColor.takeOrElse { this.disabledLabelColor },
            requiredColor = requiredColor.takeOrElse { this.requiredColor },
            textColor = textColor.takeOrElse { this.textColor },
            disabledTextColor = disabledTextColor.takeOrElse { this.disabledTextColor },
            placeholderColor = placeholderColor.takeOrElse { this.placeholderColor },
            backgroundColor = backgroundColor.takeOrElse { this.backgroundColor },
            disabledBackgroundColor = disabledBackgroundColor.takeOrElse { this.disabledBackgroundColor },
            borderColor = borderColor.takeOrElse { this.borderColor },
            disabledBorderColor = disabledBorderColor.takeOrElse { this.disabledBorderColor },
            focusedBorderColor = focusedBorderColor.takeOrElse { this.focusedBorderColor },
            errorBorderColor = errorBorderColor.takeOrElse { this.errorBorderColor },
            helperColor = helperColor.takeOrElse { this.helperColor },
            errorHelperColor = errorHelperColor.takeOrElse { this.errorHelperColor },
            counterColor = counterColor.takeOrElse { this.counterColor },
            trailingButtonColor = trailingButtonColor.takeOrElse { this.trailingButtonColor },
            assistiveTrailingButtonColor =
                assistiveTrailingButtonColor.takeOrElse { this.assistiveTrailingButtonColor },
            disabledTrailingButtonColor = disabledTrailingButtonColor.takeOrElse { this.disabledTrailingButtonColor },
            positiveIconColor = positiveIconColor.takeOrElse { this.positiveIconColor },
            negativeIconColor = negativeIconColor.takeOrElse { this.negativeIconColor },
            disabledIconColor = disabledIconColor.takeOrElse { this.disabledIconColor },
            clearIconColor = clearIconColor.takeOrElse { this.clearIconColor },
        )

    @Stable
    internal fun labelColor(enabled: Boolean): Color = if (enabled) labelColor else disabledLabelColor

    @Stable
    internal fun textColor(enabled: Boolean): Color = if (enabled) textColor else disabledTextColor

    @Stable
    internal fun backgroundColor(enabled: Boolean): Color = if (enabled) backgroundColor else disabledBackgroundColor

    // 컨테이너 겉면(배경·테두리·그림자)은 Figma에서도 Textfield·Textarea가 같은 리소스 컴포넌트를
    // 공유한다. 다만 두 컴포넌트의 `Status` 축은 값 수가 달라(3값/2값) 서로 다른 enum이므로,
    // 공유 지점에서는 두 축이 공통으로 갖는 "에러인가"만 불리언으로 받는다.

    @Stable
    internal fun borderColor(
        enabled: Boolean,
        negative: Boolean,
        focused: Boolean,
    ): Color =
        when {
            !enabled -> disabledBorderColor
            negative -> errorBorderColor
            focused -> focusedBorderColor
            else -> borderColor
        }

    @Stable
    internal fun helperColor(
        enabled: Boolean,
        negative: Boolean,
    ): Color = if (enabled && negative) errorHelperColor else helperColor

    @Stable
    internal fun trailingButtonColor(
        enabled: Boolean,
        variant: MinoTextFieldButtonVariant,
    ): Color =
        when {
            !enabled -> disabledTrailingButtonColor
            variant == MinoTextFieldButtonVariant.Assistive -> assistiveTrailingButtonColor
            else -> trailingButtonColor
        }

    @Stable
    internal fun statusIconColor(
        status: MinoTextFieldStatus,
        enabled: Boolean,
    ): Color =
        when {
            !enabled -> disabledIconColor
            status == MinoTextFieldStatus.Negative -> negativeIconColor
            else -> positiveIconColor
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoTextInputColors) return false

        if (labelColor != other.labelColor) return false
        if (disabledLabelColor != other.disabledLabelColor) return false
        if (requiredColor != other.requiredColor) return false
        if (textColor != other.textColor) return false
        if (disabledTextColor != other.disabledTextColor) return false
        if (placeholderColor != other.placeholderColor) return false
        if (backgroundColor != other.backgroundColor) return false
        if (disabledBackgroundColor != other.disabledBackgroundColor) return false
        if (borderColor != other.borderColor) return false
        if (disabledBorderColor != other.disabledBorderColor) return false
        if (focusedBorderColor != other.focusedBorderColor) return false
        if (errorBorderColor != other.errorBorderColor) return false
        if (helperColor != other.helperColor) return false
        if (errorHelperColor != other.errorHelperColor) return false
        if (counterColor != other.counterColor) return false
        if (trailingButtonColor != other.trailingButtonColor) return false
        if (assistiveTrailingButtonColor != other.assistiveTrailingButtonColor) return false
        if (disabledTrailingButtonColor != other.disabledTrailingButtonColor) return false
        if (positiveIconColor != other.positiveIconColor) return false
        if (negativeIconColor != other.negativeIconColor) return false
        if (disabledIconColor != other.disabledIconColor) return false
        if (clearIconColor != other.clearIconColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            labelColor,
            disabledLabelColor,
            requiredColor,
            textColor,
            disabledTextColor,
            placeholderColor,
            backgroundColor,
            disabledBackgroundColor,
            borderColor,
            disabledBorderColor,
            focusedBorderColor,
            errorBorderColor,
            helperColor,
            errorHelperColor,
            counterColor,
            trailingButtonColor,
            assistiveTrailingButtonColor,
            disabledTrailingButtonColor,
            positiveIconColor,
            negativeIconColor,
            disabledIconColor,
            clearIconColor,
        ).contentHashCode()
}

/**
 * 라벨 + 필수 표시(`*`)로 구성된 필드 상단 헤딩. Textfield/Textarea 공통.
 *
 * Figma `Heading` 프레임에 대응한다. 필수 표시는 별표 하나로, 라벨과 같은 크기에 굵기만 한 단계 낮다.
 */
@Composable
internal fun MinoTextInputHeading(
    label: String,
    required: Boolean,
    enabled: Boolean,
    colors: MinoTextInputColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TextInputTokens.HeadingSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = TextInputTokens.LabelFont.value,
            color = colors.labelColor(enabled = enabled),
        )
        if (required) {
            Text(
                text = "*",
                style = TextInputTokens.RequiredFont.value,
                color = colors.requiredColor,
            )
        }
    }
}

/**
 * [MinoTextField] 오른쪽에 입력 상자와 한 몸으로 붙는 박스형 버튼.
 *
 * Figma 원본(`Textinput/Resource/Textfield/Button` 16215-32904)은 이름 그대로 **Textfield 전용
 * 리소스**라 공용 버튼 계열(`Button/Button`·`Button/Text`)과 별개다. 그래서 여기 패키지에 둔다.
 *
 * 배경·테두리·그림자는 입력 상자를 감싸는 바깥 컨테이너가 한 번에 그리고, 이 컴포넌트는 두 영역을
 * 가르는 세로 경계선만 직접 긋는다. Figma도 버튼의 왼쪽 테두리는 그리지 않고 입력 상자의 오른쪽
 * 테두리가 경계가 되게 해 두 겹이 겹치지 않도록 했다.
 */
@Composable
internal fun MinoTextFieldTrailingButton(
    label: String,
    variant: MinoTextFieldButtonVariant,
    enabled: Boolean,
    dividerColor: Color,
    colors: MinoTextInputColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dividerWidthPx = with(LocalDensity.current) { TextInputTokens.BorderWidth.toPx() }
    val font =
        if (variant == MinoTextFieldButtonVariant.Assistive) {
            TextInputTokens.AssistiveTrailingButtonFont
        } else {
            TextInputTokens.TrailingButtonFont
        }

    Box(
        modifier = modifier
            .widthIn(min = TextInputTokens.TrailingButtonMinWidth)
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset(x = dividerWidthPx / 2f, y = 0f),
                    end = Offset(x = dividerWidthPx / 2f, y = size.height),
                    strokeWidth = dividerWidthPx,
                )
            }.rippleSingleClickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = TextInputTokens.TrailingButtonHorizontalPadding,
                vertical = TextInputTokens.TrailingButtonVerticalPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = font.value,
            color = colors.trailingButtonColor(enabled = enabled, variant = variant),
        )
    }
}
