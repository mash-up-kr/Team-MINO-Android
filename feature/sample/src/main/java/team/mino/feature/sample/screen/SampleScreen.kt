package team.mino.feature.sample.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import team.mino.feature.sample.model.SampleIntent
import team.mino.feature.sample.model.SampleSideEffect
import team.mino.feature.sample.vm.SampleViewModel

@Composable
fun SampleScreen(
    modifier: Modifier = Modifier,
    viewModel: SampleViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is SampleSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (state.isLoading) {
        Text(text = "로딩 중")
    } else {
        Column {
            Spacer(modifier = Modifier.height(50.dp))

            Text(text = state.title)

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { viewModel.processIntent(SampleIntent.ClickRefreshTeam) }) {
                Text(text = "팀원 소개 완료")
            }
            Button(onClick = { viewModel.processIntent(SampleIntent.ClickTriggerError) }) {
                Text(text = "에러 강제 발생")
            }
            Button(onClick = { viewModel.processIntent(SampleIntent.ClickResetState) }) {
                Text(text = "대기 상태로 되돌리기")
            }

            Spacer(modifier = Modifier.height(20.dp))

            when {
                state.isError -> {
                    Text(text = "에러 발생: ${state.errorMessage}")
                }
                state.isIdle -> {
                    Text(text = "대기 중")
                }
                state.teamMembers.isNotEmpty() -> {
                    Text(text = "성공: ${state.teamMembers.joinToString(", ")}")
                }
            }
        }
    }
}
