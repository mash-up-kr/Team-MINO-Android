package team.mino.core.data.auth

internal interface IdTokenProvider {
    /**
     * 현재 세션의 신원 증명. 세션이 없으면 null.
     * 유효 기간 관리·갱신은 인증 제공자가 수행하며 강제 갱신을 요청하지 않는다.
     */
    suspend fun getIdToken(): String?
}
