package team.mino.core.designsystem.util.image

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * [imageUrl]을 Coil로 비동기 로드한다. url이 없거나 **로딩에 실패하면** [fallback] 글리프로 대체한다.
 *
 * @param fallback placeholder로 보여줄 아이콘.
 * @param fallbackTint [fallback] tint.
 * @param modifier 렌더링되는 쪽(이미지 또는 아이콘)에 그대로 적용된다.
 */
@Composable
internal fun FallbackAsyncImage(
    imageUrl: String?,
    fallback: Painter,
    fallbackTint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    // imageUrl이 바뀌면 이전 실패 상태를 리셋한다.
    var loadFailed by remember(imageUrl) { mutableStateOf(false) }

    if (imageUrl != null && !loadFailed) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            onError = { loadFailed = true },
        )
    } else {
        Icon(
            painter = fallback,
            contentDescription = contentDescription,
            tint = fallbackTint,
            modifier = modifier,
        )
    }
}
