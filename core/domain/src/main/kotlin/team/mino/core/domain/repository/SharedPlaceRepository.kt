package team.mino.core.domain.repository

import team.mino.core.domain.model.SharedPlaceSaveRequest

/**
 * 공유받은 장소의 저장 계약.
 *
 * 노출하는 것은 예약 하나뿐이다 — 전송용 함수를 두지 않는다.
 * 실제 전송은 데이터 계층 안에서 끝나며 그 표면은 도메인에 올라오지 않는다
 * (`docs/specs/shared-link-receiver/research.md` R-017).
 */
interface SharedPlaceRepository {
    /**
     * [request]의 저장을 예약한다.
     *
     * `suspend`가 아니다 — 예약은 즉시 반환하고 전송 결과를 기다리지 않는다.
     * `저장하기` 이후 토스트가 사라지면 곧바로 물러나야 하므로(FR-011·UX-006) 이 함수가 네트워크에 묶이면 안 된다.
     *
     * 반환값이 없고 `MinoDomainException`을 던지지도 않는다. 이 함수가 확정하는 것은 "요청이 예약됐다"까지이며,
     * 저장의 성패는 호출자가 떠난 뒤에 갈린다. 그 결과를 사용자에게 알리는 것은 이 계약의 몫이 아니다.
     *
     * 예약의 실행 단위도 정하지 않는다 — `roomIds` 전부를 한 요청에 실을지 어떻게 나를지는 구현이 소유한다
     * (`docs/specs/shared-link-receiver/research.md` R-021).
     */
    fun scheduleSave(request: SharedPlaceSaveRequest)
}
