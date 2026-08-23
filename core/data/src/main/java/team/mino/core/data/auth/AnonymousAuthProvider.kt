package team.mino.core.data.auth

internal interface AnonymousAuthProvider {
    /** 로컬에 유지된 현재 세션의 사용자 식별자. 없으면 null. 네트워크 왕복이 없다. */
    suspend fun currentUserId(): String?

    /** 인증 제공자에서 새 익명 세션을 발급받고 사용자 식별자를 반환한다. */
    suspend fun signInAnonymously(): String
}
