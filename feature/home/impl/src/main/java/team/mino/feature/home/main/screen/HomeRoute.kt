package team.mino.feature.home.main.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.mino.feature.home.main.vm.HomeViewModel

@Composable
internal fun HomeRoute(
    onReturnResult: () -> Unit,
    onNavigateToSample: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onReturnResult = onReturnResult,
        onNavigateToSample = onNavigateToSample,
        modifier = modifier,
    )
}
