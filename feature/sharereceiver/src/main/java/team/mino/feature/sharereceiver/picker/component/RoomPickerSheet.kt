package team.mino.feature.sharereceiver.picker.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.singleClickable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.sharereceiver.picker.model.SheetStep
import kotlin.math.roundToInt

/**
 * 딤 배경 위에 하단 정렬로 떠 있는 방 선택 시트의 껍데기. 헤더·목록·액션 영역은 [content]로 받는다.
 *
 * 시트는 두 단계 사이를 끌어 옮길 수 있고, 그 아래로 더 끌면 닫힌다(EC-001). 단계별 높이는 콘텐츠와
 * 무관한 고정값이라 방이 몇 개든 단계 구성이 같다(EC-005 · TS-020) — 방이 적으면 [content] 안에서
 * 남는 자리가 빌 뿐이다. 시트가 늘고 줄 때 그 차이를 흡수하는 것은 `ColumnScope.weight`를 쓴 [content]의
 * 몫이고, 위아래 고정 영역은 자기 높이를 지킨다.
 *
 * **단계를 소유하지 않는다.** 끌어 옮긴 결과는 [onStepChange]로 올리고, 어떤 단계로 서 있을지는 [step]으로
 * 받는다. 열림·닫힘도 마찬가지로 갖지 않는다 — 이 컴포저블이 그려지는 동안은 시트가 떠 있는 것이고,
 * 걷어내는 판단은 호스트(Activity)가 한다.
 *
 * @param onDismissRequest 딤 영역 탭과 시트를 끝까지 끌어내린 결과가 올라온다. 저장 없이 닫는 경로이며,
 *   종료 자체는 호스트가 한다.
 * @param step 시트가 서 있어야 할 단계.
 * @param roomCount 넓힌 단계의 높이를 가르는 방 개수. 목록 자체는 [content]가 그리므로 시트는 개수만 안다.
 * @param onStepChange 끌어 옮긴 시트가 멈춰 선 단계가 올라온다.
 */
@Composable
internal fun RoomPickerSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    step: SheetStep = SheetStep.PEEK,
    roomCount: Int = 0,
    onStepChange: (SheetStep) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val expandedHeightPx = with(density) { expandedHeightFor(roomCount).toPx() }
    val anchors = remember(expandedHeightPx, density) {
        val peekHeightPx = with(density) { PeekHeight.toPx() }
        DraggableAnchors {
            SheetAnchor.EXPANDED at 0f
            SheetAnchor.PEEK at expandedHeightPx - peekHeightPx
            SheetAnchor.GONE at expandedHeightPx
        }
    }
    val dragState = remember {
        AnchoredDraggableState(initialValue = step.toAnchor(), anchors = anchors)
    }
    // 앵커가 레이아웃 크기가 아니라 방 개수에서 나오므로, 개수가 바뀌면 선 단계를 지킨 채 자리만 옮긴다.
    SideEffect { dragState.updateAnchors(anchors, dragState.targetValue) }

    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val currentOnStepChange by rememberUpdatedState(onStepChange)
    LaunchedEffect(dragState) {
        snapshotFlow { dragState.settledValue }
            // 처음 선 자리는 호출부가 준 것이라 되돌려 보내지 않는다.
            .drop(1)
            .collect { anchor ->
                when (anchor) {
                    SheetAnchor.GONE -> currentOnDismissRequest()
                    SheetAnchor.PEEK -> currentOnStepChange(SheetStep.PEEK)
                    SheetAnchor.EXPANDED -> currentOnStepChange(SheetStep.FULL)
                }
            }
    }
    LaunchedEffect(step) {
        val target = step.toAnchor()
        if (dragState.settledValue != target) {
            dragState.animateTo(target)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.materialDimmer)
                .singleClickable(onClick = onDismissRequest),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .anchoredSheetHeight(state = dragState, expandedHeightPx = expandedHeightPx)
                .surface(shape = ContainerShape, containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal)
                .anchoredDraggable(state = dragState, orientation = Orientation.Vertical)
                // 시트가 히트 테스트에 잡혀야 그 위의 탭이 뒤의 딤으로 내려가 닫히지 않는다.
                .pointerInput(Unit) {},
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RoomPickerSheetHandle()
            content()
        }
    }
}

/**
 * 시트 맨 위의 손잡이. 끄는 것을 받는 것은 시트 전체이고, 이 표식은 그 자리를 알린다.
 */
@Composable
private fun RoomPickerSheetHandle(modifier: Modifier = Modifier) {
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
 * 끌린 거리만큼 위를 잘라낸 높이로 세운다. 하단 정렬이라 아래 고정 영역은 화면 밑에 붙어 있고, 줄어드는
 * 쪽은 늘 위다.
 */
private fun Modifier.anchoredSheetHeight(
    state: AnchoredDraggableState<SheetAnchor>,
    expandedHeightPx: Float,
): Modifier =
    layout { measurable, constraints ->
        val height = constraints.constrainHeight((expandedHeightPx - state.requireOffset()).roundToInt())
        val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
        layout(placeable.width, height) { placeable.place(x = 0, y = 0) }
    }

/**
 * 시트가 멈춰 서는 자리. [SheetStep]과 달리 화면 밖([GONE])까지 포함한다 — 아래로 끝까지 끌면 닫히는 경로가
 * 앵커 하나로 표현되어야 하기 때문이다.
 */
private enum class SheetAnchor {
    GONE,
    PEEK,
    EXPANDED,
}

private fun SheetStep.toAnchor(): SheetAnchor =
    when (this) {
        SheetStep.PEEK -> SheetAnchor.PEEK
        SheetStep.FULL -> SheetAnchor.EXPANDED
    }

private fun expandedHeightFor(roomCount: Int): Dp =
    if (roomCount <= COMPACT_EXPANDED_ROOM_COUNT) CompactExpandedHeight else TallExpandedHeight

private const val COMPACT_EXPANDED_ROOM_COUNT = 4

private val PeekHeight = 436.dp

private val CompactExpandedHeight = 612.dp

private val TallExpandedHeight = 644.dp

private val ContainerShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

private val HandleContainerHeight = 30.dp

private val HandleWidth = 38.dp

private val HandleHeight = 4.dp

private val HandleShape = RoundedCornerShape(4.dp)

@UiModePreviews
@Composable
private fun RoomPickerSheetPeekPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        RoomPickerSheet(onDismissRequest = {}, modifier = modifier, roomCount = 4) {}
    }
}

@UiModePreviews
@Composable
private fun RoomPickerSheetExpandedPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        RoomPickerSheet(
            onDismissRequest = {},
            modifier = modifier,
            step = SheetStep.FULL,
            roomCount = 4,
        ) {}
    }
}

@UiModePreviews
@Composable
private fun RoomPickerSheetExpandedTallPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        RoomPickerSheet(
            onDismissRequest = {},
            modifier = modifier,
            step = SheetStep.FULL,
            roomCount = 5,
        ) {}
    }
}
