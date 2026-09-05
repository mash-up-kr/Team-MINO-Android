package team.mino.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.scaffold.MinoScaffold
import team.mino.feature.onboarding.flow.vm.OnboardingFlowIntent
import team.mino.feature.onboarding.flow.vm.OnboardingFlowSideEffect
import team.mino.feature.onboarding.flow.vm.OnboardingFlowViewModel

/**
 * `:feature:onboarding`의 진입 컴포저블. Activity가 여는 것은 이 셸이다.
 *
 * **[MinoScaffold]에 `bottomBar`를 넘기지 않는다.** 온보딩 어느 스텝에도 바텀 네비게이션이
 * 보이지 않아야 하고, 그 요구사항의 구현이 여기서 슬롯을 비워 두는 것이다
 * (`contracts/onboarding-flow-ui.md` §1·§5).
 *
 * 진입 인자가 없어 시작 목적지를 Activity에서 받지 않고 그래프가 갖는다. `navController`도 다른
 * feature와 같이 셸이 만든다 — 화면 조회 로깅이 거기 딸려 오기 때문이다
 * (`feature-module.md` 4장 · `2026-07-31-common-shell-mino-scaffold.md`).
 *
 * **[OnboardingFlowSideEffect]의 수집기는 이 한 곳뿐이다.** 다른 Activity로의 위임과 그래프 이동이
 * 같은 스트림으로 오는데 채널의 소비자는 하나이므로, 수집기를 둘로 나누면 한쪽이 이벤트를 가져가
 * 버린다. 그래서 그래프 이동은 셸이 `navController`로 직접 실행하고, Activity만 할 수 있는 일
 * (다른 Activity 열기)은 콜백으로 올려보낸다.
 *
 * 스텝을 넘기는 화면 조작(초대 [X]·튜토리얼 종료)도 화면이 해석하지 않고 그대로 올라와 여기서
 * 플로우 [OnboardingFlowViewModel]의 Intent가 된다 — 전이를 결정하는 것은 그 한 곳이다
 * (`contracts/onboarding-flow-ui.md` §2.4).
 *
 * [onShareInviteLink]는 스텝을 넘기는 조작이 아니지만 같은 이유로 셸을 지나간다 — 외부 앱으로의
 * 전환이라 시작 지점이 Activity여야 한다(`feature-navigation.md` 1장).
 *
 * @param viewModel 플로우 ViewModel. 셸은 NavHost 위에 있어 이 주입은 Activity의 스토어를 타고,
 *   Activity가 결과 수신에 쓰는 인스턴스와 같다 — 두 진입 경로의 Intent가 한 컨테이너에서 만난다.
 */
@Composable
internal fun OnboardingShell(
    pendingInviteCode: String?,
    onLaunchProfile: () -> Unit,
    onLaunchRoomForm: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHomeWithRoom: (String) -> Unit,
    onShareInviteLink: (String) -> Unit,
    onBackToBackground: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingFlowViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            OnboardingFlowSideEffect.LaunchProfile -> onLaunchProfile()
            OnboardingFlowSideEffect.LaunchRoomForm -> onLaunchRoomForm()
            is OnboardingFlowSideEffect.NavigateToInvite -> navController.replaceWith(OnboardingInvite(effect.roomId))
            OnboardingFlowSideEffect.NavigateToTutorial -> navController.replaceWith(OnboardingTutorial)
            // 완료 표시는 이 지시가 나오기 전에 ViewModel이 이미 기록했다(research.md R-019).
            OnboardingFlowSideEffect.NavigateToHome -> onNavigateToHome()
            // 초대 코드 자동 참여(SYS-010 Flow A)로 나머지 스텝을 건너뛰고 바로 그 방으로 간다.
            is OnboardingFlowSideEffect.NavigateToHomeWithRoom -> onNavigateToHomeWithRoom(effect.roomId)
        }
    }

    // 수집기가 붙은 뒤에 보낸다. 재개 조회는 ViewModel이 한 번만 돌린다.
    LaunchedEffect(Unit) { viewModel.processIntent(OnboardingFlowIntent.Start(pendingInviteCode)) }

    // 온보딩이 소유한 지점의 시스템 뒤로가기는 앱을 백그라운드로 보낸다. 앞 스텝으로 돌아가지도,
    // 온보딩을 끝낸 것으로 보지도 않는다 — 완료 표시를 기록하지 않고 스텝도 바꾸지 않는다
    // (contracts/onboarding-flow-ui.md §2.5). 튜토리얼 스텝 2~5는 자기 화면의 BackHandler가
    // 먼저 가로채므로 여기까지 오지 않는다.
    BackHandler(onBack = onBackToBackground)

    MinoScaffold(modifier = modifier) { innerPadding ->
        OnboardingNavHost(
            navController = navController,
            onShareInviteLink = onShareInviteLink,
            onInviteClosed = { viewModel.processIntent(OnboardingFlowIntent.InviteClosed) },
            onTutorialFinished = { viewModel.processIntent(OnboardingFlowIntent.TutorialFinished) },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

/**
 * 앞 스텝을 백스택에 남기지 않고 [route]로 옮긴다.
 *
 * 온보딩 안에서는 어느 스텝에서도 뒤로가기로 앞 스텝에 닿을 수 없어야 하는데, 그것을 화면마다
 * 뒤로가기를 막아 지키면 한 곳만 빠뜨려도 무너진다. 백스택에 애초에 쌓지 않는 쪽이 구조적 보장이다
 * (FR-006·TS-007 · `research.md` R-006).
 */
private fun NavHostController.replaceWith(route: Any) {
    val current = currentDestination?.id
    navigate(route) {
        if (current != null) popUpTo(current) { inclusive = true }
    }
}
