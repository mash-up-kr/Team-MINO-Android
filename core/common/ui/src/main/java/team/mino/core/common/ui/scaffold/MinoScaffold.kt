package team.mino.core.common.ui.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import team.mino.core.common.ui.R
import team.mino.core.common.ui.error.CollectUncaughtError
import team.mino.core.designsystem.component.snackbar.MinoSnackbar
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Check

/**
 * 네비게이션 셸(`XShell`)이 여는 프로젝트 표준 [Scaffold]. **Activity당 하나만** 연다.
 *
 * 화면 전환이 있으면 [content] 안에서 `MinoNavHost`를, 단일 화면이면 화면 컴포저블을 직접 그린다.
 * 미처리 예외(버그) 안내와 스낵바 호스트를 셸이 소유하므로 feature는 별도 배선이 필요 없다.
 * 도메인 에러는 ViewModel 인스턴스별 채널이라 셸이 아니라 Route가 [LocalSnackbarHostState]로 표시한다.
 *
 * 토스트의 **모양([MinoSnackbar])과 표출 위치**도 셸이 소유한다. 화면은 메시지를 올리는 데까지만
 * 관여하고 오프셋도 스낵바 컴포저블도 다루지 않는다
 * (`docs/adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md`).
 *
 * 규약은 `docs/conventions/error_handling.md`와 `docs/architecture/feature-module.md` 4장 참조.
 */
@Composable
fun MinoScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    containerColor: Color = MinoScaffoldDefaults.containerColor,
    content: @Composable (PaddingValues) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val message = stringResource(R.string.error_unknown)

    // 미처리 예외는 전역 채널로 오므로 화면이 아니라 셸이 한 번만 수집한다. RESUMED Activity가
    // 최대 1개라는 전제로 이중 수신이 없으므로, 한 Activity에서 셸을 두 번 열면 안 된다.
    CollectUncaughtError {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Box(modifier = modifier) {
            Scaffold(
                bottomBar = bottomBar,
                snackbarHost = {},
                containerColor = containerColor,
                content = content,
            )

            // 표출 위치의 기준선이 스크린 하단이라 Scaffold의 snackbarHost 슬롯을 비워 두고 셸이
            // 직접 얹는다. 그 슬롯은 호스트를 bottomBar 위에 놓아 하단 바 유무로 기준선이 갈린다
            // (`docs/adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md`).
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = SnackbarHorizontalMargin,
                        end = SnackbarHorizontalMargin,
                        bottom = SnackbarBottomMargin,
                    ),
            ) { data ->
                MinoSnackbar(
                    message = data.visuals.message,
                    leadingIcon = rememberVectorPainter(MinoIcons.Check),
                    modifier = Modifier.fillMaxWidth(),
                    actionLabel = data.visuals.actionLabel,
                    onActionClick = data::performAction,
                    onCloseClick = if (data.visuals.withDismissAction) data::dismiss else null,
                )
            }
        }
    }
}

private val SnackbarHorizontalMargin = 20.dp
private val SnackbarBottomMargin = 40.dp
