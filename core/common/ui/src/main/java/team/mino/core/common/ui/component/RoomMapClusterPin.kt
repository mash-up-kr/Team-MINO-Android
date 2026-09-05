package team.mino.core.common.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.designsystem.component.roomcolorchip.representativeColor

/**
 * 지도에서 같은 방(색)의 핀이 겹칠 때 [RoomMapPin] 대신 그리는 클러스터 배지(PRD Flow C
 * "지도 축소 시: 핀이 겹치는 구간을 클러스터로 묶는다", Figma node `2392-128633`~`2392-128646`).
 *
 * 원 색은 방 대표색 팔레트(`RoomColor` 12색)와 1:1로 대응한다 — [color]가 `null`이면(개인 방 등
 * 색 미선택) 기본 회색을 쓴다. 클러스터는 항상 같은 방(색)의 핀끼리만 묶이므로([MapPinCluster]
 * KDoc), 이 컴포저블은 색 하나만 받는다.
 *
 * [count]는 2 이상일 때만 의미가 있다 — 1개는 [RoomMapPin]으로 그리는 것이 호출부([RoomListMap])
 * 책임이다. 100 미만은 실제 개수 뒤에 `+`(예: `23+`), 100 이상은 정확한 개수 대신 고정 `100+`로
 * 표시한다(`docs/prd/business-context.md` Flow C — "1~99개는 `50+`식 숫자 표기, 100개 이상은
 * `100+`").
 *
 * [TBD] 원 지름·테두리 두께·폰트 크기는 Figma에서 스크린샷 스케일 값만 확인돼(정확한 dev-mode px
 * 미확보) 임의로 고른 값이다 — 리드 확인 후 [RoomMapClusterPinTokens]만 고치면 된다.
 */
@Composable
fun RoomMapClusterPin(
    color: MinoRoomColor?,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(RoomMapClusterPinTokens.Diameter)
            .background(color = color.toClusterColor(), shape = CircleShape)
            .border(width = RoomMapClusterPinTokens.BorderWidth, color = Color.White, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toClusterLabel(),
            color = Color.White,
            fontSize = RoomMapClusterPinTokens.FontSize,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** `{count}+`(99 이하) 또는 `100+`(100 이상, 정확한 개수를 보이지 않는다). */
private fun Int.toClusterLabel(): String = if (this >= RoomMapClusterPinTokens.MAX_EXACT_COUNT) "100+" else "$this+"

private fun MinoRoomColor?.toClusterColor(): Color = this?.representativeColor ?: RoomMapClusterPinTokens.Default

/** [RoomMapClusterPin] 치수 토큰. 색은 [representativeColor]를 그대로 쓴다(값의 단일 출처). */
private object RoomMapClusterPinTokens {
    val Diameter = 44.dp
    val BorderWidth = 3.dp
    val FontSize = 14.sp
    const val MAX_EXACT_COUNT = 100

    /** 색을 고르지 않은 방(개인 방 등) 클러스터 — [RoomMapPin]의 기본(검정) 핀과 같은 이유. */
    val Default = Color(0xFF171719)
}
