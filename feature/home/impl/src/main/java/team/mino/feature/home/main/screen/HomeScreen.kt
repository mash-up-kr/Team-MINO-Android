package team.mino.feature.home.main.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeScreen(
    greeting: String,
    onReturnResult: () -> Unit,
    onNavigateToSample: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(50.dp))

        Text(text = "Home 화면입니다")

        if (greeting.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = greeting)
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
