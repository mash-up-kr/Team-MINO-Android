package team.mino.feature.splash

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
import team.mino.core.navigation.activity.launcher.MainLauncher
import team.mino.core.navigation.activity.launcher.OnboardingLauncher
import javax.inject.Inject

/**
 * OS 런처가 여는 앱의 진입 Activity.
 *
 * 다른 feature가 이 화면을 열지 않으므로 `:core:navigation`에 전환 계약(`SplashLauncher`)을 두지 않는다
 * (contracts/splash-ui.md §1).
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inviteCode = intent.data?.lastPathSegment

        enableEdgeToEdge()
        setContent {
            MinoAndroidAppTheme {
                SplashShell(
                    inviteCode = inviteCode,
                    onNavigateToMain = { mainLauncher.launch(this, withFinish = true) },
                    onNavigateToInvitedRoom = { roomId ->
                        mainLauncher.launch(this, withFinish = true) {
                            putExtra(EXTRA_MAIN_ROOM_ID, roomId)
                        }
                    },
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
}
