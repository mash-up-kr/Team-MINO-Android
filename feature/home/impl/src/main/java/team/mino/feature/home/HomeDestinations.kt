package team.mino.feature.home

import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route

@Serializable
internal data class HomeMain(
    val greeting: String? = null,
) : Route
