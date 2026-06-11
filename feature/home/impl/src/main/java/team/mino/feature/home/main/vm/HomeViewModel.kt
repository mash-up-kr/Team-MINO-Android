package team.mino.feature.home.main.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.feature.home.HomeMain
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel(), MviContainer<HomeUiState, HomeSideEffect> by mviContainer(HomeUiState()) {
    init {
        val route = savedStateHandle.toRoute<HomeMain>()
        updateState { copy(greeting = route.greeting.orEmpty()) }
    }
}
