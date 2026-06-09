package team.mino.feature.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.activityArgsOrNull
import team.mino.core.navigation.activity.resultOrNull
import team.mino.feature.home.api.HomeArgs
import team.mino.feature.home.api.HomeLauncher
import team.mino.feature.home.api.HomeResult
import team.mino.feature.sample.api.SampleArgs
import team.mino.feature.sample.navigation.SampleNavHost
import javax.inject.Inject

@AndroidEntryPoint
class SampleActivity : ComponentActivity() {
    @Inject
    lateinit var homeLauncher: HomeLauncher

    private val homeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val output = result.resultOrNull(HomeResult.serializer())
            Toast.makeText(this, "Home 결과: confirmed=${output?.confirmed}", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.activityArgsOrNull(SampleArgs.serializer())?.fromHome == true) {
            Toast.makeText(this, "Home에서 돌아왔어요", Toast.LENGTH_SHORT).show()
        }

        enableEdgeToEdge()
        setContent {
            MinoAndroidAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SampleNavHost(
                        onNavigateToHome = {
                            homeLauncher.launch(this, HomeArgs(greeting = "Sample이 인사를 전합니다"))
                        },
                        onRequestHomeResult = {
                            homeLauncher.launch(this, HomeArgs(greeting = "결과를 부탁해요"), homeResultLauncher)
                        },
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                    )
                }
            }
        }
    }
}
