package team.mino.feature.splash

import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route

/** 이 feature의 유일한 화면. 진입 인자가 없어 프로퍼티를 갖지 않는다. */
@Serializable
internal data object SplashMain : Route
