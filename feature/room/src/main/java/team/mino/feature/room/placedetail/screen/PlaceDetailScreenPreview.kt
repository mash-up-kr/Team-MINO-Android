@file:OptIn(ExperimentalTime::class)

package team.mino.feature.room.placedetail.screen

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
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.PlaceComment
import team.mino.core.domain.model.PlaceCommentAuthor
import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.model.PlaceRegistrant
import team.mino.core.domain.model.RoomColor
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.room.component.previewRoomShareItems
import team.mino.feature.room.placedetail.model.PlaceCommentUiModel
import team.mino.feature.room.placedetail.model.PlaceHeaderMode
import team.mino.feature.room.placedetail.model.PlaceSheetLevel
import team.mino.feature.room.placedetail.model.toUiModels
import team.mino.feature.room.placedetail.vm.PlaceDetailUiState
import team.mino.feature.room.placedetail.vm.ShareSheetUiState
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/*
 * PlaceDetailScreen의 상태별 렌더 프리뷰.
 *
 * 화면은 stateless라 상태를 직접 만들어 넘긴다. 장소·코멘트 샘플은 이 파일이 직접 든다 — 지난 라운드에는
 * Fake Repository와 원천 하나를 함께 썼으나 그 Fake가 실 API로 교체되면서 사라졌다. 한 장이 여러 조건을
 * 겹쳐 들고 있는 샘플은 보고 싶은 조건 하나만 copy로 갈아 끼운다 — 그래야 두 프리뷰의 차이가 그 하나로 좁혀진다.
 *
 * 화면은 지도를 그리지 않는다. 실제로는 호출부(RoomListScreen)의 RoomListMap이 뒤에 깔리지만 프리뷰의
 * 컨테이너는 그 자리를 배경색으로만 채운다 — 이 프리뷰가 보는 것은 그 위에 얹히는 시트·컨트롤·오버레이다.
 *
 * 시트는 높이가 유계인 자리에 놓여야 FULL이 설 자리를 안다. 프리뷰는 기기 크기를 주지 않으므로 컨테이너가
 * 화면만 한 크기를 직접 만들어 준다.
 *
 * 코멘트 입력 버퍼는 화면 밖(Route)이 소유하므로 프리뷰도 같은 자리에서 만들어 넘긴다.
 */

/** 상세가 아직 도착하지 않은 구간. 시트 콘텐츠 없이 빈 시트만 서 있다. */
@UiModePreviews
@Composable
private fun PlaceDetailScreenLoadingPreview() {
    PlaceDetailScreenPreviewContainer(PlaceDetailUiState(pinId = BASE_PLACE.pinId))
}

/**
 * 주 데이터 조회가 실패한 구간. 시트도 컨트롤도 없이 안내와 [다시 시도]만 선다.
 *
 * 리프를 갈라도 문구가 같아 한 장만 둔다. 원인 예외는 프리뷰가 리프를 만들려면 있어야 하는 자리라
 * 아무것이나 채운다 — 화면에 드러나지 않는다.
 */
@UiModePreviews
@Composable
private fun PlaceDetailScreenLoadErrorPreview() {
    PlaceDetailScreenPreviewContainer(
        PlaceDetailUiState(
            pinId = BASE_PLACE.pinId,
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
        baseState(comments = LATEST_COMMENT_PAGE).copy(hasOlderComments = true),
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
                rooms = previewRoomShareItems(),
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
                onCurrentLocationClick = {},
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
        comments = comments,
    )

private const val PREVIEW_ROOM_ID = "preview-room-1"

/** 코멘트 입력 상한(spec FR-012). 상한이 꽉 찬 샘플 한 건을 만드는 데 쓴다. */
private const val COMMENT_MAX_LENGTH = 200

/** 서버 기본 페이지 크기를 흉내 낸다 — 이 크기로 잘려야 목록 위에 「더 오래된 코멘트」 자리가 생긴다. */
private const val COMMENT_PAGE_SIZE = 20

private const val MANY_COMMENT_COUNT = 45

private const val SELECTED_ROOM_ID = "shared-2"

private const val FILLER_SENTENCE =
    "여기 루프탑에서 보는 노을이 진짜 좋아요. 브런치는 오픈런이 아니면 웨이팅이 길고, 주차는 골목 안쪽 공영주차장이 편해요. "

/**
 * 길이가 정확히 [COMMENT_MAX_LENGTH]인 코멘트 본문.
 *
 * 문장을 이어 붙인 뒤 잘라 길이를 맞춘다 — 손으로 센 문자열을 두면 고칠 때마다 다시 세어야 한다.
 */
private fun maxLengthContent(prefix: String = ""): String =
    buildString {
        append(prefix)
        while (length < COMMENT_MAX_LENGTH) {
            append(FILLER_SENTENCE)
        }
    }.take(COMMENT_MAX_LENGTH)

private val REGISTRANT = PlaceRegistrant(
    userId = "preview-user-1",
    nickname = "성수동산책러",
    avatarColor = RoomColor.LIME,
)

private val COMMENT_AUTHORS = listOf(
    PlaceCommentAuthor(userId = "preview-user-1", nickname = "성수동산책러", avatarColor = RoomColor.LIME),
    PlaceCommentAuthor(userId = "preview-user-2", nickname = "주말미식가", avatarColor = null),
    PlaceCommentAuthor(userId = "preview-user-3", nickname = "카페탐험대", avatarColor = RoomColor.VIOLET),
    PlaceCommentAuthor(userId = "preview-user-4", nickname = "동네한바퀴", avatarColor = RoomColor.RED_ORANGE),
)

/** 내 코멘트. `canDelete`가 `true`라 [⋮]가 붙는다(spec FR-015). */
private val ME = COMMENT_AUTHORS[0]

/** 사진 3장 · 원문 링크 있음 · 등록자 있음. 긴 장소명·주소는 그것만 보는 프리뷰에 남긴다. */
private val BASE_PLACE = PlaceDetail(
    pinId = "preview-pin-1",
    roomId = PREVIEW_ROOM_ID,
    placeId = "preview-place-1",
    name = "이름도 주소도 짧은 동네 분식집",
    address = "서울특별시 광진구 아차산로 200",
    location = GeoPoint(latitude = 37.5446, longitude = 127.0559),
    imageUrls = listOf(previewImageUrl("place-a"), previewImageUrl("place-b"), previewImageUrl("place-c")),
    registrant = REGISTRANT,
    sourceUrl = "https://www.instagram.com/p/preview-place-detail-source/",
    mapUrl = "https://map.naver.com/p/entry/place/1000000001",
)

private val LONG_NAME_PLACE = BASE_PLACE.copy(
    pinId = "preview-pin-long-name",
    name = "성수동 골목 안쪽에 숨어 있는 통유리 루프탑 브런치 카페 미노스테이션 2호점",
    address = "서울특별시 성동구 아차산로17길 48 성수낙낙 지하 1층 101호 (성수동2가)",
)

/** 200자 꽉 찬 한 건과 `canDelete`가 갈리는 건이 섞여 있다. */
private val BASE_COMMENTS: ImmutableList<PlaceCommentUiModel> = listOf(
    previewComment(id = "preview-comment-1", content = "여기 웨이팅 길어요. 평일 오전 추천!", author = COMMENT_AUTHORS[1]),
    previewComment(id = "preview-comment-2", content = maxLengthContent(), author = ME),
    previewComment(id = "preview-comment-3", content = "주차 자리 넉넉했어요 👍", author = COMMENT_AUTHORS[2]),
).toUiModels()

/**
 * 코멘트 [MANY_COMMENT_COUNT]건 중 최신 한 페이지.
 *
 * 본문에 순번을 적어 두어 위로 더 받았을 때 **더 오래된** 것이 앞에 붙는지 눈으로 확인할 수 있다(spec FR-010).
 */
private val LATEST_COMMENT_PAGE: ImmutableList<PlaceCommentUiModel> = List(MANY_COMMENT_COUNT) { index ->
    val ordinal = index + 1
    previewComment(
        id = "preview-comment-many-$ordinal",
        content = "${ordinal}번째 코멘트 · 페이지 경계를 넘어 오래된 것이 위에 붙는지 보는 샘플이에요.",
        author = COMMENT_AUTHORS[index % COMMENT_AUTHORS.size],
    )
}.takeLast(COMMENT_PAGE_SIZE).toUiModels()

private fun previewComment(
    id: String,
    content: String,
    author: PlaceCommentAuthor,
) = PlaceComment(
    id = id,
    content = content,
    createdAt = Clock.System.now(),
    author = author,
    canDelete = author == ME,
)

/** 캐러셀 스와이프를 눈으로 구분하려면 장마다 다른 그림이어야 해서 seed를 준다. */
private fun previewImageUrl(seed: String) = "https://picsum.photos/seed/$seed/1080/720"

// 시트가 놓일 자리의 높이가 정해져야 FULL과 HALF가 갈린다. 일반적인 세로 화면 크기를 흉내 낸다.
private val PreviewWidth = 360.dp

private val PreviewHeight = 800.dp
