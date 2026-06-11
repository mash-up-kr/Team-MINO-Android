package team.mino.core.common.android.architecture

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel이 상속받아 사용할 MVI 인터페이스
 */
interface MviContainer<S : UiState, E : SideEffect> {
    val state: StateFlow<S>
    val sideEffect: Flow<E>

    fun updateState(reducer: S.() -> S)

    suspend fun postSideEffect(effect: E)
}

/**
 * ViewModel 내부에서 MviContainer 구현체를 편리하게 조립하기 위한 함수입니다.
 *
 * @param initialState 화면의 초기 [UiState]
 */
fun <S : UiState, E : SideEffect> mviContainer(initialState: S): MviContainer<S, E> = MviContainerImpl(initialState)
