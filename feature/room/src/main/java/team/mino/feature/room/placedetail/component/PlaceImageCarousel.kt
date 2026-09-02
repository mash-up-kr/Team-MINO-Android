package team.mino.feature.room.placedetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * 장소 사진을 한 장씩 넘겨 보는 캐러셀.
 *
 * **다음 장이 오른쪽 경계에 걸쳐 보인다.** 그 걸침이 더 있다는 신호를 대신하므로 점·숫자 같은 인디케이터를
 * 따로 두지 않는다(spec UX-003). 사진이 한 장뿐이면 넘길 곳이 없어 밀어도 그대로 있는다(spec EC-008).
 *
 * **비어 있는 경우를 여기서 가르지 않는다.** 사진이 없으면 캐러셀 영역 자체가 사라지는 것이라
 * (spec EC-009) 그 판단은 이 컴포저블을 놓을지 말지를 정하는 화면의 몫이다.
 *
 * @param imageUrls 넘겨 볼 사진. 한 장이 한 페이지다.
 * @param currentPage 지금 보고 있는 페이지. 상태를 이 컴포저블이 들지 않아 지도·원문 링크로 나갔다 돌아와도
 *   보던 장이 그대로다(spec UX-009).
 * @param onPageChange 넘겨진 페이지를 올린다.
 */
@Composable
internal fun PlaceImageCarousel(
    imageUrls: ImmutableList<String>,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = currentPage) { imageUrls.size }
    val fallbackPainter = rememberVectorPainter(MinoIcons.Image)
    val latestOnPageChange by rememberUpdatedState(onPageChange)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { latestOnPageChange(it) }
    }

    // 페이지의 단일 출처는 넘겨받은 상태다. 밖에서 달라지면 캐러셀이 따라간다.
    LaunchedEffect(currentPage) {
        if (currentPage != pagerState.currentPage) {
            pagerState.scrollToPage(currentPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ContentHorizontalPadding, vertical = ContentVerticalPadding),
        pageSize = PageSize.Fixed(ImageSize),
        pageSpacing = ImageSpacing,
    ) { page ->
        PlaceImage(imageUrl = imageUrls[page], fallback = fallbackPainter)
    }
}

/**
 * 장소 사진 한 장. 아직 못 받았거나 로딩에 실패하면 자리표시자 글리프로 대신한다(spec EC-010).
 *
 * 캐러셀의 한 장과 공유 시트의 대상 장소 썸네일이 같은 것을 그린다 — 실패 표현을 바꿀 때 두 곳을 찾지 않는다.
 *
 * [fallback]을 밖에서 받는 것은 `rememberVectorPainter`가 페인터마다 서브컴포지션을 하나씩 세우기 때문이다.
 * 페이지 안에서 만들면 동시에 살아 있는 장 수만큼 벡터 컴포지션이 생기고 페이지가 재활용될 때마다 다시 선다.
 * 페인터는 상태가 없어 장끼리 나눠 써도 안전하다.
 */
@Composable
internal fun PlaceImage(
    imageUrl: String?,
    fallback: Painter,
    modifier: Modifier = Modifier,
    size: Dp = ImageSize,
    shape: Shape = ImageShape,
) {
    MinoAsyncImage(
        imageUrl = imageUrl,
        fallback = fallback,
        fallbackTint = MinoAndroidTheme.colors.labelAssistive,
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(MinoAndroidTheme.colors.fillNormal),
        fallbackModifier = Modifier.padding(size / 4),
    )
}

private val ImageSize = 240.dp

private val ImageSpacing = 12.dp

private val ContentHorizontalPadding = 20.dp

private val ContentVerticalPadding = 12.dp

// Figma md 변수 대응 — 토큰 미존재
private val ImageShape = RoundedCornerShape(16.dp)

@UiModePreviews
@Composable
private fun PlaceImageCarouselPreview() {
    MinoAndroidAppTheme {
        PlaceImageCarousel(
            imageUrls = persistentListOf("place-a", "place-b", "place-c"),
            currentPage = 0,
            onPageChange = {},
        )
    }
}

@UiModePreviews
@Composable
private fun PlaceImageCarouselSinglePreview() {
    MinoAndroidAppTheme {
        PlaceImageCarousel(
            imageUrls = persistentListOf("place-a"),
            currentPage = 0,
            onPageChange = {},
        )
    }
}
