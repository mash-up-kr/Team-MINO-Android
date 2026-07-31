package team.mino.feature.sample.detail.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.mino.feature.sample.detail.vm.SampleDetailViewModel

@Composable
fun SampleDetailRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SampleDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SampleDetailScreen(
        state = state,
        onBack = onBack,
        modifier = modifier,
    )
}
