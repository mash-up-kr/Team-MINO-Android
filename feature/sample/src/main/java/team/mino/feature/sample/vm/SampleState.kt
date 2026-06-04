package team.mino.feature.sample.vm

import team.mino.core.common.android.architecture.UiState

sealed interface SampleStatus {
    data object Idle : SampleStatus

    data object Loading : SampleStatus

    data class Success(val members: List<String>) : SampleStatus

    data class Error(val errorMessage: String) : SampleStatus
}

data class SampleState(
    val title: String = "안녕하세요, 민호야 잘하자 안드팀 입니다!",
    val defaultErrorMessage: String = "안드는 제발 에러가 없기를...",
    val status: SampleStatus = SampleStatus.Idle,
) : UiState
