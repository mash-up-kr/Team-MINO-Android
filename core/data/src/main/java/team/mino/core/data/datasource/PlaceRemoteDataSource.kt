package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.PinResponse

/**
 * 장소(핀)의 원격 출처. [getPins]·[deletePin]의 계약은
 * `docs/specs/room-detail/contracts/place-repository.md` 근거.
 *
 * **복제(다른 방에 공유)는 여기 없다.** 같은 엔드포인트를 `PinRemoteDataSource.duplicatePin`이 이미
 * 가리키고 있어 [team.mino.core.domain.repository.PlaceRepository] 한 갈래로 모았다.
 */
internal interface PlaceRemoteDataSource {
    /**
     * 핀 목록 조회. `GET /api/v1/pins`.
     *
     * [roomId]를 생략하면 내가 속한 모든 활성 방의 핀을 조회한다. [category]·[sort]는 서버 쿼리 파라미터
     * 값(`"all"`/`"cafe"`/`"restaurant"`, `"all"`/`"latest"`/`"ggukPick"`/`"distance"`/`"commented"`)을
     * 그대로 받는다 — 값 변환은 `RoomPlacesRepositoryImpl`이 도메인 enum을 받아 여기 넘기기 전에 끝낸다.
     * [lat]·[lng]는 `sort = "distance"`일 때만 서버가 요구한다. [page]·[pageSize]를 둘 다 생략하면
     * 서버가 전체를 반환한다(지도 전체 보기).
     */
    suspend fun getPins(
        roomId: String? = null,
        category: String = "all",
        sort: String = "all",
        lat: Double? = null,
        lng: Double? = null,
        page: Int? = null,
        pageSize: Int? = null,
    ): List<PinResponse>

    /** 특정 핀 삭제. `DELETE /api/v1/pins/{pinId}`. */
    suspend fun deletePin(pinId: String)
}
