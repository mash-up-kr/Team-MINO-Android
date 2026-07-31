package team.mino.feature.sample.detail.vm

import androidx.compose.runtime.Stable
import team.mino.core.common.android.architecture.UiState
import team.mino.feature.sample.detail.model.SampleQuery

@Stable
data class SampleDetailUiState(
    val query: SampleQuery? = null,
) : UiState
