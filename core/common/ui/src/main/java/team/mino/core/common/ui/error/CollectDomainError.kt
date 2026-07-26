package team.mino.core.common.ui.error

import androidx.compose.runtime.Composable
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException

/**
 * [DomainErrorEmitter]가 방출한 도메인 에러를 Route에서 수집한다.
 *
 * RESUMED에서만 수집한다 — NavController가 RESUMED 엔트리를 최대 1개만 보장하므로
 * 화면 전환 중 이중 수신이 없고, 수집 공백 중 이벤트는 채널 버퍼가 보존한다.
 */
@Composable
fun CollectDomainError(
    emitter: DomainErrorEmitter,
    onError: (MinoDomainException) -> Unit,
) {
    CollectOnResumed(emitter.domainErrors, onError)
}
