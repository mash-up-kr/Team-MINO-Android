package team.mino.feature.mypage.profile.vm

import team.mino.core.common.android.architecture.SideEffect

sealed interface ProfileSideEffect : SideEffect {
    data object NavigateBack : ProfileSideEffect
}
