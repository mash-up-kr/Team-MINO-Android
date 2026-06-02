package team.mino.feature.sample.model

import team.mino.core.common.android.architecture.UiIntent

sealed interface SampleIntent : UiIntent {
    data object RefreshTeam : SampleIntent
    data object TriggerError : SampleIntent
    data object ResetState : SampleIntent
}
