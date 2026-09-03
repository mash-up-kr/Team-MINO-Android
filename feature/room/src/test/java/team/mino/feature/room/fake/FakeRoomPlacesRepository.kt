package team.mino.feature.room.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import team.mino.core.domain.model.Place
import team.mino.core.domain.repository.RoomPlacesRepository

/**
 * `:feature:room` 테스트용 [RoomPlacesRepository] 테스트 더블.
 *
 * [givenPlaces]로 방 하나의 장소 목록을 미리 채워 두고 [observePlaces]로 흘려보낸다 — 방마다 다른 목록을
 * 흘릴 필요가 생기기 전까지는 방 id를 구분하지 않는다. [deletePlace]가 `roomId`를 보지 않는 것도 같은
 * 이유이며, 실제 구현이 `pinId` 단건으로 삭제하는 것과도 일치한다.
 */
internal class FakeRoomPlacesRepository : RoomPlacesRepository {
    private val places = MutableStateFlow<List<Place>>(emptyList())

    fun givenPlaces(vararg values: Place) {
        places.value = values.toList()
    }

    override fun observePlaces(roomId: String): Flow<List<Place>> = places

    override suspend fun deletePlace(
        roomId: String,
        pinId: String,
    ) {
        places.value = places.value.filterNot { it.id == pinId }
    }
}
