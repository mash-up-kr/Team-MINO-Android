package team.mino.core.common.ui.architecture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Composable
internal fun <T> CollectFlowWithLifecycle(
    flow: Flow<T>,
    minActiveState: Lifecycle.State,
    onEvent: (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // recomposition으로 onEvent가 바뀌어도 collect를 재시작하지 않고 최신 람다를 참조한다.
    val currentOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(flow, lifecycleOwner, minActiveState) {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            // 수집 재개 직후 곧바로 방출된 이벤트도 놓치지 않도록 즉시 디스패치한다.
            withContext(Dispatchers.Main.immediate) {
                flow.collect(currentOnEvent)
            }
        }
    }
}
