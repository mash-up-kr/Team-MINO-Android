package team.mino.feature.mypage

import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.graph
import team.mino.core.navigation.screen.screen
import team.mino.feature.mypage.main.screen.MyPageRoute

/** 마이페이지 탭 그래프의 진입 Route. 셸의 탭 목록이 참조하므로 이 모듈이 밖으로 여는 유일한 Route다. */
@Serializable
data object MyPageGraph : Route

@Serializable
internal data object MyPageMain : Route

/**
 * 마이페이지 탭 그래프를 셸의 [NavGraphBuilder]에 등록한다. 이 모듈의 화면 표면은 이 함수 하나다.
 *
 * 프로필 편집은 이 모듈 안에서 끝나지 않는다 — `:feature:profile`이 소유한 별도 Activity라
 * `ProfileLauncher`로 여는 것이 셸(Activity)의 책임이다(`docs/specs/profile/contracts/profile-launcher-contract.md`
 * §호출 방법 — 마이페이지 진입). 이 함수는 그 실행을 셸에서 받은 콜백으로만 위임받는다.
 */
fun NavGraphBuilder.mypageGraph(onNavigateToProfileEdit: () -> Unit) {
    graph<MyPageGraph>(startDestination = MyPageMain) {
        screen<MyPageMain> {
            MyPageRoute(
                onNavigateToProfileSetup = onNavigateToProfileEdit,
            )
        }
    }
}
