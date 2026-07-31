package team.mino.feature.sample.main.vm

import team.mino.core.common.android.architecture.SideEffect

sealed interface SampleSideEffect : SideEffect {
    data class ShowToast(
        val message: String,
    ) : SampleSideEffect
}
