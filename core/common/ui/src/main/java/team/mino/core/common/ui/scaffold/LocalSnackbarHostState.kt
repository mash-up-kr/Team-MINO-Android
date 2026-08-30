package team.mino.core.common.ui.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * [MinoScaffold]가 소유한 스낵바 호스트.
 *
 * 셸 생애 동안 값이 바뀌지 않으므로 static으로 둬 읽는 쪽의 재구성 추적을 없앤다.
 * Route에서만 읽는다 — stateless한 `XScreen`으로는 내려보내지 않는다.
 */
val LocalSnackbarHostState =
    staticCompositionLocalOf<SnackbarHostState> {
        error("LocalSnackbarHostState는 MinoScaffold 안에서만 사용할 수 있습니다.")
    }
