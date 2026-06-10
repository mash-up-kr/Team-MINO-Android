package team.mino.feature.sample.detail.vm

import team.mino.core.common.android.architecture.UiState
import team.mino.feature.sample.detail.model.SampleQuery

data class SampleDetailState(
    val query: SampleQuery? = null,
) : UiState
