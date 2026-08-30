package team.mino.core.domain.repository

import team.mino.core.domain.model.AnonymousSession

interface AnonymousAuthRepository {
    /**
     * 익명 세션을 확보해 반환한다. 이미 확보된 세션이 있으면 인증 제공자와의 왕복 없이 그 세션을 돌려준다.
     *
     * 몇 번을 호출해도(동시 호출을 포함해) 세션은 하나만 만들어지며, 이 함수의 어떤 호출도 세션을 폐기하지 않는다.
     * 확보에 실패하면 `MinoDomainException.Network`·`MinoDomainException.Auth`로 던지고 취소는 그대로 전파한다.
     * 스스로 타임아웃 상한을 두지 않는다 — 재시도·지연 판정은 호출자가 소유한다.
     */
    suspend fun ensureSession(): AnonymousSession

    /**
     * 로컬에 유지된 익명 세션을 인증 제공자와의 왕복 없이 돌려준다. 없으면 `null`이다.
     *
     * 확보(`ensureSession`)와 짝을 이루는 조회다 — 세션이 없어도 새로 발급하지 않고, 그 판단은 호출자가 소유한다.
     * 세션 유무를 `null`로 표현하므로 이 함수는 `MinoDomainException`을 던지지 않으며 취소만 그대로 전파한다.
     */
    suspend fun currentSession(): AnonymousSession?
}
