package team.mino.feature.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_SAMPLE_FROM_HOME
import team.mino.core.navigation.activity.launcher.EXTRA_SAMPLE_GREETING
import team.mino.core.navigation.activity.launcher.EXTRA_SAMPLE_RESULT_CONFIRMED
import team.mino.core.navigation.activity.launcher.SampleLauncher
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sampleLauncher: SampleLauncher

    private val sampleResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val confirmed = result.data?.getBooleanExtra(EXTRA_SAMPLE_RESULT_CONFIRMED, false) ?: false
            Toast.makeText(this, "Sample 결과: confirmed=$confirmed", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinoAndroidAppTheme {
                // 전환 콜백은 Activity·resultLauncher를 캡처해 컴파일러가 memoize하지 못한다. 매번 새 인스턴스가
                // 되면 그래프 빌더 람다의 identity가 바뀌어 NavHost가 화면 그래프를 통째로 다시 만든다.
                val onNavigateToSample: () -> Unit = remember {
                    { sampleLauncher.launch(this) { putExtra(EXTRA_SAMPLE_FROM_HOME, true) } }
                }
                val onRequestSampleResult: () -> Unit = remember {
                    {
                        sampleLauncher.launch(this, resultLauncher = sampleResultLauncher) {
                            putExtra(EXTRA_SAMPLE_GREETING, "결과를 부탁해요")
                        }
                    }
                }

                MainShell(
                    onNavigateToSample = onNavigateToSample,
                    onRequestSampleResult = onRequestSampleResult,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
