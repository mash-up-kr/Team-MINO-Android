package team.mino.feature.placedetail.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.model.RoomColor
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.placedetail.fake.FakePlaceDetailData
import team.mino.feature.placedetail.main.component.previewRoomPickerItems
import team.mino.feature.placedetail.main.model.PlaceCommentUiModel
import team.mino.feature.placedetail.main.model.PlaceHeaderMode
import team.mino.feature.placedetail.main.model.PlaceSheetLevel
import team.mino.feature.placedetail.main.model.toUiModels
import team.mino.feature.placedetail.main.vm.PlaceDetailUiState
import team.mino.feature.placedetail.main.vm.ShareSheetUiState
import java.io.IOException

/*
 * PlaceDetailScreen의 상태별 렌더 프리뷰.
 *
 * 화면은 stateless라 상태를 직접 만들어 넘긴다. 장소·코멘트 샘플은 지어내지 않고 FakePlaceDetailData에서
 * 가져오며, 한 장이 여러 조건을 겹쳐 들고 있는 샘플은 보고 싶은 조건 하나만 copy로 갈아 끼운다 — 그래야
 * 두 프리뷰의 차이가 그 하나로 좁혀진다.
 *
 * 시트는 높이가 유계인 자리에 놓여야 FULL이 설 자리를 안다. 프리뷰는 기기 크기를 주지 않으므로 컨테이너가
 * 화면만 한 크기를 직접 만들어 준다.
 *
 * 코멘트 입력 버퍼는 화면 밖(Route)이 소유하므로 프리뷰도 같은 자리에서 만들어 넘긴다.
 */

/** 상세가 아직 도착하지 않은 구간. 지도도 시트 콘텐츠도 없이 빈 시트만 서 있다. */
@UiModePreviews
@Composable
private fun PlaceDetailScreenLoadingPreview() {
    PlaceDetailScreenPreviewContainer(PlaceDetailUiState(pinId = FakePlaceDetailData.DEFAULT_PIN_ID))
}

/**
 * 주 데이터 조회가 실패한 구간. 지도도 시트도 없이 안내와 [다시 시도]만 선다.
 *
 * 리프를 갈라도 문구가 같아 한 장만 둔다. 원인 예외는 프리뷰가 리프를 만들려면 있어야 하는 자리라
 * 아무것이나 채운다 — 화면에 드러나지 않는다.
 */
@UiModePreviews
@Composable
private fun PlaceDetailScreenLoadErrorPreview() {
    PlaceDetailScreenPreviewContainer(
        PlaceDetailUiState(
            pinId = FakePlaceDetailData.DEFAULT_PIN_ID,
            loadError = MinoDomainException.Network(IOException()),
        ),
    )
}

/** 기본 상태 — 사진 3장에 코멘트 3건. 아래 프리뷰들이 여기서 조건 하나씩만 달리한다. */
@UiModePreviews
@Composable
private fun PlaceDetailScreenPreview() {
    PlaceDetailScreenPreviewContainer(baseState())
}

/**
 * 장소명과 주소가 한 줄에 담기지 않는 샘플.
 *
 * 두 줄로 늘어나지 않고 한 줄을 지킨 채 `...`으로 잘리는지를 본다.
 */
@UiModePreviews
@Composable
private fun PlaceDetailScreenLongPlaceNamePreview() {
    PlaceDetailScreenPreviewContainer(baseState(place = LONG_NAME_PLACE))
}

/** 등록자가 없는 장소. 확장형 헤더의 등록자 자리에 기본 아바타가 선다(spec EC-004). */
@UiModePreviews
@Composable
private fun PlaceDetailScreenNoRegistrantPreview() {
    PlaceDetailScreenPreviewContainer(baseState(place = BASE_PLACE.copy(registrant = null)))
}

/** 사진이 한 장도 없는 장소. 캐러셀 영역 자체가 사라져 액션 행 아래로 코멘트가 곧바로 이어진다(spec EC-009). */
@UiModePreviews
@Composable
private fun PlaceDetailScreenNoImagePreview() {
    PlaceDetailScreenPreviewContainer(baseState(place = BASE_PLACE.copy(imageUrls = emptyList())))
}

/** 원문 링크가 없는 장소. 액션 행의 [원문보기]가 비활성으로 그려진다(spec EC-017). */
@UiModePreviews
@Composable
private fun PlaceDetailScreenNoSourceLinkPreview() {
    PlaceDetailScreenPreviewContainer(baseState(place = BASE_PLACE.copy(sourceUrl = null)))
}

/** 코멘트가 한 건도 없는 장소. 목록 자리를 빈 상태가 대신한다(spec EC-014). */
@UiModePreviews
@Composable
private fun PlaceDetailScreenNoCommentPreview() {
    PlaceDetailScreenPreviewContainer(baseState(comments = persistentListOf()))
}

/**
 * 코멘트가 한 페이지를 넘는 장소.
 *
 * 최신 한 페이지만 실려 있고 위로 더 받을 것이 남아 있는 시점이라, 목록 위쪽에 더 오래된 코멘트를 부르는
 * 자리가 함께 보인다(spec FR-010).
 */
@UiModePreviews
@Composable
private fun PlaceDetailScreenManyCommentsPreview() {
    PlaceDetailScreenPreviewContainer(
        baseState(comments = latestCommentPageOf(FakePlaceDetailData.PIN_ID_COMMENTS_MANY))
            .copy(hasOlderComments = true),
    )
}

/**
 * 시트를 끝까지 올리고 콘텐츠를 내려 읽는 중.
 *
 * 확장형 헤더는 콘텐츠와 함께 밀려 올라가고 「장소명 + [나가기]」만 상단에 남는다(spec FR-008).
 */
@UiModePreviews
@Composable
private fun PlaceDetailScreenCollapsedHeaderPreview() {
    PlaceDetailScreenPreviewContainer(
        baseState().copy(
            sheetLevel = PlaceSheetLevel.FULL,
            headerMode = PlaceHeaderMode.COLLAPSED,
        ),
    )
}

/** [다른방에 공유] 시트가 열려 화면 위에 겹친 상태. 한 방을 골라 [공유하기]가 열려 있다(spec FR-018·FR-022). */
@UiModePreviews
@Composable
private fun PlaceDetailScreenShareSheetPreview() {
    PlaceDetailScreenPreviewContainer(
        baseState().copy(
            shareSheet = ShareSheetUiState(
                rooms = previewRoomPickerItems(),
                selectedRoomIds = persistentSetOf(SELECTED_ROOM_ID),
            ),
        ),
    )
}

@Composable
private fun PlaceDetailScreenPreviewContainer(
    state: PlaceDetailUiState,
    modifier: Modifier = Modifier,
) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .size(width = PreviewWidth, height = PreviewHeight)
                .background(MinoAndroidTheme.colors.backgroundNormalNormal),
        ) {
            PlaceDetailScreen(
                state = state,
                commentState = rememberTextFieldState(state.commentDraft),
                onIntent = {},
            )
        }
    }
}

/** 조건 하나만 갈아 끼울 수 있게, 상세가 다 도착한 시점의 상태를 한 자리에서 만든다. */
private fun baseState(
    place: PlaceDetail = BASE_PLACE,
    comments: ImmutableList<PlaceCommentUiModel> = BASE_COMMENTS,
): PlaceDetailUiState =
    PlaceDetailUiState(
        pinId = place.pinId,
        place = place,
        roomColor = RoomColor.CYAN,
        comments = comments,
    )

private fun latestCommentPageOf(pinId: String): ImmutableList<PlaceCommentUiModel> =
    FakePlaceDetailData.commentsOf(pinId).takeLast(FakePlaceDetailData.PAGE_SIZE).toUiModels()

/** 사진 3장 · 원문 링크 있음 · 등록자 있음. 긴 장소명·주소는 그것만 보는 프리뷰에 남긴다. */
private val BASE_PLACE: PlaceDetail =
    FakePlaceDetailData.placeOf(FakePlaceDetailData.PIN_ID_IMAGES_THREE).let { longNamePlace ->
        val shortNamePlace = FakePlaceDetailData.placeOf(FakePlaceDetailData.PIN_ID_IMAGES_NONE)
        longNamePlace.copy(name = shortNamePlace.name, address = shortNamePlace.address)
    }

private val LONG_NAME_PLACE: PlaceDetail = FakePlaceDetailData.placeOf(FakePlaceDetailData.PIN_ID_IMAGES_THREE)

/** 200자 꽉 찬 한 건과 canDelete가 갈리는 건이 섞여 있다. */
private val BASE_COMMENTS: ImmutableList<PlaceCommentUiModel> =
    FakePlaceDetailData.commentsOf(FakePlaceDetailData.PIN_ID_IMAGES_THREE).toUiModels()

private const val SELECTED_ROOM_ID = "shared-2"

// 시트가 놓일 자리의 높이가 정해져야 FULL과 HALF가 갈린다. 일반적인 세로 화면 크기를 흉내 낸다.
private val PreviewWidth = 360.dp

private val PreviewHeight = 800.dp
