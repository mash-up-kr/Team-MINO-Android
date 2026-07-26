package team.mino.core.designsystem.component.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import team.mino.core.designsystem.component.button.token.ButtonTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoButton]의 기본값 모음.
 */
object MinoButtonDefaults {
    /** [MinoButton]의 기본 [MinoButtonColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(): MinoButtonColors = MinoAndroidTheme.colors.defaultButtonColors

    /**
     * 기본값에서 일부만 바꾼 [MinoButtonColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        containerColor: Color = Color.Unspecified,
        mainContainerColor: Color = Color.Unspecified,
        mainContentColor: Color = Color.Unspecified,
        subContentColor: Color = Color.Unspecified,
        subBorderColor: Color = Color.Unspecified,
        alternativeContentColor: Color = Color.Unspecified,
        alternativeBorderColor: Color = Color.Unspecified,
    ): MinoButtonColors =
        MinoAndroidTheme.colors.defaultButtonColors.copy(
            containerColor = containerColor,
            mainContainerColor = mainContainerColor,
            mainContentColor = mainContentColor,
            subContentColor = subContentColor,
            subBorderColor = subBorderColor,
            alternativeContentColor = alternativeContentColor,
            alternativeBorderColor = alternativeBorderColor,
        )

    internal val ColorScheme.defaultButtonColors: MinoButtonColors
        get() =
            defaultButtonColorsCached
                ?: MinoButtonColors(
                    containerColor = fromToken(ButtonTokens.ContainerColor),
                    mainContainerColor = fromToken(ButtonTokens.MainContainerColor),
                    mainContentColor = fromToken(ButtonTokens.MainContentColor),
                    subContentColor = fromToken(ButtonTokens.SubContentColor),
                    subBorderColor = fromToken(ButtonTokens.SubBorderColor),
                    alternativeContentColor = fromToken(ButtonTokens.AlternativeContentColor),
                    alternativeBorderColor = fromToken(ButtonTokens.AlternativeBorderColor),
                ).also { defaultButtonColorsCached = it }
}

/**
 * [MinoButton]의 버튼·컨테이너 슬롯별 색. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 * 비활성(disabled) 상태는 별도 색 슬롯 대신 컴포넌트 쪽에서 알파를 낮춰 표현한다.
 */
@Immutable
class MinoButtonColors(
    val containerColor: Color,
    val mainContainerColor: Color,
    val mainContentColor: Color,
    val subContentColor: Color,
    val subBorderColor: Color,
    val alternativeContentColor: Color,
    val alternativeBorderColor: Color,
) {
    fun copy(
        containerColor: Color = this.containerColor,
        mainContainerColor: Color = this.mainContainerColor,
        mainContentColor: Color = this.mainContentColor,
        subContentColor: Color = this.subContentColor,
        subBorderColor: Color = this.subBorderColor,
        alternativeContentColor: Color = this.alternativeContentColor,
        alternativeBorderColor: Color = this.alternativeBorderColor,
    ): MinoButtonColors =
        MinoButtonColors(
            containerColor = containerColor.takeOrElse { this.containerColor },
            mainContainerColor = mainContainerColor.takeOrElse { this.mainContainerColor },
            mainContentColor = mainContentColor.takeOrElse { this.mainContentColor },
            subContentColor = subContentColor.takeOrElse { this.subContentColor },
            subBorderColor = subBorderColor.takeOrElse { this.subBorderColor },
            alternativeContentColor = alternativeContentColor.takeOrElse { this.alternativeContentColor },
            alternativeBorderColor = alternativeBorderColor.takeOrElse { this.alternativeBorderColor },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoButtonColors) return false

        if (containerColor != other.containerColor) return false
        if (mainContainerColor != other.mainContainerColor) return false
        if (mainContentColor != other.mainContentColor) return false
        if (subContentColor != other.subContentColor) return false
        if (subBorderColor != other.subBorderColor) return false
        if (alternativeContentColor != other.alternativeContentColor) return false
        if (alternativeBorderColor != other.alternativeBorderColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            containerColor,
            mainContainerColor,
            mainContentColor,
            subContentColor,
            subBorderColor,
            alternativeContentColor,
            alternativeBorderColor,
        ).contentHashCode()
}
