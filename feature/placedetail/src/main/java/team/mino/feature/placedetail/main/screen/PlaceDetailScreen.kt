package team.mino.feature.placedetail.main.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import team.mino.feature.placedetail.R
import team.mino.feature.placedetail.main.component.PlaceActionRow
import team.mino.feature.placedetail.main.component.PlaceCommentInput
import team.mino.feature.placedetail.main.component.PlaceCommentList
import team.mino.feature.placedetail.main.component.PlaceDetailCollapsedHeader
import team.mino.feature.placedetail.main.component.PlaceDetailExpandedHeader
import team.mino.feature.placedetail.main.component.PlaceDetailMap
import team.mino.feature.placedetail.main.component.PlaceDetailSheet
import team.mino.feature.placedetail.main.component.PlaceImageCarousel
import team.mino.feature.placedetail.main.component.PlaceMapControls
import team.mino.feature.placedetail.main.component.RoomShareSheet
import team.mino.feature.placedetail.main.component.SheetSectionDivider
import team.mino.feature.placedetail.main.model.PlaceHeaderMode
import team.mino.feature.placedetail.main.vm.PlaceDetailIntent
import team.mino.feature.placedetail.main.vm.PlaceDetailUiState

/**
 * 장소 상세 화면. 지도 위에 시트가 겹쳐 서고, 시트 안에 헤더·액션 행·캐러셀·코멘트가 한 축으로 이어진다.
 *
 * **스크롤 축을 이 화면이 소유한다.** 시트가 그 축 위에 콘텐츠를 얹고 헤더 밀도는 그 축이 최상단인지로
 * 갈리므로(spec FR-008), 축을 만들어 시트에 넘기는 것도 그 위치를 인텐트로 올리는 것도 여기다.
 *
 * **`Full`의 윗변은 상태바 아래다.** 시트는 자기가 놓인 자리의 높이를 그대로 `Full` 높이로 쓰므로, 상태바만큼을
 * 빼서 넘기는 것이 시트를 놓는 이 화면의 몫이다. 시트 아래로는 여백을 두지 않는다 — 화면 하단에 붙는다.
 *
 * **헤더는 시트의 고정 자리 하나뿐이다.** 두 밀도는 그 한 자리를 번갈아 차지하므로 어느 순간에도 헤더가
 * 둘일 수 없다 — 확장형을 스크롤 콘텐츠에 두면 축소형이 선 뒤에도 그것이 콘텐츠 맨 위에 남아 장소명과
 * [나가기]가 두 벌 보인다. 액션 행은 그 자리에 넣지 않고 콘텐츠와 함께 스크롤된다. spec FR-008이 고정 영역을
 * 「장소명 + [나가기]」로 규정하기 때문이다.
 *
 * **주 데이터 조회가 깨지면 지도도 시트도 그리지 않는다.** 장소·코멘트·방 목록은 이 화면이 그릴 것의 전부라,
 * 셋 중 하나라도 못 받은 채로는 남은 것을 그려 봐야 사용자가 보는 것이 실제 상태와 갈린다. 화면 전체를
 * 재시도 가능한 오류로 바꾸는 것이 에러 처리 규약 §5의 규정이다.
 *
 * **지도 컨트롤은 지도가 아니라 이 화면이 놓는다.** 시트보다 위에 그려져야 시트 그림자에 덮이지 않으므로
 * 시트 다음 순서로 얹고, 세로 자리는 시트가 지금 차지한 높이에서 낸다 — 시트 높이를 다시 상수로 들면
 * 두 출처가 갈린다.
 *
 * @param commentState 코멘트 입력 버퍼. 소유자는 `PlaceDetailRoute`다 — 등록이 끝난 뒤 버퍼를 비우는 것도
 *   그쪽이라 이 화면은 받아서 넘기기만 한다.
 */
@Composable
internal fun PlaceDetailScreen(
    state: PlaceDetailUiState,
    commentState: TextFieldState,
    onIntent: (PlaceDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    var sheetHeight by remember { mutableStateOf(0.dp) }

    ReportScrollPosition(scrollState = scrollState, onIntent = onIntent)
    KeepPositionOnPrepend(scrollState = scrollState, topCommentId = state.comments.firstOrNull()?.id)

    val place = state.place
    val loadError = state.loadError

    Box(modifier = modifier.fillMaxSize()) {
        if (loadError != null) {
            LoadErrorContent(
                error = loadError,
                onRetryClick = { onIntent(PlaceDetailIntent.OnRetryLoadClick) },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            if (place != null) {
                PlaceDetailMap(
                    location = place.location,
                    roomColor = state.roomColor,
                )
            }

            PlaceDetailSheet(
                onLevelChange = { onIntent(PlaceDetailIntent.OnSheetLevelChange(it)) },
                onExitRequest = { onIntent(PlaceDetailIntent.OnExitClick) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .statusBarsPadding()
                    .onSizeChanged { size -> sheetHeight = with(density) { size.height.toDp() } },
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = sheetHeight + ControlsSheetSpacing),
            )

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
 * 지도가 뒤에 남지 않도록 배경을 직접 깐다. 시스템 바 뒤까지 꽉 차는 화면이라(`PlaceDetailShell`) 안내와
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
                text = stringResource(loadErrorMessageRes(error)),
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
 * 로드 실패 문구. 리프를 가르지 않는다 — 어느 쪽이든 사용자가 할 수 있는 일이 재시도로 같아, 원인을 나눠도
 * 행동이 달라지지 않는다. 스낵바로 나가는 액션 실패와 같은 문구를 쓰는 것도 같은 이유다.
 *
 * `else`를 두지 않아 리프가 늘면 컴파일이 멈추고 여기서 다시 판단하게 된다.
 * 공통 매퍼를 두지 않는 이유는 `docs/conventions/error_handling.md` §8이 소유한다.
 */
@StringRes
private fun loadErrorMessageRes(error: MinoDomainException): Int =
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
            label = place.label,
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
 * **하단 시스템 인셋을 이 축의 바닥이 낸다.** 시트가 화면 바닥에 붙어 있고 셸도 인셋을 소비하지 않아
 * (`PlaceDetailShell`), 이 축을 끝까지 내렸을 때 마지막 요소가 제스처 내비게이션 바에 붙는다. 인셋을 입력
 * 영역이 들면 그 영역이 어디에 놓이든 여백을 데리고 다니게 되므로, 바닥에 닿는 축을 소유한 이쪽이 낸다.
 * Figma가 액션 영역에 둔 고정 높이의 하단 안전 영역은 디자인 툴이 기기 인셋을 읽지 못해 박아둔 값이라
 * 그대로 옮기지 않는다(`MinoActionArea`).
 *
 * **사진이 없으면 캐러셀을 부르지 않는다.** 높이 0의 빈 캐러셀을 두는 것이 아니라 영역 자체가 사라져 액션 행
 * 아래로 코멘트가 곧바로 이어진다(spec EC-009).
 *
 * **코멘트가 0건인 갈래를 여기서 만들지 않는다.** 빈 상태로 넘어가는 판정은 [PlaceCommentList]가 소유한다
 * (spec EC-014).
 */
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
