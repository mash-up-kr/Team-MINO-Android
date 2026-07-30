package team.mino.feature.main

import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route

@Serializable
internal data object Home : Route

@Serializable
internal data object Saved : Route

@Serializable
internal data object Notification : Route

@Serializable
internal data object MyPage : Route
