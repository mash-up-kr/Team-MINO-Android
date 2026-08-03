package team.mino.core.designsystem.component.actionarea.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.shape.token.AtomicShapeToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Action Area 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Action Area/Action Area`(컴포넌트셋 16215:35682)와, 그 안에 인스턴스로 들어가는
 * `Action Area/Resource/Actions`(16215:35697) 실측값 기준.
 */
internal object ActionAreaTokens {
    val ContainerPadding = PaddingValues(20.dp)

    /** 캡션과 액션 묶음 사이 간격(Figma `Container`의 gap). */
    val ContainerSpacing = 16.dp

    /** 액션을 세로로 쌓을 때의 간격(Figma `Contents`·`Actions`의 gap). */
    val ActionColumnSpacing = 8.dp

    /** 액션을 가로로 늘어놓을 때의 간격(Figma `Variant=Neutral`의 `Contents` gap). 세로와 값이 다르다. */
    val ActionRowSpacing = 12.dp

    // Figma는 Sub Action을 높이 44 프레임에 담고 그 안에 28 높이 버튼을 세로 가운데 둔다. 위아래로
    // 남는 8dp가 [ActionColumnSpacing]에 더해져, 텍스트 버튼만 다른 액션보다 넓은 여백을 갖는다.
    val SubActionVerticalPadding = 8.dp

    val CaptionFont = TypographyAccessKeyToken.Label2Regular
    val CaptionColor = ColorAccessKeyToken.LabelAlternative

    // 배경은 sticky이거나 extra가 있을 때만 존재한다. Figma에서 sticky 쪽 배경은 `Background`
    // 프레임이 `Sticky` 속성에 묶여 있고, extra 쪽은 `Contents`가 자체 배경을 깐다. 둘 다 콘텐츠
    // 위에 떠 있는 표면이라 Normal이 아니라 Elevated를 쓴다.
    val SurfaceColor = ColorAccessKeyToken.BackgroundElevatedNormal

    /** sticky 배경 위에 얹히는 페이드 높이(Figma `Background` > `Mask` 프레임). */
    val StickyGradientHeight = 20.dp

    /** extra 영역이 붙으면 상단 모서리가 둥글어진다(Figma `Extra=True` 루트). */
    val ExtraShape: Shape = RoundedCornerShape(
        topStart = AtomicShapeToken.CornerMedium,
        topEnd = AtomicShapeToken.CornerMedium,
    )

    /**
     * extra 콘텐츠 여백. 위·좌우는 `Margin/Content/Content`(20), 아래는
     * `Margin/Action/Contents Bottom`(4)이다. 아래가 좁은 것은 이어지는 액션 컨테이너가
     * 자기 상단 패딩 20을 따로 갖기 때문이다(합쳐서 24).
     */
    val ExtraPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp)

    val DividerColor = ColorAccessKeyToken.LineNormalNeutral
    val DividerThickness = 1.dp

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
