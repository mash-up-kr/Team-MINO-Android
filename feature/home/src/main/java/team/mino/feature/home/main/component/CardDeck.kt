package team.mino.feature.home.main.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.PlaceCard
import team.mino.core.domain.model.PlaceLabel
import team.mino.core.domain.model.Registrant
import team.mino.feature.home.R
import kotlin.math.abs

/**
 * 카드 덱. 맨 앞 한 장만 내용을 그리고 뒷장은 뒤로 겹쳐 좁아지며 위로 쌓인다.
 *
 * **뒷장은 카드 외곽만 그린다.** 디자인에서도 뒷장의 내용은 앞장에 거의 다 가려 보이지 않는 잔상 수준이라,
 * 다섯 장 전부의 이미지를 받아 오는 값을 하지 않는다.
 *
 * 세 조작을 **하나의 `pointerInput`**에서 가른다(spec SC-006, `research.md` R-006).
 * - 터치 슬롭을 넘지 않고 손을 떼면 탭 — 맨 앞 카드의 상세로 간다(spec FR-007).
 * - 슬롭을 넘으면 드래그 — 탭으로 처리하지 않는다(spec EC-006).
 * - `[...]`는 자체 클릭 영역이 먼저 down을 소비하므로 여기까지 오지 않는다(spec EC-007).
 *
 * 드래그는 **제스처의 시작점**이 카드 폭 절반보다 오른쪽일 때만 소비한다(spec FR-003, R-005). 시작점으로
 * 판정해야 경계를 넘나드는 드래그에서 동작이 갈리지 않는다. 임계값에 못 미치면 제자리로 돌아온다(spec EC-002).
 *
 * **전환 중 입력을 버리는 판정은 [team.mino.feature.home.main.vm.HomeViewModel]이 한다**(R-007). 이 컴포저블은
 * 손을 뗀 즉시 의도를 보내고, 전환을 **시작한** 제스처만 애니메이션이 끝날 때 `TransitionSettled`를 보낸다 —
 * 전환 중에 도착한 제스처까지 완료를 알리면 진행 중이던 전환이 일찍 풀려 카드가 두 장 넘어간다(spec SC-005).
 *
 * @param cards 남은 카드. 첫 원소가 맨 앞이다.
 * @param isTransitioning 전환 애니메이션이 도는 중인가. 입력을 막는 데 쓰지 않고 완료 신호의 주인만 가린다.
 * @param actionMenuTarget 액션 메뉴가 열린 카드의 pinId. **맨 앞 장에만 반영한다** — 뒷장은 내용을 그리지 않아
 *  앵커가 될 `[...]`가 없다.
 * @param onCardClick 카드 본문 탭.
 * @param onMoreClick 카드의 `[...]` 탭. 무엇을 열지는 호출자가 정하고, 그 결과가 [actionMenuTarget]으로 돌아온다.
 * @param onSaveToAnotherRoom 액션 메뉴의 `다른 방 저장` 선택.
 * @param onDismissActionMenu 메뉴 바깥 탭. 스와이프로 닫는 경로는 ViewModel이 판정하므로 여기서 보내지 않는다.
 */
@Composable
internal fun CardDeck(
    cards: ImmutableList<PlaceCard>,
    isTransitioning: Boolean,
    actionMenuTarget: String?,
    onSwipeForward: () -> Unit,
    onSwipeBackward: () -> Unit,
    onTransitionSettled: () -> Unit,
    onCardClick: (pinId: String) -> Unit,
    onMoreClick: (pinId: String) -> Unit,
    onSaveToAnotherRoom: (pinId: String) -> Unit,
    onDismissActionMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val dragOffsetX = remember { Animatable(0f) }
    val exitOffsetX = remember { Animatable(0f) }
    var exitingCard by remember { mutableStateOf<PlaceCard?>(null) }

    // pointerInput 블록은 한 번만 뜨므로 그 안에서 읽는 값은 최신 것을 따로 들고 있어야 한다.
    val currentCards by rememberUpdatedState(cards)
    val currentIsTransitioning by rememberUpdatedState(isTransitioning)
    val currentOnSwipeForward by rememberUpdatedState(onSwipeForward)
    val currentOnSwipeBackward by rememberUpdatedState(onSwipeBackward)
    val currentOnTransitionSettled by rememberUpdatedState(onTransitionSettled)
    val currentOnCardClick by rememberUpdatedState(onCardClick)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(DeckHeight)
            .pointerInput(Unit) {
                awaitEachGesture {
                    // [...]가 down을 소비했으면 awaitFirstDown이 걸러 낸다 — 그 탭은 여기 오지 않는다.
                    val down = awaitFirstDown()
                    val fromSwipeArea = down.position.x >= size.width * SWIPE_START_AREA_FRACTION
                    var travel = Offset.Zero
                    var dragging = false
                    var released = false

                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed) break
                        if (!change.pressed) {
                            released = true
                            break
                        }
                        travel += change.positionChange()
                        if (!dragging && travel.getDistance() > viewConfiguration.touchSlop) dragging = true
                        if (dragging && fromSwipeArea) {
                            change.consume()
                            scope.launch { dragOffsetX.snapTo(travel.x) }
                        }
                    }

                    val topCard = currentCards.firstOrNull()
                    when {
                        !dragging -> if (released && topCard != null) currentOnCardClick(topCard.pinId)
                        !fromSwipeArea -> Unit
                        else -> {
                            val committed = released && abs(travel.x) >= size.width * SWIPE_COMMIT_FRACTION
                            val forward = travel.x > 0f
                            val startsTransition = !currentIsTransitioning
                            val releasedAt = travel.x
                            val exitTarget = size.width * EXIT_TRAVEL_FACTOR

                            scope.launch {
                                if (!committed) {
                                    dragOffsetX.animateTo(0f, SettleSpec)
                                    return@launch
                                }
                                if (forward) currentOnSwipeForward() else currentOnSwipeBackward()
                                if (forward && startsTransition && topCard != null) {
                                    exitingCard = topCard
                                    exitOffsetX.snapTo(releasedAt)
                                    dragOffsetX.snapTo(0f)
                                    exitOffsetX.animateTo(exitTarget, SettleSpec)
                                    exitingCard = null
                                } else {
                                    dragOffsetX.animateTo(0f, SettleSpec)
                                }
                                if (startsTransition) currentOnTransitionSettled()
                            }
                        }
                    }
                }
            },
    ) {
        val visible = cards.take(VISIBLE_CARD_COUNT)
        // 뒤에서 앞으로 그린다. 리스트가 한 칸 밀리면 각 카드가 제 자리로 애니메이션하며 올라온다(spec UX-001).
        for (index in visible.indices.reversed()) {
            val card = visible[index]
            key(card.pinId) {
                DeckCard(
                    card = card,
                    depth = index,
                    isActionMenuOpen = index == 0 && card.pinId == actionMenuTarget,
                    offsetX = { if (index == 0) dragOffsetX.value else 0f },
                    onCardClick = { currentOnCardClick(card.pinId) },
                    onMoreClick = { onMoreClick(card.pinId) },
                    onSaveToAnotherRoom = { onSaveToAnotherRoom(card.pinId) },
                    onDismissActionMenu = onDismissActionMenu,
                )
            }
        }
        // 넘어간 카드. 덱에서는 이미 빠졌고 화면 밖으로 나가는 동안만 남는다.
        exitingCard?.let { card ->
            DeckCard(
                card = card,
                depth = 0,
                isActionMenuOpen = false,
                offsetX = { exitOffsetX.value },
                onCardClick = {},
                onMoreClick = {},
                onSaveToAnotherRoom = {},
                onDismissActionMenu = {},
            )
        }
    }
}

/**
 * 덱의 한 칸. [depth]가 0이면 내용을 그리고, 뒷장이면 카드 외곽만 그린다.
 *
 * 자리(크기·위치)는 [depth]를 애니메이션한 값으로 잡아 리스트가 밀릴 때 이어서 움직인다. 값은 전부
 * `graphicsLayer` 안에서 읽어 레이아웃을 다시 재지 않는다.
 */
@Composable
private fun DeckCard(
    card: PlaceCard,
    depth: Int,
    isActionMenuOpen: Boolean,
    offsetX: () -> Float,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit,
    onSaveToAnotherRoom: () -> Unit,
    onDismissActionMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedDepth = animateFloatAsState(
        targetValue = depth.toFloat(),
        animationSpec = tween(TRANSITION_DURATION_MILLIS),
        label = "deckCardDepth",
    )
    val isFront = depth == 0
    val openDetailLabel = stringResource(R.string.home_card_open_detail)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                val currentDepth = animatedDepth.value
                val depthScale = 1f - currentDepth * DepthScaleStep
                scaleX = depthScale
                scaleY = depthScale
                transformOrigin = TransformOrigin(0.5f, 0f)
                translationX = offsetX()
                translationY = cardTop(currentDepth).toPx()
            },
    ) {
        if (isFront) {
            PlaceCardItem(
                card = card,
                isActionMenuOpen = isActionMenuOpen,
                onMoreClick = onMoreClick,
                onSaveToAnotherRoom = onSaveToAnotherRoom,
                onDismissActionMenu = onDismissActionMenu,
                // 탭은 제스처 쪽에서 받으므로 보조도구가 쓸 클릭 동작만 따로 얹는다.
                modifier = Modifier.semantics {
                    onClick(label = openDetailLabel) {
                        onCardClick()
                        true
                    }
                },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardHeight)
                    .clip(CardShape)
                    .background(MinoAndroidTheme.colors.backgroundNormalNormal)
                    .border(CardBorderWidth, MinoAndroidTheme.colors.backgroundNormalAlternative, CardShape),
            )
        }
    }
}

/** 맨 앞 칸과 그 뒤 칸의 간격만 다르고, 뒤로는 일정하게 벌어진다. */
private fun cardTop(depth: Float): Dp =
    if (depth <= 1f) {
        FrontCardTop + (FirstBackCardTop - FrontCardTop) * depth
    } else {
        FirstBackCardTop - BackCardTopStep * (depth - 1f)
    }

private val CardShape = RoundedCornerShape(24.dp)
private val CardBorderWidth = 1.dp
private val CardHeight = 328.dp
private val FrontCardTop = 80.dp
private val FirstBackCardTop = 60.dp
private val BackCardTopStep = 20.dp
private val DeckHeight = FrontCardTop + CardHeight
private val ReferenceCardWidth = 335.dp
private val DepthWidthStep = 20.dp
private val DepthScaleStep = DepthWidthStep / ReferenceCardWidth
private val SettleSpec = tween<Float>(TRANSITION_DURATION_MILLIS)

private const val VISIBLE_CARD_COUNT = 5
private const val TRANSITION_DURATION_MILLIS = 260

/**
 * 드래그를 소비하기 시작하는 지점. `research.md` R-005가 **가정으로 둔 값**이라 디자이너 확인 뒤 바뀔 수 있어
 * 여기 하나로 뺀다.
 */
private const val SWIPE_START_AREA_FRACTION = 0.5f

/** 넘김·되돌리기로 확정되는 이동 거리. 디자인에 정의가 없다. */
private const val SWIPE_COMMIT_FRACTION = 0.25f

/** 넘어간 카드가 화면 밖으로 빠지는 거리. */
private const val EXIT_TRAVEL_FACTOR = 1.2f

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun CardDeckPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative)
                .padding(horizontal = 20.dp),
        ) {
            CardDeck(
                cards = persistentListOf(
                    previewCard(0, PlaceLabel.WORTH_VISITING),
                    previewCard(1, PlaceLabel.MANY_SAVES),
                    previewCard(2, PlaceLabel.MANY_COMMENTS),
                    previewCard(3, PlaceLabel.MANY_VIEWS),
                    previewCard(4, PlaceLabel.WORTH_VISITING),
                ),
                isTransitioning = false,
                actionMenuTarget = null,
                onSwipeForward = {},
                onSwipeBackward = {},
                onTransitionSettled = {},
                onCardClick = {},
                onMoreClick = {},
                onSaveToAnotherRoom = {},
                onDismissActionMenu = {},
            )
        }
    }
}

private fun previewCard(
    index: Int,
    label: PlaceLabel,
) = PlaceCard(
    pinId = "pin-$index",
    placeName = "레이어스튜디오 10",
    address = "서울 성동구 상원4길 10",
    imageUrls = emptyList(),
    label = label,
    registrant = Registrant(userId = "u1", nickname = "미노", avatarId = 1),
)
