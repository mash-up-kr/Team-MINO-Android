package team.mino.core.domain.repository

/**
 * 푸시 토큰의 서버 등록 계약. 토큰을 어디서 얻어 어디로 보내는지는 구현이 소유한다
 * (`docs/specs/push-notification/research.md` D5).
 *
 * 계약 문구는 `docs/specs/push-notification/data-model.md` §4가 소유한다.
 */
interface PushRegistrationRepository {
    /**
     * 현재 등록 토큰을 조회해 서버에 등록한다.
     *
     * **`MinoDomainException`을 밖으로 내보내지 않는다.** 실패는 구현 안에서 삼키고 재시도하지 않는다 —
     * [PlaceRepository.recordAccess]와 같은 형태다(FR-004). 호출자는 결과를 확인하지 않아도 된다.
     *
     * **취소는 예외다.** `CancellationException`은 삼키지 않고 그대로 전파한다
     * (`docs/conventions/error_handling.md`).
     *
     * 반환값이 없다. 토큰 값도, 서버 응답의 성공 여부도 도메인에 올리지 않는다.
     */
    suspend fun registerCurrentToken()
}
