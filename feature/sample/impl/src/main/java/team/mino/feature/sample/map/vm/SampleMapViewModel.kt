package team.mino.feature.sample.map.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import javax.inject.Inject

@HiltViewModel
class SampleMapViewModel
    @Inject
    constructor() :
    ViewModel(),
        MviContainer<SampleMapUiState, SampleMapSideEffect> by mviContainer(SampleMapUiState())
