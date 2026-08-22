package team.mino.core.data.device

internal interface PushTokenProvider {
    suspend fun getToken(): String
}
