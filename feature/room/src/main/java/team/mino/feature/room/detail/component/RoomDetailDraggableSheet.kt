package team.mino.feature.room.detail.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
 * [RoomDetailDraggableSheet]의 각 단 높이 표현 — `core:design-system`이 아니라 `feature:room/detail`에
 * 두는 이유는 [RoomDetailDraggableSheet] KDoc 참고.
 */
sealed interface RoomDetailSheetHeight {
    /** 화면 비율이 아닌 고정 dp로 딱 자른다(대부분의 Peek/Half 단). */
    data class Fixed(val height: Dp) : RoomDetailSheetHeight

    /**
     * 이 dp 이상을 보장하되, 내용이 넘치면 시트가 그만큼 커지게 둔다(예: 방 상세 Peek — 메모가 있는
     * 방은 [Fixed]로 자르면 내용이 잘린다).
     *
     * **이 단으로/에서 전환할 때는 애니메이션되지 않고 순간 전환된다.** 내용이 정하는 높이라 미리 알
     * 목표 dp가 없다 — [RoomDetailDraggableSheet]의 부드러운 전환은 `Fixed`↔`Full` 사이에서만 동작한다.
     * `RoomDetailBottomSheet`는 Peek가 이 타입이라 Peek↔Half 전환이 아직 순간 전환이고, Half↔Full만
     * 부드럽다 — 방 상세의 첫 전환(Peek→Half)이 이 예외에 걸린다는 뜻이니, 체감 개선이 필요해지면 여기가
     * 먼저 봐야 할 자리다.
     */
    data class AtLeast(val minHeight: Dp) : RoomDetailSheetHeight

    /** 화면 전체를 채운다 — 지도 등 뒤 배경이 안 보이므로 [RoomDetailDraggableSheet]가 [fullShape]를 적용한다. */
    data object Full : RoomDetailSheetHeight

    /** 높이를 직접 제약하지 않고 내용(children)에 맞춘다 — [RoomInviteSheet]·`RoomShareSheet`처럼 시트
     * 컨테이너 자체는 항상 wrap-content이고 안의 특정 영역만 단마다 다른 고정 높이를 갖는 화면에 쓴다. */
    data object WrapContent : RoomDetailSheetHeight
}

/**
 * 방 상세(`feature:room/detail`)와 그 시트를 재사용하는 화면들(장소 상세 「다른 방에 공유」)의
 * Peek/Half/Full류 다단 드래그 바텀시트 공용 뼈대(이슈 #144).
 *
 * **`core:design-system`이 아니라 이 패키지에 있다.** 소비자가 [RoomDetailBottomSheet]·[RoomInviteSheet]·
 * `RoomShareSheet`(모듈 루트, 방 상세·장소 상세 공유) 셋뿐이라 "여러 feature가 가져다 쓰는 범용 디자인
 * 컴포넌트"가 아니다 — height 파라미터 설계도 이 소비자들의 요구에 맞춰 계속 바뀔 여지가 있어(2026-08-11
 * API 재검토 메모) 디자인 시스템의 안정된 계약으로 묶어두지 않는다. `feature:room/main`도 같은 이유로
 * [RoomListDraggableSheet][team.mino.feature.room.main.component.RoomListDraggableSheet]를 따로 갖는다
 * — 두 화면의 요구가 갈리면 이 사본이 각자 자유롭게 바뀌어야지, 한쪽 변경이 다른 쪽에 새지 않아야 한다.
 * (재성님이 작업한 [SCR-006] 장소 상세 자체 시트들은 이 컴포넌트를 쓰지 않는다 — 별개다.)
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
internal fun RoomDetailDraggableSheet(
    levelIndex: Int,
    heights: ImmutableList<RoomDetailSheetHeight>,
    onDraggedUp: () -> Unit,
    onDraggedDown: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    enableContentNestedScroll: Boolean = false,
    dragThreshold: Dp = RoomDetailDraggableSheetDefaults.DragThreshold,
    shape: Shape = RoomDetailDraggableSheetDefaults.shape,
    fullShape: Shape = RoomDetailDraggableSheetDefaults.fullShape,
    containerColor: Color = MinoAndroidTheme.colors.backgroundElevatedNormal,
    handle: @Composable () -> Unit = { RoomDetailDraggableSheetDefaults.Handle() },
    header: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    require(heights.isNotEmpty()) { "heights는 최소 1개 이상이어야 한다." }
    require(levelIndex in heights.indices) { "levelIndex($levelIndex)는 heights 범위(${heights.indices}) 안이어야 한다." }

    val isLowest = levelIndex == 0
    val isHighest = levelIndex == heights.lastIndex
    val currentHeight = heights[levelIndex]
    val isFull = currentHeight == RoomDetailSheetHeight.Full

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
    // 안 쓰일 객체를 매번 새로 만든다. 소비자 셋(RoomDetailBottomSheet·RoomInviteSheet·RoomShareSheet)
    // 중 이 값을 켜는 건 RoomDetailBottomSheet뿐이다.
    val nestedScrollConnection = if (enableContentNestedScroll) {
        remember(isFull, isLowest, thresholdPx, onDraggedUp, onDraggedDown, onDismiss) {
            RoomDetailSheetNestedScrollConnection(
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

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // `Fixed`·`Full`끼리 전환할 때만 애니메이션한다 — `AtLeast`는 내용이 정하는 높이라 미리 알
        // 목표값이 없고(억지로 하나 잡으면 그 값으로 딱 잘려 원래 이 타입을 도입한 이유였던 내용 잘림이
        // 재현된다), `WrapContent`는 단일 단계라 전환 자체가 없다. `RoomDetailBottomSheet`의 Peek(AtLeast)
        // ↔ Half(Fixed) 전환은 그래서 여전히 순간 전환이고, Half↔Full만 부드럽게 늘어난다.
        val animatedTargetDp = when (currentHeight) {
            is RoomDetailSheetHeight.Fixed -> currentHeight.height
            RoomDetailSheetHeight.Full -> maxHeight
            else -> null
        }?.let { animateDpAsState(it, label = "RoomDetailSheetHeight").value }

        val heightModifier = when (currentHeight) {
            is RoomDetailSheetHeight.Fixed -> Modifier.height(animatedTargetDp ?: currentHeight.height)
            is RoomDetailSheetHeight.AtLeast -> Modifier.heightIn(min = currentHeight.minHeight)
            RoomDetailSheetHeight.Full -> Modifier.height(animatedTargetDp ?: maxHeight)
            RoomDetailSheetHeight.WrapContent -> Modifier
        }

        // `Full`은 지도를 완전히 덮어 화면에 지도가 안 보이므로 상태바·내비게이션 바 뒤로 파고들
        // 이유가 없다 — `MinoScaffold`가 이미 물러나 준 자리(= [maxHeight])를 그대로 채우면 그 아랫변이
        // 내비게이션 바 위 경계와 정확히 맞물린다. Peek/Half와 똑같은 비-엣지투엣지 레이아웃이다(엣지투엣지로
        // 시도했던 이전 구현은 지도가 실제로 보이는 다른 화면(장소 상세 Full)의 전제를 잘못 옮겨온 것이었다).
        //
        // Figma Full(`2542:125333`)에도 핸들 노드가 없다 — 첫 자식이 곧장 헤더 줄이다. Peek/Half엔 있다.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(heightModifier)
                .surface(shape = if (isFull) fullShape else shape, containerColor = containerColor)
                .then(if (nestedScrollConnection != null) Modifier.nestedScroll(nestedScrollConnection) else Modifier),
        ) {
            Column(modifier = Modifier.fillMaxWidth().then(dragModifier)) {
                if (!isFull) handle()
                header()
            }
            content()
        }
    }
}

/**
 * [RoomDetailDraggableSheet]의 `content` 스크롤을 드래그 신호로 바꾸는 연결. `Full`이 아닐 때는 스크롤
 * 시도를 전부 가로채고, `Full`일 때는 리스트가 스크롤 경계(최상단)에 닿아 더 소비할 수 없는 나머지만 받아
 * 이전 단으로 접는 신호로 쓴다.
 */
private class RoomDetailSheetNestedScrollConnection(
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

/** [RoomDetailDraggableSheet] 기본값·치수. */
internal object RoomDetailDraggableSheetDefaults {
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
