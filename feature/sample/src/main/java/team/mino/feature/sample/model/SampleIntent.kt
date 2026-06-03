package team.mino.feature.sample.model

import team.mino.core.common.android.architecture.UiIntent

sealed interface SampleIntent : UiIntent {
    data object ClickRefreshTeam : SampleIntent

    data object ClickTriggerError : SampleIntent

    data object ClickResetState : SampleIntent
}
