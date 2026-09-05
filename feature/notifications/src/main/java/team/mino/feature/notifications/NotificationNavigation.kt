package team.mino.feature.notifications

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.savedstate.SavedState
import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.graph
import team.mino.core.navigation.screen.popBackStackIfResumed
import team.mino.core.navigation.screen.screen
import team.mino.feature.notifications.main.screen.NotificationRoute
import team.mino.feature.notifications.main.screen.SaveErrorGuideScreen

/** 알림 탭 그래프의 진입 Route. 셸의 탭 목록이 참조하므로 이 모듈이 밖으로 여는 유일한 Route다. */
@Serializable
data object NotificationGraph : Route

@Serializable
internal data object NotificationMain : Route

/**
 * 저장 오류 안내 화면. [NotificationMain] 위에 쌓이는 **같은 그래프의 두 번째 목적지**다
 * (`docs/specs/notifications/research.md` D2).
 *
 * 목록을 대체하는 상태가 아니라 목적지인 덕분에 시스템 뒤로가기를 가로챌 것이 없고(spec EC-014),
 * 셸이 상위 그래프로 선택 탭을 찾으므로 여기서도 `알림` 탭이 선택된 채로 남는다(spec TS-030).
 *
 * **다른 탭을 다녀오면 이 목적지는 남지 않는다.** 떠난 탭의 백스택이 통째로 저장·복원되는 탓에 그냥 두면
 * 돌아온 사용자가 목록이 아니라 이 화면을 다시 만나므로, 그 복원을 [SaveErrorGuideDismisser]가 되돌린다
 * (spec TS-031·UX-008).
 */
@Serializable
internal data object SaveErrorGuide : Route

/**
 * 알림 탭 그래프를 셸의 [NavGraphBuilder]에 등록한다. 이 모듈의 화면 표면은 이 함수 하나다
 * (`docs/specs/notifications/contracts/notification-ui.md` §1).
 *
 * **밖으로 나가는 전환만 콜백으로 받는다.** 두 목적지 모두 저장 탭 안의 화면이라 요청을 받아 둘 홀더도 탭
 * 목록도 셸의 것이고, 이 모듈이 아는 것은 **어느 값을 올린다**까지다(`research.md` D10·D14). 저장 오류 안내는
 * 이 그래프 안에서 끝나는 전환이라 콜백으로 올리지 않고 [navController]로 직접 옮긴다
 * (`docs/architecture/feature-navigation.md` 3장).
 *
 * **바텀 네비게이션을 다루지 않는다**(같은 계약 §5). 셸이 항상 그리고 현재 목적지의 상위 그래프로 선택 탭을
 * 찾으므로, 이 그래프 안의 어느 화면에 있든 `알림` 탭이 선택된 채로 남는다.
 *
 * @param navController 그래프 안에서 끝나는 전환에만 쓴다 — 목록 → 안내 화면과 그 되돌림 둘뿐이다.
 * @param onNavigateToPlaceDetail 장소 대상 알림 탭 → [SCR-006] 장소 상세 (FR-005·FR-022)
 * @param onNavigateToRoomDetail 공동방 참가 알림 탭 → [SCR-005] 방 상세 (FR-005)
 */
fun NavGraphBuilder.notificationGraph(
    navController: NavHostController,
    onNavigateToPlaceDetail: (pinId: String) -> Unit,
    onNavigateToRoomDetail: (roomId: String) -> Unit,
) {
    graph<NotificationGraph>(startDestination = NotificationMain) {
        screen<NotificationMain> {
            NotificationRoute(
                onNavigateToPlaceDetail = onNavigateToPlaceDetail,
                onNavigateToRoomDetail = onNavigateToRoomDetail,
                onNavigateToSaveErrorGuide = { navController.openSaveErrorGuide() },
            )
        }
        screen<SaveErrorGuide> { entry ->
            // 상단 뒤로가기가 시스템 뒤로가기와 같은 일을 한다 — 둘 다 이 목적지를 걷어내는 것뿐이다
            // (spec FR-011·EC-014). 전환 중 들어온 두 번째 탭은 RESUMED 검사가 걸러 이중 pop을 막는다.
            SaveErrorGuideScreen(onBackClick = { navController.popBackStackIfResumed(entry) })
        }
    }
}

/**
 * 저장 오류 안내 화면을 열고, 탭을 다녀오는 동안 이 목적지를 지켜볼 [SaveErrorGuideDismisser]를 붙인다.
 *
 * 감시자를 **이동한 뒤에** 붙인다 — 등록 즉시 현재 목적지로 한 번 불리므로, 먼저 붙이면 아직 목록에 선 채로
 * 첫 통보를 받아 할 일이 끝난 것으로 읽는다.
 */
private fun NavHostController.openSaveErrorGuide() {
    navigate(SaveErrorGuide)
    addOnDestinationChangedListener(SaveErrorGuideDismisser())
}

/**
 * 다른 탭을 다녀온 사용자에게 안내 화면 대신 목록을 보여 준다(spec TS-031·UX-008).
 *
 * **탭 전환은 떠난 탭의 백스택을 통째로 저장했다가 복원한다.** `[NotificationMain, SaveErrorGuide]`가 그대로
 * 되살아나므로 안내 화면이 다시 뜨고, 그것을 걷어내는 것이 이 감시자의 유일한 일이다.
 *
 * **떠나는 길이 아니라 돌아오는 길에서 걷어낸다.** 탭을 떠나는 `navigate` 안에서는 저장이 이미 끝난 뒤라
 * 걷어낼 목적지가 백스택에 없다. 그래서 떠났다는 사실만 기억해 두었다가, 복원으로 안내 화면이 다시 현재
 * 목적지가 되는 순간에 판단한다.
 *
 * **[NavController.OnDestinationChangedListener]인 것이 이 구현의 핵심이다.**
 * - 이 통보는 `navigate` 안에서 **동기로** 불려, 복원된 안내 화면이 한 프레임도 그려지기 전에 pop이 끝난다.
 *   컴포지션 쪽 효과(`LaunchedEffect`·`currentBackStackEntryFlow` 수집)로 옮기면 복원된 화면이 먼저 서고
 *   되돌아가는 전환이 한 번 더 보인다.
 * - 컴포지션이 아니라 `NavController`에 매여 있어 **탭을 떠나 안내 화면이 화면에서 내려가도 살아남는다.**
 *   돌아오는 순간을 볼 수 있는 자리가 이 모듈에는 여기뿐이다.
 * - **목적지가 바뀔 때만 불린다.** 앱을 백그라운드로 보냈다 돌아오는 것은 목적지를 바꾸지 않으므로 이 감시자가
 *   발동하지 않고, 안내 화면이 그대로 남는다.
 *
 * 목록으로 돌아오면(뒤로가기로든 이 감시자의 pop으로든) 스스로 떨어져 나가므로, 안내 화면이 백스택에 있는
 * 동안만 붙어 있다.
 */
private class SaveErrorGuideDismisser : NavController.OnDestinationChangedListener {
    private var leftGraph = false

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: SavedState?,
    ) {
        // 통보받은 목적지가 아니라 지금 백스택의 꼭대기를 본다 — pop이 재진입으로 도는 동안에는 둘이 갈린다.
        val current = controller.currentDestination ?: return
        when {
            current.hierarchy.none { it.hasRoute(NotificationGraph::class) } -> leftGraph = true
            !current.hasRoute<SaveErrorGuide>() -> controller.removeOnDestinationChangedListener(this)
            leftGraph -> {
                // 먼저 떼고 pop 한다. pop이 곧바로 다음 통보를 부르므로, 붙어 있으면 자기 pop을 다시 받는다.
                controller.removeOnDestinationChangedListener(this)
                controller.popBackStack()
            }
        }
    }
}
