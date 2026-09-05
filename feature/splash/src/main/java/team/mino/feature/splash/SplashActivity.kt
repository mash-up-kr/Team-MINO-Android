package team.mino.feature.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_MAIN_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_ONBOARDING_INVITE_CODE
import team.mino.core.navigation.activity.launcher.EXTRA_PUSH_DESTINATION_ID
import team.mino.core.navigation.activity.launcher.EXTRA_PUSH_DESTINATION_TYPE
import team.mino.core.navigation.activity.launcher.MainLauncher
import team.mino.core.navigation.activity.launcher.OnboardingLauncher
import team.mino.core.navigation.entry.MainEntryGate
import javax.inject.Inject

/**
 * OS 런처가 여는 앱의 진입 Activity.
 *
 * 다른 feature가 이 화면을 열지 않으므로 `:core:navigation`에 전환 계약(`SplashLauncher`)을 두지 않는다
 * (contracts/splash-ui.md §1). 단, 프로세스가 죽은 채 알림을 누르면 `MainActivity`가 푸시 extra를 실어
 * 이 화면으로 우회하므로(push-deeplink-contract.md §3·§4), 그 extra만 받아 Main 전환에 그대로 싣는다.
 * 값을 해석하거나 판단하지 않는다 — 도착지 판정은 Main이 한다.
 *
 * **App Links(SYS-010) 진입 인자.** `gguk.org/r/{code}`로 열렸으면 `intent.data`가 그 URI이고
 * [inviteCode]는 마지막 경로 세그먼트다 — 일반 런처 진입은 `intent.data`가 애초에 없어 자연히 `null`이다.
 * 이 값의 해석(미리보기·참여)은 여기서 하지 않는다: 기존 유저는 [SplashShell]의 시작 Intent에 실어
 * `SplashViewModel`이 자동 참여하고, 신규 유저는 온보딩의 프로필 저장 시점까지 그대로 들고 가야 하므로
 * [onNavigateToOnboarding]에서만 [EXTRA_ONBOARDING_INVITE_CODE]로 넘긴다.
 *
 * 세 전환 모두 `withFinish = true`다 — 스플래시로 되돌아올 수 있으면 판정이 끝난 뒤 다시 대기 화면이
 * 뜬다. 종료된 Activity는 결과를 받을 수 없어 `resultLauncher`와 함께 쓰지 않는다.
 */
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    @Inject
    lateinit var mainLauncher: MainLauncher

    @Inject
    lateinit var onboardingLauncher: OnboardingLauncher

    @Inject
    lateinit var mainEntryGate: MainEntryGate

    // 필드로 보관한다 — 진행 중에 우회 Intent가 오면 onNewIntent가 갱신할 수 있어야 한다(research.md D16)
    private var pushDestinationType: String? = null
    private var pushDestinationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readPushDestination(intent)

        val inviteCode = intent.data?.lastPathSegment

        enableEdgeToEdge()
        setContent {
            MinoAndroidAppTheme {
                SplashShell(
                    inviteCode = inviteCode,
                    onNavigateToMain = { navigateToMain() },
                    onNavigateToInvitedRoom = { roomId -> navigateToMain(invitedRoomId = roomId) },
                    // 온보딩 갈래는 푸시 extra를 버리고 게이트도 켜지 않는다 — 온보딩을 마친 뒤 Main으로 갈 때
                    // 스플래시를 다시 지나며 그 시점에 켜진다(push-deeplink-contract.md §3)
                    onNavigateToOnboarding = {
                        onboardingLauncher.launch(this, withFinish = true) {
                            apply { inviteCode?.let { putExtra(EXTRA_ONBOARDING_INVITE_CODE, it) } }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    /**
     * 스플래시가 진행 중에 콜드 우회(push-deeplink-contract.md §4)가 도착한 경우. `singleTop`이라
     * 재생성 대신 여기로 온다. 진행 중인 세션 확보·최소 노출은 그대로 두고 전환 시점에 실을 도착지만
     * 바꾼다 — `SplashViewModel`은 건드리지 않는다(D16).
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readPushDestination(intent)
    }

    /**
     * Main으로 넘긴다. 스플래시를 거친 Main 진입은 모두 이 한 자리를 지나야 게이트가 빠짐없이 켜진다
     * (push-deeplink-contract.md §3).
     *
     * @param invitedRoomId 초대 딥링크(SYS-010)로 자동 참여를 끝낸 방. 있으면 Main이 그 방 상세부터 연다.
     */
    private fun navigateToMain(invitedRoomId: String? = null) {
        mainEntryGate.markPassed()
        mainLauncher.launch(this, withFinish = true) {
            apply {
                invitedRoomId?.let { putExtra(EXTRA_MAIN_ROOM_ID, it) }
                pushDestinationType?.let { putExtra(EXTRA_PUSH_DESTINATION_TYPE, it) }
                pushDestinationId?.let { putExtra(EXTRA_PUSH_DESTINATION_ID, it) }
            }
        }
    }

    // type이 있을 때만 갱신한다 — 알림 없는 Intent가 이미 받아 둔 도착지를 지우지 않도록
    private fun readPushDestination(intent: Intent) {
        intent.getStringExtra(EXTRA_PUSH_DESTINATION_TYPE)?.let {
            pushDestinationType = it
            pushDestinationId = intent.getStringExtra(EXTRA_PUSH_DESTINATION_ID)
        }
    }
}
