package team.mino.feature.mypage.main.vm

import team.mino.core.common.android.architecture.SideEffect

sealed interface MyPageSideEffect : SideEffect {
    data object NavigateToProfileSetup : MyPageSideEffect

    data object RequestNotificationPermission : MyPageSideEffect

    data object RequestLocationPermission : MyPageSideEffect

    data object OpenAppSettings : MyPageSideEffect

    data class OpenUrl(val url: String) : MyPageSideEffect

    data object OpenPlayStoreListing : MyPageSideEffect
}
