package team.mino.core.designsystem.component.actionarea.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken

/**
 * Action Area 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Action Area/Action Area`(컴포넌트셋 16215:35682) 실측값 기준.
 */
internal object ActionAreaTokens {
    val ContainerPadding = PaddingValues(20.dp)

    /** Sub Action과 메인 액션을 가로로 놓을 때의 간격. */
    val ActionRowSpacing = 12.dp

    /** Alternative Action과 메인 액션을 세로로 쌓을 때의 간격. */
    val ActionColumnSpacing = 8.dp

    // 배경은 sticky일 때만 존재한다. Figma에서 배경을 그리는 `Background` 프레임이 `Sticky` 속성에
    // 묶여 있고, 그 안의 Solid가 Elevated 계열을 쓴다(떠 있는 표면이라 Normal이 아니다).
    val StickyContainerColor = ColorAccessKeyToken.BackgroundElevatedNormal

    /** sticky 배경 위에 얹히는 페이드 높이(Figma `Background` > `Mask` 프레임). */
    val StickyGradientHeight = 20.dp

    /**
     * sticky 페이드의 알파 램프(투명 → 불투명). Figma `Gradient/Solid`(16215:17121)는 개발 코멘트로
     * `Gradient Ease: 0.25, 0.1, 0.25, 1`을 명시하고 있어 선형 보간이 아니다. Compose Brush는
     * 정지점 사이를 선형으로만 잇기 때문에, Figma가 이징을 풀어 내보낸 정지점을 그대로 옮겨 둔다.
     */
    val StickyGradientAlphaStops = arrayOf(
        0.00f to 0.00f,
        0.14f to 0.14f,
        0.26f to 0.27f,
        0.37f to 0.38f,
        0.46f to 0.48f,
        0.54f to 0.57f,
        0.60f to 0.65f,
        0.66f to 0.71f,
        0.71f to 0.77f,
        0.76f to 0.82f,
        0.80f to 0.86f,
        0.83f to 0.90f,
        0.87f to 0.93f,
        0.91f to 0.96f,
        0.95f to 0.98f,
        1.00f to 1.00f,
    )
}
