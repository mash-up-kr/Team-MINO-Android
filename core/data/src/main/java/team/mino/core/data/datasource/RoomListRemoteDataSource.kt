package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.RoomSummaryResponse

/**
 * 참여 중인 방 목록의 원격 출처. 시그니처는
 * `docs/specs/shared-link-receiver/contracts/room-list-api.md` §6이 소유한다.
 *
 * 같은 `room` 리소스를 다루는 [RoomRemoteDataSource]와 별개로 존재하는 이유는 바인딩 대상이 다르기 때문이다 —
 * 그쪽은 mock, 이쪽은 실서버다. 둘이 합쳐지는 시점과 그때 지워지는 쪽은
 * `docs/specs/shared-link-receiver/research.md` R-015가 적었다.
 */
internal interface RoomListRemoteDataSource {
    /**
     * 참여 중인 방 목록을 조회한다. 나간 방은 서버가 제외한다.
     *
     * 세션이 없거나(`401`) 네트워크·서버 오류면 `MinoDomainException`이 전파된다.
     * 그 셋을 빈 목록으로 수렴시키는 것은 화면의 몫이다(계약 §5).
     */
    suspend fun listRooms(): List<RoomSummaryResponse>
}
