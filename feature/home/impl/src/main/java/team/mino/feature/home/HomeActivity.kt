package team.mino.feature.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.feature.home.api.EXTRA_HOME_GREETING
import team.mino.feature.home.api.EXTRA_HOME_RESULT_CONFIRMED
import team.mino.feature.sample.api.EXTRA_FROM_HOME
import team.mino.feature.sample.api.SampleLauncher
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    @Inject
    lateinit var sampleLauncher: SampleLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val greeting = intent.getStringExtra(EXTRA_HOME_GREETING).orEmpty()

        setContent {
            MinoAndroidAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeNavHost(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        greeting = greeting,
                        onReturnResult = {
                            setResult(RESULT_OK, Intent().putExtra(EXTRA_HOME_RESULT_CONFIRMED, true))
                            finish()
                        },
                        onNavigateToSample = {
                            sampleLauncher.launch(this) { putExtra(EXTRA_FROM_HOME, true) }
                        },
                    )
                }
            }
        }
    }
}
