package team.mino.feature.mypage.profile.vm

import team.mino.core.common.android.architecture.Intent

sealed interface ProfileIntent : Intent {
    data class OnNicknameChanged(val value: String) : ProfileIntent

    data class OnAvatarSelected(val avatarId: Int) : ProfileIntent

    data object OnClearClick : ProfileIntent

    data object OnSaveClick : ProfileIntent
}
