package team.mino.core.domain.repository

import team.mino.core.domain.model.PlaceDetail

/**
 * 핀 하나의 조회·접근 기록·복제 계약.
 *
 * 세 함수 모두 1회성 요청이라 `Flow`를 흘리지 않는다. [recordAccess]를 제외하면 실패를 `Result`로 감싸지 않고
 * `MinoDomainException`으로 던지며, 취소는 그대로 전파한다.
 *
 * 정렬·필터 책임을 갖지 않는다 — 서버가 준 순서를 그대로 돌려준다.
 *
 * 코멘트는 이 계약에 없다. 생애가 달라 [PlaceCommentRepository]로 갈랐다
 * (`docs/specs/place-detail/research.md` D8).
 */
interface PlaceRepository {
    /**
     * 핀 상세를 가져온다.
     *
     * 등록자가 없으면 [PlaceDetail.registrant]가 `null`이다. 기본 아바타로 대체하는 판정은 화면이 한다(EC-004).
     */
    suspend fun getPlaceDetail(pinId: String): PlaceDetail

    /**
     * 이 핀을 열었다는 사실을 기록한다(FR-026).
     *
     * **`MinoDomainException`을 밖으로 내보내지 않는다.** 실패는 구현 안에서 삼키고 재시도하지 않는다 —
     * `docs/specs/place-detail/spec.md` EC-022가 "기록 실패는 화면 동작에 영향을 주지 않는다"를 규정했고,
     * 호출자가 `try`로 감싸야 한다면 그 규칙이 호출부마다 새어 나가기 때문이다. 호출자는 이 함수의 결과를
     * 확인하지 않아도 된다.
     *
     * **취소는 예외다.** `CancellationException`은 삼키지 않고 그대로 전파한다
     * (`docs/conventions/error_handling.md`).
     *
     * 반환값이 없다. 서버 응답의 성공 여부를 도메인에 올리지 않는다.
     *
     * 디바운스·중복 제거를 하지 않는다(EC-023). 열 때마다 그대로 기록하며, append-only 로그라 서버도
     * 중복을 문제 삼지 않는다.
     */
    suspend fun recordAccess(pinId: String)

    /**
     * 이 핀을 [roomIds]의 방들에 복제한다(FR-018).
     *
     * [roomIds]가 비어 있으면 호출하지 않는다 — 서버 스키마가 `minItems: 1`이다. 빈 목록을 막는 것은 화면의
     * [공유하기] 비활성 규칙(FR-022)이고, 이 계약은 그 전제를 신뢰해 다시 검사하지 않는다.
     *
     * 반환값이 없다. 성공 이후 화면이 무엇을 하는지(토스트·잔류)는 화면이 정한다.
     *
     * 대상 방 중 하나라도 이미 저장돼 있어 서버가 `409`를 주면 `MinoDomainException`으로 전파한다.
     * 이 계약은 그 경우를 위한 별도 분기를 두지 않는다(`docs/specs/place-detail/research.md` D14).
     */
    suspend fun duplicatePin(
        pinId: String,
        roomIds: List<String>,
    )
}
