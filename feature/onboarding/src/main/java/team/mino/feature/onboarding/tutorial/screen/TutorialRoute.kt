package team.mino.feature.onboarding.tutorial.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import team.mino.feature.onboarding.tutorial.model.TutorialStep

/**
 * [TutorialScreen]의 연결부.
 *
 * **ViewModel을 두지 않는다.** 이 스텝에는 도메인 호출도, 비동기 상태도, 복원 대상도 없어
 * 캐러셀 위치 하나가 상태의 전부다(`docs/specs/onboarding-flow/research.md` R-013).
 * 튜토리얼을 끝냈다는 기록은 온보딩 스텝 단위의 사건이라 [onFinish]로 올려보내고
 * 플로우 ViewModel이 남긴다 — 이 Route는 완료 여부를 판단하지도 기록하지도 않는다.
 *
 * **캐러셀 위치를 저장소에 싣지 않는다.** 프로세스가 죽었다 살아나면 튜토리얼은 항상 첫 장부터
 * 다시 시작한다(EC-022). 페이지 수는 [TutorialStep]이 이미 알고 있어 여기서 다시 세지 않는다.
 *
 * @param onFinish 상단 [건너뛰기]와 하단 시작 버튼이 함께 부른다.
 */
@Composable
internal fun TutorialRoute(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { TutorialStep.entries.size })
    val scope = rememberCoroutineScope()

    // 첫 장에서만 이 핸들러를 비활성으로 둔다. 그러면 제스처가 셸의 핸들러로 내려가 앱이
    // 백그라운드로 물러난다(TS-035·EC-015).
    //
    // 되짚을 자리는 pager가 지금 든 위치 하나에서 나오고 거쳐 온 장을 따로 기억하지 않는다.
    // dot을 눌러 첫 장으로 옮겨 온 뒤의 뒤로가기가 앞서 보던 장으로 되살아나지 않는 것이
    // 그 결과다(EC-016).
    val canGoBack by remember(pagerState) { derivedStateOf { pagerState.currentPage > 0 } }
    BackHandler(enabled = canGoBack) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    TutorialScreen(
        pagerState = pagerState,
        onDotClick = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
        onFinish = onFinish,
        modifier = modifier,
    )
}
