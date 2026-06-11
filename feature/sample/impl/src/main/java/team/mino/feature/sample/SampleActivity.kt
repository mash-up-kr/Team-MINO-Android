package team.mino.feature.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.feature.home.api.EXTRA_HOME_GREETING
import team.mino.feature.home.api.EXTRA_HOME_RESULT_CONFIRMED
import team.mino.feature.home.api.HomeLauncher
import team.mino.feature.sample.api.EXTRA_FROM_HOME
import javax.inject.Inject

@AndroidEntryPoint
class SampleActivity : ComponentActivity() {
    @Inject
    lateinit var homeLauncher: HomeLauncher

    private val homeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val confirmed = result.data?.getBooleanExtra(EXTRA_HOME_RESULT_CONFIRMED, false) ?: false
            Toast.makeText(this, "Home 결과: confirmed=$confirmed", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getBooleanExtra(EXTRA_FROM_HOME, false)) {
            Toast.makeText(this, "Home에서 돌아왔어요", Toast.LENGTH_SHORT).show()
        }

        enableEdgeToEdge()
        setContent {
            MinoAndroidAppTheme {
                SampleNavHost(
                    onNavigateToHome = {
                        homeLauncher.launch(this) { putExtra(EXTRA_HOME_GREETING, "Sample이 인사를 전합니다") }
                    },
                    onRequestHomeResult = {
                        homeLauncher.launch(this, resultLauncher = homeResultLauncher) {
                            putExtra(EXTRA_HOME_GREETING, "결과를 부탁해요")
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
