package team.mino.feature.roomform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.screen
import team.mino.feature.roomform.form.screen.RoomFormRoute
import team.mino.feature.roomform.form.vm.RoomFormOutcome

/**
 * `:feature:roomform`의 화면 그래프. 현재 목적지는 폼 한 장뿐이지만,
 * 진입 인자 복원(`toRoute<RoomForm>`)과 화면 조회 로깅이 NavHost에 딸려 오므로 그래프를 둔다.
 *
 * [RoomForm]의 인자는 모두 primitive라 `typeMap`이 필요 없다.
 */
@Composable
internal fun RoomFormNavHost(
    navController: NavHostController,
    startDestination: Route,
    onFinish: (RoomFormOutcome) -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        screen<RoomForm> {
            RoomFormRoute(onFinish = onFinish)
        }
    }
}
