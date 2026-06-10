package team.mino.feature.sample.detail.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.feature.sample.detail.vm.SampleDetailState

@Composable
fun SampleDetailScreen(
    state: SampleDetailState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(50.dp))

        Text(text = "SampleDetail 인자")
        Text(text = "keyword: ${state.query?.keyword ?: "-"}")
        Text(text = "page: ${state.query?.page ?: "-"}")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onBack) {
            Text(text = "뒤로")
        }
    }
}
