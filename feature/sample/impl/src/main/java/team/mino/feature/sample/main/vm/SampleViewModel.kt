package team.mino.feature.sample.main.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import javax.inject.Inject

@HiltViewModel
class SampleViewModel
    @Inject
    constructor() :
    ViewModel(),
        MviContainer<SampleUiState, SampleSideEffect> by mviContainer(SampleUiState()) {
        fun processIntent(intent: SampleIntent) {
            when (intent) {
                is SampleIntent.ClickRefreshTeam -> loadTeamMembers()
                is SampleIntent.ClickTriggerError -> makeError()
                is SampleIntent.ClickResetState -> updateState { SampleUiState() }
            }
        }

        private fun loadTeamMembers() {
            viewModelScope.launch {
                updateState { copy(status = SampleStatus.Loading) }
                delay(2000)
                updateState { copy(status = SampleStatus.Success(listOf("재성리", "은석심", "윤지"))) }
                postSideEffect(SampleSideEffect.ShowToast("팀원 안내 끝."))
            }
        }

        private fun makeError() {
            viewModelScope.launch {
                updateState { copy(status = SampleStatus.Loading) }
                delay(1500)
                updateState { copy(status = SampleStatus.Error("실제로는 에러가 없기를...")) }
            }
        }
    }
