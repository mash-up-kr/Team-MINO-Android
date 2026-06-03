package team.mino.feature.sample.model

import team.mino.core.common.android.architecture.UiState

data class SampleState(
    val title: String = "안녕하세요, 민호야 잘하자 안드팀 입니다!",
    val errorMessage: String? = "안드는 제발 에러가 없기를...",
    val isIdle: Boolean = true,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val teamMembers: List<String> = emptyList(),
) : UiState
