package team.mino.core.designsystem.component.roomthumbnail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.roomthumbnail.token.RoomThumbnailTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.util.image.MinoAsyncImage

/**
 * 방에 저장된 장소 사진을 모아 보여주는 정사각형 썸네일(Figma `Room Thumbnail`).
 *
 * **콜라주 배치만 소유한다.** 방 대표 색이나 캐릭터 이미지는 알지 않으며, 사진이 없을 때 무엇을
 * 그릴지는 [fallback] 슬롯을 주는 호출부가 정한다. `:core:design-system`이 `:core:domain`을
 * 의존하지 않고 이미지 에셋도 받지 않기 때문이다.
 *
 * @param imageUrls 콜라주에 넣을 사진 URL. 비어 있으면 [fallback]을 그린다. **네 장 이하만 들어온다** —
 *   그보다 많으면 호출부가 앞에서부터 잘라 넘긴다. 레이아웃은 장수(1·2·3·4)마다 다르다(Figma `Room Thumbnail`).
 * @param fallback 사진이 한 장도 없을 때 썸네일 자리를 채울 내용. 썸네일과 같은 정사각형을 채우고
 *   같은 모서리로 잘린다.
 */
@Composable
fun MinoRoomThumbnail(
    imageUrls: ImmutableList<String>,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(MinoRoomThumbnailDefaults.size)
            .clip(MinoRoomThumbnailDefaults.shape),
    ) {
        when (imageUrls.size) {
            0 -> fallback()
            1 -> ThumbnailCell(imageUrl = imageUrls[0], modifier = Modifier.fillMaxSize())
            2 -> SideBySideCollage(imageUrls = imageUrls)
            3 -> AsymmetricCollage(imageUrls = imageUrls)
            else -> GridCollage(imageUrls = imageUrls)
        }
    }
}

/** 좌우 두 칸. */
@Composable
private fun SideBySideCollage(
    imageUrls: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(RoomThumbnailTokens.CellSpacing),
    ) {
        ThumbnailCell(imageUrl = imageUrls[0], modifier = Modifier.weight(1f).fillMaxHeight())
        ThumbnailCell(imageUrl = imageUrls[1], modifier = Modifier.weight(1f).fillMaxHeight())
    }
}

/** 왼쪽은 세로로 긴 한 칸, 오른쪽은 위아래 두 칸. */
@Composable
private fun AsymmetricCollage(
    imageUrls: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(RoomThumbnailTokens.CellSpacing),
    ) {
        ThumbnailCell(imageUrl = imageUrls[0], modifier = Modifier.weight(1f).fillMaxHeight())
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(RoomThumbnailTokens.CellSpacing),
        ) {
            ThumbnailCell(imageUrl = imageUrls[1], modifier = Modifier.weight(1f).fillMaxWidth())
            ThumbnailCell(imageUrl = imageUrls[2], modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }
}

/** 2 × 2 네 칸. */
@Composable
private fun GridCollage(
    imageUrls: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(RoomThumbnailTokens.CellSpacing),
    ) {
        GridRow(startImageUrl = imageUrls[0], endImageUrl = imageUrls[1], modifier = Modifier.weight(1f))
        GridRow(startImageUrl = imageUrls[2], endImageUrl = imageUrls[3], modifier = Modifier.weight(1f))
    }
}

@Composable
private fun GridRow(
    startImageUrl: String,
    endImageUrl: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(RoomThumbnailTokens.CellSpacing),
    ) {
        ThumbnailCell(imageUrl = startImageUrl, modifier = Modifier.weight(1f).fillMaxHeight())
        ThumbnailCell(imageUrl = endImageUrl, modifier = Modifier.weight(1f).fillMaxHeight())
    }
}

/**
 * 콜라주 한 칸. 이미지를 채워 자르고, 아직 못 받았거나 로딩에 실패하면 placeholder 글리프를 둔다.
 */
@Composable
private fun ThumbnailCell(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    MinoAsyncImage(
        imageUrl = imageUrl,
        fallback = rememberVectorPainter(MinoIcons.Image),
        fallbackTint = MinoRoomThumbnailDefaults.placeholderTint,
        modifier = modifier.background(MinoRoomThumbnailDefaults.placeholderBackgroundColor),
    )
}
