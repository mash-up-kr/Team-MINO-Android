package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.PinCreateRequest
import team.mino.core.data.network.dto.request.PinDuplicateRequest

/**
 * 핀의 원격 출처. 함수마다 시그니처의 소유 계약이 다르다 — [createPin]은
 * `docs/specs/shared-link-receiver/contracts/shared-place-save-api.md` §4가,
 * [recordAccess]·[duplicatePin]은 `docs/specs/home-deck-exploration/contracts/deck-api.md`
 * §3.2·§3.3이 갖는다.
 *
 * mock 구현을 두지 않는다 — 세 계약 모두 서버에 배포돼 있어 실구현 하나뿐이다
 * (`docs/specs/shared-link-receiver/research.md` R-013).
 *
 * `internal`로 닫혀 있어 전송용 DTO가 도메인 표면에 올라가지 않는다(같은 문서 R-017). 소비자는 같은 모듈의
 * 워커와 RepositoryImpl뿐이다.
 */
internal interface PinRemoteDataSource {
    /**
     * 공유받은 링크를 [request]의 방들에 핀으로 추가하도록 요청한다.
     *
     * 방 개수와 무관하게 요청은 1건이다 — 저장 대상 방은 `roomIds`로 실린다(같은 문서 R-021).
     *
     * 서버가 접수만 하고 `202`를 돌려주므로 반환값이 없다. 실패는 `MinoDomainException`으로 전파된다.
     */
    suspend fun createPin(request: PinCreateRequest)

    /**
     * [pinId] 장소의 「경과일 초기화 확인」을 알린다(FR-007·023).
     *
     * 서버 응답에서 쓸 값이 없어 반환값이 없다. 호출자가 결과를 기다리지 않는 것은 이쪽 사정이 아니라
     * 화면의 판정이다(`docs/specs/home-deck-exploration/research.md` R-012) — 이 함수는 그저 막지 않는다.
     */
    suspend fun recordAccess(pinId: String)

    /**
     * [pinId] 장소를 [request]의 방들에 복제한다(FR-005).
     *
     * 대상 방에 같은 장소가 이미 있으면 서버가 전체를 거절하며, 그 `409`는 `MinoDomainException`으로
     * 전파된다. **성공으로 흡수하지 않는다** — 저장되지 않았는데 저장됐다고 보이면 안 된다.
     */
    suspend fun duplicatePin(
        pinId: String,
        request: PinDuplicateRequest,
    )
}
