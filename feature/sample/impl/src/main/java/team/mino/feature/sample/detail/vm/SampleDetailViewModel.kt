package team.mino.feature.sample.detail.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.feature.sample.SampleDetail
import javax.inject.Inject

@HiltViewModel
class SampleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel(), MviContainer<SampleDetailState, SampleDetailSideEffect> by mviContainer(SampleDetailState()) {
    init {
        val route = savedStateHandle.toRoute<SampleDetail>(SampleDetail.typeMap)
        updateState { copy(query = route.query) }
    }
}
