package team.mino.feature.home.main.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.PlaceCard
import team.mino.core.domain.model.PlaceCategoryLabel

/**
 * 장소 카드를 스택으로 쌓아 보여주고, 화면 우측 영역의 가로 스와이프로 넘기고 되돌리는 덱.
 *
 * 목록을 입력으로 받고 신호는 콜백으로만 내보낸다. 목록 조회·시트 표시·정렬은 호출자 몫이다.
 * 동작 계약은 `docs/specs/home-card-deck/contracts/card-deck-component.md` §3이 소유한다.
 *
 * @param cards 덱을 채울 목록. 중복 `pinId` 제거와 장수 상한은 [CardDeckState]가 처리한다.
 * @param onCardConfirmed 카드를 넘겨 확인한 시점에 카드당 1회 발생한다. 되돌려도 취소되지 않는다.
 * @param onLoadMore `장소 더 보기` 요청. 호출자가 새 목록을 가져와 [cards]로 다시 넣는다.
 * @param onSaveToOtherRoom `다른 방 저장` 선택. 호출자가 「홈 방 시트」를 연다.
 * @param state 덱 진행 상태. 호출자가 관찰할 수 있게 호이스팅한다.
 */
@Composable
internal fun CardDeck(
    cards: List<PlaceCard>,
    onCardConfirmed: (pinId: String) -> Unit,
    onLoadMore: () -> Unit,
    onSaveToOtherRoom: (pinId: String) -> Unit,
    modifier: Modifier = Modifier,
    state: CardDeckState = rememberCardDeckState(),
) {
    val scope = rememberCoroutineScope()
    val swipeOffset = remember { Animatable(0f) }
    val currentOnCardConfirmed by rememberUpdatedState(onCardConfirmed)

    // 목록이 바뀔 때만 덱을 다시 구성한다. 같은 목록이면 [CardDeckState]가 진행 상태를 지킨다.
    // 끌려 있던 자리도 함께 되돌려, 새 덱의 첫 장이 직전 카드의 오프셋을 물려받지 않게 한다.
    LaunchedEffect(cards) {
        state.setCards(cards)
        swipeOffset.snapTo(0f)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // 그릴 카드가 없으면 스택만 비운다. 버튼은 아래에서 그대로 그린다.
        state.currentCard?.let { topCard ->
            CardStack(
                topCard = topCard,
                state = state,
                swipeOffset = swipeOffset,
                scope = scope,
                onCardConfirmed = { pinId -> currentOnCardConfirmed(pinId) },
                onSaveToOtherRoom = onSaveToOtherRoom,
            )
        }
        // 잔여 2장 이하부터 노출하고, 덱이 비어도 내리지 않는다. 빈 상태 안내는 덱의 몫이 아니다.
        if (state.remainingCount <= LoadMoreVisibleThreshold) {
            LoadMoreButton(
                onClick = onLoadMore,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = LoadMoreOverhang),
            )
        }
    }
}

/**
 * 최상단 카드와 그 뒤에 겹친 뒷장들. 우측 영역의 가로 스와이프를 받는 곳도 여기다.
 */
@Composable
private fun CardStack(
    topCard: PlaceCard,
    state: CardDeckState,
    swipeOffset: Animatable<Float, AnimationVector1D>,
    scope: CoroutineScope,
    onCardConfirmed: (pinId: String) -> Unit,
    onSaveToOtherRoom: (pinId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    // 최상단 카드가 바뀌면 열려 있던 메뉴는 대상이 사라진 것이므로 닫는다.
    LaunchedEffect(topCard.pinId) { menuExpanded = false }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = StackPeekHeight)
            .pointerInput(state, menuExpanded) {
                val flickThresholdPx = FlickVelocityThreshold.toPx()
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // 메뉴가 열려 있으면 이 제스처는 메뉴를 닫는 데서 끝난다.
                    if (menuExpanded) {
                        menuExpanded = false
                        return@awaitEachGesture
                    }
                    // 좌측 영역에서 시작한 제스처는 전환·복구 어디에도 반영하지 않는다.
                    if (down.position.x < size.width * SwipeAreaStartFraction) return@awaitEachGesture
                    // 전환 애니메이션 중에는 추가 입력을 받지 않는다.
                    if (state.isAnimating) return@awaitEachGesture

                    val tracker = VelocityTracker()
                    tracker.addPosition(down.uptimeMillis, down.position)
                    val dragStart = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                        change.consume()
                        tracker.addPosition(change.uptimeMillis, change.position)
                        scope.launch { swipeOffset.snapTo(swipeOffset.value + overSlop) }
                    } ?: return@awaitEachGesture

                    val finished = horizontalDrag(dragStart.id) { change ->
                        change.consume()
                        tracker.addPosition(change.uptimeMillis, change.position)
                        scope.launch { swipeOffset.snapTo(swipeOffset.value + change.positionChange().x) }
                    }
                    val velocity = if (finished) tracker.calculateVelocity().x else 0f
                    scope.launch {
                        settleSwipe(
                            state = state,
                            swipeOffset = swipeOffset,
                            deckWidthPx = size.width.toFloat(),
                            velocity = velocity,
                            flickThresholdPx = flickThresholdPx,
                            onCardConfirmed = onCardConfirmed,
                        )
                    }
                }
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        // 최상단 카드 뒤로 남은 장수만큼 겹쳐 보인다. 뒤쪽부터 그려야 앞장이 위에 온다.
        // 보이는 장수보다 한 겹 더 그린다 — 앞장이 빠져나가며 스택 전체가 한 칸씩 올라올 때
        // 맨 뒷자리를 이어받을 카드가 필요하다. 제자리에서는 투명해서 보이지 않는다.
        val backLayerCount = (state.remainingCount - 1).coerceIn(0, MaxBackLayers + 1)
        repeat(backLayerCount) { index ->
            val depth = backLayerCount - index
            state.cardAtDepth(depth)?.let { backCard ->
                BackCardLayer(
                    card = backCard,
                    depth = depth,
                    swipeOffset = swipeOffset,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        // 최상단 카드와 그 카드에 붙는 메뉴. 메뉴 앵커가 카드 밖으로 밀리지 않게 한 겹으로 묶는다.
        Box {
            HomeCard(
                category = topCard.label.toHomeCardCategory(),
                title = topCard.placeName,
                address = topCard.address,
                imageCount = CardImageCount,
                avatarImageUrl = topCard.registrant?.avatarUrl,
                onMoreClick = { menuExpanded = true },
                modifier = Modifier.graphicsLayer {
                    val progress = if (size.width == 0f) {
                        0f
                    } else {
                        (swipeOffset.value / size.width).coerceIn(-1f, 1f)
                    }
                    translationX = swipeOffset.value
                    rotationZ = -SwipeMaxRotationDegrees * progress
                    alpha = lerp(1f, SwipeMinAlpha, abs(progress))
                },
            )
            CardActionMenu(
                expanded = menuExpanded,
                onSaveToOtherRoom = {
                    menuExpanded = false
                    onSaveToOtherRoom(topCard.pinId)
                },
                onHidePlace = {
                    menuExpanded = false
                    state.hidePlace(topCard.pinId)
                },
                onDismiss = { menuExpanded = false },
                alignment = Alignment.TopEnd,
                offset = with(LocalDensity.current) {
                    // 카드 헤더 `[...]` 버튼의 오른쪽 아래 모서리에 메뉴 위쪽 모서리를 붙인다.
                    IntOffset(x = -MoreButtonEndInset.roundToPx(), y = MoreButtonBottomInset.roundToPx())
                },
            )
        }
    }
}

/**
 * 손을 뗀 뒤의 처리. 임계값을 넘겼으면 방향에 따라 넘기거나 되돌리고, 아니면 원위치한다.
 *
 * 넘김·되돌리기 애니메이션 동안에는 [CardDeckState.isAnimating]을 세워 입력을 막지만,
 * 원위치는 신호가 나가지 않는 되돌림이라 잠그지 않는다 — 사용자가 곧바로 다시 잡을 수 있어야 한다.
 */
private suspend fun settleSwipe(
    state: CardDeckState,
    swipeOffset: Animatable<Float, AnimationVector1D>,
    deckWidthPx: Float,
    velocity: Float,
    flickThresholdPx: Float,
    onCardConfirmed: (pinId: String) -> Unit,
) {
    val dragged = swipeOffset.value
    val passedThreshold =
        abs(dragged) >= deckWidthPx * SwipeDistanceThreshold || abs(velocity) >= flickThresholdPx
    val towardRight = if (dragged != 0f) dragged > 0f else velocity > 0f

    when {
        // 임계값 미만 — 원위치하고 아무 신호도 보내지 않는다.
        !passedThreshold -> swipeOffset.animateTo(0f)

        // 좌→우: 카드를 화면 밖으로 보낸 뒤 확인 신호를 1회 발생시킨다.
        towardRight -> {
            state.whileAnimating { swipeOffset.animateTo(deckWidthPx * ExitDistanceRatio) }
            val confirmed = state.confirmCurrent()
            swipeOffset.snapTo(0f)
            confirmed?.let { onCardConfirmed(it.pinId) }
        }

        // 우→좌: 직전 1장을 복구하고 끌려 있던 자리에서 제자리로 돌린다.
        state.canUndo -> {
            state.undo()
            state.whileAnimating { swipeOffset.animateTo(0f) }
        }

        // 되돌릴 카드가 없으면 무동작.
        else -> swipeOffset.animateTo(0f)
    }
}

private suspend fun CardDeckState.whileAnimating(block: suspend () -> Unit) {
    isAnimating = true
    try {
        block()
    } finally {
        isAnimating = false
    }
}

/**
 * 최상단 카드 뒤에 겹쳐 보이는 카드. 앞장에 가려 상단 [StackOffsetStep]만큼만 드러나지만
 * 내용까지 갖춘 카드를 그대로 그린다 — 그 띠에 뱃지 색이 비쳐야 하고, 최상단 카드가 빠져나가는
 * 동안 바로 뒷장이 이미 완성된 카드로 보여야 하기 때문이다.
 *
 * 최상단 카드가 오른쪽으로 빠져나간 만큼 깊이를 당겨, 앞자리가 비지 않게 미리 승격시킨다.
 * 카드가 다 나갔을 때 이미 다음 카드가 앞자리에 서 있으므로 손을 떼는 순간 스택이 튀지 않는다.
 *
 * 뒷장도 [HomeCard]이므로 카드 이미지 로딩은 덱 한 벌에 최대 [MaxBackLayers]+2배로 걸린다.
 * 이미지가 실제 네트워크 로딩으로 바뀌면 [depth]로 로딩을 미루는 지점이 여기다.
 *
 * @param depth 최상단 카드로부터의 깊이(1이 바로 뒷장). 깊을수록 작고 위로 올라가며 흐려진다.
 * @param swipeOffset 최상단 카드가 끌려간 거리. 승격 정도를 그리기 시점에 읽는다.
 */
@Composable
private fun BackCardLayer(
    card: PlaceCard,
    depth: Int,
    swipeOffset: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier,
) {
    HomeCard(
        category = card.label.toHomeCardCategory(),
        title = card.placeName,
        address = card.address,
        imageCount = CardImageCount,
        avatarImageUrl = card.registrant?.avatarUrl,
        modifier = modifier
            .graphicsLayer {
                // 되돌리기(우->좌)는 앞장이 제자리로 돌아오는 동작이라 승격시키지 않는다.
                val exitProgress = if (size.width == 0f) {
                    0f
                } else {
                    (swipeOffset.value / size.width).coerceIn(0f, 1f)
                }
                val currentDepth = depth - exitProgress
                val shrinkPx = StackWidthStep.toPx() * currentDepth
                val scale = if (size.width == 0f) {
                    1f
                } else {
                    ((size.width - shrinkPx) / size.width).coerceAtLeast(0f)
                }
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0f)
                translationY = -StackOffsetStep.toPx() * currentDepth
                alpha = backCardAlphaAt(currentDepth)
            }
            // 뒷장은 앞장을 받쳐 주는 장식이라 접근성 트리에서 뺀다.
            .clearAndSetSemantics {}
            // 시맨틱만 지워서는 뒷장 카드의 `[...]` 버튼이 눌리는 것을 막지 못한다.
            // 자식에게 내려가기 전에 포인터를 삼켜 뒷장 전체를 입력에서 뺀다.
            .consumeAllPointerInput(),
    )
}

/** 최상단 카드 뒤에 보이는 최대 장수. */
private const val MaxBackLayers = 4

/**
 * 깊이 0([MaxBackLayers]+1까지)별 불투명도. 0은 앞자리, [MaxBackLayers]+1은 아직 보이지 않는 자리다.
 * 승격 중의 소수 깊이는 이웃한 두 값을 선형 보간한다.
 */
private val BackCardAlphas = listOf(1f, 0.98f, 0.90f, 0.80f, 0.70f, 0f)

private fun backCardAlphaAt(depth: Float): Float {
    val clamped = depth.coerceIn(0f, BackCardAlphas.lastIndex.toFloat())
    val lower = clamped.toInt()
    val upper = (lower + 1).coerceAtMost(BackCardAlphas.lastIndex)
    return lerp(BackCardAlphas[lower], BackCardAlphas[upper], clamped - lower)
}

/** 자식까지 포함해 포인터 입력을 삼킨다. */
private fun Modifier.consumeAllPointerInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

private val StackOffsetStep = 20.dp
private val StackWidthStep = 20.dp
private val StackPeekHeight = StackOffsetStep * MaxBackLayers

/** 카드 헤더 `[...]` 버튼의 카드 우측 여백과, 카드 위쪽에서 그 버튼 아래 모서리까지의 거리. */
private val MoreButtonEndInset = 16.dp
private val MoreButtonBottomInset = 52.dp

/** `장소 더 보기`가 카드덱 아래로 내려와 걸치는 만큼. */
private val LoadMoreOverhang = 10.dp

/** 덱 폭에서 스와이프를 인식하기 시작하는 지점. 이 왼쪽에서 시작한 제스처는 무시한다. */
private const val SwipeAreaStartFraction = 0.5f

/** 전환으로 판정하는 드래그 거리(카드 폭 대비). */
private const val SwipeDistanceThreshold = 0.25f

/** 거리가 모자라도 전환으로 판정하는 가로 플릭 속도(초당 이동 거리). */
private val FlickVelocityThreshold = 400.dp

/** 넘어가는 카드가 화면 밖으로 빠지는 거리(카드 폭 대비). */
private const val ExitDistanceRatio = 1.5f

private const val SwipeMaxRotationDegrees = 20f
private const val SwipeMinAlpha = 0.70f

/** `장소 더 보기`를 노출하기 시작하는 잔여 장수. 0장이 되어도 조건을 만족해 계속 보인다. */
private const val LoadMoreVisibleThreshold = 2

/** 카드가 그리는 이미지 칸 수. 카드 레이아웃이 2칸 그리드로 고정이다. */
private const val CardImageCount = 2

private fun previewCards(count: Int, label: PlaceCategoryLabel? = null): List<PlaceCard> =
    List(count) { index ->
        PlaceCard(
            pinId = "pin-$index",
            placeName = "레이어스튜디오 ${index + 1}",
            address = "서울 성동구 상원4길 ${index + 1}",
            imageUrls = emptyList(),
            label = label ?: PlaceCategoryLabel.entries[index % PlaceCategoryLabel.entries.size],
        )
    }

@Composable
private fun CardDeckPreviewFrame(
    cardCount: Int,
    modifier: Modifier = Modifier,
) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative)
                .padding(horizontal = 20.dp),
        ) {
            CardDeck(
                cards = previewCards(count = cardCount),
                onCardConfirmed = {},
                onLoadMore = {},
                onSaveToOtherRoom = {},
            )
        }
    }
}

/** 12장 주입 — 덱은 상한인 10장만 쓰고 `장소 더 보기`는 아직 나오지 않는다(quickstart B). */
@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun CardDeckPreview() {
    CardDeckPreviewFrame(cardCount = 12)
}

/** 4장 주입 — 있는 만큼만 쌓인다(quickstart B). */
@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun CardDeckFewCardsPreview() {
    CardDeckPreviewFrame(cardCount = 4)
}

/** 0장 주입 — 카드는 하나도 없고 `장소 더 보기`만 남는다. 빈 상태 안내는 없다(quickstart B·C). */
@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun CardDeckEmptyPreview() {
    CardDeckPreviewFrame(cardCount = 0)
}

/** 라벨 4종 — 덱마다 최상단 카드의 분류 라벨이 다르다(quickstart E). */
@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@Preview(name = "CardDeck - 라벨 4종")
@Composable
private fun CardDeckLabelPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlaceCategoryLabel.entries.forEach { label ->
                CardDeck(
                    cards = previewCards(count = 3, label = label),
                    onCardConfirmed = {},
                    onLoadMore = {},
                    onSaveToOtherRoom = {},
                )
            }
        }
    }
}
