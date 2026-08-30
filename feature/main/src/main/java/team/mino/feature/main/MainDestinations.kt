package team.mino.feature.main

import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route

// 탭 feature 모듈로 분리되기 전인 탭만 여기 남는다. 분리되면 그 모듈이 진입 Route를 소유한다(→ HomeGraph).

@Serializable
internal data object Saved : Route

@Serializable
internal data object Notification : Route

@Serializable
internal data object MyPage : Route
