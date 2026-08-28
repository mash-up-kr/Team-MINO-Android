package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.PinCreateRequest

/**
 * 방에 핀을 추가하는 원격 출처. 시그니처는
 * `docs/specs/shared-link-receiver/contracts/shared-place-save-api.md` §4가 소유한다.
 *
 * mock 구현을 두지 않는다 — 저장 계약이 서버에 배포돼 있어 실구현 하나뿐이다
 * (`docs/specs/shared-link-receiver/research.md` R-013).
 *
 * 이 계약의 소비자는 같은 모듈의 워커뿐이라 `internal`로 닫혀 있고, 그래서 전송용 함수가
 * 도메인 표면에 올라가지 않는다(같은 문서 R-017).
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
}
