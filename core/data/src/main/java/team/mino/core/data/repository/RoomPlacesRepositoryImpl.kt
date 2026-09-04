package team.mino.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.data.datasource.PlaceRemoteDataSource
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.repository.RoomPlacesRepository
import javax.inject.Inject

/**
 * DTO가 밖으로 나가지 않는 경계다. [PlaceRemoteDataSource]가 반환하는 `PinResponse`는 여기서 [Place]로
 * 전부 변환된다(`core/data/README.md` §6).
 *
 * 예외를 잡지 않는다 — 실패는 이미 `HttpClient`의 `convertDomainException`이 `MinoDomainException`으로
 * 매핑해 던지고, 이 클래스는 그대로 위로 전파한다(`docs/conventions/error_handling.md`).
 */
internal class RoomPlacesRepositoryImpl @Inject constructor(
    private val remoteDataSource: PlaceRemoteDataSource,
) : RoomPlacesRepository {
    override fun observePlaces(
        roomId: String?,
        category: PlaceCategoryFilter,
        sort: MapMarkerSortOption,
        currentLocation: GeoPoint?,
    ): Flow<List<Place>> =
        flow {
            emit(
                remoteDataSource
                    .getPins(
                        roomId = roomId,
                        category = category.toQueryValue(),
                        sort = sort.toQueryValue(),
                        lat = currentLocation?.latitude,
                        lng = currentLocation?.longitude,
                    ).map { it.toDomain() },
            )
        }

    /**
     * [FR-010] 장소 삭제 — 호출한 방에서만 제거한다(`DELETE /api/v1/pins/{pinId}`).
     *
     * **[roomId]를 요청에 싣지 않아도 "그 방에서만 제거"가 지켜진다.** 핀 레코드(`PinResponse.roomId`)가
     * 방 하나에 1:1로 귀속되고, 다른 방 복제(`POST /pins/{pinId}/duplicate`)는 새 `pinId`를 발급한다 —
     * 다른 방의 사본은 애초에 다른 핀이라 이 삭제에 영향받지 않는다. 요청 유저가 그 방의 멤버인지는
     * 서버가 검증한다.
     */
    override suspend fun deletePlace(
        roomId: String,
        pinId: String,
    ) {
        remoteDataSource.deletePin(pinId)
    }
}

/** `GET /api/v1/pins`의 `category` 쿼리 파라미터 값으로 변환. */
private fun PlaceCategoryFilter.toQueryValue(): String =
    when (this) {
        PlaceCategoryFilter.ALL -> "all"
        PlaceCategoryFilter.CAFE -> "cafe"
        PlaceCategoryFilter.RESTAURANT -> "restaurant"
    }

/** `GET /api/v1/pins`의 `sort` 쿼리 파라미터 값으로 변환. */
private fun MapMarkerSortOption.toQueryValue(): String =
    when (this) {
        MapMarkerSortOption.ALL -> "all"
        MapMarkerSortOption.LATEST -> "latest"
        MapMarkerSortOption.GGUK_PICK -> "ggukPick"
        MapMarkerSortOption.NEARBY -> "distance"
        MapMarkerSortOption.MOST_COMMENTED -> "commented"
    }
