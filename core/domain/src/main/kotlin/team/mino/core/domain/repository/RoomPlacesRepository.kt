package team.mino.core.domain.repository

import kotlinx.coroutines.flow.Flow
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter

/**
 * 방에 저장된 장소(핀) 목록 단위 동작 계약 — 핀 한 개 자체의 조회·기록·복제는 [PlaceRepository] 소관이다.
 *
 * `observePlaces`·`deletePlace` 모두 배포된 서버 API로 근거를 대조했다
 * (`docs/specs/room-detail/research.md` D14).
 *
 * **다른 방에 공유는 여기 없다.** room-detail(#154)과 place-detail(#161)이 같은 엔드포인트
 * (`POST /pins/{pinId}/duplicate`)에 대고 각자 `sharePlaces`와 [PlaceRepository.duplicatePin]을 만들었고,
 * 두 화면이 [SYS-003] 시트를 한 벌로 합치면서 [PlaceRepository] 쪽으로 모았다.
 */
interface RoomPlacesRepository {
    /**
     * 핀 목록을 실시간 관찰. 필터·정렬은 서버가 수행한다(클라이언트 메모리 필터링 아님).
     *
     * @param roomId 생략(`null`)하면 내가 속한 모든 활성 방의 핀을 조회한다(전체 지도용). 지정하면 그 방
     * 핀만 조회하며 멤버십 검증은 서버가 한다.
     * @param category 카테고리 필터. 기본값 [PlaceCategoryFilter.ALL]은 필터 없음(기존과 동일).
     * @param sort 정렬 기준. 기본값 [MapMarkerSortOption.ALL]은 최신순(기존과 동일).
     * @param currentLocation [sort]가 [MapMarkerSortOption.NEARBY](거리순)일 때만 서버가 요구하는 내 위치.
     * 그 외 정렬에서는 `null`이어도 된다.
     */
    fun observePlaces(
        roomId: String? = null,
        category: PlaceCategoryFilter = PlaceCategoryFilter.ALL,
        sort: MapMarkerSortOption = MapMarkerSortOption.ALL,
        currentLocation: GeoPoint? = null,
    ): Flow<List<Place>>

    /**
     * 핀 목록을 [page]("0부터 시작") 단위로 나눠 한 번만 조회한다 — 방 상세 장소 목록([RoomDetailViewModel])
     * 전용. [observePlaces]와 달리 전체를 한 번에 받지 않아, 장소가 많은 방에서도 화면에 필요한 만큼만
     * 불러온다("api 낭비 없게").
     *
     * @param pageSize 한 번에 받을 개수. 기본값 [DEFAULT_PLACES_PAGE_SIZE](20).
     * @return 결과 개수가 [pageSize]보다 적으면 더 가져올 다음 페이지가 없다는 뜻이다(호출부가 그 크기로
     * 판정한다).
     */
    suspend fun getPlacesPage(
        roomId: String? = null,
        category: PlaceCategoryFilter = PlaceCategoryFilter.ALL,
        sort: MapMarkerSortOption = MapMarkerSortOption.ALL,
        currentLocation: GeoPoint? = null,
        page: Int,
        pageSize: Int = DEFAULT_PLACES_PAGE_SIZE,
    ): List<Place>

    /**
     * [FR-010] 장소 삭제 — 호출한 방에서만 제거한다(다른 방에 복제된 사본은 남는다).
     *
     * [roomId]는 "어느 방에서 지우는가"라는 호출자의 의도를 계약에 남기려고 받는다 — 그 값을 서버 요청에
     * 싣는지는 데이터 레이어가 정한다. 단건 삭제로 이 계약이 성립하는 근거는
     * `docs/specs/room-detail/contracts/place-repository.md`에 있다.
     */
    suspend fun deletePlace(
        roomId: String,
        pinId: String,
    )
}

/** [RoomPlacesRepository.getPlacesPage] 기본 페이지 크기. */
const val DEFAULT_PLACES_PAGE_SIZE = 20
