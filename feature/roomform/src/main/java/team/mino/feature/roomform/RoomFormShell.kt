package team.mino.feature.roomform

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.common.ui.scaffold.MinoScaffold
import team.mino.core.navigation.screen.Route
import team.mino.feature.roomform.form.vm.RoomFormOutcome

/**
 * `:feature:roomform`의 진입 컴포저블. Activity가 여는 것은 이 셸이다.
 *
 * 진입 인자(편집 대상·온보딩 여부)는 시작 라우트([RoomForm])에 실려 들어오므로 셸은
 * [startDestination]을 그대로 흘려보낸다.
 *
 * 폼이 끝났다는 신호도 해석하지 않고 그대로 올려보낸다 — [RoomFormOutcome]을 결과로 옮기는 것은
 * `RoomFormActivity` 한 곳이 한다.
 *
 * `Scaffold`와 인셋은 이 셸이 소유한다. 화면은 [MinoScaffold]가 계산한 영역 안만 그리므로
 * 하단 액션 영역은 별도의 `navigationBarsPadding()`을 얹지 않는다(feature-module.md 4장).
 */
@Composable
internal fun RoomFormShell(
    startDestination: Route,
    onFinish: (RoomFormOutcome) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    MinoScaffold(modifier = modifier) { innerPadding ->
        RoomFormNavHost(
            navController = navController,
            startDestination = startDestination,
            onFinish = onFinish,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
