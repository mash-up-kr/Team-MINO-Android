package team.mino.core.designsystem.component.textfield

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import team.mino.core.designsystem.component.textfield.token.TextFieldTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.foundation.shape.token.value
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * [MinoTextInput]·[MinoTextArea]의 기본값 모음.
 */
object MinoTextFieldDefaults {
    /** 필드 컨테이너 기본 셰이프. */
    val shape: Shape
        @Composable @ReadOnlyComposable get() = TextFieldTokens.ContainerShape.value

    /** TextArea 최대 글자수 기본값. */
    val MaxLength: Int = TextFieldTokens.DefaultMaxLength

    /** 기본 [MinoTextFieldColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(): MinoTextFieldColors = MinoAndroidTheme.colors.defaultTextFieldColors

    /**
     * 기본값에서 일부만 바꾼 [MinoTextFieldColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        labelColor: Color = Color.Unspecified,
        disabledLabelColor: Color = Color.Unspecified,
        textColor: Color = Color.Unspecified,
        disabledTextColor: Color = Color.Unspecified,
        placeholderColor: Color = Color.Unspecified,
        backgroundColor: Color = Color.Unspecified,
        disabledBackgroundColor: Color = Color.Unspecified,
        borderColor: Color = Color.Unspecified,
        focusedBorderColor: Color = Color.Unspecified,
        errorBorderColor: Color = Color.Unspecified,
        helperColor: Color = Color.Unspecified,
        errorHelperColor: Color = Color.Unspecified,
        counterColor: Color = Color.Unspecified,
        trailingButtonColor: Color = Color.Unspecified,
        disabledTrailingButtonColor: Color = Color.Unspecified,
        positiveIconColor: Color = Color.Unspecified,
        negativeIconColor: Color = Color.Unspecified,
        disabledIconColor: Color = Color.Unspecified,
        clearIconColor: Color = Color.Unspecified,
    ): MinoTextFieldColors =
        MinoAndroidTheme.colors.defaultTextFieldColors.copy(
            labelColor = labelColor,
            disabledLabelColor = disabledLabelColor,
            textColor = textColor,
            disabledTextColor = disabledTextColor,
            placeholderColor = placeholderColor,
            backgroundColor = backgroundColor,
            disabledBackgroundColor = disabledBackgroundColor,
            borderColor = borderColor,
            focusedBorderColor = focusedBorderColor,
            errorBorderColor = errorBorderColor,
            helperColor = helperColor,
            errorHelperColor = errorHelperColor,
            counterColor = counterColor,
            trailingButtonColor = trailingButtonColor,
            disabledTrailingButtonColor = disabledTrailingButtonColor,
            positiveIconColor = positiveIconColor,
            negativeIconColor = negativeIconColor,
            disabledIconColor = disabledIconColor,
            clearIconColor = clearIconColor,
        )

    internal val ColorScheme.defaultTextFieldColors: MinoTextFieldColors
        get() =
            defaultTextFieldColorsCached
                ?: MinoTextFieldColors(
                    labelColor = fromToken(TextFieldTokens.LabelColor),
                    disabledLabelColor = fromToken(TextFieldTokens.DisabledLabelColor),
                    textColor = fromToken(TextFieldTokens.TextColor),
                    disabledTextColor = fromToken(TextFieldTokens.DisabledTextColor),
                    placeholderColor = fromToken(TextFieldTokens.PlaceholderColor),
                    backgroundColor = fromToken(TextFieldTokens.BackgroundColor),
                    disabledBackgroundColor = fromToken(TextFieldTokens.DisabledBackgroundColor),
                    borderColor = fromToken(TextFieldTokens.BorderColor),
                    focusedBorderColor = fromToken(TextFieldTokens.FocusedBorderColor),
                    errorBorderColor = fromToken(TextFieldTokens.ErrorBorderColor),
                    helperColor = fromToken(TextFieldTokens.HelperColor),
                    errorHelperColor = fromToken(TextFieldTokens.ErrorHelperColor),
                    counterColor = fromToken(TextFieldTokens.CounterColor),
                    trailingButtonColor = fromToken(TextFieldTokens.TrailingButtonColor),
                    disabledTrailingButtonColor = fromToken(TextFieldTokens.DisabledTrailingButtonColor),
                    positiveIconColor = fromToken(TextFieldTokens.PositiveIconColor),
                    negativeIconColor = fromToken(TextFieldTokens.NegativeIconColor),
                    disabledIconColor = fromToken(TextFieldTokens.DisabledIconColor),
                    clearIconColor = fromToken(TextFieldTokens.ClearIconColor),
                ).also { defaultTextFieldColorsCached = it }
}

/**
 * [MinoTextInput]·[MinoTextArea]의 상태별 색. 슬롯이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 *
 * 상태 우선순위(Figma): Disable > Negative > Focus > 기본. Negative는 포커스 여부와 무관하게 빨강 유지.
 */
@Immutable
class MinoTextFieldColors(
    val labelColor: Color,
    val disabledLabelColor: Color,
    val textColor: Color,
    val disabledTextColor: Color,
    val placeholderColor: Color,
    val backgroundColor: Color,
    val disabledBackgroundColor: Color,
    val borderColor: Color,
    val focusedBorderColor: Color,
    val errorBorderColor: Color,
    val helperColor: Color,
    val errorHelperColor: Color,
    val counterColor: Color,
    val trailingButtonColor: Color,
    val disabledTrailingButtonColor: Color,
    val positiveIconColor: Color,
    val negativeIconColor: Color,
    val disabledIconColor: Color,
    val clearIconColor: Color,
) {
    fun copy(
        labelColor: Color = this.labelColor,
        disabledLabelColor: Color = this.disabledLabelColor,
        textColor: Color = this.textColor,
        disabledTextColor: Color = this.disabledTextColor,
        placeholderColor: Color = this.placeholderColor,
        backgroundColor: Color = this.backgroundColor,
        disabledBackgroundColor: Color = this.disabledBackgroundColor,
        borderColor: Color = this.borderColor,
        focusedBorderColor: Color = this.focusedBorderColor,
        errorBorderColor: Color = this.errorBorderColor,
        helperColor: Color = this.helperColor,
        errorHelperColor: Color = this.errorHelperColor,
        counterColor: Color = this.counterColor,
        trailingButtonColor: Color = this.trailingButtonColor,
        disabledTrailingButtonColor: Color = this.disabledTrailingButtonColor,
        positiveIconColor: Color = this.positiveIconColor,
        negativeIconColor: Color = this.negativeIconColor,
        disabledIconColor: Color = this.disabledIconColor,
        clearIconColor: Color = this.clearIconColor,
    ): MinoTextFieldColors =
        MinoTextFieldColors(
            labelColor = labelColor.takeOrElse { this.labelColor },
            disabledLabelColor = disabledLabelColor.takeOrElse { this.disabledLabelColor },
            textColor = textColor.takeOrElse { this.textColor },
            disabledTextColor = disabledTextColor.takeOrElse { this.disabledTextColor },
            placeholderColor = placeholderColor.takeOrElse { this.placeholderColor },
            backgroundColor = backgroundColor.takeOrElse { this.backgroundColor },
            disabledBackgroundColor = disabledBackgroundColor.takeOrElse { this.disabledBackgroundColor },
            borderColor = borderColor.takeOrElse { this.borderColor },
            focusedBorderColor = focusedBorderColor.takeOrElse { this.focusedBorderColor },
            errorBorderColor = errorBorderColor.takeOrElse { this.errorBorderColor },
            helperColor = helperColor.takeOrElse { this.helperColor },
            errorHelperColor = errorHelperColor.takeOrElse { this.errorHelperColor },
            counterColor = counterColor.takeOrElse { this.counterColor },
            trailingButtonColor = trailingButtonColor.takeOrElse { this.trailingButtonColor },
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

    @Stable
    internal fun borderColor(
        enabled: Boolean,
        status: MinoTextFieldStatus,
        focused: Boolean,
    ): Color =
        when {
            !enabled -> borderColor
            status == MinoTextFieldStatus.Negative -> errorBorderColor
            focused -> focusedBorderColor
            else -> borderColor
        }

    @Stable
    internal fun helperColor(
        enabled: Boolean,
        status: MinoTextFieldStatus,
    ): Color = if (enabled && status == MinoTextFieldStatus.Negative) errorHelperColor else helperColor

    @Stable
    internal fun trailingButtonColor(enabled: Boolean): Color =
        if (enabled) trailingButtonColor else disabledTrailingButtonColor

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
        if (other !is MinoTextFieldColors) return false

        if (labelColor != other.labelColor) return false
        if (disabledLabelColor != other.disabledLabelColor) return false
        if (textColor != other.textColor) return false
        if (disabledTextColor != other.disabledTextColor) return false
        if (placeholderColor != other.placeholderColor) return false
        if (backgroundColor != other.backgroundColor) return false
        if (disabledBackgroundColor != other.disabledBackgroundColor) return false
        if (borderColor != other.borderColor) return false
        if (focusedBorderColor != other.focusedBorderColor) return false
        if (errorBorderColor != other.errorBorderColor) return false
        if (helperColor != other.helperColor) return false
        if (errorHelperColor != other.errorHelperColor) return false
        if (counterColor != other.counterColor) return false
        if (trailingButtonColor != other.trailingButtonColor) return false
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
            textColor,
            disabledTextColor,
            placeholderColor,
            backgroundColor,
            disabledBackgroundColor,
            borderColor,
            focusedBorderColor,
            errorBorderColor,
            helperColor,
            errorHelperColor,
            counterColor,
            trailingButtonColor,
            disabledTrailingButtonColor,
            positiveIconColor,
            negativeIconColor,
            disabledIconColor,
            clearIconColor,
        ).contentHashCode()
}

/**
 * TextInput/TextArea 오른쪽에 붙는 "텍스트" 트레일링 버튼. 두 컴포넌트 공통.
 */
@Composable
internal fun MinoTextFieldTrailingButton(
    label: String,
    enabled: Boolean,
    colors: MinoTextFieldColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        modifier = modifier
            .clip(TextFieldTokens.TrailingButtonShape.value)
            .rippleSingleClickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = TextFieldTokens.TrailingButtonHorizontalPadding,
                vertical = TextFieldTokens.TrailingButtonVerticalPadding,
            ),
        style = TextFieldTokens.TrailingButtonFont.value,
        color = colors.trailingButtonColor(enabled = enabled),
    )
}
