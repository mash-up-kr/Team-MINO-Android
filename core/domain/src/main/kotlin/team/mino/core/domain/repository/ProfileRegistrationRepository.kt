package team.mino.core.domain.repository

interface ProfileRegistrationRepository {
    /**
     * 현재 익명 세션에 등록된 프로필이 있는지 돌려준다. 프로필의 값은 다루지 않는다 — 값은 [ProfileRepository]가 소유한다.
     *
     * 세션이 확보된 뒤에만 호출된다. 순서는 호출자가 보장한다.
     * 등록되지 않은 세션은 `false`이며 실패가 아니다. 실패는 `MinoDomainException`으로 던지고 취소는 그대로 전파한다 —
     * 401을 포함해 어떤 실패도 `false`로 뭉개지 않는다. 어떤 실패가 어느 리프가 되는지는
     * `docs/adr/2026-08-22-domain-exception-mapping-per-source.md`가 소유한다.
     */
    suspend fun isRegistered(): Boolean
}
