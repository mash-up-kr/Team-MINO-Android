package team.mino.feature.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.common.ui.scaffold.MinoScaffold
import team.mino.core.navigation.screen.Route

/**
 * `:feature:profile`의 진입 컴포저블. Activity가 여는 것은 이 셸이다.
 *
 * 진입 인자는 시작 라우트([ProfileMain])에 실려 들어오므로 셸은 [startDestination]을 그대로 흘려보낸다.
 * 뒤로가기와 저장 완료는 이 모듈 밖에서 결정되므로 콜백으로 올려보낸다.
 */
@Composable
internal fun ProfileShell(
    startDestination: Route,
    onBackClick: () -> Unit,
    onSaveCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    MinoScaffold(modifier = modifier) { innerPadding ->
        ProfileNavHost(
            navController = navController,
            startDestination = startDestination,
            onBackClick = onBackClick,
            onSaveCompleted = onSaveCompleted,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
