package team.mino.feature.home

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
import team.mino.core.navigation.activity.activityArgsOrNull
import team.mino.core.navigation.activity.setActivityResult
import team.mino.feature.home.api.HomeArgs
import team.mino.feature.home.api.HomeResult
import team.mino.feature.home.navigation.HomeNavHost
import team.mino.feature.sample.api.SampleArgs
import team.mino.feature.sample.api.SampleLauncher
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    @Inject
    lateinit var sampleLauncher: SampleLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val greeting = intent.activityArgsOrNull(HomeArgs.serializer())?.greeting.orEmpty()

        setContent {
            MinoAndroidAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeNavHost(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        greeting = greeting,
                        onReturnResult = {
                            setActivityResult(HomeResult.serializer(), HomeResult(confirmed = true))
                            finish()
                        },
                        onNavigateToSample = {
                            sampleLauncher.launch(this, SampleArgs(fromHome = true))
                        },
                    )
                }
            }
        }
    }
}
