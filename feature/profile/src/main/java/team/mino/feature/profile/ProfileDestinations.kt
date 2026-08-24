package team.mino.feature.profile

import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route

@Serializable
internal data class ProfileMain(
    val entryPoint: String,
) : Route
