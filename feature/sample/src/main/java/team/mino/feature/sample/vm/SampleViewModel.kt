package team.mino.feature.sample.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.feature.sample.model.SampleIntent
import team.mino.feature.sample.model.SampleSideEffect
import team.mino.feature.sample.model.SampleState


@HiltViewModel
class SampleViewModel @Inject constructor() : ViewModel(),
    MviContainer<SampleState, SampleSideEffect> by mviContainer(SampleState()) {

    fun processIntent(intent: SampleIntent) {
        when (intent) {
            is SampleIntent.RefreshTeam -> loadTeamMembers()
            is SampleIntent.TriggerError -> makeError()
            is SampleIntent.ResetState -> updateState { SampleState() }
        }
    }

    private fun loadTeamMembers() {
        viewModelScope.launch {
            updateState { copy(isIdle = false, isLoading = true, isError = false) }
            delay(2000)

            updateState {
                copy(
                    isLoading = false,
                    teamMembers = listOf("재성리", "은석킴", "윤지")
                )
            }

            postSideEffect(SampleSideEffect.ShowToast("팀원 안내 끝."))
        }
    }

    private fun makeError() {
        viewModelScope.launch {
            updateState { copy(isIdle = false, isLoading = true, isError = false) }
            delay(1500)

            updateState {
                copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = "실제로는 에러가 없기를..."
                )
            }
        }
    }
}
