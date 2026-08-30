package team.mino.feature.onboarding.invite.screen

import android.content.ClipData
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.toClipEntry
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.error.CollectDomainError
import team.mino.core.common.ui.scaffold.LocalSnackbarHostState
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.onboarding.R
import team.mino.feature.onboarding.invite.vm.InviteIntent
import team.mino.feature.onboarding.invite.vm.InviteSideEffect
import team.mino.feature.onboarding.invite.vm.InviteViewModel

/**
 * [InviteScreen]의 연결부.
 *
 * **상태를 구독하지 않는다.** 화면이 상태를 그리지 않기 때문이다 — 링크를 확보했는지가 화면을 바꾸지
 * 않으므로 여기서 읽어 내려보낼 것이 없다.
 *
 * 링크를 밖으로 내보내는 두 통로 중 **클립보드 쓰기와 복사 토스트만 여기서 실행하고 공유 시트는
 * 콜백으로 올린다** — 외부 앱 전환이라 Activity가 시작한다(feature-navigation.md 1장).
 * **어느 쪽도 스텝을 넘기지 않는다.** 이 Route에 네비게이션 호출이 없는 것이 그 표현이다.
 *
 * 링크 없이 눌린 액션의 실패는 상태가 아니라 ViewModel 인스턴스별 채널로 오므로 셸이 아니라 여기서
 * 수집해 셸이 내려준 스낵바 호스트에 띄운다(에러 처리 규약 §5·§6).
 *
 * @param onShareInviteLink 공유 시트를 열 링크. 어떤 앱으로 보낼지는 OS가 정하고 결과는 읽지 않는다.
 * @param onClose 우상단 [X]. 스텝을 넘기는 조작이라 이 화면의 Intent가 아니라 콜백으로 올라간다.
 */
@Composable
internal fun InviteRoute(
    onShareInviteLink: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InviteViewModel = hiltViewModel(),
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    val clipboard = LocalClipboard.current
    val toast = remember(snackbarHostState, scope) { ReplacingToast(snackbarHostState, scope) }

    // 링크 확보는 화면이 보이는 순간 한 번. 중복 요청 방어는 ViewModel이 갖는다.
    LaunchedEffect(viewModel) {
        viewModel.processIntent(InviteIntent.Load)
    }

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            is InviteSideEffect.ShareInviteLink -> onShareInviteLink(effect.link)

            // 토스트가 쓰기보다 앞서면 실패한 복사를 완료로 알린다. 그래서 한 코루틴 안에서 순서를 묶는다.
            is InviteSideEffect.CopyInviteLink ->
                scope.launch {
                    clipboard.setClipEntry(ClipData.newPlainText(CLIP_LABEL, effect.link).toClipEntry())
                    toast.show(resources.getString(R.string.onboarding_invite_link_copied))
                }
        }
    }

    // 에러 수집기가 받는 것은 ViewModel이 아니라 에러 방출자다. 그 자리에 ViewModel을 그대로 놓으면
    // 하위 Composable로 ViewModel을 흘려보내는 것과 구분되지 않으므로 넘길 능력만 남겨 타입을 좁힌다.
    val errorEmitter: DomainErrorEmitter = viewModel
    CollectDomainError(errorEmitter) { error ->
        toast.show(resources.getString(messageResOf(error)))
    }

    InviteScreen(
        onIntent = viewModel::processIntent,
        onClose = onClose,
        modifier = modifier,
    )
}

/**
 * 앞선 안내를 밀어내고 지금 것을 띄우는 표시기.
 *
 * [SnackbarHostState.showSnackbar]를 그대로 부르면 새 안내가 뜨는 것이 아니라 **줄을 선다** — 호스트가
 * 뮤텍스로 한 번에 하나만 표시하므로 [초대 링크 복사]를 연달아 누르면 같은 안내가 표시 시간만큼씩
 * 순차로 뜬다. 앞 표시를 취소해 뮤텍스를 놓게 만드는 것이 대체를 만드는 유일한 수단이다.
 *
 * 취소를 새 표시와 같은 코루틴 안에서 기다리는 것은, 그러지 않으면 두 코루틴의 실행 순서가 정해지지
 * 않아 새 표시가 먼저 뮤텍스를 기다리기 시작할 수 있기 때문이다.
 *
 * 화면이 아니라 이 Route가 드는 이유는 셸이 호스트의 모양과 위치만 소유하고 겹침 처리는 올리지
 * 않았기 때문이다. **승격 후보다** — `:feature:splash`가 같은 뮤텍스 문제를 먼저 만나 호스트를
 * 통째로 우회하는 방식으로 풀었으므로, 대체 표출은 이미 두 화면이 필요로 한다. 호스트를 소유한
 * `:core:common:ui`가 그 표면을 내려주면 두 방식이 하나로 합쳐진다.
 */
private class ReplacingToast(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
) {
    private var showJob: Job? = null

    fun show(message: String) {
        val previous = showJob
        showJob =
            scope.launch {
                previous?.cancelAndJoin()
                hostState.showSnackbar(message)
            }
    }
}

/**
 * 리프별 사용자 문구. 공통 매퍼를 두지 않고 화면마다 자기 문맥에 맞춰 적는다
 * (에러 처리 규약 §8 — 리프별 문구 정책이 미정이다).
 *
 * 셋을 가르지 않는다 — 사용자가 할 수 있는 일이 다시 누르거나 [X]로 나아가는 것으로 같아
 * 원인을 나눠 봐야 행동이 달라지지 않는다(`contracts/invite-link.md` §5).
 */
@StringRes
private fun messageResOf(error: MinoDomainException): Int =
    when (error) {
        is MinoDomainException.Network,
        is MinoDomainException.Http,
        is MinoDomainException.Auth,
        -> R.string.onboarding_invite_error_link_unavailable
    }

/** 클립 항목의 식별자. 화면에 나오는 문구가 아니라 문자열 리소스로 두지 않는다. */
private const val CLIP_LABEL = "invite_link"
