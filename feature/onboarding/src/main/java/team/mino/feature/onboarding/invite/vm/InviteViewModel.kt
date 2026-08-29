package team.mino.feature.onboarding.invite.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.domain.usecase.GetInviteLinkUseCase
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.feature.onboarding.OnboardingInvite
import javax.inject.Inject

/**
 * 친구 초대 스텝의 ViewModel.
 *
 * 진입 인자 `roomId`는 화면으로 드릴링하지 않고 여기서 라우트 인자로 복원한다.
 *
 * 실패의 통로가 둘로 갈린다(`contracts/invite-link.md` §5) — 진입 시 확보 실패는 알리지 않고
 * [InviteUiState.inviteLink]를 `null`로 남기는 것으로 끝나고, 그 상태에서 액션이 눌렸을 때에만
 * [DomainErrorEmitter]로 방출해 `InviteRoute`가 스낵바로 표시한다. 진입 실패를 곧바로 알리면 아직
 * 아무것도 누르지 않은 사용자에게 스낵바가 뜬다.
 */
@HiltViewModel
internal class InviteViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getInviteLink: GetInviteLinkUseCase,
    ) :
    ViewModel(),
        MviContainer<InviteUiState, InviteSideEffect> by mviContainer(InviteUiState()),
        DomainErrorEmitter by domainErrorEmitter() {
        private val roomId = savedStateHandle.toRoute<OnboardingInvite>().roomId

        /**
         * 진행 중인 확보. 같은 요청이 겹쳐 나가지 못하게 막는 유일한 수단이다 —
         * [InviteUiState.isLoading]은 첫 확보 전부터 참이라 진행 여부를 가르지 못한다.
         */
        private var loadJob: Job? = null

        /**
         * 마지막 확보 실패. 알릴 시점이 실패한 시점보다 뒤라서 들고 있어야 한다.
         *
         * 상태가 아니라 필드인 것은 화면이 읽지 않기 때문이다. [InviteUiState]에 두면 화면이 그리지도
         * 않을 값을 상태에 얹는다.
         */
        private var lastLoadError: MinoDomainException? = null

        fun processIntent(intent: InviteIntent) {
            when (intent) {
                InviteIntent.Load -> loadInviteLink()
                InviteIntent.ShareLink -> postWithLink(InviteSideEffect::ShareInviteLink)
                InviteIntent.CopyLink -> postWithLink(InviteSideEffect::CopyInviteLink)
            }
        }

        /**
         * 초대 링크를 확보한다. 실패해도 스스로 다시 걸지 않는다 — 재확보는 사용자가 액션을 누를 때에만
         * 일어난다. 실패가 곧 다음 요청을 부르면 끊긴 네트워크에서 요청이 멈추지 않는다.
         *
         * 이미 확보한 링크는 다시 받지 않는다. 서버가 멱등이라 같은 값이 오지만, 백그라운드에서 돌아올
         * 때마다 요청이 나가는 것을 막는 것이 이 가드다.
         */
        private fun loadInviteLink() {
            if (state.value.inviteLink != null || loadJob?.isActive == true) return
            updateState { copy(isLoading = true) }
            loadJob =
                launchSafely {
                    runCatchingDomain { getInviteLink(roomId) }
                        .onSuccess { link ->
                            lastLoadError = null
                            updateState { copy(inviteLink = link) }
                        }.onDomainFailure { error -> lastLoadError = error }
                    updateState { copy(isLoading = false) }
                }
        }

        /**
         * 링크를 실어 통로로 내보낸다. 링크가 없으면 아무것도 내보내지 않는다 —
         * 빈 문자열을 공유하거나 클립보드에 쓰는 것보다 아무 일도 일어나지 않는 편이 낫다.
         */
        private fun postWithLink(effect: (String) -> InviteSideEffect) {
            val link = state.value.inviteLink
            if (link == null) {
                reportMissingLink()
                return
            }
            launchSafely { postSideEffect(effect(link)) }
        }

        /**
         * 링크 없이 눌린 액션의 처리. 실패를 알린 뒤 확보를 다시 시도한다.
         *
         * 아직 첫 확보가 돌아오지 않았다면 알릴 실패가 없다. 그때는 이미 요청이 나가 있으므로 조용히
         * 두 번째 요청을 내보내지도 않는다.
         */
        private fun reportMissingLink() {
            lastLoadError?.let(::emitDomainError)
            loadInviteLink()
        }
    }
