package team.mino.core.common.ui.error

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import team.mino.core.common.ui.architecture.CollectFlowWithLifecycle
import team.mino.core.errorhandling.UncaughtErrorHandler

/**
 * CEH에 도달한 미처리 예외(버그)를 [UncaughtErrorHandler]에서 수집한다.
 *
 * 네비게이션 셸(`MinoScaffold`)이 NavHost 밖에서 선언하므로 feature가 직접 호출할 일은 없다.
 * RESUMED에서만 수집한다 — resumed Activity는 최대 1개이므로 이중 수신이 없고,
 * 수집 공백 중 이벤트는 채널 버퍼가 보존한다.
 */
@Composable
fun CollectUncaughtError(onError: (Throwable) -> Unit) {
    CollectFlowWithLifecycle(UncaughtErrorHandler.errors, Lifecycle.State.RESUMED, onError)
}
