package team.mino.feature.sample.screen

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.mino.feature.sample.vm.SampleSideEffect
import team.mino.feature.sample.vm.SampleViewModel

@Composable
fun SampleRoute(
    modifier: Modifier = Modifier,
    viewModel: SampleViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is SampleSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    SampleScreen(
        state = state,
        modifier = modifier,
        onIntent = { intent -> viewModel.processIntent(intent) },
    )
}
