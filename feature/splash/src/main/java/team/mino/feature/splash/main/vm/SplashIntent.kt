package team.mino.feature.splash.main.vm

import team.mino.core.common.android.architecture.Intent

internal sealed interface SplashIntent : Intent {
    /** 화면 최초 진입(콜드 스타트) 1회. 스플래시는 사용자 입력을 소비하지 않는다. */
    data object Start : SplashIntent
}
