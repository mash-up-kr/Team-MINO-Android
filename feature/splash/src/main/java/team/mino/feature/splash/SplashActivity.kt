package team.mino.feature.splash

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.MainLauncher
import team.mino.core.navigation.activity.launcher.OnboardingLauncher
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
    lateinit var onboardingLauncher: OnboardingLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 마스코트가 화면 바닥까지 닿아야 하는데(SplashShell), 시스템이 3버튼 내비바에 까는 대비
        // 스크림이 그 아래를 덮어 눈 아랫부분을 잘라낸다. 이 화면은 내비바 자리까지 아트가 차지하는
        // 것이 디자인이므로 스크림을 끄고, 대신 어두운 마스코트 위에서 읽히도록 내비 아이콘을 밝게 둔다.
        // 상단은 배경이 밝아 기본값 그대로다.
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        window.isNavigationBarContrastEnforced = false

        setContent {
            MinoAndroidAppTheme {
                SplashShell(
                    onNavigateToMain = { mainLauncher.launch(this, withFinish = true) },
                    onNavigateToOnboarding = { onboardingLauncher.launch(this, withFinish = true) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
