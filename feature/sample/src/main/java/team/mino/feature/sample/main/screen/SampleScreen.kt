package team.mino.feature.sample.main.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.feature.sample.main.component.SAMPLE_COMMENT_LONG
import team.mino.feature.sample.main.component.SAMPLE_COMMENT_SHORT
import team.mino.feature.sample.main.component.SampleComment
import team.mino.feature.sample.main.vm.SampleIntent
import team.mino.feature.sample.main.vm.SampleStatus
import team.mino.feature.sample.main.vm.SampleUiState

@Composable
fun SampleScreen(
    state: SampleUiState,
    onIntent: (SampleIntent) -> Unit,
    onReturnResult: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Text(text = state.title)

        if (state.greeting.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.greeting)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onReturnResult) {
            Text(text = "확인하고 돌아가기")
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

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SampleComment(authorName = "이름", commentText = SAMPLE_COMMENT_SHORT)
            Spacer(modifier = Modifier.height(16.dp))
            SampleComment(authorName = "이름", commentText = SAMPLE_COMMENT_LONG)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
