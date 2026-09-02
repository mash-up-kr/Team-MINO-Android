package team.mino.feature.room.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.image.MinoAsyncImage

/**
 * 장소 사진 한 장. 아직 못 받았거나 로딩에 실패하면 자리표시자 글리프로 대신한다(spec EC-010).
 *
 * **장소 상세 캐러셀의 한 장과 공유 시트의 대상 장소 썸네일이 같은 것을 그린다** — 실패 표현을 바꿀 때 두
 * 곳을 찾지 않는다. 쓰는 화면이 둘이라 화면 패키지가 아니라 모듈 루트에 있다
 * (`docs/architecture/feature-module.md` 「모듈 루트 `component/`」).
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

/** 장소 상세 캐러셀 한 장의 크기 — 이 컴포저블의 첫 소비자가 정한 기본값이다. */
private val ImageSize = 240.dp

// Figma md 변수 대응 — 토큰 미존재
private val ImageShape: Shape = RoundedCornerShape(16.dp)
