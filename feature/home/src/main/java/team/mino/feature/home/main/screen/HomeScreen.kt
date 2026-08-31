package team.mino.feature.home.main.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.PlaceCard
import team.mino.core.domain.model.PlaceLabel
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.model.Registrant
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.home.R
import team.mino.feature.home.main.component.AllExhaustedContent
import team.mino.feature.home.main.component.CardDeck
import team.mino.feature.home.main.component.EmptyContent
import team.mino.feature.home.main.component.HomeGuideOverlay
import team.mino.feature.home.main.component.HomeRoomSheet
import team.mino.feature.home.main.component.HomeTooltipOverlay
import team.mino.feature.home.main.component.HomeTopShell
import team.mino.feature.home.main.component.PlaceCardItem
import team.mino.feature.home.main.component.RoomBadge
import team.mino.feature.home.main.component.RoomCharacter
import team.mino.feature.home.main.component.SortChipRow
import team.mino.feature.home.main.model.HomePhase
import team.mino.feature.home.main.model.HomeTooltip
import team.mino.feature.home.main.vm.HomeIntent
import team.mino.feature.home.main.vm.HomeUiState

/**
 * 홈 탭 화면. 상태와 콜백만으로 그린다 — ViewModel도 navController도 모른다.
 *
 * **요소를 세로로 쌓지 않고 위쪽 기준 오프셋으로 놓는다.** 방 캐릭터가 정렬 칩 행과 세로로 겹쳐 있어
 * (시안에서도 화면 프레임의 형제로 떠 있다) 열로 쌓으면 칩이 캐릭터 아래로 밀린다.
 *
 * [HomeUiState.phase]는 **카드 자리 하나만** 가른다. 상단(방 뱃지·인사 문구·정렬 칩)은 어느 값에서도
 * 그대로 남는다(spec FR-014). 가이드는 그 갈래와 직교해 어느 화면 위에도 뜬다(spec EC-016).
 *
 * @param onCreateRoom 방 시트의 `방 만들기` 칸(spec EC-015).
 * @param onCreateRoomFromEmpty 빈 상태 안내의 `공동방 만들기` CTA(spec FR-020). 앞의 것과 가는 곳이 같더라도
 *  홈이 그 판단을 대신하지 않는다.
 */
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onCreateRoom: () -> Unit,
    onCreateRoomFromEmpty: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MinoAndroidTheme.colors.backgroundNormalAlternative),
    ) {
        HomeContent(
            state = state,
            onIntent = onIntent,
            onCreateRoomFromEmpty = onCreateRoomFromEmpty,
            // 딤의 backdrop blur는 딤 자신의 속성이지만 Compose에 그 API가 없어 뒤 계층을 흐린다.
            modifier = if (state.isGuideVisible) Modifier.blur(GuideBackdropBlurRadius) else Modifier,
        )

        if (state.isRoomSheetOpen) {
            HomeRoomSheet(
                rooms = state.rooms,
                currentRoomId = state.room?.id,
                onSelectRoom = { onIntent(HomeIntent.SelectRoom(it)) },
                onCreateRoom = {
                    // 폼으로 나가는 것은 시트를 닫는 경로이기도 하다 — 남겨 두면 돌아왔을 때 그대로 떠 있다.
                    onIntent(HomeIntent.DismissRoomSheet)
                    onCreateRoom()
                },
                onDismissRequest = { onIntent(HomeIntent.DismissRoomSheet) },
            )
        }

        if (state.isGuideVisible) {
            // 가이드는 상태바·바텀 네비까지 덮는다. 화면은 셸이 계산한 영역 안에 있어 그 안에 두면
            // 인셋만큼 밀리므로, 창 왼쪽 위를 원점으로 삼는 팝업으로 띄운다.
            //
            // 상태바 높이는 팝업 **밖**에서 읽는다 — 팝업은 제 창을 열어 인셋 계층이 갈리므로 그 안에서 읽은
            // 값은 이 화면이 놓인 자리를 말해 주지 않는다. 하이라이트와 안내 요소가 같은 값을 써야
            // 화살표가 가리키는 대상과 어긋나지 않는다.
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Popup(popupPositionProvider = WindowOrigin, properties = PopupProperties(focusable = false)) {
                HomeGuideOverlay(
                    onDismiss = { onIntent(HomeIntent.DismissGuide) },
                    statusBarHeight = statusBarHeight,
                ) {
                    // 딤 위에서 원색으로 되살아나는 셋. 인사 문구와 정렬 칩은 딤에 그대로 남는다.
                    // 콜백을 잇지 않는다 — 가이드가 떠 있는 동안의 조작은 ViewModel이 이미 버린다(spec TS-030).
                    // 딤 뒤에 진짜 위젯이 그대로 있어, 사본은 보조도구에서 지운다.
                    state.cards.firstOrNull()?.let { frontCard ->
                        PlaceCardItem(
                            card = frontCard,
                            isActionMenuOpen = false,
                            onMoreClick = null,
                            onSaveToAnotherRoom = {},
                            onDismissActionMenu = {},
                            modifier = Modifier
                                .padding(horizontal = ContentHorizontalPadding)
                                .padding(top = statusBarHeight + GuideCardTop)
                                .clearAndSetSemantics {},
                        )
                    }
                    RoomCharacter(
                        onClick = {},
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = statusBarHeight + GuideCharacterTop)
                            .clearAndSetSemantics {},
                    )
                    RoomBadge(
                        roomName = state.room?.name.orEmpty(),
                        onClick = {},
                        modifier = Modifier
                            .padding(start = ContentHorizontalPadding, top = statusBarHeight + GuideBadgeTop)
                            .clearAndSetSemantics {},
                    )
                }
            }
        }
    }
}

/** 딤 뒤에 놓이는 홈 본체. 가이드가 이 계층을 통째로 흐린다. */
@Composable
private fun HomeContent(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onCreateRoomFromEmpty: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        HomeTopShell(
            // 방을 아직 못 정한 동안에도 뱃지와 인사 문구는 남는다(spec FR-021) — 이름만 빈다.
            roomName = state.room?.name.orEmpty(),
            onRoomChangeClick = { onIntent(HomeIntent.OpenRoomSheet) },
            modifier = Modifier.padding(top = TopShellTop),
        )
        SortChipRow(
            sort = state.sort,
            onSelect = { onIntent(HomeIntent.SelectSort(it)) },
            modifier = Modifier
                .padding(horizontal = ContentHorizontalPadding)
                .padding(top = SortChipRowTop),
        )

        // 카드 자리. 다섯 갈래가 여기서만 갈리고 상단은 어느 값에서도 그대로 남는다(spec FR-014).
        //
        // ALL_EXHAUSTED와 EMPTY를 합치지 않는다 — 다 둘러본 것과 애초에 볼 것이 없던 것은 서로 다른
        // 화면이고 CTA도 후자에만 붙는다(spec EC-011·FR-020).
        //
        // ERROR에 재시도 버튼을 두지 않는다. 회복 경로는 방을 다시 고르는 것뿐이라 그 의도가
        // 계약(`contracts/home-ui.md` §2)에 없다.
        when (state.phase) {
            // 첫 덱을 받는 동안의 화면은 시안에 없다. 상단만 남기고 카드 자리를 비워 둔다.
            HomePhase.LOADING -> Unit

            HomePhase.DECK ->
                CardDeck(
                    cards = state.cards,
                    isTransitioning = state.isTransitioning,
                    canSwipeBackward = state.undoStack.isNotEmpty(),
                    actionMenuTarget = state.actionMenuTarget,
                    onSwipeForward = { onIntent(HomeIntent.SwipeForward) },
                    onSwipeBackward = { onIntent(HomeIntent.SwipeBackward) },
                    onTransitionSettled = { onIntent(HomeIntent.TransitionSettled) },
                    onCardClick = { onIntent(HomeIntent.OpenPlaceDetail(it)) },
                    onMoreClick = { onIntent(HomeIntent.OpenActionMenu(it)) },
                    onSaveToAnotherRoom = { onIntent(HomeIntent.SaveToAnotherRoom(it)) },
                    onDismissActionMenu = { onIntent(HomeIntent.DismissActionMenu) },
                    // 좌우 여백은 덱이 직접 넣는다 — 스와이프를 화면 끝까지 받으려면 제스처를 받는 Box가
                    // 여백 바깥까지 덮어야 한다.
                    modifier = Modifier.padding(top = CardDeckTop),
                )

            HomePhase.ALL_EXHAUSTED ->
                AllExhaustedContent(modifier = Modifier.align(Alignment.TopCenter).padding(top = NoticeTop))

            HomePhase.EMPTY ->
                EmptyContent(
                    onCreateRoom = onCreateRoomFromEmpty,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = NoticeTop),
                )

            HomePhase.ERROR ->
                Text(
                    text = stringResource(loadErrorMessageResOf(state.loadError)),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = NoticeTop),
                    color = MinoAndroidTheme.colors.primaryNormal,
                    style = MinoAndroidTheme.typography.label1NormalRegular,
                    textAlign = TextAlign.Center,
                )
        }

        // 정렬 칩·카드 자리보다 **뒤에** 그린다. 시안에서 캐릭터는 화면 프레임의 마지막 자식이라
        // 칩 행 위에 얹힌다. 앞서 그리면 겹치는 띠에서 칩 행이 탭을 먼저 받아 캐릭터의 아래쪽이
        // 먹히고, 캐릭터로 방 시트를 여는 경로가 그만큼 좁아진다(spec FR-017·TS-026).
        RoomCharacter(
            onClick = { onIntent(HomeIntent.OpenRoomSheet) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = TopShellTop),
        )
        HomeTooltipOverlay(
            tooltip = state.tooltip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = TooltipTop, end = ContentHorizontalPadding),
        )
    }
}

/**
 * 주 데이터 로드 실패 문구. 리프를 구분하지 않고 한 줄로 안내한다 — 사용자가 할 수 있는 일이 방을 다시
 * 고르는 것으로 같아 원인을 갈라 봐야 행동이 달라지지 않는다.
 *
 * `else`를 두지 않아 리프가 늘면 컴파일이 멈추고 여기서 다시 판단하게 된다. 공통 매퍼를 두지 않는 이유는
 * `docs/conventions/error_handling.md` §8이 소유한다.
 */
@StringRes
private fun loadErrorMessageResOf(error: MinoDomainException?): Int =
    when (error) {
        is MinoDomainException.Network,
        is MinoDomainException.Http,
        is MinoDomainException.Auth,
        null,
        -> R.string.home_error_load_failed
    }

/** 창 왼쪽 위. 셸이 계산한 인셋 바깥까지 덮어야 하는 가이드가 쓴다. */
private object WindowOrigin : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset.Zero
}

private val ContentHorizontalPadding = 20.dp

/** 방 캐릭터의 위쪽 끝. 상단 셸도 같은 기준에서 제 몫만큼 스스로 내려온다. */
private val TopShellTop = 10.dp

private val SortChipRowTop = 164.dp

private val CardDeckTop = 236.dp

private val NoticeTop = 279.dp

/** 방 캐릭터 바로 아래. 툴팁은 캐릭터를 앵커로 삼는다(spec UX-003). */
private val TooltipTop = 182.dp

private val GuideBackdropBlurRadius = 6.dp

/**
 * 가이드가 딤 위에 다시 그리는 셋의 자리. **상태바 아래를 원점으로 잡는다** — 시안은 상태바를 포함한 좌표를
 * 주지만 그대로 옮기면 상태바가 시안보다 낮은 기기에서 딤 뒤의 진짜 위젯과 어긋나 사본이 겹쳐 보인다.
 */
private val GuideCharacterTop = 10.dp

private val GuideBadgeTop = 32.dp

private val GuideCardTop = 318.dp

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun HomeScreenDeckPreview() {
    MinoAndroidAppTheme {
        HomeScreen(
            state = previewState(
                phase = HomePhase.DECK,
                cards = List(5) { previewCard(it) },
                tooltip = HomeTooltip.DeckAhead.NextSort(DeckSort.LATEST),
            ),
            onIntent = {},
            onCreateRoom = {},
            onCreateRoomFromEmpty = {},
        )
    }
}

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun HomeScreenAllExhaustedPreview() {
    MinoAndroidAppTheme {
        HomeScreen(
            state = previewState(phase = HomePhase.ALL_EXHAUSTED),
            onIntent = {},
            onCreateRoom = {},
            onCreateRoomFromEmpty = {},
        )
    }
}

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun HomeScreenEmptyPreview() {
    MinoAndroidAppTheme {
        HomeScreen(
            state = previewState(phase = HomePhase.EMPTY),
            onIntent = {},
            onCreateRoom = {},
            onCreateRoomFromEmpty = {},
        )
    }
}

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun HomeScreenErrorPreview() {
    MinoAndroidAppTheme {
        HomeScreen(
            state = previewState(phase = HomePhase.ERROR),
            onIntent = {},
            onCreateRoom = {},
            onCreateRoomFromEmpty = {},
        )
    }
}

private fun previewState(
    phase: HomePhase,
    cards: List<PlaceCard> = emptyList(),
    tooltip: HomeTooltip? = null,
) = HomeUiState(
    phase = phase,
    room = RoomSummary(
        id = "room-1",
        name = "민호야잘하자",
        description = "",
        type = RoomType.PERSONAL,
        color = RoomColor.entries.first(),
        placeCount = cards.size,
        thumbnailImageUrls = emptyList(),
    ),
    cards = cards.toImmutableList(),
    tooltip = tooltip,
)

private fun previewCard(index: Int) =
    PlaceCard(
        pinId = "pin-$index",
        placeName = "레이어스튜디오 10",
        address = "서울 성동구 상원4길 10",
        imageUrls = emptyList(),
        label = PlaceLabel.entries[index % PlaceLabel.entries.size],
        registrant = Registrant(userId = "u1", nickname = "미노", avatar = ProfileAvatar.Person1),
    )
