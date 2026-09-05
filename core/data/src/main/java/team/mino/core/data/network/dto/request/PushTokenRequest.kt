package team.mino.core.data.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
internal data class PushTokenRequest(
    val token: String,
    val platform: String,
)
