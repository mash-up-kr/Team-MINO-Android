package team.mino.feature.mypage.main.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.domain.model.PermissionType
import team.mino.core.domain.repository.AppSettingsRepository
import team.mino.core.domain.repository.PermissionRepository
import team.mino.core.domain.repository.ProfileRepository
import team.mino.core.domain.usecase.RegisterPushTokenUseCase
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
        private val registerPushToken: RegisterPushTokenUseCase,
    ) : ViewModel(),
        MviContainer<MyPageUiState, MyPageSideEffect> by mviContainer(MyPageUiState()),
        DomainErrorEmitter by domainErrorEmitter() {
        init {
            // 원격이 원천, 로컬은 캐시다(core/domain/README.md·ProfileRepository 계약) — 닉네임·아바타는
            // 이 구독 하나로만 갱신하고, refresh()는 캐시를 최신화시키는 트리거만 맡는다.
            profileRepository
                .observeProfile()
                .onEach { profile ->
                    updateState { copy(nickname = profile?.nickname.orEmpty(), avatar = profile?.avatar) }
                }.launchIn(viewModelScope)
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
        // 프로필(원격)과 권한(로컬·OS)을 별도 블록으로 분리한다 — 하나로 묶으면 원격 조회 실패가
        // 로컬 전용인 권한 상태 갱신까지 막아 스위치가 멈춘 것처럼 보인다(baseUrl 미배선 시 실제 관측된 버그).
        private suspend fun refresh() {
            refreshPermissionState()
            refreshProfile()
        }

        private suspend fun refreshPermissionState() {
            val notificationDeliveryEnabled = appSettingsRepository.observeNotificationDeliveryEnabled().first()
            val isNotificationPermissionGranted = permissionRepository.isNotificationPermissionGranted()
            val isLocationPermissionGranted = permissionRepository.isLocationPermissionGranted()
            updateState {
                copy(
                    isNotificationSwitchOn = isNotificationPermissionGranted && notificationDeliveryEnabled,
                    isLocationSwitchOn = isLocationPermissionGranted,
                )
            }
        }

        // 캐시를 서버 값으로 최신화한다. 성공하면 init의 observeProfile() 구독이 새 값을 자동으로 흘린다 —
        // 여기서 State를 직접 쓰지 않는다. 실패 시 State 리프가 아니라 emitDomainError(스낵바)로 알린다.
        private suspend fun refreshProfile() {
            runCatchingDomain { profileRepository.refreshProfile() }
                .onDomainFailure(::emitDomainError)
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
                        // 등록 실패는 PushRegistrationRepository.registerCurrentToken 계약대로 밖으로 오르지 않는다.
                        registerPushToken()
                    } else {
                        permissionRepository.markPermissionRequested(PermissionType.NOTIFICATION)
                    }
                }.onDomainFailure(::emitDomainError)
                refresh()
            }
        }
    }
