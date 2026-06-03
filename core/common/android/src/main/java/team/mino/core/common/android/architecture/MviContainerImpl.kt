package team.mino.core.common.android.architecture

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * [MviContainer]의 실제 동작을 담당하는 구현체입니다.
 */
class MviContainerImpl<S : UiState, E : UiSideEffect>(
    initialState: S,
) : MviContainer<S, E> {
    private val _state = MutableStateFlow(initialState)

    override val state: StateFlow<S> = _state.asStateFlow()

    private val _sideEffect = Channel<E>(capacity = Channel.BUFFERED)

    override val sideEffect = _sideEffect.receiveAsFlow()

    /** 현재 상태를 안전하게 업데이트합니다. */
    override fun updateState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    /** UI 레이어로 일회성 이벤트를 안전하게 전송합니다. */
    override suspend fun postSideEffect(effect: E) {
        _sideEffect.send(effect)
    }
}
