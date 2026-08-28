package team.mino.core.common.ui.scaffold

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 셸의 하단 탭바 표시 여부를 화면이 직접 요청할 수 있게 하는 공용 플래그.
 *
 * `ImmersiveRoute`(`core/navigation`)는 목적지 단위로만 몰입 여부를 판단하는데, 방 리스트/상세처럼
 * 하나의 목적지 안에서 로컬 상태로 화면이 전환되는 경우엔 목적지가 안 바뀌어 적용할 수 없다 —
 * 이 플래그로 대체한다. `MainShell`이 `mutableStateOf(true)`로 제공하고, 하단 탭바를 숨겨야 하는
 * Route가 `DisposableEffect`로 값을 바꿨다가 벗어나면 되돌린다. [LocalSnackbarHostState]와 같은 이유로
 * Route에서만 읽는다 — stateless한 `XScreen`으로는 내려보내지 않는다.
 */
val LocalBottomNavVisibility =
    staticCompositionLocalOf<MutableState<Boolean>> {
        error("LocalBottomNavVisibility는 MainShell 안에서만 사용할 수 있습니다.")
    }
