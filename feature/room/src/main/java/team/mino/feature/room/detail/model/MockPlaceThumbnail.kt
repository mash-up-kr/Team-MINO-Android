package team.mino.feature.room.detail.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import team.mino.feature.room.R

/**
 * [Place.thumbnailUrl]이 없을 때 [PlaceListItem][team.mino.feature.room.detail.component.PlaceListItem]·
 * [PlaceGridItem][team.mino.feature.room.detail.component.PlaceGridItem]이 대신 채우는 목업 사진.
 *
 * Figma `MU_디자인` 파일의 `Card_Location` 참조 사진(`imageRef 59f7d8...`)을
 * `feature/room/src/main/res/drawable-xxhdpi/place_thumbnail_mock.png`로 내려받아 쓴다. [TBD] —
 * 서버가 장소 사진 필드를 내려주기 시작하면 이 목업과 호출부의 `?: mockPlaceThumbnailUrl()` 자리를
 * 함께 지운다.
 */
@Composable
internal fun mockPlaceThumbnailUrl(): String {
    val context = LocalContext.current
    return "android.resource://${context.packageName}/${R.drawable.place_thumbnail_mock}"
}
