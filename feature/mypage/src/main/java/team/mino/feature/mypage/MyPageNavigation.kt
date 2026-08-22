package team.mino.feature.mypage

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.graph
import team.mino.core.navigation.screen.popBackStackIfResumed
import team.mino.core.navigation.screen.screen
import team.mino.feature.mypage.main.screen.MyPageRoute
import team.mino.feature.mypage.profile.screen.ProfileRoute

/** 마이페이지 탭 그래프의 진입 Route. 셸의 탭 목록이 참조하므로 이 모듈이 밖으로 여는 유일한 Route다. */
@Serializable
data object MyPageGraph : Route

@Serializable
internal data object MyPageMain : Route

@Serializable
internal data object MyPageProfile : Route

/**
 * 마이페이지 탭 그래프를 셸의 [NavGraphBuilder]에 등록한다. 이 모듈의 화면 표면은 이 함수 하나다.
 *
 * 모듈 안에서 끝나는 전환(프로필 편집 진입·뒤로가기)은 여기서 `navController`로 직접 처리한다.
 */
fun NavGraphBuilder.mypageGraph(
    navController: NavHostController,
) {
    graph<MyPageGraph>(startDestination = MyPageMain) {
        screen<MyPageMain> {
            MyPageRoute(
                onNavigateToProfileSetup = { navController.navigate(MyPageProfile) },
            )
        }
        screen<MyPageProfile> { entry ->
            ProfileRoute(onBack = { navController.popBackStackIfResumed(entry) })
        }
    }
}
