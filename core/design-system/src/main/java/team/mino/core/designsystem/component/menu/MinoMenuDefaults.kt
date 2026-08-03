package team.mino.core.designsystem.component.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import team.mino.core.designsystem.component.menu.token.MenuTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.shadow.MinoShadow
import team.mino.core.designsystem.foundation.shadow.token.value
import team.mino.core.designsystem.foundation.shape.token.value
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoMenu]·[MinoMenuItem]의 기본값 모음.
 */
object MinoMenuDefaults {
    /** 메뉴 컨테이너 기본 셰이프. */
    val shape: Shape
        @Composable @ReadOnlyComposable get() = MenuTokens.ContainerShape.value

    /** 메뉴 컨테이너 기본 배경색. */
    val containerColor: Color
        @Composable @ReadOnlyComposable get() = MenuTokens.ContainerColor.value

    /** 메뉴 컨테이너 기본 보더. */
    val border: BorderStroke
        @Composable @ReadOnlyComposable get() =
            BorderStroke(
                width = MenuTokens.ContainerBorderWidth,
                color = MenuTokens.ContainerBorderColor.value,
            )

    /** 메뉴 컨테이너 기본 그림자. */
    val shadow: MinoShadow
        @Composable @ReadOnlyComposable get() = MenuTokens.ContainerShadow.value

    /** 메뉴 아이템 기본 세로 패딩 (Figma Vertical Padding=12px). */
    val ItemContentPadding: PaddingValues = PaddingValues(vertical = MenuTokens.ItemVerticalPadding)

    /** 메뉴 아이템 축소 세로 패딩 (Figma Vertical Padding=8px). */
    val ItemContentPaddingCompact: PaddingValues =
        PaddingValues(vertical = MenuTokens.ItemVerticalPaddingCompact)

    /** [MinoMenuItem]의 기본 [MinoMenuItemColors]. */
    @Composable
    @ReadOnlyComposable
    fun itemColors(): MinoMenuItemColors = MinoAndroidTheme.colors.defaultMenuItemColors

    /**
     * 기본값에서 일부만 바꾼 [MinoMenuItemColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun itemColors(
        textColor: Color = Color.Unspecified,
        activeTextColor: Color = Color.Unspecified,
        captionColor: Color = Color.Unspecified,
        disabledTextColor: Color = Color.Unspecified,
        disabledCaptionColor: Color = Color.Unspecified,
        activeControlColor: Color = Color.Unspecified,
        controlBorderColor: Color = Color.Unspecified,
        controlIconColor: Color = Color.Unspecified,
    ): MinoMenuItemColors =
        MinoAndroidTheme.colors.defaultMenuItemColors.copy(
            textColor = textColor,
            activeTextColor = activeTextColor,
            captionColor = captionColor,
            disabledTextColor = disabledTextColor,
            disabledCaptionColor = disabledCaptionColor,
            activeControlColor = activeControlColor,
            controlBorderColor = controlBorderColor,
            controlIconColor = controlIconColor,
        )

    internal val ColorScheme.defaultMenuItemColors: MinoMenuItemColors
        get() =
            defaultMenuItemColorsCached
                ?: MinoMenuItemColors(
                    textColor = fromToken(MenuTokens.LabelColor),
                    activeTextColor = fromToken(MenuTokens.ActiveLabelColor),
                    captionColor = fromToken(MenuTokens.CaptionColor),
                    disabledTextColor = fromToken(MenuTokens.DisabledLabelColor),
                    disabledCaptionColor = fromToken(MenuTokens.CaptionColor),
                    activeControlColor = fromToken(MenuTokens.ControlActiveColor),
                    controlBorderColor = fromToken(MenuTokens.ControlBorderColor),
                    controlIconColor = fromToken(MenuTokens.ControlIconColor),
                ).also { defaultMenuItemColorsCached = it }
}

/**
 * [MinoMenuItem]의 상태별 색. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 */
@Immutable
class MinoMenuItemColors(
    val textColor: Color,
    val activeTextColor: Color,
    val captionColor: Color,
    val disabledTextColor: Color,
    val disabledCaptionColor: Color,
    val activeControlColor: Color,
    val controlBorderColor: Color,
    val controlIconColor: Color,
) {
    fun copy(
        textColor: Color = this.textColor,
        activeTextColor: Color = this.activeTextColor,
        captionColor: Color = this.captionColor,
        disabledTextColor: Color = this.disabledTextColor,
        disabledCaptionColor: Color = this.disabledCaptionColor,
        activeControlColor: Color = this.activeControlColor,
        controlBorderColor: Color = this.controlBorderColor,
        controlIconColor: Color = this.controlIconColor,
    ): MinoMenuItemColors =
        MinoMenuItemColors(
            textColor = textColor.takeOrElse { this.textColor },
            activeTextColor = activeTextColor.takeOrElse { this.activeTextColor },
            captionColor = captionColor.takeOrElse { this.captionColor },
            disabledTextColor = disabledTextColor.takeOrElse { this.disabledTextColor },
            disabledCaptionColor = disabledCaptionColor.takeOrElse { this.disabledCaptionColor },
            activeControlColor = activeControlColor.takeOrElse { this.activeControlColor },
            controlBorderColor = controlBorderColor.takeOrElse { this.controlBorderColor },
            controlIconColor = controlIconColor.takeOrElse { this.controlIconColor },
        )

    /** 표식이 선택을 나타내는 변형은 라벨 색이 그대로다 — [MenuItemVariant] 참조. */
    @Stable
    internal fun textColor(
        enabled: Boolean,
        active: Boolean,
        variant: MenuItemVariant,
    ): Color =
        when {
            !enabled -> disabledTextColor
            active && variant.highlightsActiveLabel -> activeTextColor
            else -> textColor
        }

    @Stable
    internal fun captionColor(enabled: Boolean): Color = if (enabled) captionColor else disabledCaptionColor

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoMenuItemColors) return false

        if (textColor != other.textColor) return false
        if (activeTextColor != other.activeTextColor) return false
        if (captionColor != other.captionColor) return false
        if (disabledTextColor != other.disabledTextColor) return false
        if (disabledCaptionColor != other.disabledCaptionColor) return false
        if (activeControlColor != other.activeControlColor) return false
        if (controlBorderColor != other.controlBorderColor) return false
        if (controlIconColor != other.controlIconColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            textColor,
            activeTextColor,
            captionColor,
            disabledTextColor,
            disabledCaptionColor,
            activeControlColor,
            controlBorderColor,
            controlIconColor,
        ).contentHashCode()
}
