package team.mino.feature.home.main.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.feature.home.main.vm.HomeState

@Composable
internal fun HomeScreen(
    state: HomeState,
    onReturnResult: () -> Unit,
    onNavigateToSample: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(text = "Home 화면입니다")

            if (state.greeting.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.greeting)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onReturnResult) {
                Text(text = "확인하고 돌아가기")
            }
            Button(onClick = onNavigateToSample) {
                Text(text = "Sample 새로 열기")
            }
        }
    }
}
