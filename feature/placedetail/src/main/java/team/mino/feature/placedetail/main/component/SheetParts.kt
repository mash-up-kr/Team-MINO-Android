package team.mino.feature.placedetail.main.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.surface.surface

/*
 * 두 시트(장소 상세·방 공유)가 함께 쓰는 조각들.
 *
 * 손잡이·구분선·모서리는 어느 시트냐와 무관하게 같은 값이어야 한다. 시트마다 한 벌씩 두면 디자인이 손잡이
 * 치수나 모서리를 바꿀 때 한쪽만 따라가고, 같은 화면 안에서 두 시트가 서로 다른 모양이 된다.
 *
 * 이 모듈 밖으로는 올리지 않는다 — 세 번째 소비자가 다른 feature에서 나오면 그때
 * [`component-asset-placement.md`](../../../../../../../../../../docs/conventions/component-asset-placement.md)
 * §2.1이 승격 자리를 정한다.
 */

/** 시트 컨테이너의 위쪽 모서리. 두 시트가 같은 곡률로 올라온다. */
internal val SheetContainerShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

/** 시트 안 구분선의 굵기. 코멘트 목록의 항목 사이 선도 같은 값을 쓴다. */
internal val SheetDividerThickness: Dp = 1.dp

/** 시트 맨 위의 손잡이. 끄는 것을 받는 것은 시트 전체이고, 이 표식은 그 자리를 알린다. */
@Composable
internal fun SheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HandleContainerHeight),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            modifier = Modifier
                .size(width = HandleWidth, height = HandleHeight)
                .surface(shape = HandleShape, containerColor = MinoAndroidTheme.colors.fillNormal),
        )
    }
}

/**
 * 시트 안 구역을 가르는 선. 선은 한 줄이고 위아래 여백은 그것을 담은 띠가 낸다.
 *
 * [horizontalPadding]은 선을 좌우로 물리는 폭이다. 콘텐츠 폭에 맞춰 선을 줄이는 시트가 있어 열어 둔다.
 */
@Composable
internal fun SheetSectionDivider(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 0.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(DividerBandHeight)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalDivider(
            thickness = SheetDividerThickness,
            color = MinoAndroidTheme.colors.lineNormalNormal,
        )
    }
}

private val HandleContainerHeight = 30.dp

private val HandleWidth = 38.dp

private val HandleHeight = 4.dp

private val HandleShape = RoundedCornerShape(4.dp)

private val DividerBandHeight = 12.dp
