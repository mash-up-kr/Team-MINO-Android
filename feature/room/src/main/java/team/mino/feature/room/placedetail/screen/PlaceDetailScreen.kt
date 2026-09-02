package team.mino.feature.room.placedetail.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.domain.model.PlaceDetail
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.room.R
import team.mino.feature.room.placedetail.component.PlaceActionRow
import team.mino.feature.room.placedetail.component.PlaceCommentInput
import team.mino.feature.room.placedetail.component.PlaceCommentList
import team.mino.feature.room.placedetail.component.PlaceDetailCollapsedHeader
import team.mino.feature.room.placedetail.component.PlaceDetailExpandedHeader
import team.mino.feature.room.placedetail.component.PlaceDetailSheet
import team.mino.feature.room.placedetail.component.PlaceImageCarousel
import team.mino.feature.room.placedetail.component.PlaceMapControls
import team.mino.feature.room.placedetail.component.RoomShareSheet
import team.mino.feature.room.placedetail.component.SavedRoomsSheet
import team.mino.feature.room.placedetail.component.SheetSectionDivider
import team.mino.feature.room.placedetail.component.systemBarBleed
import team.mino.feature.room.placedetail.model.PlaceHeaderMode
import team.mino.feature.room.placedetail.vm.PlaceDetailIntent
import team.mino.feature.room.placedetail.vm.PlaceDetailUiState
import kotlin.time.ExperimentalTime

/**
 * 장소 상세 화면. 지도 위에 시트가 겹쳐 서고, 시트 안에 헤더·액션 행·캐러셀·코멘트가 한 축으로 이어진다.
 *
 * **지도는 이 화면이 그리지 않는다.** 호출부(`RoomListScreen`)가 이미 그린 `RoomListMap` 위에 이 컴포저블의
 * 컨트롤·시트·오버레이만 얹는다 — 리스트↔방 상세↔장소 상세 전환에도 지도 인스턴스를 하나만 유지하기 위함이며
 * (`RoomDetailScreen`과 같은 형태), 그 `Box`의 [BoxScope]를 그대로 받아 각 조각이 그 지도 위에 겹쳐 선다.
 *
 * **스크롤 축을 이 화면이 소유한다.** 시트가 그 축 위에 콘텐츠를 얹고 헤더 밀도는 그 축이 최상단인지로
 * 갈리므로(spec FR-008), 축을 만들어 시트에 넘기는 것도 그 위치를 인텐트로 올리는 것도 여기다.
 *
 * **`Full`의 윗변은 상태바 아래, 아랫변은 화면 바닥이다.** 이 화면이 받는 자리는 셸의 `MinoScaffold`가 상태바·
 * 내비게이션 바만큼 이미 물러난 뒤의 영역이다. 윗변은 그래서 아무것도 하지 않아야 상태바 바로 아래에 선다 —
 * 여기서 상태바를 한 번 더 빼면 그만큼 내려앉아 그 빈 띠를 뒤의 지도가 도로 차지한다. 아랫변은 반대로 물러난
 * 만큼을 되찾아야 화면 바닥에 닿으므로, 시트와 컨트롤 행을 담은 컨테이너를 내비게이션 바 인셋만큼 부풀린다
 * ([systemBarBleed]).
 *
 * **그 컨테이너가 시트와 컨트롤 행의 세로 기준선을 함께 쥔다.** 컨트롤 행은 시트 윗변에서 거리를 재는데, 둘이
 * 서로 다른 바닥을 기준으로 서면 그 간격이 인셋만큼 벌어진다.
 *
 * **헤더는 시트의 고정 자리 하나뿐이다.** 두 밀도는 그 한 자리를 번갈아 차지하므로 어느 순간에도 헤더가
 * 둘일 수 없다 — 확장형을 스크롤 콘텐츠에 두면 축소형이 선 뒤에도 그것이 콘텐츠 맨 위에 남아 장소명과
 * [나가기]가 두 벌 보인다. 액션 행은 그 자리에 넣지 않고 콘텐츠와 함께 스크롤된다. spec FR-008이 고정 영역을
 * 「장소명 + [나가기]」로 규정하기 때문이다.
 *
 * **주 데이터 조회가 깨지면 시트도 컨트롤도 그리지 않는다.** 장소·코멘트·방 목록은 이 화면이 그릴 것의 전부라,
 * 셋 중 하나라도 못 받은 채로는 남은 것을 그려 봐야 사용자가 보는 것이 실제 상태와 갈린다. 화면 전체를
 * 재시도 가능한 오류로 바꾸는 것이 에러 처리 규약 §5의 규정이다.
 *
 * **지도 컨트롤은 지도가 아니라 이 화면이 놓는다.** 시트보다 위에 그려져야 시트 그림자에 덮이지 않으므로
 * 시트 다음 순서로 얹고, 세로 자리는 시트가 지금 차지한 높이에서 낸다 — 시트 높이를 다시 상수로 들면
 * 두 출처가 갈린다.
 *
 * @param commentState 코멘트 입력 버퍼. 소유자는 `PlaceDetailRoute`다 — 등록이 끝난 뒤 버퍼를 비우는 것도
 *   그쪽이라 이 화면은 받아서 넘기기만 한다.
 * @param onCurrentLocationClick 현재 위치 버튼 클릭. 지도가 이 화면 소유가 아니므로(위 문단) [PlaceDetailIntent]로
 *   처리하지 않는다 — 지도를 실제로 그리는 `RoomListViewModel`의 `OnCurrentLocationClick`으로 직접 연결해야
 *   지도가 움직인다(`RoomDetailScreen`이 같은 이유로 같은 배선을 한다).
 */
@Composable
internal fun BoxScope.PlaceDetailScreen(
    state: PlaceDetailUiState,
    commentState: TextFieldState,
    onIntent: (PlaceDetailIntent) -> Unit,
    onCurrentLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    var sheetHeight by remember { mutableStateOf(0.dp) }

    ReportScrollPosition(scrollState = scrollState, onIntent = onIntent)
    KeepPositionOnPrepend(scrollState = scrollState, topCommentId = state.comments.firstOrNull()?.id)

    val place = state.place
    val loadError = state.loadError

    if (loadError != null) {
        LoadErrorContent(
            error = loadError,
            onRetryClick = { onIntent(PlaceDetailIntent.OnRetryLoadClick) },
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .systemBarBleed(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            contentAlignment = Alignment.BottomCenter,
        ) {
            PlaceDetailSheet(
                onLevelChange = { onIntent(PlaceDetailIntent.OnSheetLevelChange(it)) },
                onExitRequest = { onIntent(PlaceDetailIntent.OnExitClick) },
                modifier = Modifier.onSizeChanged { size -> sheetHeight = with(density) { size.height.toDp() } },
                level = state.sheetLevel,
                scrollState = scrollState,
                pinnedHeader = {
                    if (place != null) {
                        PlaceDetailHeader(
                            place = place,
                            headerMode = state.headerMode,
                            onCloseClick = { onIntent(PlaceDetailIntent.OnExitClick) },
                        )
                    }
                },
            ) {
                if (place != null) {
                    PlaceDetailSheetContent(
                        place = place,
                        state = state,
                        commentState = commentState,
                        onIntent = onIntent,
                    )
                }
            }

            PlaceMapControls(
                sheetLevel = state.sheetLevel,
                isSavedRoomsEnabled = state.isSavedRoomsEnabled,
                onSavedRoomsClick = { onIntent(PlaceDetailIntent.OnSavedRoomsClick) },
                onCurrentLocationClick = onCurrentLocationClick,
                modifier = Modifier.padding(bottom = sheetHeight + ControlsSheetSpacing),
            )
        }

        val shareSheet = state.shareSheet
        if (place != null && shareSheet != null) {
            RoomShareSheet(
                placeName = place.name,
                placeAddress = place.address,
                placeImageUrl = place.imageUrls.firstOrNull(),
                state = shareSheet,
                onRoomToggle = { roomId -> onIntent(PlaceDetailIntent.OnShareRoomToggle(roomId)) },
                onShareClick = { onIntent(PlaceDetailIntent.OnShareConfirmClick) },
                onDismissRequest = { onIntent(PlaceDetailIntent.OnShareSheetDismiss) },
            )
        }

        // 공유 시트와 같은 자리에 겹치지만 둘이 함께 떠 있지는 않는다 — 어느 한쪽이 서면 그 딤이 나머지를 여는
        // 입구(시트 안 [다른방에 공유] · 지도 위 [저장된 방])를 덮어 다른 쪽을 열 수 없다.
        // 이쪽은 장소를 보여 주지 않고 옮겨 갈 방만 늘어놓으므로 `place`를 묻지 않는다.
        val savedRoomsSheet = state.savedRoomsSheet
        if (savedRoomsSheet != null) {
            SavedRoomsSheet(
                state = savedRoomsSheet,
                onRoomSelected = { pinId, roomId ->
                    onIntent(PlaceDetailIntent.OnSavedRoomSelected(pinId = pinId, roomId = roomId))
                },
                onDismissRequest = { onIntent(PlaceDetailIntent.OnSavedRoomsSheetDismiss) },
            )
        }
    }
}

/**
 * 주 데이터 조회가 실패했을 때의 화면. 안내 한 줄과 [다시 시도] 버튼이 전부다.
 *
 * 스낵바가 아니라 화면을 통째로 바꾸는 이유는 이것이 화면의 주 데이터이기 때문이다 — 잠깐 떴다 사라지는
 * 안내 뒤에 빈 시트를 남기면 사용자가 그 빈 화면을 「코멘트도 사진도 없는 장소」로 읽는다
 * (`docs/conventions/error_handling.md` §5).
 *
 * **여기에는 [나가기]가 없다.** 그 버튼은 장소명과 한 몸인 시트 헤더의 것이라 장소를 못 받은 자리에는 설 수
 * 없고, 화면을 벗어나는 길은 뒤로가기가 그대로 연다(`PlaceDetailRoute`).
 *
 * 호출부가 그린 지도가 뒤에 남지 않도록 배경을 직접 깐다. 시스템 바 뒤까지 꽉 차는 화면이라 안내와
 * 버튼만 인셋 안쪽으로 들인다.
 */
@Composable
private fun LoadErrorContent(
    error: MinoDomainException,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MinoAndroidTheme.colors.backgroundNormalNormal),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = SectionHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LoadErrorSpacing),
        ) {
            Text(
                text = stringResource(placeDetailErrorMessageRes(error)),
                style = MinoAndroidTheme.typography.body1NormalMedium,
                color = MinoAndroidTheme.colors.labelNeutral,
                textAlign = TextAlign.Center,
            )
            MinoButton(
                text = stringResource(R.string.placedetail_error_retry),
                onClick = onRetryClick,
                size = ButtonSize.Medium,
                style = ButtonStyle.OutlinedAssistive,
            )
        }
    }
}

/**
 * 장소 상세의 실패 문구. 리프를 가르지 않는다 — 어느 쪽이든 사용자가 할 수 있는 일이 재시도로 같아, 원인을
 * 나눠도 행동이 달라지지 않는다. 로드 실패(오류 화면)와 액션 실패(스낵바)가 같은 문구를 쓰는 것도 같은 이유이며,
 * 그래서 판정도 이 한 자리에 둔다.
 *
 * `else`를 두지 않아 리프가 늘면 컴파일이 멈추고 여기서 다시 판단하게 된다.
 * 공통 매퍼를 두지 않는 이유는 `docs/conventions/error_handling.md` §8이 소유한다.
 */
@StringRes
internal fun placeDetailErrorMessageRes(error: MinoDomainException): Int =
    when (error) {
        is MinoDomainException.Network,
        is MinoDomainException.Http,
        is MinoDomainException.Auth,
        -> R.string.placedetail_error_generic
    }

/**
 * 시트 위쪽 고정 자리를 차지하는 헤더. 밀도만 갈릴 뿐 언제나 하나다.
 *
 * **두 헤더를 한 자리에서 갈라 놓는 것이 핵심이다.** 확장형이 스크롤 콘텐츠에 있으면 축소형이 선 순간에도
 * 콘텐츠 맨 위에 그대로 남아 장소명과 [나가기]가 두 벌 보인다. 밀도가 갈리는 시점은 [headerMode]가 이미
 * 소유하므로(spec FR-008) 여기서는 그 값에 맞는 헤더 하나만 부른다.
 *
 * **[나가기]는 어느 밀도에서도 같은 콜백이다.** 두 헤더에서 같은 자리에 놓이고 같은 처리로 이어져야 하기
 * 때문이다(spec UX-002 · EC-006).
 */
@Composable
private fun PlaceDetailHeader(
    place: PlaceDetail,
    headerMode: PlaceHeaderMode,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (headerMode) {
        PlaceHeaderMode.EXPANDED -> PlaceDetailExpandedHeader(
            name = place.name,
            address = place.address,
            registrantNickname = place.registrant?.nickname,
            onCloseClick = onCloseClick,
            modifier = modifier,
        )

        PlaceHeaderMode.COLLAPSED -> PlaceDetailCollapsedHeader(
            name = place.name,
            onCloseClick = onCloseClick,
            modifier = modifier,
        )
    }
}

/**
 * 시트가 얹은 스크롤 축 위에 놓이는 것 전부.
 *
 * **헤더는 여기에 없다.** 시트의 고정 자리가 그것을 소유한다 — 이 축에도 두면 두 헤더가 함께 선다.
 *
 * **하단 시스템 인셋을 이 축의 바닥이 낸다.** 시트를 담은 컨테이너가 인셋만큼 부풀어 시트 아랫변이 화면
 * 바닥에 닿으므로(위 화면 KDoc), 이 축을 끝까지 내렸을 때 마지막 요소가 제스처 내비게이션 바에 붙는다.
 * 인셋을 여는 곳은 여기 하나뿐이어야 한다 — 컨테이너가 이미 되찾은 자리를 시트가 또 물러나면 두 번
 * 얹힌다. 입력 영역이 인셋을 들면 그 영역이 어디에 놓이든 여백을 데리고 다니게 되므로, 바닥에 닿는 축을
 * 소유한 이쪽이 낸다.
 * Figma가 액션 영역에 둔 고정 높이의 하단 안전 영역은 디자인 툴이 기기 인셋을 읽지 못해 박아둔 값이라
 * 그대로 옮기지 않는다(`MinoActionArea`).
 *
 * **사진이 없으면 캐러셀을 부르지 않는다.** 높이 0의 빈 캐러셀을 두는 것이 아니라 영역 자체가 사라져 액션 행
 * 아래로 코멘트가 곧바로 이어진다(spec EC-009).
 *
 * **코멘트가 0건인 갈래를 여기서 만들지 않는다.** 빈 상태로 넘어가는 판정은 [PlaceCommentList]가 소유한다
 * (spec EC-014).
 */
@OptIn(ExperimentalTime::class)
@Composable
private fun PlaceDetailSheetContent(
    place: PlaceDetail,
    state: PlaceDetailUiState,
    commentState: TextFieldState,
    onIntent: (PlaceDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        PlaceActionRow(
            isSourceEnabled = state.isSourceEnabled,
            onPlaceClick = { onIntent(PlaceDetailIntent.OnOpenMapClick) },
            onSourceClick = { onIntent(PlaceDetailIntent.OnOpenSourceClick) },
            onShareClick = { onIntent(PlaceDetailIntent.OnShareClick) },
        )

        if (place.imageUrls.isNotEmpty()) {
            val imageUrls = remember(place.imageUrls) { place.imageUrls.toImmutableList() }
            PlaceImageCarousel(
                imageUrls = imageUrls,
                currentPage = state.carouselPage,
                onPageChange = { page -> onIntent(PlaceDetailIntent.OnCarouselPageChange(page)) },
            )
        }

        SheetSectionDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = SectionHorizontalPadding,
                    end = SectionHorizontalPadding,
                    top = SectionTopPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(SectionSpacing),
        ) {
            Text(
                text = stringResource(R.string.placedetail_comment_section_title),
                style = MinoAndroidTheme.typography.headline1Bold,
                color = MinoAndroidTheme.colors.labelNormal,
            )
            PlaceCommentList(
                comments = state.comments,
                commentsObservedAt = state.commentsObservedAt,
                hasOlderComments = state.hasOlderComments,
                onLoadOlderComments = { onIntent(PlaceDetailIntent.OnLoadOlderComments) },
                onDeleteComment = { commentId ->
                    onIntent(PlaceDetailIntent.OnDeleteCommentClick(commentId))
                },
            )
            PlaceCommentInput(
                state = commentState,
                isSubmitEnabled = state.isSubmitEnabled,
                onSubmitClick = { onIntent(PlaceDetailIntent.OnSubmitCommentClick) },
            )
        }
    }
}

/**
 * 스크롤이 최상단인지를 올린다. 헤더 밀도를 가르는 유일한 근거이며(spec FR-008), 콘텐츠가 시트보다 짧아
 * 스크롤이 일어나지 않으면 계속 최상단이라 확장형에 머문다(spec EC-007).
 */
@Composable
private fun ReportScrollPosition(
    scrollState: ScrollState,
    onIntent: (PlaceDetailIntent) -> Unit,
) {
    val currentOnIntent by rememberUpdatedState(onIntent)
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value == 0 }
            .collect { isAtTop -> currentOnIntent(PlaceDetailIntent.OnScrollOffsetChange(isAtTop)) }
    }
}

/**
 * 앞에 붙은 페이지만큼 스크롤을 밀어 보던 자리를 지킨다.
 *
 * 오래된 코멘트는 목록 **앞**에 붙으므로(spec FR-010) 스크롤 값이 그대로면 읽던 줄이 그만큼 아래로 밀려난다.
 * 스크롤 축을 소유한 것이 이 화면이라 보정도 여기서 한다.
 *
 * 맨 위 코멘트가 바뀌는 것을 계기로 삼고, 그 시점의 콘텐츠 높이를 기준으로 둔 뒤 배치가 끝나 높이가 달라지면
 * 그 차이만큼 스크롤을 옮긴다. 코멘트가 뒤에 붙거나 지워지는 경우에는 맨 위가 그대로라 보정이 일어나지 않는다.
 */
@Composable
private fun KeepPositionOnPrepend(
    scrollState: ScrollState,
    topCommentId: String?,
) {
    val currentTopCommentId by rememberUpdatedState(topCommentId)
    LaunchedEffect(scrollState) {
        var knownTopCommentId: String? = null
        var observedHeight = scrollState.maxValue
        var heightBeforeChange: Int? = null

        snapshotFlow { currentTopCommentId to scrollState.maxValue }
            .collect { (currentTop, contentHeight) ->
                if (currentTop != knownTopCommentId) {
                    // 첫 페이지가 도착하는 순간에는 되돌릴 자리가 없다.
                    heightBeforeChange = observedHeight.takeIf { knownTopCommentId != null }
                    knownTopCommentId = currentTop
                }
                val before = heightBeforeChange
                if (before != null && contentHeight != before) {
                    scrollState.scrollTo(scrollState.value + contentHeight - before)
                    heightBeforeChange = null
                }
                observedHeight = contentHeight
            }
    }
}

private val SectionHorizontalPadding = 20.dp

private val SectionTopPadding = 20.dp

private val SectionSpacing = 30.dp

/** 지도 컨트롤 행과 시트 윗변 사이. */
private val ControlsSheetSpacing = 16.dp

/** 로드 실패 화면에는 대조할 디자인이 없다. 이 값은 디자인이 그려지면 그때 맞춘다. */
private val LoadErrorSpacing = 16.dp
