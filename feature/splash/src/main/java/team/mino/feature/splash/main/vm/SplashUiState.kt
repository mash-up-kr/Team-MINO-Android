package team.mino.feature.splash.main.vm

import team.mino.core.common.android.architecture.UiState

/**
 * 브랜드 화면은 항상 노출되므로 상태로 두지 않고, 토스트·전환은 상태가 아니라 [SplashSideEffect]다.
 */
internal data class SplashUiState(
    val isSpinnerVisible: Boolean = false,
) : UiState
