package team.mino.feature.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_PROFILE_ENTRY_POINT

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* 진입점 문자열은 해석하지 않고 시작 라우트에 그대로 싣는다. 값이 없거나 알 수 없는 값이면
         * ViewModel이 마이페이지 진입으로 읽는다(ProfileEntryPoint.from). */
        val entryPoint = intent.getStringExtra(EXTRA_PROFILE_ENTRY_POINT).orEmpty()

        enableEdgeToEdge()
        setContent {
            MinoAndroidAppTheme {
                ProfileShell(
                    startDestination = ProfileMain(entryPoint),
                    onBackClick = ::finish,
                    onSaveCompleted = ::finishWithSaved,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    /* 저장 사실만 코드로 알린다. 저장된 값의 원천은 ProfileRepository.observeProfile()이므로
     * 결과 Intent에 프로필 값을 싣지 않는다(launcher 계약 §결과). */
    private fun finishWithSaved() {
        setResult(RESULT_OK)
        finish()
    }
}
