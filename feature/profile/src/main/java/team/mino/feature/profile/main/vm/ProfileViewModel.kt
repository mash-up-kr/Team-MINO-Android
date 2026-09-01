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
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.repository.ProfileRepository
import team.mino.core.domain.usecase.SaveProfileUseCase
import team.mino.core.domain.usecase.ValidateNicknameUseCase
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.feature.profile.ProfileMain
import team.mino.feature.profile.main.model.ProfileEntryPoint
import team.mino.feature.profile.main.model.image
import team.mino.feature.profile.main.model.profileAvatar
import javax.inject.Inject

/**
 * 프로필 설정 화면의 ViewModel.
 *
 * 진입점은 화면으로 드릴링하지 않고 여기서 라우트 인자로 복원해 상태의 초기값으로 넣는다.
 *
 * 저장 실패는 SideEffect가 아니라 [DomainErrorEmitter]로 나간다 — 채널이 ViewModel 인스턴스별이라
 * `ProfileRoute`가 직접 수집한다.
 *
 * 저장된 프로필의 프리필과 진입 시 갱신도 여기서 건다. 화면은 조회를 걸지 않는다.
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
        /**
         * 캐시로 먼저 채우고 갱신은 그다음이다 — 갱신을 기다렸다가 채우면 왕복하는 동안 화면이 빈 채로 선다.
         *
         * 둘을 코루틴 하나에 순서대로 넣는 것이 그 순서를 보장하는 유일한 방법이다. 따로 띄우면 갱신이
         * 캐시 조회를 앞질러 끝날 수 있고, 그러면 뒤늦게 도착한 캐시 값이 갱신 값을 덮어쓴다.
         *
         * 갱신은 진입 시 한 번뿐이고, 그마저 [ProfileEntryPoint.needsRefresh]인 진입점에서만 건다. 실패해도
         * 프리필된 값은 되돌리지 않아 화면이 캐시 값으로 계속 선다.
         */
        init {
            launchSafely {
                prefill()
                if (!state.value.entryPoint.needsRefresh) return@launchSafely
                runCatchingDomain { profileRepository.refreshProfile() }
                    .onSuccess { prefillIfAllowed() }
                    .onDomainFailure { error -> emitDomainError(error) }
            }
        }

        /**
         * 갱신된 캐시로 한 번 더 채운다.
         *
         * 사용자가 이미 입력을 시작했거나([ProfileUiState.isNicknameTouched]) 저장 왕복 중이면
         * ([ProfileUiState.isSaving]) 사용자의 의도가 서버 값보다 우선하므로 화면을 건드리지 않는다.
         * 가드는 갱신이 돌아온 시점의 상태로 판정해야 한다 — 진입 시점의 상태는 아직 둘 다 거짓이다.
         */
        private suspend fun prefillIfAllowed() {
            val current = state.value
            if (current.isNicknameTouched || current.isSaving) return
            prefill()
        }

        /**
         * 저장된 값이 있으면 입력 상태를 미리 채운다. 진입점은 보지 않는다 — 채울 것이 있느냐만 본다.
         *
         * **부른 그 시점의 값 하나만 읽는다.** 계속 구독하면 저장 직후 흘러나온 값이 사용자가 그 사이에
         * 입력한 것을 덮어쓴다. 갱신된 값은 [prefillIfAllowed]가 다시 불러 반영한다.
         * 프리필은 사용자의 입력이 아니므로 `isNicknameTouched`를 올리지 않는다.
         */
        private suspend fun prefill() {
            val saved = profileRepository.observeProfile().first() ?: return
            updateState {
                copy(
                    nickname = saved.nickname,
                    selectedAvatar = saved.avatar.image,
                    isNicknameValid = true,
                    isNicknameTouched = false,
                )
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

        /**
         * 닉네임의 상한을 여기서 자른다 — 상한은 오류가 아니라 입력 차단이라
         * [ValidateNicknameUseCase]는 길이를 모르고 입력 컴포넌트에도 상한을 넘길 자리가 없다.
         * 상한을 넘겨 붙여넣은 값도 요청 전체를 버리지 않고 앞부분만 남긴다.
         *
         * 판정은 잘라낸 값으로 다시 받는다. 원본으로 판정하면 화면에 없는 글자가 오류를 만든다.
         */
        private fun changeNickname(value: String) {
            val nickname = value.take(NICKNAME_MAX_LENGTH)
            updateState {
                copy(
                    nickname = nickname,
                    isNicknameValid = validateNickname(nickname),
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
         * 아바타는 선택 입력이라 고르지 않은 채로도 저장된다. 그때 나가는 값은 [ProfileAvatar.Default]이며,
         * 어느 항목인지는 여기서 다시 유도하지 않는다 — 기본값을 아는 곳은 도메인 하나다.
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
                        avatar = current.selectedAvatar?.profileAvatar ?: ProfileAvatar.Default,
                    )
                }.onSuccess {
                    postSideEffect(ProfileSideEffect.SaveCompleted)
                }.onDomainFailure { error ->
                    emitDomainError(error)
                }
                updateState { copy(isSaving = false) }
            }
        }

        private companion object {
            /** 닉네임의 허용 문자는 모두 코드 유닛 하나라 [String.length]가 화면 글자 수와 같다. */
            const val NICKNAME_MAX_LENGTH = 15
        }
    }
