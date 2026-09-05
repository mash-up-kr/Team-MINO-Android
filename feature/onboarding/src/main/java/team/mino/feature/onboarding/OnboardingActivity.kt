package team.mino.feature.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_MAIN_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_ONBOARDING_INVITE_CODE
import team.mino.core.navigation.activity.launcher.EXTRA_PROFILE_ENTRY_POINT
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ONBOARDING
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_OUTCOME
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.core.navigation.activity.launcher.MainLauncher
import team.mino.core.navigation.activity.launcher.PROFILE_ENTRY_POINT_ONBOARDING
import team.mino.core.navigation.activity.launcher.ProfileLauncher
import team.mino.core.navigation.activity.launcher.ROOM_FORM_OUTCOME_CREATED
import team.mino.core.navigation.activity.launcher.ROOM_FORM_OUTCOME_SKIPPED
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.feature.onboarding.flow.vm.OnboardingFlowIntent
import team.mino.feature.onboarding.flow.vm.OnboardingFlowViewModel
import javax.inject.Inject

/**
 * 온보딩 플로우의 진입 Activity.
 *
 * 스텝 전이 규칙은 [OnboardingFlowViewModel]이 갖고 이 Activity는 실행만 한다 —
 * `registerForActivityResult`가 Activity에서만 가능하고, 다른 Activity로의 전환을 시작하는 자리도
 * Activity여야 하기 때문이다(`research.md` R-003 · `feature-navigation.md` 1장). 여기 남는 것은
 * 결과 수신 등록, [OnboardingShell]이 올려보낸 지시를 `Launcher` 호출로 옮기는 일, 받은 결과를
 * [OnboardingFlowIntent]로 옮기는 일, 그리고 외부 앱으로 나가는 공유 시트를 여는 일뿐이다.
 *
 * **그래프 안의 이동은 여기까지 올라오지 않는다** — `navController`는 셸이 갖고 스텝 전환 지시의
 * 수집기도 셸에 하나뿐이다(`feature-module.md` 4장 · [OnboardingShell]).
 *
 * **`setResult`를 호출하지 않는다.** 온보딩의 종착지는 호출자가 아니라 홈 탭이고 그 전환을 온보딩이
 * 직접 한다(`contracts/onboarding-launcher.md` §3). **어느 스텝부터 시작할지를 정하는 진입 인자는
 * 읽지 않는다** — 그것은 호출자가 아니라 저장된 진행 상태가 정한다(같은 계약 §2).
 *
 * 단, [EXTRA_ONBOARDING_INVITE_CODE](SYS-010)는 예외다 — 재개 지점과 무관하게 "프로필 저장이 끝나는
 * 시점에 이 코드로 자동 참여하라"는 값일 뿐이라 스텝 판정에 관여하지 않는다.
 */
@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {
    @Inject
    lateinit var profileLauncher: ProfileLauncher

    @Inject
    lateinit var roomFormLauncher: RoomFormLauncher

    @Inject
    lateinit var mainLauncher: MainLauncher

    /** 결과 수신 콜백이 Intent를 보낼 대상. 셸이 주입받는 것과 같은 Activity 스코프 인스턴스다. */
    private val viewModel: OnboardingFlowViewModel by viewModels()

    /**
     * 프로필 스텝의 결과. `RESULT_OK`만 읽는다(`contracts/onboarding-launcher.md` §4).
     *
     * 온보딩 진입의 프로필 화면은 뒤로가기를 막아 이탈 수단이 없고, 전이 표에도 그에 대응하는 Intent가
     * 없다 — 그래서 다른 결과 코드에는 아무 반응도 하지 않는다. 다시 열어도 갈 곳은 같으므로 이 경우
     * 다음 실행의 재개 판정이 프로필 스텝을 그대로 돌려준다.
     */
    private val profileResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.processIntent(OnboardingFlowIntent.ProfileSaved)
            }
        }

    private val roomFormResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.processIntent(result.toFlowIntent())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pendingInviteCode = intent.getStringExtra(EXTRA_ONBOARDING_INVITE_CODE)

        enableEdgeToEdge()
        setContent {
            MinoAndroidAppTheme {
                OnboardingShell(
                    pendingInviteCode = pendingInviteCode,
                    onLaunchProfile = ::launchProfile,
                    onLaunchRoomForm = ::launchRoomForm,
                    // 홈은 되돌아올 대상이 아니라 withFinish = true다. 종료된 Activity는 결과를 받을
                    // 수 없으므로 resultLauncher와 함께 쓰지 않는다.
                    onNavigateToHome = { mainLauncher.launch(this, withFinish = true) },
                    onNavigateToHomeWithRoom = { roomId ->
                        mainLauncher.launch(this, withFinish = true) {
                            putExtra(EXTRA_MAIN_ROOM_ID, roomId)
                        }
                    },
                    onShareInviteLink = ::shareInviteLink,
                    onBackToBackground = { moveTaskToBack(true) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    /** 두 위임 Activity는 결과를 돌려줘야 하므로 `resultLauncher`와 함께 부르고 `withFinish`를 쓰지 않는다. */
    private fun launchProfile() {
        profileLauncher.launch(this, resultLauncher = profileResultLauncher) {
            putExtra(EXTRA_PROFILE_ENTRY_POINT, PROFILE_ENTRY_POINT_ONBOARDING)
        }
    }

    private fun launchRoomForm() {
        roomFormLauncher.launch(this, resultLauncher = roomFormResultLauncher) {
            putExtra(EXTRA_ROOM_FORM_ONBOARDING, true)
        }
    }

    /**
     * 초대 링크를 담아 OS 공유 시트를 연다.
     *
     * **`resultLauncher`로 열지 않는다.** OS는 보냈는지 취소했는지를 구분해 돌려주지 않으므로
     * 결과를 받아 봐야 무엇도 알 수 없고, 그것을 스텝 전이의 근거로 쓰면 취소한 사용자까지
     * 다음 스텝으로 밀어 버린다. 그래서 이 경로에는 [OnboardingFlowIntent] 발화가 없다 —
     * 시트가 닫혀도 친구 초대 스텝이 그대로 남는 것이 그 결과다
     * (FR-011·TS-021·EC-010 · `contracts/onboarding-flow-ui.md` §3.3).
     *
     * 시트가 떠 있는 동안 앱이 백그라운드로 갔다 와도 이 Activity와 그래프는 그대로이므로
     * 링크를 다시 확보하지 않는다(EC-012).
     */
    private fun shareInviteLink(link: String) {
        val sendIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE_PLAIN_TEXT
                putExtra(Intent.EXTRA_TEXT, link)
            }
        // createChooser로 감싸는 것은 기본 앱이 정해져 있어도 매번 시트를 띄우기 위한 것이다.
        // 제목은 넘기지 않는다 — 현재 지원 버전의 시스템 공유 시트가 쓰지 않는 값이다.
        startActivity(Intent.createChooser(sendIntent, null))
    }

    /**
     * 공동방 폼의 결과 네 갈래를 전이 표의 Intent로 옮긴다
     * (`contracts/onboarding-launcher.md` §4).
     *
     * 온보딩이 아는 결과는 `created`·`skipped` 둘뿐이고 나머지는 전부 이탈로 읽어 같은 스텝을 다시
     * 연다 — 방을 만들지 않았으므로 다시 열어도 중복 생성이 없고, 그러지 않으면 조작 수단이 없는 빈
     * 릴레이 화면에 갇힌다(`research.md` R-020).
     */
    private fun ActivityResult.toFlowIntent(): OnboardingFlowIntent {
        if (resultCode != RESULT_OK) return OnboardingFlowIntent.RoomFormCanceled

        return when (data?.getStringExtra(EXTRA_ROOM_FORM_RESULT_OUTCOME)) {
            ROOM_FORM_OUTCOME_CREATED ->
                data
                    ?.getStringExtra(EXTRA_ROOM_FORM_RESULT_ROOM_ID)
                    ?.let(OnboardingFlowIntent::RoomCreated)
                    ?: OnboardingFlowIntent.RoomFormCanceled

            ROOM_FORM_OUTCOME_SKIPPED -> OnboardingFlowIntent.RoomFormSkipped
            else -> OnboardingFlowIntent.RoomFormCanceled
        }
    }
}

/** 공유 시트에 실어 보내는 내용의 형식. 링크 한 줄뿐이라 평문이다. */
private const val MIME_TYPE_PLAIN_TEXT = "text/plain"
