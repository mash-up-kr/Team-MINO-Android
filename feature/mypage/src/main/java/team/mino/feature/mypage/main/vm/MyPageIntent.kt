package team.mino.feature.mypage.main.vm

import team.mino.core.common.android.architecture.Intent

sealed interface MyPageIntent : Intent {
    data object OnScreenResumed : MyPageIntent

    data object OnEditProfileClick : MyPageIntent

    data class OnNotificationSwitchClick(val canShowSystemDialog: Boolean) : MyPageIntent

    data class OnLocationSwitchClick(val canShowSystemDialog: Boolean) : MyPageIntent

    data class OnNotificationPermissionResult(val granted: Boolean) : MyPageIntent

    data class OnLocationPermissionResult(val granted: Boolean) : MyPageIntent

    data object OnPermissionSettingsDialogConfirmed : MyPageIntent

    data object OnPermissionSettingsDialogDismissed : MyPageIntent

    data object OnTermsClick : MyPageIntent

    data object OnAppReviewClick : MyPageIntent
}
