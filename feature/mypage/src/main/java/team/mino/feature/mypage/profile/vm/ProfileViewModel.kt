package team.mino.feature.mypage.profile.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.domain.model.Profile
import team.mino.core.domain.repository.ProfileRepository
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import javax.inject.Inject

private const val NICKNAME_MIN_LENGTH = 2
private const val NICKNAME_MAX_LENGTH = 15

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
    ) : ViewModel(),
        MviContainer<ProfileUiState, ProfileSideEffect> by mviContainer(ProfileUiState()),
        DomainErrorEmitter by domainErrorEmitter() {
        init {
            loadProfile()
        }

        fun processIntent(intent: ProfileIntent) {
            when (intent) {
                is ProfileIntent.OnNicknameChanged -> onNicknameChanged(intent.value)
                is ProfileIntent.OnAvatarSelected -> onAvatarSelected(intent.avatarId)
                ProfileIntent.OnClearClick -> onClearClick()
                ProfileIntent.OnSaveClick -> onSaveClick()
            }
        }

        private fun loadProfile() {
            launchSafely {
                runCatchingDomain { profileRepository.getProfile() }
                    .onDomainFailure(::emitDomainError)
                    .onSuccess { profile ->
                        updateState {
                            copy(
                                nickname = profile.nickname,
                                avatarId = profile.avatarId,
                                isSaveEnabled = isSaveEnabled(profile.nickname, profile.avatarId),
                            )
                        }
                    }
            }
        }

        private fun onNicknameChanged(value: String) {
            updateState { copy(nickname = value, isSaveEnabled = isSaveEnabled(value, avatarId)) }
        }

        private fun onAvatarSelected(avatarId: Int) {
            updateState { copy(avatarId = avatarId, isSaveEnabled = isSaveEnabled(nickname, avatarId)) }
        }

        private fun onClearClick() {
            updateState { copy(nickname = "", avatarId = null, isSaveEnabled = false) }
        }

        private fun onSaveClick() {
            val avatarId = state.value.avatarId ?: return
            launchSafely {
                runCatchingDomain {
                    profileRepository.saveProfile(Profile(nickname = state.value.nickname, avatarId = avatarId))
                }.onDomainFailure(::emitDomainError)
                    .onSuccess { postSideEffect(ProfileSideEffect.NavigateBack) }
            }
        }

        private fun isSaveEnabled(
            nickname: String,
            avatarId: Int?,
        ): Boolean = nickname.length in NICKNAME_MIN_LENGTH..NICKNAME_MAX_LENGTH && avatarId != null
    }
