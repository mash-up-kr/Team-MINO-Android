package team.mino.core.data.push

internal interface PushTokenProvider {
    /**
     * 이 설치의 현재 FCM 등록 토큰.
     * 발급·갱신·캐시는 Messaging SDK가 수행하며, 앱은 호출 시점마다 받아 쓸 뿐 저장하지 않는다.
     */
    suspend fun currentToken(): String
}
