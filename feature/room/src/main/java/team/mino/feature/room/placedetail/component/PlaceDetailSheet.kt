package team.mino.feature.room.placedetail.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.room.component.SheetContainerShape
import team.mino.feature.room.component.SheetDragHandle
import team.mino.feature.room.placedetail.model.PlaceSheetLevel
import kotlin.math.roundToInt

/**
 * 지도 위에 겹쳐 서는 장소 상세 시트의 껍데기. 헤더·액션 행·캐러셀·코멘트는 [content]로 받는다.
 *
 * **멈춰 서는 자리는 둘뿐이다.** 끌어 옮긴 시트는 `HALF` 아니면 `FULL`에 선다 — 중간에 머무는 자리를 만들지 않아
 * 조금만 끌었다 놓으면 원래 단계로 되돌아온다(spec FR-001 · TS-015).
 *
 * **`HALF`가 하한이다.** 아래로 아무리 끌어도 369dp 밑으로 내려가지 않고 닫히지도 않는다(spec FR-001 · EC-003 ·
 * TS-015). 그래서 이 시트는 자기가 사라지는 경로를 갖지 않는다 — 장소 상세를 벗어나는 길은 [나가기]와 시스템
 * 뒤로가기뿐이고, 둘 다 시트 밖에서 처리된다.
 *
 * **단계를 소유하지 않는다.** 멈춰 선 결과는 [onLevelChange]로 올리고, 어느 단계로 서 있을지는 [level]로 받는다.
 *
 * **높이는 [level]만 보고 정한다.** 장소명·주소의 길이, 대표 이미지 장수, 코멘트 건수 어느 것도 이 높이에
 * 관여하지 않는다(spec SC-002) — 콘텐츠가 남거나 모자라는 차이는 [content] 안에서 흡수한다. `HALF`가 화면을 다
 * 덮지 않아 그 위로 지도와 선택 핀이 계속 보이고(spec UX-001), `FULL`은 놓인 자리를 끝까지 채운다. 그래서
 * **높이가 유계인 자리에 놓여야 한다** — 무계면 `FULL`이 설 자리를 알 수 없어 `HALF` 높이로 선다.
 *
 * **[content]는 스크롤 축 하나를 공유한다.** 그 축을 세우는 것이 이 시트이므로 안에서 세로 스크롤 컨테이너를 또
 * 만들지 않는다 — 코멘트 목록도 자기 스크롤을 갖지 않고 액션 행·캐러셀·입력 영역과 함께 움직인다(spec EC-015).
 * 그 축이 최상단인지가 헤더 밀도를 가르는 유일한 근거라(spec FR-008) 그것을 읽으려는 화면이 [scrollState]를 직접
 * 들고 넘긴다. 콘텐츠가 시트보다 짧으면 스크롤이 일어나지 않아 헤더는 확장형에 머문다(spec EC-007).
 *
 * **[pinnedHeader]는 그 축 밖에 선다.** 위에서 갈린 밀도에 따라 확장형·축소형 중 한 헤더가 스크롤과 무관하게
 * 상단에 남는 자리이며(spec FR-008), 채우지 않으면 콘텐츠가 시트 맨 위에서 시작한다.
 *
 * **헤더의 위쪽 여백은 이 시트가 낸다.** `HALF`에서는 손잡이가, `FULL`에서는 빈 띠가 그 자리를 채우므로, 헤더가
 * 자기 위쪽 여백까지 들면 `HALF`에서 손잡이와 겹쳐 두 번 벌어진다.
 *
 * **`FULL`에는 손잡이도 모서리 굴림도 없다.** 화면을 통째로 덮는 단계라 지도와 맞닿는 시트의 경계 자체가 없다.
 * 상태바 영역도 그 「통째로」에 든다 — 시트가 그 자리를 소유하고 상태바는 시트 배경 위에 얹힌다. 대신 헤더 위
 * 띠가 상태바 높이를 함께 내어 헤더가 아이콘에 가리지 않게 한다.
 *
 * **화면 하단에 붙이는 것은 이 컴포저블이 아니라 화면이 한다.** 시트는 자기 높이만 알고 어디에 놓일지는
 * 모른다 — 지도와 겹치는 배치를 시트가 들면 지도 없이는 그릴 수 없어진다.
 *
 * @param onLevelChange 끌어 옮긴 시트가 멈춰 선 단계가 올라온다.
 * @param level 시트가 서 있어야 할 단계.
 * @param statusBarInset 상태바 높이. **여기서 직접 읽지 않고 받는다** — 인셋을 읽는 자리가 둘이면 한쪽만 0이
 *  되는 순간 값이 갈린다(실기기에서 확인된 결함). 이 값을 시트가 실제로 놓인 자리와 견줘 **상태바에 가리는
 *  만큼만** `FULL` 위쪽 띠에 얹는다 — 시트가 상태바 아래에서 시작하면 0, 화면 최상단부터 서면 상태바 높이가
 *  되므로, 이 화면이 놓이는 자리가 이미 인셋된 자리인지 아닌지를 아무도 가정하지 않아도 된다.
 * @param scrollState [content]가 공유하는 스크롤 축.
 * @param pinnedHeader 스크롤과 무관하게 시트 위쪽에 남는 자리.
 */
@Composable
internal fun PlaceDetailSheet(
    onLevelChange: (PlaceSheetLevel) -> Unit,
    modifier: Modifier = Modifier,
    level: PlaceSheetLevel = PlaceSheetLevel.HALF,
    statusBarInset: Dp = 0.dp,
    scrollState: ScrollState = rememberScrollState(),
    pinnedHeader: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val halfHeightPx = with(LocalDensity.current) { HalfHeight.toPx() }
        val fullHeightPx = if (constraints.hasBoundedHeight) constraints.maxHeight.toFloat() else halfHeightPx
        val anchors = remember(fullHeightPx, halfHeightPx) {
            DraggableAnchors {
                SheetAnchor.FULL at 0f
                SheetAnchor.HALF at (fullHeightPx - halfHeightPx).coerceAtLeast(0f)
            }
        }
        val dragState = remember {
            AnchoredDraggableState(initialValue = level.toAnchor(), anchors = anchors)
        }
        // 앵커가 놓인 자리의 높이에서 나오므로, 그 높이가 바뀌면 선 단계를 지킨 채 자리만 옮긴다.
        SideEffect { dragState.updateAnchors(anchors, dragState.targetValue) }

        val flingBehavior = AnchoredDraggableDefaults.flingBehavior(dragState)
        val nestedScrollConnection = remember(dragState, flingBehavior) {
            sheetNestedScrollConnection(state = dragState, flingBehavior = flingBehavior)
        }

        val currentOnLevelChange by rememberUpdatedState(onLevelChange)
        LaunchedEffect(dragState) {
            snapshotFlow { dragState.settledValue }
                // 처음 선 자리는 호출부가 준 것이라 되돌려 보내지 않는다.
                .drop(1)
                .collect { anchor ->
                    when (anchor) {
                        SheetAnchor.HALF -> currentOnLevelChange(PlaceSheetLevel.HALF)
                        SheetAnchor.FULL -> currentOnLevelChange(PlaceSheetLevel.FULL)
                    }
                }
        }
        LaunchedEffect(level) {
            val target = level.toAnchor()
            if (dragState.settledValue != target) {
                dragState.animateTo(target)
            }
        }

        val containerShape = level.containerShape()
        // 시트 윗변이 창의 어디에 있는지. 이것과 [statusBarInset]의 차이가 곧 상태바에 가리는 높이다.
        // 배치가 끝나기 전에는 알 수 없으므로 그때는 겹침 없음으로 두고, 첫 배치 뒤 값이 채워지면 따라 넓어진다.
        var sheetTopInWindow by remember { mutableFloatStateOf(Float.MAX_VALUE) }
        val statusBarOverlap = with(LocalDensity.current) {
            (statusBarInset.toPx() - sheetTopInWindow).coerceAtLeast(0f).toDp()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .anchoredSheetHeight(state = dragState, fullHeightPx = fullHeightPx)
                .onGloballyPositioned { coordinates -> sheetTopInWindow = coordinates.positionInWindow().y }
                .dropShadow(shape = containerShape, shadow = MinoAndroidTheme.shadows.spreadSmall)
                .surface(
                    shape = containerShape,
                    containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal,
                )
                // 콘텐츠가 더 스크롤할 수 없는 만큼을 시트가 받아, 축 하나로 스크롤과 단계 이동이 이어진다.
                .nestedScroll(nestedScrollConnection)
                .anchoredDraggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    flingBehavior = flingBehavior,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (level) {
                PlaceSheetLevel.HALF -> SheetDragHandle()
                // 이 띠는 헤더 행에 속한 자리라 시트 배경이 아니라 헤더와 같은 배경을 깐다.
                //
                // **상태바에 가리는 만큼을 이 띠가 함께 낸다.** 시트가 화면 최상단부터 서 있으면 그 자리를 비워
                // 두지 않는 한 헤더 첫 줄이 상태바 아이콘과 겹치고, 이미 상태바 아래에서 시작한다면 더 비울 것이
                // 없다 — 어느 쪽인지는 재 본 [statusBarOverlap]이 답한다. 디자인이 정한 것은 상태바 **아래**
                // 여백 16dp다(Figma `005-2-1 full` — 상태바 54, 헤더 프레임 54, 아바타 70).
                PlaceSheetLevel.FULL -> Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(statusBarOverlap + FullHeaderTopSpacing)
                        .surface(
                            shape = RectangleShape,
                            containerColor = MinoAndroidTheme.colors.backgroundNormalNormal,
                        ),
                )
            }
            pinnedHeader()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState),
                content = content,
            )
        }
    }
}

/**
 * 끌린 거리만큼 위를 잘라낸 높이로 세운다. 화면이 하단 정렬로 놓으므로 줄어드는 쪽은 늘 위이고, 그만큼 지도가
 * 드러난다.
 */
private fun Modifier.anchoredSheetHeight(
    state: AnchoredDraggableState<SheetAnchor>,
    fullHeightPx: Float,
): Modifier =
    layout { measurable, constraints ->
        val height = constraints.constrainHeight((fullHeightPx - state.requireOffset()).roundToInt())
        val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
        layout(placeable.width, height) { placeable.place(x = 0, y = 0) }
    }

/**
 * 콘텐츠의 스크롤과 시트의 단계 이동을 한 축으로 잇는다.
 *
 * 위로 끄는 동안에는 시트를 먼저 세우고, 다 선 뒤에야 콘텐츠가 스크롤된다 — 그래서 최상단에서 한 번 끌면 코멘트
 * 영역까지 닿는다(spec SC-001). 반대로 콘텐츠가 최상단이라 더 내려갈 곳이 없으면 남은 만큼을 시트가 받아 단계가
 * 내려간다. **`HALF`에서 멈춘다** — 그 아래에는 설 자리가 없어 [AnchoredDraggableState]가 남은 델타를 흘려
 * 보낸다(spec FR-001 · EC-003).
 */
private fun sheetNestedScrollConnection(
    state: AnchoredDraggableState<SheetAnchor>,
    flingBehavior: FlingBehavior,
): NestedScrollConnection =
    object : NestedScrollConnection {
        private val dragScope = object : ScrollScope {
            override fun scrollBy(pixels: Float): Float = state.dispatchRawDelta(pixels)
        }

        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource,
        ): Offset =
            if (available.y < 0f && source == NestedScrollSource.UserInput) {
                Offset(x = 0f, y = state.dispatchRawDelta(available.y))
            } else {
                Offset.Zero
            }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset =
            if (source == NestedScrollSource.UserInput) {
                Offset(x = 0f, y = state.dispatchRawDelta(available.y))
            } else {
                Offset.Zero
            }

        override suspend fun onPreFling(available: Velocity): Velocity =
            if (available.y < 0f && state.requireOffset() > state.anchors.minPosition()) {
                with(flingBehavior) { dragScope.performFling(available.y) }
                available
            } else {
                Velocity.Zero
            }

        override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity,
        ): Velocity {
            with(flingBehavior) { dragScope.performFling(available.y) }
            return available
        }
    }

/**
 * 시트가 멈춰 서는 자리. [PlaceSheetLevel]과 일대일로 대응한다 — 화면 밖 자리를 두지 않는 것이 `HALF` 하한
 * (spec FR-001 · EC-003)의 구현이며, 앵커가 없으면 그 아래로는 끌리지 않는다.
 *
 * 그럼에도 별도 타입인 것은 드래그가 이 파일 안의 일이기 때문이다 — 화면이 쓰는 단계 타입을 시트 내부 구현이
 * 함께 쓰면 앵커를 늘리고 줄일 때마다 화면 상태 타입이 흔들린다.
 */
private enum class SheetAnchor {
    HALF,
    FULL,
}

private fun PlaceSheetLevel.toAnchor(): SheetAnchor =
    when (this) {
        PlaceSheetLevel.HALF -> SheetAnchor.HALF
        PlaceSheetLevel.FULL -> SheetAnchor.FULL
    }

private fun PlaceSheetLevel.containerShape(): Shape =
    when (this) {
        PlaceSheetLevel.HALF -> SheetContainerShape
        PlaceSheetLevel.FULL -> RectangleShape
    }

private val HalfHeight = 369.dp

/**
 * `HALF`에서 시트가 차지하는 높이(`FULL`은 화면을 채워 지도가 보이지 않으므로 `null`).
 *
 * `RoomListBottomSheet`의 `bottomSheetHeightOrNull`·`roomDetailBottomSheetHeightOrNull`과 같은 자리다 —
 * 지도를 그리는 화면이 세 시트 중 지금 서 있는 것의 높이를 알아야 지도 컨트롤을 그 위에 얹고(FR-023),
 * 마커를 시트에 가리지 않은 영역의 중앙에 놓을 수 있다(FR-002).
 */
internal fun placeDetailSheetHeightOrNull(level: PlaceSheetLevel): Dp? =
    when (level) {
        PlaceSheetLevel.HALF -> HalfHeight
        PlaceSheetLevel.FULL -> null
    }

/** `FULL` 헤더 위 여백. 상태바 아래로 이만큼 띄운다(Figma `005-2-1 full`). */
private val FullHeaderTopSpacing = 16.dp

private val PreviewSheetContainerHeight = 640.dp

@UiModePreviews
@Composable
private fun PlaceDetailSheetHalfPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        Box(modifier = modifier.height(PreviewSheetContainerHeight)) {
            PlaceDetailSheet(onLevelChange = {}) {}
        }
    }
}

@UiModePreviews
@Composable
private fun PlaceDetailSheetFullPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        Box(modifier = modifier.height(PreviewSheetContainerHeight)) {
            PlaceDetailSheet(
                onLevelChange = {},
                level = PlaceSheetLevel.FULL,
            ) {}
        }
    }
}
