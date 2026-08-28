package team.mino.feature.profile.main.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.domain.repository.ProfileRepository
import team.mino.core.domain.usecase.SaveProfileUseCase
import team.mino.core.domain.usecase.ValidateNicknameUseCase
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.feature.profile.ProfileMain
import team.mino.feature.profile.main.model.DefaultProfileAvatar
import team.mino.feature.profile.main.model.ProfileEntryPoint
import team.mino.feature.profile.main.model.avatarId
import team.mino.feature.profile.main.model.profileAvatarOf
import javax.inject.Inject

/**
 * 프로필 설정 화면의 ViewModel.
 *
 * 진입점은 화면으로 드릴링하지 않고 여기서 라우트 인자로 복원해 상태의 초기값으로 넣는다.
 *
 * 저장 실패는 SideEffect가 아니라 [DomainErrorEmitter]로 나간다 — 채널이 ViewModel 인스턴스별이라
 * `ProfileRoute`가 직접 수집한다.
 *
 * 저장된 프로필의 프리필도 여기서 건다. 화면은 조회를 걸지 않는다.
 */
@HiltViewModel
internal class ProfileViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val validateNickname: ValidateNicknameUseCase,
        private val saveProfile: SaveProfileUseCase,
        private val profileRepository: ProfileRepository,
    ) :
    ViewModel(),
        MviContainer<ProfileUiState, ProfileSideEffect> by mviContainer(
            ProfileUiState(
                entryPoint = ProfileEntryPoint.from(savedStateHandle.toRoute<ProfileMain>().entryPoint),
            ),
        ),
        DomainErrorEmitter by domainErrorEmitter() {
        init {
            prefill()
        }

        /**
         * 저장된 값이 있으면 입력 상태를 미리 채운다. 진입점은 보지 않는다 — 채울 것이 있느냐만 본다.
         *
         * **첫 값만 반영한다.** 계속 구독하면 저장 직후 흘러나온 값이 사용자가 그 사이에 입력한 것을
         * 덮어쓴다. 프리필은 사용자의 입력이 아니므로 `isNicknameTouched`를 올리지 않는다.
         */
        private fun prefill() {
            launchSafely {
                val saved = profileRepository.observeProfile().first() ?: return@launchSafely
                updateState {
                    copy(
                        nickname = saved.nickname,
                        selectedAvatar = profileAvatarOf(saved.avatarId),
                        isNicknameValid = true,
                        isNicknameTouched = false,
                    )
                }
            }
        }

        fun processIntent(intent: ProfileIntent) {
            when (intent) {
                is ProfileIntent.NicknameChanged -> changeNickname(intent.value)
                is ProfileIntent.AvatarSelected -> selectAvatar(intent.avatar)
                ProfileIntent.ClearClicked -> clear()
                ProfileIntent.SaveClicked -> save()
            }
        }

        private fun changeNickname(value: String) {
            updateState {
                copy(
                    nickname = value,
                    isNicknameValid = validateNickname(value),
                    isNicknameTouched = true,
                )
            }
        }

        private fun selectAvatar(avatar: MinoProfileAvatar) {
            updateState { copy(selectedAvatar = avatar) }
        }

        /**
         * 입력을 진입 직후와 같은 상태로 되돌린다. `isNicknameTouched`까지 함께 내려야 지운 직후에
         * 오류 문구가 뜨지 않는다. 저장 중 여부([ProfileUiState.isSaving])는 입력이 아니므로 손대지 않는다.
         */
        private fun clear() {
            updateState {
                copy(
                    nickname = "",
                    selectedAvatar = null,
                    isNicknameValid = false,
                    isNicknameTouched = false,
                )
            }
        }

        /**
         * 저장 중에 들어온 저장은 버린다 — 가드는 코루틴 밖에서 걸어야 두 번째 요청이 시작조차 하지 못한다.
         *
         * 실패해도 입력값은 손대지 않는다. 되돌리는 것은 `isSaving`뿐이라 사용자가 그대로 다시 시도할 수 있다.
         */
        private fun save() {
            val current = state.value
            if (current.isSaving) return

            updateState { copy(isSaving = true) }
            launchSafely {
                runCatchingDomain {
                    saveProfile(
                        rawNickname = current.nickname,
                        avatarId = (current.selectedAvatar ?: DefaultProfileAvatar).avatarId,
                    )
                }.onSuccess {
                    postSideEffect(ProfileSideEffect.SaveCompleted)
                }.onDomainFailure { error ->
                    emitDomainError(error)
                }
                updateState { copy(isSaving = false) }
            }
        }
    }
