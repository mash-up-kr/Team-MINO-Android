package team.mino.feature.room.main.component

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * [RoomListDraggableSheet]의 각 단 높이 표현 — `core:design-system`이 아니라 `feature:room/main`에 두는
 * 이유는 [RoomListDraggableSheet] KDoc 참고.
 */
sealed interface RoomListSheetHeight {
    /** 화면 비율이 아닌 고정 dp로 딱 자른다(대부분의 Peek/Half 단). */
    data class Fixed(val height: Dp) : RoomListSheetHeight

    /** 이 dp 이상을 보장하되, 내용이 넘치면 시트가 그만큼 커지게 둔다. */
    data class AtLeast(val minHeight: Dp) : RoomListSheetHeight

    /** 화면 전체를 채운다 — 지도 등 뒤 배경이 안 보이므로 [RoomListDraggableSheet]가 [fullShape]를 적용한다. */
    data object Full : RoomListSheetHeight

    /** 높이를 직접 제약하지 않고 내용(children)에 맞춘다. */
    data object WrapContent : RoomListSheetHeight
}

/**
 * 방 리스트 탭(`feature:room/main`)의 Peek/Half/Full류 다단 드래그 바텀시트 공용 뼈대(이슈 #144).
 *
 * **`core:design-system`이 아니라 이 패키지에 있다.** `MinoBottomSheet`라는 이름으로 디자인 시스템에 두면
 * "재사용 가능한 범용 디자인 컴포넌트"라는 신호를 주지만, 실제로는 room-list 탭 화면들([RoomListBottomSheet]·
 * [RoomNudgeSheet])만 쓰는 화면 조립 로직이다 — 다른 feature가 가져다 쓸 일이 없고, height 파라미터
 * 설계도 이 두 소비자의 요구에 맞춰 계속 바뀔 여지가 있어(2026-08-11 API 재검토 메모) 디자인 시스템의
 * 안정된 계약으로 묶어두지 않는다. `feature:room/detail`도 같은 이유로 [RoomDetailDraggableSheet]를 따로
 * 갖는다 — 두 화면의 요구가 갈리면 이 사본이 각자 자유롭게 바뀌어야지, 한쪽 변경이 다른 쪽에 새지 않아야
 * 한다.
 *
 * 현재 단이 어떤 것인지(`levelIndex`)와 전환 판정 자체는 이 컴포넌트가 갖지 않는다 — 호출부의
 * ViewModel/UiState가 소유하고, 이 컴포넌트는 드래그 제스처를 감지해 [onDraggedUp]/[onDraggedDown]/
 * [onDismiss]만 호출한다.
 *
 * @param levelIndex 현재 단의 인덱스(`heights`의 인덱스, 0=최하단).
 * @param heights 0번째(최하단)부터 순서대로 각 단의 높이.
 * @param onDraggedUp 위로 끌어 다음 단으로 전이를 요청할 때. 이미 최상단이면 호출되지 않는다.
 * @param onDraggedDown 아래로 끌어 이전 단으로 전이를 요청할 때. 이미 최하단이면 [onDismiss]로 대신 이어진다.
 * @param onDismiss 최하단에서 한 번 더 아래로 끌었을 때. `null`이면(기본값) 무시한다.
 * @param enableContentNestedScroll `true`면 [content] 안의 스크롤 가능한 자식에서 시작한 스크롤도 이 시트의
 *   드래그 신호로 받는다.
 * @param handle 드래그 핸들 슬롯.
 * @param header 핸들 아래, 항상 그리는 헤더 슬롯 — 드래그 인식 영역 안에 있다.
 * @param content 헤더 아래 본문 슬롯 — 드래그 인식 영역 밖이라, 안에 `LazyColumn` 등을 둬도 그 자체
 *   스크롤과 충돌하지 않는다.
 */
@Composable
internal fun RoomListDraggableSheet(
    levelIndex: Int,
    heights: ImmutableList<RoomListSheetHeight>,
    onDraggedUp: () -> Unit,
    onDraggedDown: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    enableContentNestedScroll: Boolean = false,
    dragThreshold: Dp = RoomListDraggableSheetDefaults.DragThreshold,
    shape: Shape = RoomListDraggableSheetDefaults.shape,
    fullShape: Shape = RoomListDraggableSheetDefaults.fullShape,
    containerColor: Color = MinoAndroidTheme.colors.backgroundElevatedNormal,
    handle: @Composable () -> Unit = { RoomListDraggableSheetDefaults.Handle() },
    header: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    require(heights.isNotEmpty()) { "heights는 최소 1개 이상이어야 한다." }
    require(levelIndex in heights.indices) { "levelIndex($levelIndex)는 heights 범위(${heights.indices}) 안이어야 한다." }

    val isLowest = levelIndex == 0
    val isHighest = levelIndex == heights.lastIndex
    val currentHeight = heights[levelIndex]
    val isFull = currentHeight == RoomListSheetHeight.Full

    val thresholdPx = with(LocalDensity.current) { dragThreshold.toPx() }

    fun onThresholdReached(draggedUp: Boolean) {
        when {
            draggedUp && !isHighest -> onDraggedUp()
            !draggedUp && !isLowest -> onDraggedDown()
            !draggedUp && isLowest -> onDismiss?.invoke()
        }
    }

    val dragModifier = Modifier.pointerInput(levelIndex, heights.size, onDraggedUp, onDraggedDown, onDismiss) {
        var accumulatedDrag = 0f
        detectVerticalDragGestures(
            onDragStart = { accumulatedDrag = 0f },
            onVerticalDrag = { change, dragAmount ->
                accumulatedDrag += dragAmount
                change.consume()
            },
            onDragEnd = {
                when {
                    accumulatedDrag <= -thresholdPx -> onThresholdReached(draggedUp = true)
                    accumulatedDrag >= thresholdPx -> onThresholdReached(draggedUp = false)
                }
            },
        )
    }

    // enableContentNestedScroll이 꺼져 있으면 아무도 안 쓸 연결 객체라, remember 자체를 건너뛴다 —
    // 그렇지 않으면 여섯 키(콜백 람다 포함, 리컴포지션마다 새 인스턴스이기 쉽다) 중 하나만 바뀌어도
    // 안 쓰일 객체를 매번 새로 만든다.
    val nestedScrollConnection = if (enableContentNestedScroll) {
        remember(isFull, isLowest, thresholdPx, onDraggedUp, onDraggedDown, onDismiss) {
            RoomListSheetNestedScrollConnection(
                isFull = isFull,
                thresholdPx = thresholdPx,
                onDraggedUp = onDraggedUp,
                onDraggedDown = if (isLowest) {
                    {
                        onDismiss?.invoke()
                        Unit
                    }
                } else {
                    onDraggedDown
                },
            )
        }
    } else {
        null
    }

    val heightModifier = when (currentHeight) {
        is RoomListSheetHeight.Fixed -> Modifier.height(currentHeight.height)
        is RoomListSheetHeight.AtLeast -> Modifier.heightIn(min = currentHeight.minHeight)
        RoomListSheetHeight.Full -> Modifier.fillMaxSize()
        RoomListSheetHeight.WrapContent -> Modifier
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(heightModifier)
            .surface(shape = if (isFull) fullShape else shape, containerColor = containerColor)
            .then(if (nestedScrollConnection != null) Modifier.nestedScroll(nestedScrollConnection) else Modifier),
    ) {
        Column(modifier = Modifier.fillMaxWidth().then(dragModifier)) {
            handle()
            header()
        }
        content()
    }
}

/**
 * [RoomListDraggableSheet]의 `content` 스크롤을 드래그 신호로 바꾸는 연결. `Full`이 아닐 때는 스크롤 시도를
 * 전부 가로채고, `Full`일 때는 리스트가 스크롤 경계(최상단)에 닿아 더 소비할 수 없는 나머지만 받아 이전
 * 단으로 접는 신호로 쓴다.
 */
private class RoomListSheetNestedScrollConnection(
    private val isFull: Boolean,
    private val thresholdPx: Float,
    private val onDraggedUp: () -> Unit,
    private val onDraggedDown: () -> Unit,
) : NestedScrollConnection {
    private var accumulatedDrag = 0f

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (isFull) return Offset.Zero
        accumulatedDrag += available.y
        fireIfThresholdReached()
        return available
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (!isFull) return Offset.Zero
        accumulatedDrag += available.y
        fireIfThresholdReached()
        return Offset.Zero
    }

    private fun fireIfThresholdReached() {
        when {
            accumulatedDrag <= -thresholdPx -> {
                onDraggedUp()
                accumulatedDrag = 0f
            }
            accumulatedDrag >= thresholdPx -> {
                onDraggedDown()
                accumulatedDrag = 0f
            }
        }
    }
}

/** [RoomListDraggableSheet] 기본값·치수. */
internal object RoomListDraggableSheetDefaults {
    val DragThreshold = 24.dp
    val HandleSize = DpSize(36.dp, 4.dp)
    val HandleTopPadding = 8.dp
    val HandleBottomPadding = 8.dp

    val shape: Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val fullShape: Shape = RoundedCornerShape(0.dp)

    /** 기본 드래그 핸들 — 상하 8dp 패딩 안에 36×4dp 캡슐. */
    @Composable
    fun Handle(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = HandleTopPadding, bottom = HandleBottomPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .size(HandleSize.width, HandleSize.height)
                    .surface(
                        shape = RoundedCornerShape(percent = 50),
                        containerColor = MinoAndroidTheme.colors.lineSolidNeutral,
                    ),
            )
        }
    }
}
