package team.mino.core.navigation.screen

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlin.reflect.KClass

/**
 * [screen]·[graph] 등록 시점에 [ImmersiveRoute]를 구현하는 [Route]를 모아 두는 레지스트리.
 *
 * 탭 셸(`:feature:main`의 `MainShell`)이 현재 목적지가 몰입 화면인지 판정하려면 구체 [Route]
 * 타입을 알아야 하는데, 그러면 feature 간 결합이 생긴다. 대신 각 [Route]를 등록하는
 * [screen]·[graph]가(자신의 feature 모듈 안에서 이미 구체 타입을 알고 있으므로) 여기에
 * 마커 구현 여부만 넘겨 두면, 셸은 [isImmersive]로 현재 목적지만 넘겨 판정할 수 있다.
 */
object ImmersiveRouteRegistry {
    private val immersiveRoutes = mutableSetOf<KClass<out Route>>()

    fun register(route: KClass<out Route>) {
        immersiveRoutes += route
    }

    /** [destination]이 [ImmersiveRoute]로 등록된 [Route]인지 판정한다. */
    fun isImmersive(destination: NavDestination?): Boolean {
        val target = destination ?: return false
        return immersiveRoutes.any { route -> target.hasRoute(route) }
    }
}
