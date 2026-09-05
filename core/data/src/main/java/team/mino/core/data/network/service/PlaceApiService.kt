package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import team.mino.core.data.network.dto.response.MinoResponse
import team.mino.core.data.network.dto.response.PinResponse
import javax.inject.Inject

/**
 * 방에 저장된 핀(장소) 목록 조회·삭제 API — `docs/specs/room-detail/contracts/place-repository.md` 근거.
 *
 * 응답은 공통 인터셉터 없이 `{ "data": ... }` 봉투를 그대로 받으므로, [RoomApiService]와 같이
 * 이 서비스가 직접 벗긴다.
 */
internal class PlaceApiService @Inject constructor(
    private val client: HttpClient,
) {
    /**
     * [roomId]를 생략하면 내가 속한 모든 활성 방의 핀을 조회한다(전체 지도용). 지정하면 해당 방 핀만
     * 조회하며 멤버십 검증은 서버가 한다.
     *
     * [category]·[sort]는 서버 기본값이 각각 `"all"`이라 생략 시 기존과 동일하게 동작한다(하위 호환).
     * [lat]·[lng]는 `sort = "distance"`일 때만 서버가 요구하며, 그 외에는 넘겨도 서버가 무시한다. Ktor의
     * `parameter()`는 값이 `null`이면 쿼리 파라미터 자체를 붙이지 않는다.
     *
     * [page]·[pageSize]를 둘 다 생략하면 서버가 전체를 offset 없이 반환한다(지도 전체 보기 — 지도는 이
     * 둘을 넘기지 않는다, `RoomListViewModel.observePlaces` 참고). 방 상세 장소 목록처럼 나눠 받을 때만
     * 채운다([RoomPlacesRepositoryImpl.getPlacesPage]).
     */
    suspend fun getPins(
        roomId: String? = null,
        category: String = "all",
        sort: String = "all",
        lat: Double? = null,
        lng: Double? = null,
        page: Int? = null,
        pageSize: Int? = null,
    ): List<PinResponse> =
        client
            .get("api/v1/pins") {
                parameter("roomId", roomId)
                parameter("category", category)
                parameter("sort", sort)
                parameter("lat", lat)
                parameter("lng", lng)
                parameter("page", page)
                parameter("pageSize", pageSize)
            }.body<MinoResponse<List<PinResponse>>>()
            .data

    suspend fun deletePin(pinId: String) {
        client.delete("api/v1/pins/$pinId")
    }
}
