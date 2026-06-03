package team.mino.feature.sample.vm

import team.mino.core.common.android.architecture.UiSideEffect

sealed interface SampleSideEffect : UiSideEffect {
    data class ShowToast(val message: String) : SampleSideEffect
}
