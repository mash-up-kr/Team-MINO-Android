package team.mino.feature.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_SAMPLE_FROM_HOME
import team.mino.core.navigation.activity.launcher.EXTRA_SAMPLE_GREETING
import team.mino.core.navigation.activity.launcher.EXTRA_SAMPLE_RESULT_CONFIRMED

@AndroidEntryPoint
class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getBooleanExtra(EXTRA_SAMPLE_FROM_HOME, false)) {
            Toast.makeText(this, "홈 탭에서 열었어요", Toast.LENGTH_SHORT).show()
        }
        val greeting = intent.getStringExtra(EXTRA_SAMPLE_GREETING)

        enableEdgeToEdge()
        setContent {
            MinoAndroidAppTheme {
                SampleShell(
                    startDestination = SampleMain(greeting),
                    onReturnResult = {
                        setResult(RESULT_OK, Intent().putExtra(EXTRA_SAMPLE_RESULT_CONFIRMED, true))
                        finish()
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
