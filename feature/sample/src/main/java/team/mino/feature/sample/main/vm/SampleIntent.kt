package team.mino.feature.sample.main.vm

import team.mino.core.common.android.architecture.Intent

sealed interface SampleIntent : Intent {
    data object ClickRefreshTeam : SampleIntent

    data object ClickTriggerError : SampleIntent

    data object ClickResetState : SampleIntent
}
