package team.mino.feature.home.main.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.feature.home.main.vm.HomeUiState

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onNavigateToSample: () -> Unit,
    onRequestSampleResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(50.dp))

        Text(text = state.title)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onNavigateToSample) {
            Text(text = "Sample 열기")
        }
        Button(onClick = onRequestSampleResult) {
            Text(text = "Sample에서 결과받기")
        }
    }
}
