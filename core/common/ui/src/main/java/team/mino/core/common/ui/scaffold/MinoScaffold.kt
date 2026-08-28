package team.mino.core.common.ui.scaffold

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
        Scaffold(
            modifier = modifier,
            bottomBar = bottomBar,
            // Figma `Snackbar/Snackbar`(예: 2542-125839) 스펙 — Material3 기본 Snackbar가 아니라
            // 디자인 시스템 MinoSnackbar로 그린다. 셸이 스낵바 호스트를 유일하게 소유하므로
            // (KDoc 위 "스낵바 호스트 제공" 참고) 이 자리 한 곳만 바꾸면 앱 전체 스낵바에 반영된다.
            // 실측 결과 화면 좌우 20dp 여백(그 안쪽 16dp는 MinoSnackbar 자체 패딩)과 체크(✓) 리딩
            // 아이콘이 있다 — SnackbarHost는 기본적으로 이 여백·아이콘을 주지 않아 직접 채운다.
            // 하단 여백도 실측(node `2542-125839`, 812pt 캔버스 기준 스낵바 하단 y=710, Home
            // Indicator 상단 y=778 → 시스템 안전영역 위로 68dp 간격) — Scaffold 기본값은 이 간격
            // 없이 안전영역에 바로 붙으므로 SNACKBAR_BOTTOM_MARGIN으로 직접 채운다.
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    MinoSnackbar(
                        message = data.visuals.message,
                        leadingIcon = rememberVectorPainter(MinoIcons.Check),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = SNACKBAR_HORIZONTAL_MARGIN)
                            .padding(bottom = SNACKBAR_BOTTOM_MARGIN),
                    )
                }
            },
            containerColor = containerColor,
            content = content,
        )
    }
}

/** Figma `Snackbar/Snackbar`(예: 2542-125839) 실측 — 화면 좌우 가장자리로부터의 여백. */
private val SNACKBAR_HORIZONTAL_MARGIN = 20.dp

/** Figma `Snackbar/Snackbar`(예: 2542-125839) 실측 — 시스템 안전영역 위로 띄우는 하단 여백. */
private val SNACKBAR_BOTTOM_MARGIN = 68.dp
