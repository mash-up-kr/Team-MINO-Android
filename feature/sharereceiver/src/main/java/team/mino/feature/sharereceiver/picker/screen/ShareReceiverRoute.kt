package team.mino.feature.sharereceiver.picker.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import team.mino.core.analytics.AnalyticsTracker
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.feature.sharereceiver.picker.vm.ShareReceiverIntent
import team.mino.feature.sharereceiver.picker.vm.ShareReceiverSideEffect
import team.mino.feature.sharereceiver.picker.vm.ShareReceiverViewModel

/**
 * [ShareReceiverScreen]의 연결부. ViewModel을 얻어 상태를 구독하고 인텐트를 넘긴다.
 *
 * 끝났다는 신호는 콜백으로 올려보낸다 — 완료를 어떻게 알리고 언제 물러날지는 이 시트가 아니라
 * 호스트가 정한다.
 *
 * @param replacedSharedUrl 시트가 떠 있는 동안 새 공유가 도착해 갈린 링크. 없으면 `null`이다(EC-013).
 * @param onReplacementConsumed [replacedSharedUrl]을 시트에 넘겼다. 호스트가 같은 링크를 다시 보내지
 *  않도록 지우는 자리다.
 * @param onSavedAndFinish 저장을 예약하고 끝났다. 완료 알림은 받는 쪽이 띄운다.
 * @param onFinish 아무것도 저장하지 않고 끝났다.
 */
@Composable
internal fun ShareReceiverRoute(
    replacedSharedUrl: String?,
    onReplacementConsumed: () -> Unit,
    onSavedAndFinish: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShareReceiverViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val analyticsTracker = rememberAnalyticsTracker()

    LaunchedEffect(analyticsTracker) {
        analyticsTracker.logScreenView(screenName = SCREEN_NAME)
    }

    /* 새 공유는 사용자 조작이 아니라 호스트가 받아 내려보낸다. 시트는 링크가 갈렸다는 사실만 알면 되고,
     * 무엇을 비우고 무엇을 남길지는 ViewModel이 정한다(계약 §2.3 · research.md R-024). */
    LaunchedEffect(replacedSharedUrl) {
        val url = replacedSharedUrl ?: return@LaunchedEffect
        viewModel.processIntent(ShareReceiverIntent.SharedUrlReplaced(url))
        onReplacementConsumed()
    }

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            ShareReceiverSideEffect.SavedAndFinish -> onSavedAndFinish()
            ShareReceiverSideEffect.Finish -> onFinish()
        }
    }

    ShareReceiverScreen(
        state = state,
        onIntent = viewModel::processIntent,
        modifier = modifier,
    )
}

/**
 * 이 화면의 조회 로깅에 쓰는 이름. 다른 화면이 Route 클래스명으로 남기는 값과 자리를 맞춘다.
 */
private const val SCREEN_NAME = "ShareReceiver"

/**
 * 앱 그래프에서 [AnalyticsTracker]를 꺼낸다.
 *
 * 화면 조회 로깅은 보통 `navController`에 붙은 `TrackScreenViews`가 대신하지만, 이 feature는 셸도
 * `NavHost`도 두지 않아 붙일 자리가 없다(`research.md` R-008). 그렇다고 로깅 한 줄 때문에 ViewModel을
 * 하나 더 세우지 않고, Hilt가 이런 경우를 위해 열어 둔 진입점으로 직접 꺼낸다.
 */
@Composable
private fun rememberAnalyticsTracker(): AnalyticsTracker {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        EntryPointAccessors
            .fromApplication(context, ShareReceiverAnalyticsEntryPoint::class.java)
            .analyticsTracker()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface ShareReceiverAnalyticsEntryPoint {
    fun analyticsTracker(): AnalyticsTracker
}
