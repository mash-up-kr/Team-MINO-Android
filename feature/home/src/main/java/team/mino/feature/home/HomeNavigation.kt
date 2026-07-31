package team.mino.feature.home

import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.graph
import team.mino.core.navigation.screen.screen
import team.mino.feature.home.main.screen.HomeRoute

/** 홈 탭 그래프의 진입 Route. 셸의 탭 목록이 참조하므로 이 모듈이 밖으로 여는 유일한 Route다. */
@Serializable
data object HomeGraph : Route

@Serializable
internal data object HomeMain : Route

/**
 * 홈 탭 그래프를 셸의 [NavGraphBuilder]에 등록한다. 이 모듈의 화면 표면은 이 함수 하나다.
 *
 * feature 밖으로 나가는 전환만 콜백으로 받는다. 모듈 안에서 끝나는 전환이 생기면
 * 셸에서 `navController`를 받아 여기서 처리하고 셸로 새어나가지 않게 한다.
 */
fun NavGraphBuilder.homeGraph(
    onNavigateToSample: () -> Unit,
    onRequestSampleResult: () -> Unit,
) {
    graph<HomeGraph>(startDestination = HomeMain) {
        screen<HomeMain> {
            HomeRoute(
                onNavigateToSample = onNavigateToSample,
                onRequestSampleResult = onRequestSampleResult,
            )
        }
    }
}
