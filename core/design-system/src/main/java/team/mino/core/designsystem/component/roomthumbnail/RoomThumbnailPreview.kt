package team.mino.core.designsystem.component.roomthumbnail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * `Room Thumbnail`의 콘텐츠 패턴별 프리뷰.
 *
 * Figma는 이 컴포넌트를 사진 개수별 컴포넌트셋으로 나눠 두었고 문서 페이지의 속성 축과 이름이
 * 대응하지 않아, 블록 헤딩은 코드 파라미터명(`imageUrls`)과 그 개수를 쓴다.
 *
 * 프리뷰는 네트워크를 타지 않으므로 콜라주 칸은 placeholder 글리프로 그려진다. 배치(칸 수와
 * 분할 방향)를 대조하는 용도다.
 */
@UiModePreviews
@Composable
private fun RoomThumbnailPreview() {
    PreviewPage {
        PreviewProperty(name = "imageUrls", values = "0 · 1 · 2 · 3 · 4") {
            PreviewRow {
                PreviewThumbnail(count = 0)
                PreviewThumbnail(count = 1)
                PreviewThumbnail(count = 2)
            }
            PreviewRow {
                PreviewThumbnail(count = 3)
                PreviewThumbnail(count = 4)
            }
        }
    }
}

@Composable
private fun PreviewThumbnail(
    count: Int,
    modifier: Modifier = Modifier,
) {
    MinoRoomThumbnail(
        imageUrls = previewImageUrls(count),
        modifier = modifier,
        fallback = { PreviewFallback() },
    )
}

/**
 * 폴백은 `:core:common:ui`가 그리므로 여기서는 슬롯이 채워지는 것만 보이면 된다. 이 색은 디자인
 * 근거가 없는 프리뷰 전용 자리표시다.
 */
@Composable
private fun PreviewFallback(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(ColorAccessKeyToken.FillStrong.value))
}

private fun previewImageUrls(count: Int): ImmutableList<String> = PreviewImageUrls.take(count).toImmutableList()

private val PreviewImageUrls = persistentListOf(
    "https://mino.example/preview/1.jpg",
    "https://mino.example/preview/2.jpg",
    "https://mino.example/preview/3.jpg",
    "https://mino.example/preview/4.jpg",
)
