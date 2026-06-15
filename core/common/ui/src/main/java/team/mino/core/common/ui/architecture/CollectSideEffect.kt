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
import team.mino.core.common.android.architecture.SideEffect

/**
 * MVI [SideEffect]를 화면 lifecycle에 맞춰 수집한다.
 *
 * [minActiveState] 이상에서만 수집하므로, 화면이 백그라운드일 때 Toast·네비게이션 같은
 * 일회성 이벤트가 처리되지 않는다. 수집이 멈춘 동안의 이벤트는 SideEffect 채널 버퍼에
 * 쌓였다가 화면 복귀 시 이어서 처리된다(defer). 유실 동작이 필요하면 채널이 아닌
 * 컨테이너(생산자) 쪽에서 정한다.
 *
 * SideEffect는 포그라운드 UI 이벤트 전용이다. 백그라운드에서도 처리돼야 하거나
 * 프로세스 사망을 넘어 살아남아야 하는 신호(예: 다운로드 완료)는 SideEffect가 아니라
 * 상태(StateFlow)나 OS 레벨 알림으로 모델링한다.
 *
 * @param sideEffect ViewModel의 SideEffect 스트림
 * @param minActiveState 수집을 유지할 최소 lifecycle 상태 (기본 [Lifecycle.State.STARTED])
 * @param onEvent 수집된 이벤트 처리 람다
 */

@Composable
fun <T : SideEffect> CollectSideEffect(
    sideEffect: Flow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEvent: (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // recomposition으로 onEvent가 바뀌어도 collect를 재시작하지 않고 최신 람다를 참조한다.
    val currentOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(sideEffect, lifecycleOwner, minActiveState) {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            // 수집 재개 직후 곧바로 방출된 이벤트도 놓치지 않도록 즉시 디스패치한다.
            withContext(Dispatchers.Main.immediate) {
                sideEffect.collect(currentOnEvent)
            }
        }
    }
}
