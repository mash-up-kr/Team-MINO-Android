package team.mino.feature.sample.main.screen

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
import team.mino.feature.sample.main.vm.SampleIntent
import team.mino.feature.sample.main.vm.SampleStatus
import team.mino.feature.sample.main.vm.SampleUiState

@Composable
fun SampleScreen(
    state: SampleUiState,
    onIntent: (SampleIntent) -> Unit,
    onNavigateToHome: () -> Unit,
    onRequestHomeResult: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(text = state.title)

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onNavigateToHome) {
                Text(text = "Home으로 이동")
            }
            Button(onClick = onRequestHomeResult) {
                Text(text = "Home에서 결과받기")
            }
            Button(onClick = onNavigateToDetail) {
                Text(text = "Detail로 이동 (인자 전달)")
            }
            Button(onClick = onNavigateToMap) {
                Text(text = "지도 열기")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { onIntent(SampleIntent.ClickRefreshTeam) }) {
                Text(text = "팀원 소개 완료")
            }
            Button(onClick = { onIntent(SampleIntent.ClickTriggerError) }) {
                Text(text = "에러 강제 발생")
            }
            Button(onClick = { onIntent(SampleIntent.ClickResetState) }) {
                Text(text = "대기 상태로 되돌리기")
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (val status = state.status) {
                is SampleStatus.Idle -> {
                    Text(text = "대기 중")
                }
                is SampleStatus.Loading -> {
                    Text(text = "로딩 중...")
                }
                is SampleStatus.Success -> {
                    Text(text = "성공: ${status.members.joinToString(", ")}")
                }
                is SampleStatus.Error -> {
                    Text(text = "에러 발생: ${status.errorMessage} (${state.defaultErrorMessage})")
                }
            }
        }
    }
}
