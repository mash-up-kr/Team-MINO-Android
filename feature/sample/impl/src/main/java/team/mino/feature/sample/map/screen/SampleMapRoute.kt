package team.mino.feature.sample.map.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.mino.feature.sample.map.vm.SampleMapViewModel

@Composable
internal fun SampleMapRoute(
    modifier: Modifier = Modifier,
    viewModel: SampleMapViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SampleMapScreen(
        state = state,
        modifier = modifier,
    )
}
