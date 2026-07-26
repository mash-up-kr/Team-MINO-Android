package team.mino.core.common.ui.error

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import team.mino.core.common.ui.architecture.CollectFlowWithLifecycle
import team.mino.core.errorhandling.UncaughtErrorHandler

/**
 * CEH에 도달한 미처리 예외(버그)를 [UncaughtErrorHandler]에서 수집한다.
 *
 * 각 Activity가 setContent 바로 아래(NavHost 밖)에서 선언한다. RESUMED에서만 수집한다 —
 * resumed Activity는 최대 1개이므로 이중 수신이 없고, 수집 공백 중 이벤트는 채널 버퍼가 보존한다.
 */
@Composable
fun CollectUncaughtError(onError: (Throwable) -> Unit) {
    CollectFlowWithLifecycle(UncaughtErrorHandler.errors, Lifecycle.State.RESUMED, onError)
}
