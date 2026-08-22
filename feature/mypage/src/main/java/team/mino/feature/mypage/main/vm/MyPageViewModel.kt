package team.mino.feature.mypage.main.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.domain.model.PermissionType
import team.mino.core.domain.repository.AppSettingsRepository
import team.mino.core.domain.repository.PermissionRepository
import team.mino.core.domain.repository.ProfileRepository
import team.mino.core.domain.repository.PushNotificationRepository
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import javax.inject.Inject

private const val TERMS_URL = "https://app.notion.com/p/3bdbee7f599680028b69f036fd989613?source=copy_link"

@HiltViewModel
class MyPageViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
        private val permissionRepository: PermissionRepository,
        private val appSettingsRepository: AppSettingsRepository,
        private val pushNotificationRepository: PushNotificationRepository,
    ) : ViewModel(),
        MviContainer<MyPageUiState, MyPageSideEffect> by mviContainer(MyPageUiState()),
        DomainErrorEmitter by domainErrorEmitter() {
        init {
            launchSafely { refresh() }
        }

        fun processIntent(intent: MyPageIntent) {
            when (intent) {
                MyPageIntent.OnScreenResumed -> launchSafely { refresh() }
                MyPageIntent.OnEditProfileClick ->
                    launchSafely { postSideEffect(MyPageSideEffect.NavigateToProfileSetup) }
                is MyPageIntent.OnNotificationSwitchClick -> onNotificationSwitchClick(intent.canShowSystemDialog)
                is MyPageIntent.OnLocationSwitchClick -> onLocationSwitchClick(intent.canShowSystemDialog)
                is MyPageIntent.OnNotificationPermissionResult -> onNotificationPermissionResult(intent.granted)
                is MyPageIntent.OnLocationPermissionResult -> launchSafely { refresh() }
                MyPageIntent.OnPermissionSettingsDialogConfirmed -> onPermissionSettingsDialogConfirmed()
                MyPageIntent.OnPermissionSettingsDialogDismissed ->
                    updateState { copy(permissionSettingsDialogTarget = null) }
                MyPageIntent.OnTermsClick -> launchSafely { postSideEffect(MyPageSideEffect.OpenUrl(TERMS_URL)) }
                MyPageIntent.OnAppReviewClick -> launchSafely { postSideEffect(MyPageSideEffect.OpenPlayStoreListing) }
            }
        }

        // 진입·복귀 시점마다 다시 읽는다 — data-model.md §5. isNotificationSwitchOn·isLocationSwitchOn은
        // 낙관적으로 미리 반영하지 않고 항상 이 재조회 결과로만 계산한다(UX-003).
        // 실패 시 State 리프가 아니라 emitDomainError(스낵바)로 알린다 — 계약에 에러 리프가 없다.
        private suspend fun refresh() {
            runCatchingDomain {
                val profile = profileRepository.getProfile()
                val notificationDeliveryEnabled = appSettingsRepository.observeNotificationDeliveryEnabled().first()
                val isNotificationPermissionGranted = permissionRepository.isNotificationPermissionGranted()
                val isLocationPermissionGranted = permissionRepository.isLocationPermissionGranted()
                updateState {
                    copy(
                        nickname = profile.nickname,
                        avatarId = profile.avatarId,
                        isNotificationSwitchOn = isNotificationPermissionGranted && notificationDeliveryEnabled,
                        isLocationSwitchOn = isLocationPermissionGranted,
                    )
                }
            }.onDomainFailure(::emitDomainError)
        }

        private fun onNotificationSwitchClick(canShowSystemDialog: Boolean) {
            launchSafely {
                runCatchingDomain {
                    if (permissionRepository.isNotificationPermissionGranted()) {
                        appSettingsRepository.setNotificationDeliveryEnabled(!state.value.isNotificationSwitchOn)
                        true
                    } else {
                        val isFirstRequest =
                            !permissionRepository.hasRequestedPermissionBefore(PermissionType.NOTIFICATION)
                        if (canShowSystemDialog || isFirstRequest) {
                            permissionRepository.markPermissionRequested(PermissionType.NOTIFICATION)
                            postSideEffect(MyPageSideEffect.RequestNotificationPermission)
                        } else {
                            updateState { copy(permissionSettingsDialogTarget = PermissionType.NOTIFICATION) }
                        }
                        false
                    }
                }.onDomainFailure(::emitDomainError)
                    .onSuccess { shouldRefresh -> if (shouldRefresh) refresh() }
            }
        }

        private fun onLocationSwitchClick(canShowSystemDialog: Boolean) {
            launchSafely {
                runCatchingDomain {
                    if (permissionRepository.isLocationPermissionGranted()) {
                        updateState { copy(permissionSettingsDialogTarget = PermissionType.LOCATION) }
                    } else {
                        val isFirstRequest = !permissionRepository.hasRequestedPermissionBefore(PermissionType.LOCATION)
                        if (canShowSystemDialog || isFirstRequest) {
                            permissionRepository.markPermissionRequested(PermissionType.LOCATION)
                            postSideEffect(MyPageSideEffect.RequestLocationPermission)
                        } else {
                            updateState { copy(permissionSettingsDialogTarget = PermissionType.LOCATION) }
                        }
                    }
                }.onDomainFailure(::emitDomainError)
            }
        }

        private fun onPermissionSettingsDialogConfirmed() {
            launchSafely {
                postSideEffect(MyPageSideEffect.OpenAppSettings)
                updateState { copy(permissionSettingsDialogTarget = null) }
            }
        }

        private fun onNotificationPermissionResult(granted: Boolean) {
            launchSafely {
                runCatchingDomain {
                    if (granted) {
                        appSettingsRepository.setNotificationDeliveryEnabled(true)
                        pushNotificationRepository.syncPushToken()
                    } else {
                        permissionRepository.markPermissionRequested(PermissionType.NOTIFICATION)
                    }
                }.onDomainFailure(::emitDomainError)
                refresh()
            }
        }
    }
