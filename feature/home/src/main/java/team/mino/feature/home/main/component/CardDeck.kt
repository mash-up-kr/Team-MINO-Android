package team.mino.feature.home.main.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.PlaceCard
import team.mino.core.domain.model.PlaceLabel
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.model.Registrant
import team.mino.feature.home.R
import kotlin.math.abs

/**
 * 카드 덱. 맨 앞 한 장이 온전히 보이고, 뒷장은 뒤로 겹쳐 좁아지며 위로 쌓인다.
 *
 * **뒷장도 내용을 그린다.** 카드가 반투명이라 앞장 너머로 뒷장의 헤더가 비쳐 보이는 것이 시안의 모습이라,
 * 외곽만 그리면 [DepthAlphas]가 드러낼 대상이 사라진다. 넘기는 동안 앞장이 비켜나며 다음 장이 통째로
 * 드러나는 것도 같은 이유로 해결된다. 대신 보이는 다섯 장 전부가 이미지를 받아 온다.
 *
 * **뒤로 갈수록 흐려진다**([DepthAlphas]). 자리(크기·위치)와 같은 depth 값을 쓰므로 한 칸 밀릴 때 함께 이어진다.
 *
 * 세 조작을 **하나의 `pointerInput`**에서 가른다(spec SC-006, `research.md` R-006).
 * - 터치 슬롭을 넘지 않고 손을 떼면 탭 — 맨 앞 카드의 상세로 간다(spec FR-007).
 * - 슬롭을 넘으면 드래그 — 탭으로 처리하지 않는다(spec EC-006).
 * - `[...]`는 자체 클릭 영역이 먼저 down을 소비하므로 여기까지 오지 않는다(spec EC-007).
 *
 * 드래그는 **제스처의 시작점**이 화면 절반보다 오른쪽일 때만 소비한다(spec FR-003, R-005). 시작점으로
 * 판정해야 경계를 넘나드는 드래그에서 동작이 갈리지 않는다. 임계값에 못 미치면 제자리로 돌아온다(spec EC-002).
 *
 * **인식 영역은 카드가 아니라 화면 폭이다.** 카드 위에서 시작해야만 먹히면 되돌리기처럼 「지금 카드를
 * 밀지 않는」 제스처가 카드를 붙잡고 시작해야 해 어색하다. 그래서 좌우 여백은 이 Box가 덮고
 * ([DeckHorizontalPadding]), 카드만 그 여백 안으로 들여 그린다. 거리 판정은 카드 폭 기준을 유지한다.
 *
 * **좌→우로 밀 때만 카드가 손을 따라간다** — 가로만이 아니라 세로로도 함께 움직이고, 가로로 밀린 만큼 눕는다
 * ([SWIPE_ROTATION_DEGREES]). 넘김이 확정되면 놓은 방향을 그대로 연장해 화면 밖으로 날아가므로,
 * 비스듬히 놓으면 비스듬히 빠진다.
 *
 * **우→좌(되돌리기)는 좌→우 이탈의 역재생이다.** 끄는 동안 맨 앞 카드는 제자리에 머물고 — 보여 줄 것은
 * 직전에 넘긴 카드가 돌아오는 움직임이지 지금 카드가 밀리는 움직임이 아니다 — 손을 떼면 그 카드가
 * 오른쪽 위([entryOriginOf])에서 기울어진 채 들어와 제자리에 앉는다. 판정과 소비는 끄는 내내 그대로 한다.
 *
 * **전환 중 입력을 버리는 판정은 [team.mino.feature.home.main.vm.HomeViewModel]이 한다**(R-007). 이 컴포저블은
 * 손을 뗀 즉시 의도를 보내고, 전환을 **시작한** 제스처만 애니메이션이 끝날 때 `TransitionSettled`를 보낸다 —
 * 전환 중에 도착한 제스처까지 완료를 알리면 진행 중이던 전환이 일찍 풀려 카드가 두 장 넘어간다(spec SC-005).
 *
 * @param cards 남은 카드. 첫 원소가 맨 앞이다.
 * @param isTransitioning 전환 애니메이션이 도는 중인가. 입력을 막는 데 쓰지 않고 완료 신호의 주인만 가린다.
 * @param canSwipeBackward 되돌릴 카드가 있는가. 되돌리기 애니메이션은 덱이 바뀌기 **전에** 자리를 잡아야 해서
 *  ViewModel의 판정을 기다릴 수 없다 — 되돌릴 것이 없는데 자리를 잡으면 지금 카드가 헛되이 날아간다(spec EC-001).
 * @param actionMenuTarget 액션 메뉴가 열린 카드의 pinId. **맨 앞 장에만 반영한다** — 다음 장도 `[...]`를
 *  그리지만 조작을 받지 않는다([DeckCard]).
 * @param onCardClick 카드 본문 탭.
 * @param onMoreClick 카드의 `[...]` 탭. 무엇을 열지는 호출자가 정하고, 그 결과가 [actionMenuTarget]으로 돌아온다.
 * @param onSaveToAnotherRoom 액션 메뉴의 `다른 방 저장` 선택.
 * @param onDismissActionMenu 메뉴 바깥 탭. 스와이프로 닫는 경로는 ViewModel이 판정하므로 여기서 보내지 않는다.
 */
@Composable
internal fun CardDeck(
    cards: ImmutableList<PlaceCard>,
    isTransitioning: Boolean,
    canSwipeBackward: Boolean,
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
    val dragOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val exitOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var exitingCard by remember { mutableStateOf<PlaceCard?>(null) }

    // pointerInput 블록은 한 번만 뜨므로 그 안에서 읽는 값은 최신 것을 따로 들고 있어야 한다.
    val currentCards by rememberUpdatedState(cards)
    val currentIsTransitioning by rememberUpdatedState(isTransitioning)
    val currentCanSwipeBackward by rememberUpdatedState(canSwipeBackward)
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
                    // 제스처는 화면 끝까지 받지만 거리 판정은 **카드 폭** 기준이다 — 좌우 여백까지 세면
                    // 임계값이 카드보다 커져 같은 손짓이 기기 폭에 따라 다르게 먹힌다.
                    val cardWidth = size.width - DeckHorizontalPadding.toPx() * 2f

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
                            // 왼쪽으로 끄는 동안은 제자리다. 되돌리기의 주인공은 돌아오는 직전 카드다.
                            val followed = if (travel.x > 0f) travel else Offset.Zero
                            scope.launch { dragOffset.snapTo(followed) }
                        }
                    }

                    val topCard = currentCards.firstOrNull()
                    when {
                        !dragging -> if (released && topCard != null) currentOnCardClick(topCard.pinId)
                        !fromSwipeArea -> Unit
                        else -> {
                            val committed = released && abs(travel.x) >= cardWidth * SWIPE_COMMIT_FRACTION
                            val forward = travel.x > 0f
                            val startsTransition = !currentIsTransitioning
                            val releasedAt = travel

                            scope.launch {
                                if (!committed) {
                                    dragOffset.animateTo(Offset.Zero, SettleSpec)
                                    return@launch
                                }
                                if (forward) {
                                    currentOnSwipeForward()
                                    if (startsTransition && topCard != null) {
                                        // 놓은 방향 그대로 연장해 날린다 — 가로가 화면 폭을 넘을 때까지 늘리면
                                        // 세로 성분도 같은 비율로 따라가 손이 그리던 궤적을 잇는다.
                                        val exitTarget =
                                            releasedAt * (cardWidth * EXIT_TRAVEL_FACTOR / abs(releasedAt.x))
                                        exitingCard = topCard
                                        exitOffset.snapTo(releasedAt)
                                        dragOffset.snapTo(Offset.Zero)
                                        exitOffset.animateTo(exitTarget, SettleSpec)
                                        exitingCard = null
                                    } else {
                                        dragOffset.animateTo(Offset.Zero, SettleSpec)
                                    }
                                } else {
                                    // 되돌아올 카드가 설 자리를 **덱이 바뀌기 전에** 잡는다. 맨 앞 자리는
                                    // dragOffset이 그리므로, 여기서 미리 밀어 두면 되돌아온 카드의 첫 프레임이
                                    // 화면 밖에서 시작한다 — 자리를 나중에 잡으면 한 프레임 제자리에 번쩍인다.
                                    if (startsTransition && currentCanSwipeBackward) {
                                        dragOffset.snapTo(entryOriginOf(cardWidth))
                                    }
                                    currentOnSwipeBackward()
                                    dragOffset.animateTo(Offset.Zero, SettleSpec)
                                }
                                if (startsTransition) currentOnTransitionSettled()
                            }
                        }
                    }
                }
            },
    ) {
        // 카드는 시안대로 좌우 여백 안에 놓고, 제스처만 바깥 Box가 화면 끝까지 받는다.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DeckHorizontalPadding),
        ) {
            val visible = cards.take(VisibleCardCount)
            // 뒤에서 앞으로 그린다. 리스트가 한 칸 밀리면 각 카드가 제 자리로 애니메이션하며 올라온다(spec UX-001).
            for (index in visible.indices.reversed()) {
                val card = visible[index]
                key(card.pinId) {
                    DeckCard(
                        card = card,
                        depth = index,
                        isActionMenuOpen = index == 0 && card.pinId == actionMenuTarget,
                        offset = { if (index == 0) dragOffset.value else Offset.Zero },
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
                    offset = { exitOffset.value },
                    onCardClick = {},
                    onMoreClick = {},
                    onSaveToAnotherRoom = {},
                    onDismissActionMenu = {},
                )
            }
        }
    }
}

/**
 * 덱의 한 칸. 자리와 무관하게 **모든 칸이 내용을 그린다**.
 *
 * 자리(크기·위치)와 불투명도는 [depth]를 애니메이션한 값으로 잡아 리스트가 밀릴 때 이어서 움직인다. 값은 전부
 * `graphicsLayer` 안에서 읽어 레이아웃을 다시 재지 않는다.
 *
 * **내용을 그리는 것과 조작을 받는 것은 다르다.** 맨 앞이 아닌 칸은 내용을 그려도 `[...]`에 클릭 영역을
 * 붙이지 않는다 — 붙이면 그 위에서 시작한 스와이프의 down을 `[...]`가 먼저 삼켜 덱이 넘어가지 않는다.
 */
@Composable
private fun DeckCard(
    card: PlaceCard,
    depth: Int,
    isActionMenuOpen: Boolean,
    offset: () -> Offset,
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
                val swipe = offset()
                scaleX = depthScale
                scaleY = depthScale
                transformOrigin = TransformOrigin(0.5f, 0f)
                translationX = swipe.x
                translationY = cardTop(currentDepth).toPx() + swipe.y
                rotationZ = if (size.width == 0f) 0f else swipe.x / size.width * SWIPE_ROTATION_DEGREES
                alpha = depthAlpha(currentDepth)
            },
    ) {
        PlaceCardItem(
            card = card,
            isActionMenuOpen = isActionMenuOpen,
            // 뒷장은 조작을 받지 않는다 — 클릭 영역이 있으면 그 위에서 시작한 스와이프의 down을 삼킨다.
            onMoreClick = onMoreClick.takeIf { isFront },
            onSaveToAnotherRoom = onSaveToAnotherRoom,
            onDismissActionMenu = onDismissActionMenu,
            // 탭은 제스처 쪽에서 받으므로 보조도구가 쓸 클릭 동작만 따로 얹는다. 뒷장은 보조도구에도
            // 내놓지 않는다 — 넘겨야 닿는 카드를 지금 열 수 있는 것처럼 읽히면 안 된다.
            modifier = if (isFront) {
                Modifier.semantics {
                    onClick(label = openDetailLabel) {
                        onCardClick()
                        true
                    }
                }
            } else {
                Modifier.clearAndSetSemantics {}
            },
        )
    }
}

/** 맨 앞 칸과 그 뒤 칸의 간격만 다르고, 뒤로는 일정하게 벌어진다. */
private fun cardTop(depth: Float): Dp =
    if (depth <= 1f) {
        FrontCardTop + (FirstBackCardTop - FrontCardTop) * depth
    } else {
        FirstBackCardTop - BackCardTopStep * (depth - 1f)
    }

/** 덱이 잡아 두는 카드 높이. [PlaceCardItem]은 내용으로 높이가 정해지므로 이 값은 자리를 확보하기 위한 시안 실측치다. */
private val CardHeight = 328.dp
private val FrontCardTop = 80.dp
private val FirstBackCardTop = 60.dp
private val BackCardTopStep = 20.dp
private val DeckHeight = FrontCardTop + CardHeight
private val ReferenceCardWidth = 335.dp
private val DepthWidthStep = 20.dp
private val DepthScaleStep = DepthWidthStep / ReferenceCardWidth
private val SettleSpec = tween<Offset>(TRANSITION_DURATION_MILLIS)

/**
 * 자리별 불투명도. 시안에서 잰 값이라 **등차가 아니다**(100·98·90·80·70%) — 한 칸마다 일정하게 줄이면
 * 바로 뒷장이 시안보다 흐려진다. **이 배열의 길이가 곧 덱이 그리는 장수다**([VisibleCardCount]).
 */
private val DepthAlphas = floatArrayOf(1f, 0.98f, 0.9f, 0.8f, 0.7f)

/** 한 번에 그리는 장수. [DepthAlphas]가 자리마다 값을 하나씩 갖고 있으므로 그 길이가 곧 이 값이다. */
private val VisibleCardCount = DepthAlphas.size

/**
 * 칸 사이를 지나는 동안의 불투명도. 자리가 정수 칸에만 머무는 것이 아니라 애니메이션으로 그 사이를 지나므로
 * [DepthAlphas]의 두 칸을 선형으로 잇는다 — 값을 칸마다 끊어 주면 카드가 밀릴 때 불투명도만 계단으로 튄다.
 */
private fun depthAlpha(depth: Float): Float {
    val lower = depth.toInt().coerceAtMost(DepthAlphas.lastIndex - 1)
    return lerp(DepthAlphas[lower], DepthAlphas[lower + 1], depth - lower)
}

private const val TRANSITION_DURATION_MILLIS = 260

/**
 * 드래그를 소비하기 시작하는 지점. `research.md` R-005가 **가정으로 둔 값**이라 디자이너 확인 뒤 바뀔 수 있어
 * 여기 하나로 뺀다.
 */
private const val SWIPE_START_AREA_FRACTION = 0.5f

/** 넘김·되돌리기로 확정되는 이동 거리. 디자인에 정의가 없다. */
private const val SWIPE_COMMIT_FRACTION = 0.25f

/** 카드가 놓이는 좌우 여백. 제스처를 받는 바깥 Box는 이 여백 **밖**까지 덮는다. */
private val DeckHorizontalPadding = 20.dp

/** 넘어간 카드가 화면 밖으로 빠지는 거리. */
private const val EXIT_TRAVEL_FACTOR = 1.2f

/**
 * 되돌아오는 카드가 출발하는 자리 — 넘김이 끝난 자리 그대로다. 되돌리기는 좌→우 이탈의 역재생이라
 * 오른쪽 **위**에서 들어온다(시안 영상).
 *
 * 세로가 가로의 절반인 것은 영상에서 잰 값이다(가로 149px일 때 위로 77px). 나갈 때는 놓은 방향이 기울기를
 * 정하지만 돌아올 때는 참고할 제스처가 없어, 대표값 하나를 쓴다.
 */
private fun entryOriginOf(cardWidth: Float): Offset =
    Offset(cardWidth * EXIT_TRAVEL_FACTOR, -cardWidth * EXIT_TRAVEL_FACTOR * ENTRY_RISE_RATIO)

private const val ENTRY_RISE_RATIO = 0.5f

/**
 * 카드가 화면 폭만큼 옆으로 밀렸을 때 눕는 각도. 시안 영상에서 잰 값이다(가로 이동 1px당 0.05°).
 *
 * **음수인 것이 의도다** — 오른쪽으로 밀면 시계 반대 방향으로, 카드의 오른쪽 위 모서리가 들리며 눕는다.
 */
private const val SWIPE_ROTATION_DEGREES = -16f

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
                canSwipeBackward = false,
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
    registrant = Registrant(userId = "u1", nickname = "미노", avatar = ProfileAvatar.Person1),
)
