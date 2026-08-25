package team.mino.core.designsystem.component.roomcolorchip.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.designsystem.foundation.color.token.AtomicColorToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.shadow.MinoShadow
import team.mino.core.designsystem.foundation.shadow.token.AtomicShadowToken

/**
 * Room Color Chip 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 */
internal object RoomColorChipTokens {
    /** 칩 도형의 한 변. 선택 여부와 무관하게 같다. */
    val Size = 70.dp

    val Shape: Shape = RoundedCornerShape(20.dp)

    /** 미선택 상태에만 그려지는 테두리의 두께. */
    val BorderWidth = 3.dp

    /** 선택 상태에 얹히는 체크 아이콘의 한 변. */
    val CheckIconSize = 28.dp

    val SelectedContentColor = ColorAccessKeyToken.StaticWhite

    /** 선택 여부와 무관하게 같은 그림자가 깔린다. */
    val ContainerShadow = MinoShadow(
        layers = listOf(
            shadowLayer(offsetY = 4.667.dp, blurRadius = 7.dp, spread = (-2.333).dp),
            shadowLayer(offsetY = 11.667.dp, blurRadius = 17.5.dp, spread = (-3.5).dp),
        ),
    )
}

private fun shadowLayer(
    offsetY: Dp,
    blurRadius: Dp,
    spread: Dp,
): Shadow =
    Shadow(
        radius = blurRadius,
        color = AtomicShadowToken.ShadowColor,
        spread = spread,
        offset = DpOffset(x = 0.dp, y = offsetY),
        alpha = AtomicShadowToken.Alpha7,
    )

// 색당 한 번만 만들어 재사용한다. when으로 매 호출마다 새로 만들지 않는다.
private val ContainerColorByRoomColor = mapOf(
    MinoRoomColor.Red to AtomicColorToken.Red60,
    MinoRoomColor.RedOrange to AtomicColorToken.RedOrange70,
    MinoRoomColor.Orange to AtomicColorToken.Orange70,
    MinoRoomColor.Lime to AtomicColorToken.Lime80,
    MinoRoomColor.Green to AtomicColorToken.Green90,
    MinoRoomColor.Cyan to AtomicColorToken.Cyan90,
    MinoRoomColor.Violet to AtomicColorToken.Violet80,
    MinoRoomColor.Pink to AtomicColorToken.Pink90,
    MinoRoomColor.Blue to AtomicColorToken.Blue65,
    MinoRoomColor.Brown to Color(0xFFDBA679),
    MinoRoomColor.LightBlue to AtomicColorToken.LightBlue60,
    MinoRoomColor.Purple to AtomicColorToken.Purple70,
)

private val BorderColorByRoomColor = mapOf(
    MinoRoomColor.Red to AtomicColorToken.Red40,
    MinoRoomColor.RedOrange to AtomicColorToken.RedOrange40,
    MinoRoomColor.Orange to AtomicColorToken.Orange40,
    MinoRoomColor.Lime to AtomicColorToken.Lime37,
    MinoRoomColor.Green to AtomicColorToken.Green40,
    MinoRoomColor.Cyan to AtomicColorToken.Cyan40,
    MinoRoomColor.Violet to AtomicColorToken.Violet50,
    MinoRoomColor.Pink to AtomicColorToken.Pink60,
    MinoRoomColor.Blue to AtomicColorToken.Blue40,
    MinoRoomColor.Brown to Color(0xFFB96013),
    MinoRoomColor.LightBlue to AtomicColorToken.LightBlue40,
    MinoRoomColor.Purple to AtomicColorToken.Purple40,
)

private val SelectedContainerColorByRoomColor = mapOf(
    MinoRoomColor.Red to AtomicColorToken.Red50,
    MinoRoomColor.RedOrange to AtomicColorToken.RedOrange50,
    MinoRoomColor.Orange to AtomicColorToken.Orange50,
    MinoRoomColor.Lime to AtomicColorToken.Lime50,
    MinoRoomColor.Green to AtomicColorToken.Green70,
    MinoRoomColor.Cyan to AtomicColorToken.Cyan50,
    MinoRoomColor.Violet to AtomicColorToken.Violet50,
    MinoRoomColor.Pink to AtomicColorToken.Pink50,
    MinoRoomColor.Blue to AtomicColorToken.Blue50,
    MinoRoomColor.Brown to Color(0xFFB96013),
    MinoRoomColor.LightBlue to AtomicColorToken.LightBlue50,
    MinoRoomColor.Purple to AtomicColorToken.Purple50,
)

/** 미선택 상태의 채움색. */
internal val MinoRoomColor.containerColor: Color
    get() = ContainerColorByRoomColor.getValue(this)

/** 미선택 상태의 테두리색. 선택 상태에는 테두리가 없다. */
internal val MinoRoomColor.borderColor: Color
    get() = BorderColorByRoomColor.getValue(this)

/** 선택 상태의 채움색. 색마다 미선택 채움과 다른 슬롯을 쓴다. */
internal val MinoRoomColor.selectedContainerColor: Color
    get() = SelectedContainerColorByRoomColor.getValue(this)
