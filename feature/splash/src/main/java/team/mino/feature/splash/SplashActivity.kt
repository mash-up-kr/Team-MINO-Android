package team.mino.feature.splash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_PROFILE_ENTRY_POINT
import team.mino.core.navigation.activity.launcher.MainLauncher
import team.mino.core.navigation.activity.launcher.PROFILE_ENTRY_POINT_ONBOARDING
import team.mino.core.navigation.activity.launcher.ProfileLauncher
import javax.inject.Inject

/**
 * OS 런처가 여는 앱의 진입 Activity.
 *
 * 다른 feature가 이 화면을 열지 않으므로 `:core:navigation`에 전환 계약(`SplashLauncher`)을 두지 않는다
 * (contracts/splash-ui.md §1). 진입 인자도 없어 셸에 넘길 값이 없다.
 *
 * 두 전환 모두 `withFinish = true`다 — 스플래시로 되돌아올 수 있으면 판정이 끝난 뒤 다시 대기 화면이
 * 뜬다. 종료된 Activity는 결과를 받을 수 없어 `resultLauncher`와 함께 쓰지 않는다.
 */
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    @Inject
    lateinit var mainLauncher: MainLauncher

    @Inject
    lateinit var profileLauncher: ProfileLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MinoAndroidAppTheme {
                SplashShell(
                    onNavigateToMain = { mainLauncher.launch(this, withFinish = true) },
                    onNavigateToOnboarding = {
                        profileLauncher.launch(this, withFinish = true) {
                            putExtra(EXTRA_PROFILE_ENTRY_POINT, PROFILE_ENTRY_POINT_ONBOARDING)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
