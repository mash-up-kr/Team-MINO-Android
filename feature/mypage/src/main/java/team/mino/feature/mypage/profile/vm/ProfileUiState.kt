package team.mino.feature.mypage.profile.vm

import team.mino.core.common.android.architecture.UiState

data class ProfileUiState(
    val nickname: String = "",
    val avatarId: Int? = null,
    val isSaveEnabled: Boolean = false,
) : UiState
